import {
  Box,
  Button,
  IconButton,
  Link,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Tooltip,
  Typography,
} from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import OpenInNewIcon from "@mui/icons-material/OpenInNew";
import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useSnackbar } from "notistack";
import { publicationService } from "../../api/publicationService";
import type { Publication, PublicationRequest } from "../../api/types";
import { StatusChip } from "../../components/StatusChip";
import { LoadingState, ErrorState, EmptyState } from "../../components/States";
import { ConfirmDialog } from "../../components/ConfirmDialog";
import { PublicationFormDialog } from "./PublicationFormDialog";
import { formatDate } from "../../utils/formHelpers";
import { getErrorMessage } from "../../api/httpClient";

export function MyPublicationsPage() {
  const queryClient = useQueryClient();
  const { enqueueSnackbar } = useSnackbar();
  const [editing, setEditing] = useState<Publication | null>(null);
  const [creating, setCreating] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<Publication | null>(null);

  const listQuery = useQuery({
    queryKey: ["publications", "me"],
    queryFn: publicationService.listMine,
  });

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ["publications", "me"] });
    queryClient.invalidateQueries({ queryKey: ["rankings", "me"] });
  };

  const createMutation = useMutation({
    mutationFn: (payload: PublicationRequest) =>
      publicationService.create(payload),
    onSuccess: () => {
      enqueueSnackbar("Publicação cadastrada", { variant: "success" });
      invalidate();
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({
      id,
      payload,
    }: {
      id: number;
      payload: PublicationRequest;
    }) => publicationService.update(id, payload),
    onSuccess: () => {
      enqueueSnackbar("Publicação atualizada", { variant: "success" });
      invalidate();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => publicationService.remove(id),
    onSuccess: () => {
      enqueueSnackbar("Publicação excluída", { variant: "success" });
      invalidate();
    },
    onError: (error) => {
      enqueueSnackbar(getErrorMessage(error, "Falha ao excluir"), {
        variant: "error",
      });
    },
  });

  const items = listQuery.data ?? [];

  return (
    <Stack spacing={3}>
      <Stack
        direction={{ xs: "column", sm: "row" }}
        justifyContent="space-between"
        gap={2}
      >
        <Box>
          <Typography variant="h4">Minhas publicações</Typography>
          <Typography color="text.secondary">
            Cadastre e gerencie suas publicações. Apenas as validadas contam nos
            relatórios MEC.
          </Typography>
        </Box>
        <Button
          startIcon={<AddIcon />}
          variant="contained"
          onClick={() => setCreating(true)}
        >
          Nova publicação
        </Button>
      </Stack>

      {listQuery.isLoading ? (
        <LoadingState />
      ) : listQuery.isError ? (
        <ErrorState
          message={getErrorMessage(
            listQuery.error,
            "Erro ao carregar publicações",
          )}
          onRetry={() => listQuery.refetch()}
        />
      ) : items.length === 0 ? (
        <Paper variant="outlined">
          <EmptyState
            title="Você ainda não cadastrou publicações"
            description="Clique em 'Nova publicação' para começar."
            action={
              <Button
                startIcon={<AddIcon />}
                variant="contained"
                onClick={() => setCreating(true)}
              >
                Nova publicação
              </Button>
            }
          />
        </Paper>
      ) : (
        <TableContainer component={Paper} variant="outlined">
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Título</TableCell>
                <TableCell>Data</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Link</TableCell>
                <TableCell align="right">Ações</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {items.map((p) => (
                <TableRow key={p.id} hover>
                  <TableCell sx={{ maxWidth: 360 }}>
                    <Typography sx={{ fontWeight: 500 }}>{p.title}</Typography>
                  </TableCell>
                  <TableCell>{formatDate(p.publicationDate)}</TableCell>
                  <TableCell>
                    <StatusChip status={p.status} />
                  </TableCell>
                  <TableCell>
                    <Link
                      href={p.link}
                      target="_blank"
                      rel="noopener noreferrer"
                    >
                      Abrir{" "}
                      <OpenInNewIcon
                        sx={{ fontSize: 14, verticalAlign: "middle" }}
                      />
                    </Link>
                  </TableCell>
                  <TableCell align="right">
                    <Tooltip title="Editar">
                      <IconButton onClick={() => setEditing(p)}>
                        <EditIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Excluir">
                      <IconButton
                        color="error"
                        onClick={() => setDeleteTarget(p)}
                      >
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

      <PublicationFormDialog
        open={creating}
        loading={createMutation.isPending}
        onClose={() => setCreating(false)}
        onSubmit={async (values) => {
          await createMutation.mutateAsync(values);
        }}
      />

      <PublicationFormDialog
        open={Boolean(editing)}
        initial={editing}
        warnRevalidation
        loading={updateMutation.isPending}
        onClose={() => setEditing(null)}
        onSubmit={async (values) => {
          if (!editing) return;
          await updateMutation.mutateAsync({ id: editing.id, payload: values });
        }}
      />

      <ConfirmDialog
        open={Boolean(deleteTarget)}
        title="Excluir publicação?"
        message={`Tem certeza que deseja excluir "${deleteTarget?.title}"? Esta ação não pode ser desfeita.`}
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
