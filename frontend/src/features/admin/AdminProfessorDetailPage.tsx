import {
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Divider,
  Grid,
  Link,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from "@mui/material";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import CheckIcon from "@mui/icons-material/Check";
import DeleteIcon from "@mui/icons-material/Delete";
import OpenInNewIcon from "@mui/icons-material/OpenInNew";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useNavigate, useParams } from "react-router-dom";
import { useState } from "react";
import { useSnackbar } from "notistack";
import { professorService } from "../../api/professorService";
import {
  CenteredSpinner,
  EmptyState,
  ErrorState,
  LoadingState,
} from "../../components/States";
import { StatusChip } from "../../components/StatusChip";
import { ConfirmDialog } from "../../components/ConfirmDialog";
import { formatDate, formatDateTime } from "../../utils/formHelpers";
import { getErrorMessage } from "../../api/httpClient";

export function AdminProfessorDetailPage() {
  const { id } = useParams();
  const professorId = Number(id);
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { enqueueSnackbar } = useSnackbar();
  const [approveOpen, setApproveOpen] = useState(false);
  const [deleteOpen, setDeleteOpen] = useState(false);

  const professorQuery = useQuery({
    queryKey: ["professors", professorId],
    queryFn: () => professorService.getById(professorId),
    enabled: !Number.isNaN(professorId),
  });

  const publicationsQuery = useQuery({
    queryKey: ["professors", professorId, "publications"],
    queryFn: () => professorService.getPublications(professorId),
    enabled: !Number.isNaN(professorId),
  });

  const approveMutation = useMutation({
    mutationFn: () => professorService.approve(professorId),
    onSuccess: () => {
      enqueueSnackbar("Professor aprovado", { variant: "success" });
      queryClient.invalidateQueries({ queryKey: ["professors"] });
    },
    onError: (e) => enqueueSnackbar(getErrorMessage(e), { variant: "error" }),
  });

  const deleteMutation = useMutation({
    mutationFn: () => professorService.remove(professorId),
    onSuccess: () => {
      enqueueSnackbar("Professor excluído", { variant: "success" });
      queryClient.invalidateQueries({ queryKey: ["professors"] });
      navigate("/admin/professors");
    },
    onError: (e) => enqueueSnackbar(getErrorMessage(e), { variant: "error" }),
  });

  if (professorQuery.isLoading) return <CenteredSpinner />;
  if (professorQuery.isError) {
    return (
      <ErrorState
        message={getErrorMessage(
          professorQuery.error,
          "Não foi possível carregar o professor",
        )}
        onRetry={() => professorQuery.refetch()}
      />
    );
  }

  const p = professorQuery.data!;

  return (
    <Stack spacing={3}>
      <Stack direction="row" spacing={2} alignItems="center">
        <Button
          startIcon={<ArrowBackIcon />}
          onClick={() => navigate("/admin/professors")}
        >
          Voltar
        </Button>
        <Typography variant="h4" sx={{ flex: 1 }}>
          {p.name}
        </Typography>
        {p.status === "PENDING" && (
          <Button
            variant="contained"
            color="success"
            startIcon={<CheckIcon />}
            onClick={() => setApproveOpen(true)}
          >
            Aprovar
          </Button>
        )}
        <Button
          variant="outlined"
          color="error"
          startIcon={<DeleteIcon />}
          onClick={() => setDeleteOpen(true)}
        >
          Excluir
        </Button>
      </Stack>

      <Grid container spacing={2}>
        <Grid item xs={12} md={6}>
          <Card variant="outlined">
            <CardContent>
              <Stack spacing={1.5}>
                <Stack
                  direction="row"
                  justifyContent="space-between"
                  alignItems="center"
                >
                  <Typography variant="h6">Dados</Typography>
                  <StatusChip status={p.status} />
                </Stack>
                <Divider />
                <Field label="E-mail" value={p.email} />
                <Field label="URL Lattes" value={p.lattesUrl} />
                <Field
                  label="Cadastrado em"
                  value={formatDateTime(p.createdAt)}
                />
              </Stack>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={6}>
          <Card variant="outlined">
            <CardContent>
              <Stack spacing={1.5}>
                <Typography variant="h6">Cursos</Typography>
                <Divider />
                {p.courses.length === 0 ? (
                  <Typography color="text.secondary">
                    Sem cursos vinculados.
                  </Typography>
                ) : (
                  <Stack direction="row" flexWrap="wrap" gap={1}>
                    {p.courses.map((c) => (
                      <Chip key={c.id} label={`${c.name} (${c.code})`} />
                    ))}
                  </Stack>
                )}
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Card variant="outlined">
        <CardContent>
          <Stack spacing={2}>
            <Typography variant="h6">Produção científica</Typography>
            {publicationsQuery.isLoading ? (
              <LoadingState />
            ) : publicationsQuery.isError ? (
              <ErrorState
                message={getErrorMessage(
                  publicationsQuery.error,
                  "Erro ao carregar publicações",
                )}
                onRetry={() => publicationsQuery.refetch()}
              />
            ) : (publicationsQuery.data ?? []).length === 0 ? (
              <EmptyState title="Nenhuma publicação cadastrada" />
            ) : (
              <TableContainer component={Paper} variant="outlined">
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Título</TableCell>
                      <TableCell>Data</TableCell>
                      <TableCell>Status</TableCell>
                      <TableCell>Link</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {(publicationsQuery.data ?? []).map((pub) => (
                      <TableRow key={pub.id} hover>
                        <TableCell>
                          <Typography sx={{ fontWeight: 500 }}>
                            {pub.title}
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            {pub.abntReference}
                          </Typography>
                        </TableCell>
                        <TableCell>{formatDate(pub.publicationDate)}</TableCell>
                        <TableCell>
                          <StatusChip status={pub.status} />
                        </TableCell>
                        <TableCell>
                          <Link
                            href={pub.link}
                            target="_blank"
                            rel="noopener noreferrer"
                          >
                            Abrir{" "}
                            <OpenInNewIcon
                              sx={{ fontSize: 14, verticalAlign: "middle" }}
                            />
                          </Link>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            )}
          </Stack>
        </CardContent>
      </Card>

      <ConfirmDialog
        open={approveOpen}
        title="Aprovar professor?"
        message={`Confirmar aprovação de ${p.name}?`}
        confirmLabel="Aprovar"
        confirmColor="success"
        loading={approveMutation.isPending}
        onClose={() => setApproveOpen(false)}
        onConfirm={async () => {
          await approveMutation.mutateAsync();
          setApproveOpen(false);
        }}
      />
      <ConfirmDialog
        open={deleteOpen}
        title="Excluir professor?"
        message={`Tem certeza que deseja excluir ${p.name}?`}
        confirmLabel="Excluir"
        confirmColor="error"
        loading={deleteMutation.isPending}
        onClose={() => setDeleteOpen(false)}
        onConfirm={async () => {
          await deleteMutation.mutateAsync();
        }}
      />
    </Stack>
  );
}

function Field({ label, value }: { label: string; value: string }) {
  return (
    <Stack
      direction={{ xs: "column", sm: "row" }}
      justifyContent="space-between"
    >
      <Typography variant="body2" color="text.secondary">
        {label}
      </Typography>
      <Typography variant="body2" sx={{ fontWeight: 500 }}>
        {value}
      </Typography>
    </Stack>
  );
}
