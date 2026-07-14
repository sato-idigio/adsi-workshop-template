import type { LeaveType, ApprovalStatus } from './types';

export const LEAVE_TYPE_LABELS: Record<LeaveType, string> = {
  PAID: '有給休暇',
  HALF_AM: '午前半休',
  HALF_PM: '午後半休',
  SPECIAL: '特別休暇',
  COMPENSATORY: '代休',
};

export const STATUS_LABELS: Record<ApprovalStatus, string> = {
  PENDING: '承認待ち',
  APPROVED: '承認済み',
  REJECTED: '却下',
};

export const STATUS_COLORS: Record<ApprovalStatus, string> = {
  PENDING: 'bg-yellow-100 text-yellow-800',
  APPROVED: 'bg-green-100 text-green-800',
  REJECTED: 'bg-red-100 text-red-800',
};
