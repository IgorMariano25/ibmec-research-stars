import {
  Box,
  Button,
  IconButton,
  MenuItem,
  Stack,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material';
import { DataGrid, type GridColDef, type GridPaginationModel } from '@mui/x-data-grid';
import CheckIcon from '@mui/icons-material/Check';
import VisibilityIcon from '@mui/icons-material/Visibility';
import DeleteIcon from '@mui/icons-material/Delete';
import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';
import { useNavigate } from 'react-router-dom';
import { professorService } from '../../api/professorService';
import type { ProfessorStatus } from '../../api/types';
import { StatusChip } from '../../components/StatusChip';
import { ConfirmDialog } from '../../components/ConfirmDialog';
import { getErrorMessage } from '../../api/httpClient';

export function AdminProfessorsPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { enqueueSnackbar } = useSnackbar();
  const [status, setStatus] = useState<ProfessorStatus | ''>('');
  const [q, setQ] = useState('');
  const [pagination, setPagination] = useState<GridPaginationModel>({ page: 0, pageSize: 10 });
  const [approveTarget, setApproveTarget] = useState<{ id: number; name: string } | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<{ id: number; name: string } | null>(null);

  const params = {
    page: pagination.page,
    size: pagination.pageSize,
    status: status || undefined,
    q: q || undefined,
  };

  const listQuery = useQuery({
    queryKey: ['professors', params],
    queryFn: () => professorService.list(params),
  });

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ['professors'] });
    queryClient.invalidateQueries({ queryKey: ['reports', 'course-compliance'] });
  };

  const approveMutation = useMutation({
    mutationFn: (id: number) => professorService.approve(id),
    onSuccess: () => {
      enqueueSnackbar('Professor aprovado', { variant: 'success' });
      invalidate();
    },
    onError: (e) => enqueueSnackbar(getErrorMessage(e), { variant: 'error' }),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => professorService.remove(id),
    onSuccess: () => {
      enqueueSnackbar('Professor excluído', { variant: 'success' });
      invalidate();
    },
    onError: (e) => enqueueSnackbar(getErrorMessage(e), { variant: 'error' }),
  });

  const columns = useMemo<GridColDef[]>(
    () => [
      { field: 'name', headerName: 'Nome', flex: 1.2, minWidth: 180 },
      { field: 'email', headerName: 'E-mail', flex: 1.2, minWidth: 200 },
      { field: 'lattesUrl', headerName: 'Lattes URL', minWidth: 220 },
      {
        field: 'courses',
        headerName: 'Cursos',
        flex: 1,
        minWidth: 180,
        sortable: false,
        valueGetter: (_v, row) => {
          const courseCodes = (row.courses ?? []).map((c: { code: string }) => c.code);
          return courseCodes.length > 0 ? courseCodes.join(', ') : '-';
        },
      },
      {
        field: 'status',
        headerName: 'Status',
        minWidth: 130,
        renderCell: (params) => <StatusChip status={params.value} />,
      },
      {
        field: 'actions',
        headerName: 'Ações',
        minWidth: 180,
        sortable: false,
        renderCell: (params) => (
          <Stack direction="row">
            <Tooltip title="Detalhes">
              <IconButton onClick={() => navigate(`/admin/professors/${params.row.id}`)}>
                <VisibilityIcon fontSize="small" />
              </IconButton>
            </Tooltip>
            {params.row.status === 'PENDING' && (
              <Tooltip title="Aprovar">
                <IconButton
                  color="success"
                  onClick={() => setApproveTarget({ id: params.row.id, name: params.row.name })}
                >
                  <CheckIcon fontSize="small" />
                </IconButton>
              </Tooltip>
            )}
            <Tooltip title="Excluir">
              <IconButton
                color="error"
                onClick={() => setDeleteTarget({ id: params.row.id, name: params.row.name })}
              >
                <DeleteIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          </Stack>
        ),
      },
    ],
    [navigate],
  );

  return (
    <Stack spacing={3}>
      <Box>
        <Typography variant="h4">Professores</Typography>
        <Typography color="text.secondary">
          Gerencie professores cadastrados, aprove novos, edite ou exclua.
        </Typography>
      </Box>

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
        <TextField
          label="Buscar (nome, e-mail, Lattes)"
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
            setStatus(e.target.value as ProfessorStatus | '');
            setPagination((p) => ({ ...p, page: 0 }));
          }}
          sx={{ minWidth: 180 }}
        >
          <MenuItem value="">Todos</MenuItem>
          <MenuItem value="PENDING">Pendentes</MenuItem>
          <MenuItem value="APPROVED">Aprovados</MenuItem>
        </TextField>
      </Stack>

      <Box sx={{ height: 560, width: '100%' }}>
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
        open={Boolean(approveTarget)}
        title="Aprovar professor?"
        message={`Confirmar aprovação de ${approveTarget?.name}? As publicações dele(a) passarão a contar nos relatórios.`}
        confirmLabel="Aprovar"
        confirmColor="success"
        loading={approveMutation.isPending}
        onClose={() => setApproveTarget(null)}
        onConfirm={async () => {
          if (!approveTarget) return;
          await approveMutation.mutateAsync(approveTarget.id);
          setApproveTarget(null);
        }}
      />
      <ConfirmDialog
        open={Boolean(deleteTarget)}
        title="Excluir professor?"
        message={`Tem certeza que deseja excluir ${deleteTarget?.name}? Esta ação não pode ser desfeita.`}
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
