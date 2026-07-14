import useSWR from 'swr';
import { apiClient } from '@/lib/api-client';
import type { PagedMonthlySummaries } from '@/lib/types';

function fetcher<T>(path: string) {
  return apiClient<T>(path);
}

export function useMonthlyReport(yearMonth: string, departmentId?: number, page = 0, size = 20) {
  const params = new URLSearchParams();
  params.set('yearMonth', yearMonth);
  if (departmentId) params.set('departmentId', String(departmentId));
  params.set('page', String(page));
  params.set('size', String(size));

  return useSWR<PagedMonthlySummaries>(
    yearMonth ? `/reports/monthly?${params.toString()}` : null,
    fetcher
  );
}

export async function generateMonthlyReport(yearMonth: string): Promise<void> {
  await apiClient<void>(`/reports/monthly/generate?yearMonth=${yearMonth}`, {
    method: 'POST',
  });
}
