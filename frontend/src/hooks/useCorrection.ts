import useSWR from 'swr';
import { apiClient } from '@/lib/api-client';
import type { CorrectionRequestCreate, CorrectionRequestResponse, PagedCorrectionRequests } from '@/lib/types';

const fetcher = <T>(path: string) => apiClient<T>(path);

export function useCorrectionList(status?: string, page = 0, size = 20) {
  const params = new URLSearchParams();
  if (status) params.set('status', status);
  params.set('page', String(page));
  params.set('size', String(size));
  const query = params.toString();

  const { data, error, isLoading, mutate } = useSWR<PagedCorrectionRequests>(
    `/attendance-corrections?${query}`,
    fetcher
  );
  return { data: data ?? { content: [], totalElements: 0, totalPages: 0, number: 0, size: 20 }, error, isLoading, mutate };
}

export async function createCorrection(request: CorrectionRequestCreate): Promise<CorrectionRequestResponse> {
  return apiClient<CorrectionRequestResponse>('/attendance-corrections', {
    method: 'POST',
    body: request,
  });
}

export async function approveCorrection(id: number): Promise<CorrectionRequestResponse> {
  return apiClient<CorrectionRequestResponse>(`/attendance-corrections/${id}/approve`, {
    method: 'PUT',
  });
}

export async function rejectCorrection(id: number): Promise<CorrectionRequestResponse> {
  return apiClient<CorrectionRequestResponse>(`/attendance-corrections/${id}/reject`, {
    method: 'PUT',
  });
}
