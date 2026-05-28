import {
  Autocomplete,
  Box,
  IconButton,
  Link,
  MenuItem,
  Stack,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material';
import { DataGrid, type GridColDef, type GridPaginationModel } from '@mui/x-data-grid';
import CheckIcon from '@mui/icons-material/Check';
import CloseIcon from '@mui/icons-material/Close';
import DeleteIcon from '@mui/icons-material/Delete';
import OpenInNewIcon from '@mui/icons-material/OpenInNew';
import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';
import { publicationService } from '../../api/publicationService';
import { professorService } from '../../api/professorService';
import type { Professor, PublicationStatus } from '../../api/types';
import { StatusChip } from '../../components/StatusChip';
import { ConfirmDialog } from '../../components/ConfirmDialog';
import { formatDate } from '../../utils/formHelpers';
import { getErrorMessage } from '../../api/httpClient';

function isValidUrl(value: string): boolean {
  try {
    new URL(value);
    return true;
  } catch {
    return false;
  }
}

export function AdminPublicationsPage() {
  const queryClient = useQueryClient();
  const { enqueueSnackbar } = useSnackbar();
  const [status, setStatus] = useState<PublicationStatus | ''>('');
  const [q, setQ] = useState('');
  const [professor, setProfessor] = useState<Professor | null>(null);
  const [pagination, setPagination] = useState<GridPaginationModel>({ page: 0, pageSize: 10 });
  const [validateTarget, setValidateTarget] = useState<{ id: number; title: string } | null>(null);
  const [rejectTarget, setRejectTarget] = useState<{ id: number; title: string } | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<{ id: number; title: string } | null>(null);

  const params = {
    page: pagination.page,
    size: pagination.pageSize,
    status: status || undefined,
    q: q || undefined,
    professorId: professor?.id,
  };

  const listQuery = useQuery({
    queryKey: ['publications', params],
    queryFn: () => publicationService.list(params),
  });

  const professorsQuery = useQuery({
    queryKey: ['professors', { size: 100 }],
    queryFn: () => professorService.list({ size: 100 }),
  });

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ['publications'] });
    queryClient.invalidateQueries({ queryKey: ['rankings'] });
    queryClient.invalidateQueries({ queryKey: ['reports', 'course-compliance'] });
  };

  const validateMutation = useMutation({
    mutationFn: (id: number) => publicationService.validate(id),
    onSuccess: () => {
      enqueueSnackbar('Publicação validada', { variant: 'success' });
      invalidate();
    },
    onError: (e) => enqueueSnackbar(getErrorMessage(e), { variant: 'error' }),
  });
  const rejectMutation = useMutation({
    mutationFn: (id: number) => publicationService.reject(id),
    onSuccess: () => {
      enqueueSnackbar('Publicação rejeitada', { variant: 'success' });
      invalidate();
    },
    onError: (e) => enqueueSnackbar(getErrorMessage(e), { variant: 'error' }),
  });
  const deleteMutation = useMutation({
    mutationFn: (id: number) => publicationService.remove(id),
    onSuccess: () => {
      enqueueSnackbar('Publicação excluída', { variant: 'success' });
      invalidate();
    },
    onError: (e) => enqueueSnackbar(getErrorMessage(e), { variant: 'error' }),
  });

  const columns = useMemo<GridColDef[]>(
    () => [
      { field: 'title', headerName: 'Título', flex: 1.4, minWidth: 220 },
      { field: 'professorName', headerName: 'Professor', flex: 1, minWidth: 160 },
      {
        field: 'publicationDate',
        headerName: 'Data',
        minWidth: 110,
        valueFormatter: (v) => formatDate(v as string),
      },
      {
        field: 'status',
        headerName: 'Status',
        minWidth: 130,
        renderCell: (p) => <StatusChip status={p.value} />,
      },
      {
        field: 'link',
        headerName: 'Link',
        minWidth: 110,
        sortable: false,
        renderCell: (p) =>
          p.value ? (
            <Link href={p.value as string} target="_blank" rel="noopener noreferrer">
              Abrir <OpenInNewIcon sx={{ fontSize: 14, verticalAlign: 'middle' }} />
            </Link>
          ) : (
            '—'
          ),
      },
      {
        field: 'actions',
        headerName: 'Ações',
        minWidth: 180,
        sortable: false,
        renderCell: (p) => {
          const canValidate = p.row.status !== 'VALIDATED' && isValidUrl(p.row.link ?? '');
          return (
            <Stack direction="row">
              <Tooltip title={canValidate ? 'Validar' : 'Necessita link válido'}>
                <span>
                  <IconButton
                    color="success"
                    disabled={!canValidate}
                    onClick={() => setValidateTarget({ id: p.row.id, title: p.row.title })}
                  >
                    <CheckIcon fontSize="small" />
                  </IconButton>
                </span>
              </Tooltip>
              <Tooltip title="Rejeitar">
                <IconButton
                  color="warning"
                  onClick={() => setRejectTarget({ id: p.row.id, title: p.row.title })}
                  disabled={p.row.status === 'REJECTED'}
                >
                  <CloseIcon fontSize="small" />
                </IconButton>
              </Tooltip>
              <Tooltip title="Excluir">
                <IconButton
                  color="error"
                  onClick={() => setDeleteTarget({ id: p.row.id, title: p.row.title })}
                >
                  <DeleteIcon fontSize="small" />
                </IconButton>
              </Tooltip>
            </Stack>
          );
        },
      },
    ],
    [],
  );

  return (
    <Stack spacing={3}>
      <Box>
        <Typography variant="h4">Publicações</Typography>
        <Typography color="text.secondary">
          Valide, rejeite ou exclua publicações enviadas pelos professores.
        </Typography>
      </Box>

      <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
        <TextField
          label="Buscar por título"
          value={q}
          onChange={(e) => {
            setQ(e.target.value);
            setPagination((p) => ({ ...p, page: 0 }));
          }}
          fullWidth
        />
        <TextField
          select
          label="Status"
          value={status}
          onChange={(e) => {
            setStatus(e.target.value as PublicationStatus | '');
            setPagination((p) => ({ ...p, page: 0 }));
          }}
          sx={{ minWidth: 180 }}
        >
          <MenuItem value="">Todos</MenuItem>
          <MenuItem value="PENDING">Pendentes</MenuItem>
          <MenuItem value="VALIDATED">Validadas</MenuItem>
          <MenuItem value="REJECTED">Rejeitadas</MenuItem>
        </TextField>
        <Autocomplete
          sx={{ minWidth: 240 }}
          options={professorsQuery.data?.content ?? []}
          getOptionLabel={(o) => o.name}
          value={professor}
          onChange={(_e, v) => {
            setProfessor(v);
            setPagination((p) => ({ ...p, page: 0 }));
          }}
          renderInput={(params) => <TextField {...params} label="Professor" />}
          isOptionEqualToValue={(a, b) => a.id === b.id}
        />
      </Stack>

      <Box sx={{ height: 600, width: '100%' }}>
        <DataGrid
          rows={listQuery.data?.content ?? []}
          columns={columns}
          loading={listQuery.isLoading}
          paginationMode="server"
          rowCount={listQuery.data?.totalElements ?? 0}
          paginationModel={pagination}
          onPaginationModelChange={setPagination}
          pageSizeOptions={[10, 20, 50]}
          disableRowSelectionOnClick
        />
      </Box>

      <ConfirmDialog
        open={Boolean(validateTarget)}
        title="Validar publicação?"
        message={`Confirmar validação de "${validateTarget?.title}"? Ela passará a contar nos relatórios.`}
        confirmLabel="Validar"
        confirmColor="success"
        loading={validateMutation.isPending}
        onClose={() => setValidateTarget(null)}
        onConfirm={async () => {
          if (!validateTarget) return;
          await validateMutation.mutateAsync(validateTarget.id);
          setValidateTarget(null);
        }}
      />
      <ConfirmDialog
        open={Boolean(rejectTarget)}
        title="Rejeitar publicação?"
        message={`Confirmar rejeição de "${rejectTarget?.title}"?`}
        confirmLabel="Rejeitar"
        confirmColor="warning"
        loading={rejectMutation.isPending}
        onClose={() => setRejectTarget(null)}
        onConfirm={async () => {
          if (!rejectTarget) return;
          await rejectMutation.mutateAsync(rejectTarget.id);
          setRejectTarget(null);
        }}
      />
      <ConfirmDialog
        open={Boolean(deleteTarget)}
        title="Excluir publicação?"
        message={`Tem certeza que deseja excluir "${deleteTarget?.title}"?`}
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
