import {
  Box,
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
import EmojiEventsIcon from '@mui/icons-material/EmojiEvents';
import StarIcon from '@mui/icons-material/Star';
import { useQuery } from '@tanstack/react-query';
import { rankingService } from '../../api/reportService';
import { EmptyState, ErrorState, LoadingState } from '../../components/States';
import { getErrorMessage } from '../../api/httpClient';

const MEDAL_COLORS = ['#d4af37', '#c0c0c0', '#cd7f32'];

export function AdminRankingPage() {
  const rankingQuery = useQuery({
    queryKey: ['rankings'],
    queryFn: rankingService.getAll,
  });

  return (
    <Stack spacing={3}>
      <Box>
        <Typography variant="h4">Ranking de produção científica</Typography>
        <Typography color="text.secondary">
          Ordenado por publicações validadas nos últimos 3 anos.
        </Typography>
      </Box>

      {rankingQuery.isLoading ? (
        <LoadingState />
      ) : rankingQuery.isError ? (
        <ErrorState
          message={getErrorMessage(rankingQuery.error, 'Erro ao carregar ranking')}
          onRetry={() => rankingQuery.refetch()}
        />
      ) : (rankingQuery.data ?? []).length === 0 ? (
        <Paper variant="outlined">
          <EmptyState title="Sem dados de ranking" />
        </Paper>
      ) : (
        <TableContainer component={Paper} variant="outlined">
          <Table>
            <TableHead>
              <TableRow>
                <TableCell width={80}>Posição</TableCell>
                <TableCell>Professor</TableCell>
                <TableCell align="right">Publicações validadas (3 anos)</TableCell>
                <TableCell align="center" width={80}>
                  Meta
                </TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {(rankingQuery.data ?? []).map((entry) => {
                const isTop3 = entry.rank <= 3;
                const meetsGoal = entry.validatedPublicationsLast3Years >= 9;
                return (
                  <TableRow
                    key={entry.professorId}
                    hover
                    sx={isTop3 ? { bgcolor: 'action.hover' } : undefined}
                  >
                    <TableCell>
                      <Stack direction="row" alignItems="center" spacing={1}>
                        {isTop3 && (
                          <EmojiEventsIcon
                            sx={{ color: MEDAL_COLORS[entry.rank - 1] }}
                            fontSize="small"
                          />
                        )}
                        <Typography sx={{ fontWeight: isTop3 ? 700 : 500 }}>
                          #{entry.rank}
                        </Typography>
                      </Stack>
                    </TableCell>
                    <TableCell>{entry.name}</TableCell>
                    <TableCell align="right">
                      <Typography sx={{ fontWeight: 600 }}>
                        {entry.validatedPublicationsLast3Years}
                      </Typography>
                    </TableCell>
                    <TableCell align="center">
                      {meetsGoal && (
                        <StarIcon
                          fontSize="small"
                          sx={{ color: 'success.main' }}
                          titleAccess="Atende à meta MEC"
                        />
                      )}
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Stack>
  );
}
