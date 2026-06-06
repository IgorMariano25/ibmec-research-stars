import {
  Alert,
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { useEffect, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useSnackbar } from 'notistack';
import { courseService } from '../../api/courseService';
import type { Course, CourseRequest } from '../../api/types';
import { LoadingState, ErrorState, EmptyState } from '../../components/States';
import { ConfirmDialog } from '../../components/ConfirmDialog';
import { applyApiFieldErrors } from '../../utils/formHelpers';
import { getErrorMessage } from '../../api/httpClient';

const schema = z.object({
  name: z.string().min(2, 'Informe o nome do curso'),
  code: z.string().min(1, 'Informe o código'),
});

type FormValues = z.infer<typeof schema>;

export function AdminCoursesPage() {
  const queryClient = useQueryClient();
  const { enqueueSnackbar } = useSnackbar();
  const [editing, setEditing] = useState<Course | null>(null);
  const [creating, setCreating] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<Course | null>(null);

  const listQuery = useQuery({
    queryKey: ['courses'],
    queryFn: courseService.list,
  });

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['courses'] });

  const createMutation = useMutation({
    mutationFn: (payload: CourseRequest) => courseService.create(payload),
    onSuccess: () => {
      enqueueSnackbar('Curso criado', { variant: 'success' });
      invalidate();
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: Partial<CourseRequest> }) =>
      courseService.update(id, payload),
    onSuccess: () => {
      enqueueSnackbar('Curso atualizado', { variant: 'success' });
      invalidate();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => courseService.remove(id),
    onSuccess: () => {
      enqueueSnackbar('Curso excluído', { variant: 'success' });
      invalidate();
    },
    onError: (e) => enqueueSnackbar(getErrorMessage(e), { variant: 'error' }),
  });

  return (
    <Stack spacing={3}>
      <Stack direction={{ xs: 'column', sm: 'row' }} justifyContent="space-between" gap={2}>
        <Box>
          <Typography variant="h4">Cursos</Typography>
          <Typography color="text.secondary">
            Cadastre e mantenha os cursos do programa.
          </Typography>
        </Box>
        <Button startIcon={<AddIcon />} variant="contained" onClick={() => setCreating(true)}>
          Novo curso
        </Button>
      </Stack>

      {listQuery.isLoading ? (
        <LoadingState />
      ) : listQuery.isError ? (
        <ErrorState
          message={getErrorMessage(listQuery.error, 'Erro ao carregar cursos')}
          onRetry={() => listQuery.refetch()}
        />
      ) : (listQuery.data ?? []).length === 0 ? (
        <Paper variant="outlined">
          <EmptyState title="Nenhum curso cadastrado" />
        </Paper>
      ) : (
        <TableContainer component={Paper} variant="outlined">
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Nome</TableCell>
                <TableCell>Código</TableCell>
                <TableCell align="right">Ações</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {(listQuery.data ?? []).map((c) => (
                <TableRow key={c.id} hover>
                  <TableCell>{c.name}</TableCell>
                  <TableCell>{c.code}</TableCell>
                  <TableCell align="right">
                    <Tooltip title="Editar">
                      <IconButton onClick={() => setEditing(c)}>
                        <EditIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Excluir">
                      <IconButton color="error" onClick={() => setDeleteTarget(c)}>
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <CourseFormDialog
        open={creating}
        loading={createMutation.isPending}
        onClose={() => setCreating(false)}
        onSubmit={async (values) => {
          await createMutation.mutateAsync(values);
        }}
      />
      <CourseFormDialog
        open={Boolean(editing)}
        initial={editing}
        loading={updateMutation.isPending}
        onClose={() => setEditing(null)}
        onSubmit={async (values) => {
          if (!editing) return;
          await updateMutation.mutateAsync({ id: editing.id, payload: values });
        }}
      />
      <ConfirmDialog
        open={Boolean(deleteTarget)}
        title="Excluir curso?"
        message={`Tem certeza que deseja excluir o curso ${deleteTarget?.name}?`}
        confirmLabel="Excluir"
        confirmColor="error"
        loading={deleteMutation.isPending}
        onClose={() => setDeleteTarget(null)}
        onConfirm={async () => {
          if (!deleteTarget) return;
          await deleteMutation.mutateAsync(deleteTarget.id);
          setDeleteTarget(null);
        }}
      />
    </Stack>
  );
}

function CourseFormDialog({
  open,
  initial,
  loading,
  onClose,
  onSubmit,
}: {
  open: boolean;
  initial?: Course | null;
  loading?: boolean;
  onClose: () => void;
  onSubmit: (values: CourseRequest) => Promise<void>;
}) {
  const [serverError, setServerError] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { name: '', code: '' },
  });

  useEffect(() => {
    if (open) {
      reset({ name: initial?.name ?? '', code: initial?.code ?? '' });
      setServerError(null);
    }
  }, [open, initial, reset]);

  const submit = async (values: FormValues) => {
    setServerError(null);
    try {
      await onSubmit(values);
      onClose();
    } catch (error) {
      const api = applyApiFieldErrors(error, setError);
      if (api?.status === 409) {
        setError('code', { type: 'server', message: 'Código já cadastrado' });
        setServerError(api.message || 'Código já cadastrado.');
      } else {
        setServerError(api?.message ?? 'Não foi possível salvar.');
      }
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle>{initial ? 'Editar curso' : 'Novo curso'}</DialogTitle>
      <form onSubmit={handleSubmit(submit)} noValidate>
        <DialogContent>
          <Stack spacing={2} sx={{ pt: 1 }}>
            {serverError && <Alert severity="error">{serverError}</Alert>}
            <TextField
              label="Nome"
              fullWidth
              {...register('name')}
              error={Boolean(errors.name)}
              helperText={errors.name?.message}
            />
            <TextField
              label="Código"
              fullWidth
              {...register('code')}
              error={Boolean(errors.code)}
              helperText={errors.code?.message}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose} disabled={loading}>
            Cancelar
          </Button>
          <Button type="submit" variant="contained" disabled={loading}>
            {loading ? 'Salvando...' : 'Salvar'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}
