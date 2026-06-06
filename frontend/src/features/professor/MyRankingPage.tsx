import {
  Box,
  Card,
  CardContent,
  Grid,
  Stack,
  Typography,
} from '@mui/material';
import EmojiEventsIcon from '@mui/icons-material/EmojiEvents';
import StarIcon from '@mui/icons-material/Star';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import { useQuery } from '@tanstack/react-query';
import { rankingService } from '../../api/reportService';
import { CenteredSpinner, ErrorState } from '../../components/States';
import { ProgressBar, StatCard } from '../../components/StatCard';
import { getErrorMessage } from '../../api/httpClient';

const GOAL = 9;

export function MyRankingPage() {
  const rankingQuery = useQuery({
    queryKey: ['rankings', 'me'],
    queryFn: rankingService.getMine,
  });

  if (rankingQuery.isLoading) return <CenteredSpinner />;
  if (rankingQuery.isError) {
    return (
      <ErrorState
        message={getErrorMessage(rankingQuery.error, 'Não foi possível carregar sua posição')}
        onRetry={() => rankingQuery.refetch()}
      />
    );
  }

  const me = rankingQuery.data!;
  const progress = Math.min(100, (me.validatedPublicationsLast3Years / GOAL) * 100);
  const meetsGoal = me.validatedPublicationsLast3Years >= GOAL;

  return (
    <Stack spacing={3}>
      <Box>
        <Typography variant="h4">Minha posição no ranking</Typography>
        <Typography color="text.secondary">
          Acompanhe seu progresso rumo à meta MEC de 9 publicações validadas nos últimos 3 anos.
        </Typography>
      </Box>

      <Grid container spacing={2}>
        <Grid item xs={12} md={6}>
          <StatCard
            label="Sua colocação"
            value={`#${me.rank}`}
            icon={<EmojiEventsIcon fontSize="large" />}
            hint="No ranking geral de produção científica"
          />
        </Grid>
        <Grid item xs={12} md={6}>
          <StatCard
            label="Publicações validadas (últimos 3 anos)"
            value={me.validatedPublicationsLast3Years}
            color={meetsGoal ? 'success.main' : 'warning.main'}
            icon={meetsGoal ? <CheckCircleIcon fontSize="large" /> : <StarIcon fontSize="large" />}
            hint={meetsGoal ? 'Você atende à meta MEC!' : `Meta: ${GOAL} publicações`}
          />
        </Grid>
      </Grid>

      <Card variant="outlined">
        <CardContent>
          <Stack spacing={2}>
            <Typography variant="h6">Progresso até a meta</Typography>
            <ProgressBar
              value={progress}
              label={`${me.validatedPublicationsLast3Years} de ${GOAL} publicações validadas`}
              color={meetsGoal ? 'success' : progress >= 50 ? 'warning' : 'error'}
            />
            <Typography variant="body2" color="text.secondary">
              {meetsGoal
                ? 'Parabéns! Você está em conformidade com a exigência MEC. Continue mantendo sua produção em dia.'
                : `Faltam ${Math.max(0, GOAL - me.validatedPublicationsLast3Years)} publicação(ões) validada(s) para atingir a meta.`}
            </Typography>
          </Stack>
        </CardContent>
      </Card>
    </Stack>
  );
}
