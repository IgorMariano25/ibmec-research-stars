import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Container,
  Link,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Link as RouterLink, useLocation, useNavigate } from "react-router-dom";
import { useState } from "react";
import { useSnackbar } from "notistack";
import { useAuth } from "../../auth/AuthProvider";
import { applyApiFieldErrors } from "../../utils/formHelpers";

const schema = z.object({
  email: z.string().email("E-mail inválido"),
  password: z.string().min(1, "Informe a senha"),
});

type FormValues = z.infer<typeof schema>;

export function LoginPage() {
  const { login, isAuthenticated, user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const { enqueueSnackbar } = useSnackbar();
  const [serverError, setServerError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { email: "", password: "" },
  });

  if (isAuthenticated && user) {
    const target = user.role === "ADMIN" ? "/admin/dashboard" : "/me";
    navigate(
      (location.state as { from?: { pathname?: string } } | null)?.from
        ?.pathname ?? target,
      {
        replace: true,
      },
    );
  }

  const onSubmit = async (values: FormValues) => {
    setServerError(null);
    setSubmitting(true);
    try {
      const sessionUser = await login(values);
      enqueueSnackbar("Login realizado com sucesso", { variant: "success" });
      navigate(sessionUser.role === "ADMIN" ? "/admin/dashboard" : "/me", {
        replace: true,
      });
    } catch (error) {
      const api = applyApiFieldErrors(error, setError);
      setServerError(
        api?.message ??
          "Não foi possível autenticar. Verifique suas credenciais.",
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Box
      sx={{
        minHeight: "100vh",
        display: "flex",
        alignItems: "center",
        bgcolor: "background.default",
        py: 4,
      }}
    >
      <Container maxWidth="xs">
        <Stack spacing={1} alignItems="center" sx={{ mb: 3 }}>
          <Typography variant="h4" color="primary" fontWeight={700}>
            IBMEC Research Stars
          </Typography>
          <Typography color="text.secondary">Entre com sua conta</Typography>
        </Stack>
        <Card variant="outlined">
          <CardContent>
            <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
              <Stack spacing={2}>
                {serverError && <Alert severity="error">{serverError}</Alert>}
                <TextField
                  label="E-mail"
                  type="email"
                  autoComplete="email"
                  fullWidth
                  {...register("email")}
                  error={Boolean(errors.email)}
                  helperText={errors.email?.message}
                />
                <TextField
                  label="Senha"
                  type="password"
                  autoComplete="current-password"
                  fullWidth
                  {...register("password")}
                  error={Boolean(errors.password)}
                  helperText={errors.password?.message}
                />
                <Button
                  type="submit"
                  variant="contained"
                  size="large"
                  disabled={submitting}
                  fullWidth
                >
                  {submitting ? "Entrando..." : "Entrar"}
                </Button>
                <Typography
                  variant="body2"
                  textAlign="center"
                  color="text.secondary"
                >
                  Ainda não tem conta?{" "}
                  <Link component={RouterLink} to="/register">
                    Cadastre-se como professor
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
