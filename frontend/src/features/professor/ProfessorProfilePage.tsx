import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Divider,
  FormControl,
  Grid,
  InputLabel,
  MenuItem,
  OutlinedInput,
  Select,
  Stack,
  Typography,
} from "@mui/material";
import { useEffect, useState } from "react";
import { useSnackbar } from "notistack";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { professorService } from "../../api/professorService";
import { courseService } from "../../api/courseService";
import { StatusChip } from "../../components/StatusChip";
import { CenteredSpinner, ErrorState } from "../../components/States";
import { formatDateTime } from "../../utils/formHelpers";
import { getErrorMessage } from "../../api/httpClient";

export function ProfessorProfilePage() {
  const queryClient = useQueryClient();
  const { enqueueSnackbar } = useSnackbar();
  const [requestedCourseIds, setRequestedCourseIds] = useState<number[]>([]);

  const meQuery = useQuery({
    queryKey: ["professors", "me"],
    queryFn: professorService.getMe,
  });

  const coursesQuery = useQuery({
    queryKey: ["courses"],
    queryFn: courseService.list,
  });

  const requestCourseChangeMutation = useMutation({
    mutationFn: () => professorService.requestCourseChange(requestedCourseIds),
    onSuccess: () => {
      enqueueSnackbar("Solicitação de cursos enviada", { variant: "success" });
      queryClient.invalidateQueries({ queryKey: ["professors", "me"] });
    },
    onError: (e) => enqueueSnackbar(getErrorMessage(e), { variant: "error" }),
  });

  useEffect(() => {
    if (meQuery.data) {
      setRequestedCourseIds(meQuery.data.courses.map((course) => course.id));
    }
  }, [meQuery.data]);

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
  const pendingCourseChange = me.pendingCourseChangeRequest;

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
                <Field label="URL Lattes" value={me.lattesUrl} />
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
                {pendingCourseChange && (
                  <Alert severity="info">
                    Solicitação pendente:{" "}
                    {pendingCourseChange.requestedCourses
                      .map((course) => `${course.name} (${course.code})`)
                      .join(", ")}
                  </Alert>
                )}
                <FormControl fullWidth disabled={coursesQuery.isLoading}>
                  <InputLabel id="requested-courses-label">
                    Cursos solicitados
                  </InputLabel>
                  <Select
                    labelId="requested-courses-label"
                    multiple
                    value={requestedCourseIds}
                    input={<OutlinedInput label="Cursos solicitados" />}
                    onChange={(event) => {
                      const value = event.target.value;
                      setRequestedCourseIds(
                        typeof value === "string"
                          ? value.split(",").map(Number)
                          : value.map(Number),
                      );
                    }}
                    renderValue={(selected) => {
                      const selectedIds = selected as number[];
                      return coursesQuery.data
                        ?.filter((course) => selectedIds.includes(course.id))
                        .map((course) => `${course.name} (${course.code})`)
                        .join(", ");
                    }}
                  >
                    {coursesQuery.data?.map((course) => (
                      <MenuItem key={course.id} value={course.id}>
                        {course.name} ({course.code})
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
                {coursesQuery.isError && (
                  <Alert severity="error">
                    {getErrorMessage(
                      coursesQuery.error,
                      "Não foi possível carregar os cursos",
                    )}
                  </Alert>
                )}
                <Button
                  variant="contained"
                  onClick={() => requestCourseChangeMutation.mutate()}
                  disabled={
                    requestedCourseIds.length === 0 ||
                    requestCourseChangeMutation.isPending ||
                    Boolean(pendingCourseChange)
                  }
                >
                  Solicitar alteração
                </Button>
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
