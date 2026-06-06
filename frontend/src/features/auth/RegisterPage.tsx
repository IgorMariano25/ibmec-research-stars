import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Container,
  FormControl,
  FormHelperText,
  InputLabel,
  Link,
  MenuItem,
  OutlinedInput,
  Select,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import { Controller, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Link as RouterLink, useNavigate } from "react-router-dom";
import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { useSnackbar } from "notistack";
import { authService } from "../../api/authService";
import { courseService } from "../../api/courseService";
import { applyApiFieldErrors } from "../../utils/formHelpers";

const schema = z.object({
  name: z.string().min(2, "Informe seu nome completo"),
  email: z.string().email("E-mail inválido"),
  lattesNumber: z.string().min(3, "Informe o número Lattes"),
  password: z.string().min(6, "A senha deve ter ao menos 6 caracteres"),
  courseIds: z.array(z.number()).min(1, "Selecione ao menos um curso"),
});

type FormValues = z.infer<typeof schema>;

export function RegisterPage() {
  const navigate = useNavigate();
  const { enqueueSnackbar } = useSnackbar();
  const [serverError, setServerError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [success, setSuccess] = useState(false);

  const coursesQuery = useQuery({
    queryKey: ["courses"],
    queryFn: courseService.list,
  });

  const {
    register,
    control,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      name: "",
      email: "",
      lattesNumber: "",
      password: "",
      courseIds: [],
    },
  });

  const onSubmit = async (values: FormValues) => {
    setServerError(null);
    setSubmitting(true);
    try {
      await authService.register(values);
      setSuccess(true);
      enqueueSnackbar("Cadastro realizado com sucesso", { variant: "success" });
    } catch (error) {
      const api = applyApiFieldErrors(error, setError);
      if (api?.status === 409) {
        setServerError(api.message || "E-mail ou número Lattes já cadastrado.");
        if (!api.fieldErrors?.length) {
          setError("email", {
            type: "server",
            message: "E-mail ou Lattes já cadastrado",
          });
          setError("lattesNumber", {
            type: "server",
            message: "E-mail ou Lattes já cadastrado",
          });
        }
      } else {
        setServerError(api?.message ?? "Não foi possível concluir o cadastro.");
      }
    } finally {
      setSubmitting(false);
    }
  };

  if (success) {
    return (
      <Box
        sx={{
          minHeight: "100vh",
          display: "flex",
          alignItems: "center",
          py: 4,
        }}
      >
        <Container maxWidth="sm">
          <Card variant="outlined">
            <CardContent>
              <Stack spacing={2}>
                <Typography variant="h5" color="success.main" fontWeight={600}>
                  Cadastro recebido!
                </Typography>
                <Typography>
                  Seu cadastro está com status <strong>PENDENTE</strong>. A
                  aprovação é feita pelo administrador. Você já pode acessar o
                  sistema e cadastrar publicações, mas elas só contarão nos
                  relatórios após sua aprovação.
                </Typography>
                <Stack direction="row" spacing={1}>
                  <Button
                    variant="contained"
                    onClick={() => navigate("/login")}
                  >
                    Ir para o login
                  </Button>
                </Stack>
              </Stack>
            </CardContent>
          </Card>
        </Container>
      </Box>
    );
  }

  return (
    <Box
      sx={{ minHeight: "100vh", display: "flex", alignItems: "center", py: 4 }}
    >
      <Container maxWidth="sm">
        <Stack spacing={1} alignItems="center" sx={{ mb: 3 }}>
          <Typography variant="h4" color="primary" fontWeight={700}>
            Cadastro de Professor
          </Typography>
          <Typography color="text.secondary" textAlign="center">
            Preencha seus dados para participar do programa de pesquisa.
          </Typography>
        </Stack>
        <Card variant="outlined">
          <CardContent>
            <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
              <Stack spacing={2}>
                {serverError && <Alert severity="error">{serverError}</Alert>}
                <TextField
                  label="Nome completo"
                  fullWidth
                  {...register("name")}
                  error={Boolean(errors.name)}
                  helperText={errors.name?.message}
                />
                <TextField
                  label="E-mail"
                  type="email"
                  fullWidth
                  {...register("email")}
                  error={Boolean(errors.email)}
                  helperText={errors.email?.message}
                />
                <TextField
                  label="Número Lattes"
                  fullWidth
                  {...register("lattesNumber")}
                  error={Boolean(errors.lattesNumber)}
                  helperText={errors.lattesNumber?.message}
                />
                <TextField
                  label="Senha"
                  type="password"
                  fullWidth
                  {...register("password")}
                  error={Boolean(errors.password)}
                  helperText={errors.password?.message}
                />
                <Controller
                  control={control}
                  name="courseIds"
                  render={({ field }) => (
                    <FormControl fullWidth error={Boolean(errors.courseIds)}>
                      <InputLabel id="courses-label">Cursos</InputLabel>
                      <Select
                        labelId="courses-label"
                        multiple
                        value={field.value}
                        onChange={(e) =>
                          field.onChange(
                            typeof e.target.value === "string"
                              ? e.target.value.split(",").map(Number)
                              : (e.target.value as number[]),
                          )
                        }
                        input={<OutlinedInput label="Cursos" />}
                        disabled={coursesQuery.isLoading}
                        renderValue={(selected) => (
                          <Stack direction="row" spacing={0.5} flexWrap="wrap">
                            {(selected as number[]).map((id) => {
                              const course = coursesQuery.data?.find(
                                (c) => c.id === id,
                              );
                              return (
                                <Chip
                                  key={id}
                                  label={course ? course.name : id}
                                  size="small"
                                />
                              );
                            })}
                          </Stack>
                        )}
                      >
                        {coursesQuery.data?.map((course) => (
                          <MenuItem key={course.id} value={course.id}>
                            {course.name} ({course.code})
                          </MenuItem>
                        ))}
                      </Select>
                      <FormHelperText>
                        {errors.courseIds?.message ??
                          "Selecione todos os cursos em que leciona"}
                      </FormHelperText>
                    </FormControl>
                  )}
                />
                <Button
                  type="submit"
                  variant="contained"
                  size="large"
                  disabled={submitting}
                  fullWidth
                >
                  {submitting ? "Enviando..." : "Criar conta"}
                </Button>
                <Typography
                  variant="body2"
                  textAlign="center"
                  color="text.secondary"
                >
                  Já possui conta?{" "}
                  <Link component={RouterLink} to="/login">
                    Entrar
                  </Link>
                </Typography>
              </Stack>
            </Box>
          </CardContent>
        </Card>
      </Container>
    </Box>
  );
}
