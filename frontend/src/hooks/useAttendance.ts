import useSWR from 'swr';
import { apiClient } from '@/lib/api-client';
import type { AttendanceRecordResponse } from '@/lib/types';

const fetcher = <T>(path: string) => apiClient<T>(path);

export function useTodayAttendance() {
  const { data, error, isLoading, mutate } = useSWR<AttendanceRecordResponse | null>(
    '/attendance/today',
    fetcher
  );
  return { today: data ?? null, error, isLoading, mutate };
}

export function useMonthlyAttendance(yearMonth: string) {
  const { data, error, isLoading } = useSWR<AttendanceRecordResponse[]>(
    yearMonth ? `/attendance/monthly?yearMonth=${yearMonth}` : null,
    fetcher
  );
  return { records: data ?? [], error, isLoading };
}

export async function clockIn(): Promise<AttendanceRecordResponse> {
  return apiClient<AttendanceRecordResponse>('/attendance/clock-in', { method: 'POST' });
}

export async function clockOut(): Promise<AttendanceRecordResponse> {
  return apiClient<AttendanceRecordResponse>('/attendance/clock-out', { method: 'POST' });
}
