import {
  Box,
  Card,
  CardContent,
  Chip,
  Grid,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';
import SchoolIcon from '@mui/icons-material/School';
import GroupsIcon from '@mui/icons-material/Groups';
import HourglassEmptyIcon from '@mui/icons-material/HourglassEmpty';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import { useQuery } from '@tanstack/react-query';
import { reportService } from '../../api/reportService';
import { professorService } from '../../api/professorService';
import type { CourseCompliance } from '../../api/types';
import { LoadingState, ErrorState, EmptyState } from '../../components/States';
import { ProgressBar, StatCard } from '../../components/StatCard';
import { getErrorMessage } from '../../api/httpClient';

const COMPLIANCE_THRESHOLD = 70;

function getCompliantProfessors(course: CourseCompliance): number {
  return course.compliantProfessors ?? course.totalCompliantProfessors ?? 0;
}

export function AdminDashboardPage() {
  const complianceQuery = useQuery({
    queryKey: ['reports', 'course-compliance'],
    queryFn: reportService.getCourseCompliance,
  });
  const pendingProfessorsQuery = useQuery({
    queryKey: ['professors', { status: 'PENDING', size: 1 }],
    queryFn: () => professorService.list({ status: 'PENDING', size: 1 }),
  });

  const data = complianceQuery.data ?? [];
  const totalCourses = data.length;
  const totalApprovedProfessors = data.reduce((sum, c) => sum + c.totalApprovedProfessors, 0);
  const totalCompliantProfessors = data.reduce((sum, c) => sum + getCompliantProfessors(c), 0);
  const avgCompliance =
    totalApprovedProfessors === 0 ? 0 : (totalCompliantProfessors / totalApprovedProfessors) * 100;
  const compliantCourses = data.filter((c) => c.compliancePercentage >= COMPLIANCE_THRESHOLD).length;
  const pendingCount = pendingProfessorsQuery.data?.totalElements ?? 0;

  return (
    <Stack spacing={3}>
      <Box>
        <Typography variant="h4">Dashboard de conformidade</Typography>
        <Typography color="text.secondary">
          Visão geral por curso — % de professores com ≥ 9 publicações validadas desde 1º de janeiro de três anos atrás.
        </Typography>
      </Box>

      <Grid container spacing={2}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            label="Cursos monitorados"
            value={totalCourses}
            icon={<SchoolIcon fontSize="large" />}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            label="Conformidade média"
            value={`${avgCompliance.toFixed(1)}%`}
            color={avgCompliance >= COMPLIANCE_THRESHOLD ? 'success.main' : 'warning.main'}
            icon={<CheckCircleIcon fontSize="large" />}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            label="Cursos acima do limiar"
            value={`${compliantCourses}/${totalCourses}`}
            icon={<GroupsIcon fontSize="large" />}
            hint={`Limiar: ${COMPLIANCE_THRESHOLD}%`}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            label="Professores pendentes"
            value={pendingCount}
            color="warning.main"
            icon={<HourglassEmptyIcon fontSize="large" />}
            hint="Aguardando aprovação"
          />
        </Grid>
      </Grid>

      <Card variant="outlined">
        <CardContent>
          <Stack spacing={2}>
            <Typography variant="h6">Conformidade por curso</Typography>
            {complianceQuery.isLoading ? (
              <LoadingState />
            ) : complianceQuery.isError ? (
              <ErrorState
                message={getErrorMessage(complianceQuery.error, 'Erro ao carregar relatório')}
                onRetry={() => complianceQuery.refetch()}
              />
            ) : data.length === 0 ? (
              <EmptyState title="Sem dados de conformidade" />
            ) : (
              <TableContainer component={Paper} variant="outlined">
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Curso</TableCell>
                      <TableCell>Código</TableCell>
                      <TableCell align="center">Conformes / Aprovados</TableCell>
                      <TableCell sx={{ minWidth: 240 }}>% Conformidade</TableCell>
                      <TableCell align="center">Status</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {data.map((c) => (
                      <TableRow key={c.courseId} hover>
                        <TableCell>{c.courseName}</TableCell>
                        <TableCell>{c.courseCode}</TableCell>
                        <TableCell align="center">
                          {getCompliantProfessors(c)} / {c.totalApprovedProfessors}
                        </TableCell>
                        <TableCell>
                          <ProgressBar value={c.compliancePercentage} />
                        </TableCell>
                        <TableCell align="center">
                          <Chip
                            size="small"
                            label={c.compliancePercentage >= COMPLIANCE_THRESHOLD ? 'Ok' : 'Atenção'}
                            color={
                              c.compliancePercentage >= COMPLIANCE_THRESHOLD ? 'success' : 'warning'
                            }
                          />
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
    </Stack>
  );
}
