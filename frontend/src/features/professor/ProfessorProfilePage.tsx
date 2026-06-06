import {
  Alert,
  Box,
  Card,
  CardContent,
  Chip,
  Divider,
  Grid,
  Stack,
  Typography,
} from "@mui/material";
import { useQuery } from "@tanstack/react-query";
import { professorService } from "../../api/professorService";
import { StatusChip } from "../../components/StatusChip";
import { CenteredSpinner, ErrorState } from "../../components/States";
import { formatDateTime } from "../../utils/formHelpers";
import { getErrorMessage } from "../../api/httpClient";

export function ProfessorProfilePage() {
  const meQuery = useQuery({
    queryKey: ["professors", "me"],
    queryFn: professorService.getMe,
  });

  if (meQuery.isLoading) return <CenteredSpinner />;
  if (meQuery.isError) {
    return (
      <ErrorState
        message={getErrorMessage(
          meQuery.error,
          "Não foi possível carregar seu perfil",
        )}
        onRetry={() => meQuery.refetch()}
      />
    );
  }

  const me = meQuery.data!;

  return (
    <Stack spacing={3}>
      <Box>
        <Typography variant="h4">Meu perfil</Typography>
        <Typography color="text.secondary">
          Bem-vindo(a), {me.name}. Aqui você acompanha seus dados acadêmicos.
        </Typography>
      </Box>

      {me.status === "PENDING" ? (
        <Alert severity="warning">
          Seu cadastro aguarda aprovação. Você já pode cadastrar publicações,
          mas elas só contarão nos relatórios após a aprovação do administrador.
        </Alert>
      ) : (
        <Alert severity="success">
          Seu cadastro está aprovado. Suas publicações validadas contam para os
          relatórios MEC.
        </Alert>
      )}

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
                  <Typography variant="h6">Dados pessoais</Typography>
                  <StatusChip status={me.status} />
                </Stack>
                <Divider />
                <Field label="Nome" value={me.name} />
                <Field label="E-mail" value={me.email} />
                <Field label="Número Lattes" value={me.lattesNumber} />
                <Field label="Criado em" value={formatDateTime(me.createdAt)} />
              </Stack>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={6}>
          <Card variant="outlined">
            <CardContent>
              <Stack spacing={1.5}>
                <Typography variant="h6">Cursos em que leciona</Typography>
                <Divider />
                {me.courses.length === 0 ? (
                  <Typography color="text.secondary">
                    Nenhum curso vinculado.
                  </Typography>
                ) : (
                  <Stack direction="row" flexWrap="wrap" gap={1}>
                    {me.courses.map((c) => (
                      <Chip key={c.id} label={`${c.name} (${c.code})`} />
                    ))}
                  </Stack>
                )}
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
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
