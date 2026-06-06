import { Box, CircularProgress, Skeleton, Stack, Typography, Button } from '@mui/material';
import type { ReactNode } from 'react';
import InboxIcon from '@mui/icons-material/Inbox';
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutline';

export function LoadingState({ rows = 3 }: { rows?: number }) {
  return (
    <Stack spacing={1} sx={{ p: 2 }}>
      {Array.from({ length: rows }).map((_, idx) => (
        <Skeleton key={idx} variant="rounded" height={48} />
      ))}
    </Stack>
  );
}

export function CenteredSpinner() {
  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
      <CircularProgress />
    </Box>
  );
}

export function ErrorState({
  message,
  onRetry,
}: {
  message: string;
  onRetry?: () => void;
}) {
  return (
    <Stack spacing={2} alignItems="center" sx={{ py: 6, px: 2, textAlign: 'center' }}>
      <ErrorOutlineIcon color="error" sx={{ fontSize: 48 }} />
      <Typography color="text.secondary">{message}</Typography>
      {onRetry && (
        <Button variant="outlined" onClick={onRetry}>
          Tentar novamente
        </Button>
      )}
    </Stack>
  );
}

export function EmptyState({
  title = 'Nada por aqui ainda',
  description,
  action,
}: {
  title?: string;
  description?: string;
  action?: ReactNode;
}) {
  return (
    <Stack spacing={1.5} alignItems="center" sx={{ py: 6, px: 2, textAlign: 'center' }}>
      <InboxIcon color="disabled" sx={{ fontSize: 48 }} />
      <Typography variant="subtitle1">{title}</Typography>
      {description && (
        <Typography color="text.secondary" variant="body2">
          {description}
        </Typography>
      )}
      {action}
    </Stack>
  );
}
