import {
  Autocomplete,
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
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
import type { Professor, Publication, PublicationStatus, PublicationType } from '../../api/types';
import { StatusChip } from '../../components/StatusChip';
import { ConfirmDialog } from '../../components/ConfirmDialog';
import { formatDate } from '../../utils/formHelpers';
import { getErrorMessage } from '../../api/httpClient';

const publicationTypeLabels: Record<PublicationType, string> = {
  JOURNAL_ARTICLE: 'Artigo em periódico',
  CONFERENCE_PAPER: 'Artigo em conferência',
  BOOK_CHAPTER: 'Capítulo de livro',
  BOOK: 'Livro',
  EXPANDED_ABSTRACT: 'Resumo expandido',
  SIMPLE_ABSTRACT: 'Resumo simples',
  PROCEEDINGS_WORK: 'Trabalho em anais',
  OTHER: 'Outro',
};

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
  const [reviewId, setReviewId] = useState<number | null>(null);

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

  const reviewQuery = useQuery({
    queryKey: ['publications', 'review', reviewId],
    queryFn: () => publicationService.getById(reviewId!),
    enabled: reviewId !== null,
  });

  const reviewPublication = reviewQuery.data;

  const relatedValidatedQuery = useQuery({
    queryKey: ['publications', 'review', reviewPublication?.professorId, 'validated'],
    queryFn: () =>
      publicationService.list({
        professorId: reviewPublication!.professorId,
        status: 'VALIDATED',
        size: 50,
      }),
    enabled: Boolean(reviewPublication?.professorId),
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
      {
        field: 'title',
        headerName: 'Título',
        flex: 1.4,
        minWidth: 260,
        renderCell: (p) => (
          <Stack sx={{ py: 1, minWidth: 0 }}>
            <Typography sx={{ fontWeight: 500 }} noWrap>
              {p.row.title}
            </Typography>
            <Typography variant="body2" color="text.secondary" noWrap>
              {p.row.abntReference}
            </Typography>
          </Stack>
        ),
      },
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
            <Link
              href={p.value as string}
              target="_blank"
              rel="noopener noreferrer"
              onClick={(event) => event.stopPropagation()}
            >
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
                    onClick={(event) => {
                      event.stopPropagation();
                      setValidateTarget({ id: p.row.id, title: p.row.title });
                    }}
                  >
                    <CheckIcon fontSize="small" />
                  </IconButton>
                </span>
              </Tooltip>
              <Tooltip title="Rejeitar">
                <IconButton
                  color="warning"
                  onClick={(event) => {
                    event.stopPropagation();
                    setRejectTarget({ id: p.row.id, title: p.row.title });
                  }}
                  disabled={p.row.status === 'REJECTED'}
                >
                  <CloseIcon fontSize="small" />
                </IconButton>
              </Tooltip>
              <Tooltip title="Excluir">
                <IconButton
                  color="error"
                  onClick={(event) => {
                    event.stopPropagation();
                    setDeleteTarget({ id: p.row.id, title: p.row.title });
                  }}
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
          getRowHeight={() => 'auto'}
          onRowClick={(params) => setReviewId(params.row.id)}
          disableRowSelectionOnClick
        />
      </Box>

      <PublicationReviewDialog
        open={reviewId !== null}
        publication={reviewPublication}
        loading={reviewQuery.isLoading}
        relatedValidated={(relatedValidatedQuery.data?.content ?? []).filter(
          (item) => item.id !== reviewPublication?.id,
        )}
        relatedLoading={relatedValidatedQuery.isLoading}
        onClose={() => setReviewId(null)}
        onValidate={(publication) => {
          setReviewId(null);
          setValidateTarget({ id: publication.id, title: publication.title });
        }}
        onReject={(publication) => {
          setReviewId(null);
          setRejectTarget({ id: publication.id, title: publication.title });
        }}
      />

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

interface PublicationReviewDialogProps {
  open: boolean;
  publication?: Publication;
  loading: boolean;
  relatedValidated: Publication[];
  relatedLoading: boolean;
  onClose: () => void;
  onValidate: (publication: Publication) => void;
  onReject: (publication: Publication) => void;
}

function PublicationReviewDialog({
  open,
  publication,
  loading,
  relatedValidated,
  relatedLoading,
  onClose,
  onValidate,
  onReject,
}: PublicationReviewDialogProps) {
  const canValidate = publication?.status !== 'VALIDATED' && isValidUrl(publication?.link ?? '');

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>Revisar publicação</DialogTitle>
      <DialogContent dividers>
        {loading || !publication ? (
          <Typography color="text.secondary">Carregando publicação...</Typography>
        ) : (
          <Stack spacing={2.5}>
            <Stack spacing={0.5}>
              <Typography variant="h6">{publication.title}</Typography>
              <Typography color="text.secondary">{publication.abntReference}</Typography>
            </Stack>

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <ReviewField label="Professor" value={publication.professorName ?? 'Sem professor'} />
              <ReviewField label="Data" value={formatDate(publication.publicationDate)} />
              <Stack spacing={0.5}>
                <Typography variant="caption" color="text.secondary">
                  Status
                </Typography>
                <StatusChip status={publication.status} />
              </Stack>
              <ReviewField
                label="Tipo"
                value={publicationTypeLabels[publication.publicationType]}
              />
            </Stack>

            <Stack spacing={0.5}>
              <Typography variant="caption" color="text.secondary">
                Link
              </Typography>
              <Link href={publication.link} target="_blank" rel="noopener noreferrer">
                {publication.link} <OpenInNewIcon sx={{ fontSize: 14, verticalAlign: 'middle' }} />
              </Link>
            </Stack>

            <Divider />

            <Stack spacing={1}>
              <Typography variant="subtitle1" fontWeight={600}>
                Publicações validadas do mesmo professor
              </Typography>
              {relatedLoading ? (
                <Typography color="text.secondary">Carregando publicações validadas...</Typography>
              ) : relatedValidated.length === 0 ? (
                <Typography color="text.secondary">
                  Nenhuma publicação validada anterior encontrada para comparação.
                </Typography>
              ) : (
                <Stack spacing={1.5}>
                  {relatedValidated.map((item) => (
                    <Box key={item.id}>
                      <Typography sx={{ fontWeight: 500 }}>{item.title}</Typography>
                      <Typography variant="body2" color="text.secondary">
                        {item.abntReference}
                      </Typography>
                      <Stack direction="row" spacing={2} sx={{ mt: 0.5 }}>
                        <Typography variant="body2">{formatDate(item.publicationDate)}</Typography>
                        <Link href={item.link} target="_blank" rel="noopener noreferrer" variant="body2">
                          Abrir <OpenInNewIcon sx={{ fontSize: 14, verticalAlign: 'middle' }} />
                        </Link>
                      </Stack>
                    </Box>
                  ))}
                </Stack>
              )}
            </Stack>
          </Stack>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Fechar</Button>
        {publication && publication.status !== 'REJECTED' && (
          <Button color="warning" onClick={() => onReject(publication)}>
            Rejeitar
          </Button>
        )}
        {publication && (
          <Button
            variant="contained"
            color="success"
            disabled={!canValidate}
            onClick={() => onValidate(publication)}
          >
            Validar
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
}

function ReviewField({ label, value }: { label: string; value: string }) {
  return (
    <Stack spacing={0.5} sx={{ minWidth: 120 }}>
      <Typography variant="caption" color="text.secondary">
        {label}
      </Typography>
      <Typography>{value}</Typography>
    </Stack>
  );
}
