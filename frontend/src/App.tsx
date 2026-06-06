import { Navigate, Route, Routes } from 'react-router-dom';
import { Box, Button, Container, Stack, Typography } from '@mui/material';
import { ProtectedRoute, RoleRoute } from './auth/guards';
import { AppLayout } from './components/AppLayout';
import { LoginPage } from './features/auth/LoginPage';
import { RegisterPage } from './features/auth/RegisterPage';
import { ProfessorProfilePage } from './features/professor/ProfessorProfilePage';
import { MyPublicationsPage } from './features/professor/MyPublicationsPage';
import { MyRankingPage } from './features/professor/MyRankingPage';
import { AdminDashboardPage } from './features/admin/AdminDashboardPage';
import { AdminProfessorsPage } from './features/admin/AdminProfessorsPage';
import { AdminProfessorDetailPage } from './features/admin/AdminProfessorDetailPage';
import { AdminPublicationsPage } from './features/admin/AdminPublicationsPage';
import { AdminCoursesPage } from './features/admin/AdminCoursesPage';
import { AdminRankingPage } from './features/admin/AdminRankingPage';
import { useAuth } from './auth/AuthProvider';
import { Link as RouterLink } from 'react-router-dom';

function RootRedirect() {
  const { isAuthenticated, user } = useAuth();
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return <Navigate to={user?.role === 'ADMIN' ? '/admin/dashboard' : '/me'} replace />;
}

function ForbiddenPage() {
  return (
    <Container maxWidth="sm" sx={{ py: 10 }}>
      <Stack spacing={2} alignItems="center" textAlign="center">
        <Typography variant="h2" fontWeight={700}>
          403
        </Typography>
        <Typography variant="h5">Acesso negado</Typography>
        <Typography color="text.secondary">
          Você não tem permissão para acessar esta página.
        </Typography>
        <Button component={RouterLink} to="/" variant="contained">
          Voltar
        </Button>
      </Stack>
    </Container>
  );
}

function NotFoundPage() {
  return (
    <Container maxWidth="sm" sx={{ py: 10 }}>
      <Stack spacing={2} alignItems="center" textAlign="center">
        <Typography variant="h2" fontWeight={700}>
          404
        </Typography>
        <Typography variant="h5">Página não encontrada</Typography>
        <Button component={RouterLink} to="/" variant="contained">
          Voltar
        </Button>
      </Stack>
    </Container>
  );
}

export default function App() {
  return (
    <Box>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/forbidden" element={<ForbiddenPage />} />

        <Route
          element={
            <ProtectedRoute>
              <AppLayout />
            </ProtectedRoute>
          }
        >
          <Route path="/" element={<RootRedirect />} />

          {/* Professor */}
          <Route
            path="/me"
            element={
              <RoleRoute allow={['PROFESSOR', 'ADMIN']}>
                <ProfessorProfilePage />
              </RoleRoute>
            }
          />
          <Route
            path="/me/publications"
            element={
              <RoleRoute allow={['PROFESSOR']}>
                <MyPublicationsPage />
              </RoleRoute>
            }
          />
          <Route
            path="/me/ranking"
            element={
              <RoleRoute allow={['PROFESSOR']}>
                <MyRankingPage />
              </RoleRoute>
            }
          />

          {/* Admin */}
          <Route
            path="/admin/dashboard"
            element={
              <RoleRoute allow={['ADMIN']}>
                <AdminDashboardPage />
              </RoleRoute>
            }
          />
          <Route
            path="/admin/professors"
            element={
              <RoleRoute allow={['ADMIN']}>
                <AdminProfessorsPage />
              </RoleRoute>
            }
          />
          <Route
            path="/admin/professors/:id"
            element={
              <RoleRoute allow={['ADMIN']}>
                <AdminProfessorDetailPage />
              </RoleRoute>
            }
          />
          <Route
            path="/admin/publications"
            element={
              <RoleRoute allow={['ADMIN']}>
                <AdminPublicationsPage />
              </RoleRoute>
            }
          />
          <Route
            path="/admin/courses"
            element={
              <RoleRoute allow={['ADMIN']}>
                <AdminCoursesPage />
              </RoleRoute>
            }
          />
          <Route
            path="/admin/ranking"
            element={
              <RoleRoute allow={['ADMIN']}>
                <AdminRankingPage />
              </RoleRoute>
            }
          />
        </Route>

        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </Box>
  );
}
