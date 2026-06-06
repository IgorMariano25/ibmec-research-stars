import { Chip } from '@mui/material';
import type { ProfessorStatus, PublicationStatus } from '../api/types';

const publicationMap: Record<PublicationStatus, { label: string; color: 'warning' | 'success' | 'error' }> = {
  PENDING: { label: 'Pendente', color: 'warning' },
  VALIDATED: { label: 'Validada', color: 'success' },
  REJECTED: { label: 'Rejeitada', color: 'error' },
};

const professorMap: Record<ProfessorStatus, { label: string; color: 'warning' | 'success' }> = {
  PENDING: { label: 'Pendente', color: 'warning' },
  APPROVED: { label: 'Aprovado', color: 'success' },
};

interface Props {
  status: PublicationStatus | ProfessorStatus;
  size?: 'small' | 'medium';
}

export function StatusChip({ status, size = 'small' }: Props) {
  const info =
    (publicationMap as Record<string, { label: string; color: 'warning' | 'success' | 'error' }>)[status] ||
    (professorMap as Record<string, { label: string; color: 'warning' | 'success' }>)[status];
  if (!info) return <Chip size={size} label={status} />;
  return <Chip size={size} label={info.label} color={info.color} variant="filled" />;
}
