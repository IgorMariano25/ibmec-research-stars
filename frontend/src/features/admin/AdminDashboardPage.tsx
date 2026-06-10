import {
  Box,
  Card,
  CardContent,
  Chip,
  Collapse,
  Grid,
  IconButton,
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
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp';
import SchoolIcon from '@mui/icons-material/School';
import GroupsIcon from '@mui/icons-material/Groups';
import HourglassEmptyIcon from '@mui/icons-material/HourglassEmpty';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import { useQuery } from '@tanstack/react-query';
import { Fragment, useState } from 'react';
import { reportService } from '../../api/reportService';
import { professorService } from '../../api/professorService';
import type { CourseCompliance } from '../../api/types';
import { LoadingState, ErrorState, EmptyState } from '../../components/States';
import { ProgressBar, StatCard } from '../../components/StatCard';
import { getErrorMessage } from '../../api/httpClient';

const COMPLIANCE_THRESHOLD = 50;
const PUBLICATION_GOAL = 9;

function getCompliantProfessors(course: CourseCompliance): number {
  return course.compliantProfessors ?? course.totalCompliantProfessors ?? 0;
}

export function AdminDashboardPage() {
  const [expandedCourseIds, setExpandedCourseIds] = useState<Set<number>>(new Set());
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

  function toggleCourse(courseId: number) {
    setExpandedCourseIds((current) => {
      const next = new Set(current);
      if (next.has(courseId)) {
        next.delete(courseId);
      } else {
        next.add(courseId);
      }
      return next;
    });
  }

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
                      <TableCell width={48} />
                      <TableCell>Curso</TableCell>
                      <TableCell>Código</TableCell>
                      <TableCell align="center">Professores conformes / aprovados</TableCell>
                      <TableCell sx={{ minWidth: 240 }}>% Conformidade</TableCell>
                      <TableCell align="center">Status</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {data.map((c) => {
                      const isExpanded = expandedCourseIds.has(c.courseId);
                      const professorCompliance = c.professorCompliance ?? [];

                      return (
                        <Fragment key={c.courseId}>
                          <TableRow hover>
                            <TableCell>
                              <IconButton
                                size="small"
                                onClick={() => toggleCourse(c.courseId)}
                                aria-label={isExpanded ? 'Ocultar professores' : 'Mostrar professores'}
                              >
                                {isExpanded ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
                              </IconButton>
                            </TableCell>
                            <TableCell>{c.courseName}</TableCell>
                            <TableCell>{c.courseCode}</TableCell>
                            <TableCell align="center">
                              {getCompliantProfessors(c)} / {c.totalApprovedProfessors}
                            </TableCell>
                            <TableCell>
                              <Stack direction="row" spacing={1.5} alignItems="center">
                                <Box sx={{ flex: 1, minWidth: 160 }}>
                                  <ProgressBar value={c.compliancePercentage} />
                                </Box>
                                <Typography variant="body2" sx={{ minWidth: 48, textAlign: 'right' }}>
                                  {c.compliancePercentage.toFixed(1)}%
                                </Typography>
                              </Stack>
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
                          <TableRow>
                            <TableCell sx={{ p: 0, borderBottom: isExpanded ? undefined : 0 }} colSpan={6}>
                              <Collapse in={isExpanded} timeout="auto" unmountOnExit>
                                <Box sx={{ px: 2, py: 1.5, bgcolor: 'action.hover' }}>
                                  <Typography variant="subtitle2" sx={{ mb: 1 }}>
                                    Detalhe por professor
                                  </Typography>
                                  {professorCompliance.length === 0 ? (
                                    <Typography variant="body2" color="text.secondary">
                                      Nenhum professor aprovado vinculado ao curso.
                                    </Typography>
                                  ) : (
                                    <Stack spacing={1}>
                                      {professorCompliance.map((professor) => (
                                        <Stack
                                          key={professor.professorId}
                                          direction={{ xs: 'column', sm: 'row' }}
                                          spacing={1}
                                          alignItems={{ xs: 'flex-start', sm: 'center' }}
                                          justifyContent="space-between"
                                        >
                                          <Typography variant="body2">{professor.professorName}</Typography>
                                          <Stack direction="row" spacing={1} alignItems="center">
                                            <Typography variant="body2" color="text.secondary">
                                              {professor.validatedPublications} / {PUBLICATION_GOAL}
                                            </Typography>
                                            <Chip
                                              size="small"
                                              label={professor.compliant ? 'Conforme' : 'Abaixo da meta'}
                                              color={professor.compliant ? 'success' : 'warning'}
                                            />
                                          </Stack>
                                        </Stack>
                                      ))}
                                    </Stack>
                                  )}
                                </Box>
                              </Collapse>
                            </TableCell>
                          </TableRow>
                        </Fragment>
                      );
                    })}
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
