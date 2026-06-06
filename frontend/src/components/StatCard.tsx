import {
  Card,
  CardContent,
  LinearProgress,
  Stack,
  Typography,
  Box,
} from '@mui/material';
import type { ReactNode } from 'react';

export function StatCard({
  label,
  value,
  hint,
  icon,
  color = 'primary.main',
}: {
  label: string;
  value: ReactNode;
  hint?: string;
  icon?: ReactNode;
  color?: string;
}) {
  return (
    <Card variant="outlined" sx={{ height: '100%' }}>
      <CardContent>
        <Stack direction="row" justifyContent="space-between" alignItems="flex-start">
          <Box>
            <Typography color="text.secondary" variant="body2" gutterBottom>
              {label}
            </Typography>
            <Typography variant="h4" sx={{ color }}>
              {value}
            </Typography>
            {hint && (
              <Typography variant="caption" color="text.secondary">
                {hint}
              </Typography>
            )}
          </Box>
          {icon && (
            <Box
              sx={{
                bgcolor: 'action.hover',
                borderRadius: 2,
                p: 1,
                color,
                display: 'flex',
              }}
            >
              {icon}
            </Box>
          )}
        </Stack>
      </CardContent>
    </Card>
  );
}

export function ProgressBar({
  value,
  label,
  hint,
  color,
}: {
  value: number;
  label?: string;
  hint?: string;
  color?: 'primary' | 'success' | 'warning' | 'error';
}) {
  const clamped = Math.max(0, Math.min(100, value));
  return (
    <Box sx={{ width: '100%' }}>
      {label && (
        <Stack direction="row" justifyContent="space-between" sx={{ mb: 0.5 }}>
          <Typography variant="body2">{label}</Typography>
          <Typography variant="body2" color="text.secondary">
            {clamped.toFixed(0)}%
          </Typography>
        </Stack>
      )}
      <LinearProgress
        variant="determinate"
        value={clamped}
        color={color ?? (clamped >= 80 ? 'success' : clamped >= 50 ? 'warning' : 'error')}
        sx={{ height: 8, borderRadius: 4 }}
      />
      {hint && (
        <Typography variant="caption" color="text.secondary">
          {hint}
        </Typography>
      )}
    </Box>
  );
}
