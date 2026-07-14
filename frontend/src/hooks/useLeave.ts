import useSWR from 'swr';
import { apiClient } from '@/lib/api-client';
import type {
  LeaveRequestCreate,
  LeaveRequestResponse,
  LeaveBalanceResponse,
  PagedLeaveRequests,
  ApprovalStatus,
} from '@/lib/types';

function fetcher<T>(path: string) {
  return apiClient<T>(path);
}

export function useLeaveRequests(status?: ApprovalStatus, page = 0, size = 20) {
  const params = new URLSearchParams();
  if (status) params.set('status', status);
  params.set('page', String(page));
  params.set('size', String(size));

  return useSWR<PagedLeaveRequests>(
    `/leaves?${params.toString()}`,
    fetcher
  );
}

export function useLeaveBalance(fiscalYear?: number) {
  const params = fiscalYear ? `?fiscalYear=${fiscalYear}` : '';
  return useSWR<LeaveBalanceResponse>(`/leaves/balance${params}`, fetcher);
}

export async function createLeaveRequest(data: LeaveRequestCreate): Promise<LeaveRequestResponse> {
  return apiClient<LeaveRequestResponse>('/leaves', {
    method: 'POST',
    body: data,
  });
}

export async function approveLeaveRequest(id: number): Promise<LeaveRequestResponse> {
  return apiClient<LeaveRequestResponse>(`/leaves/${id}/approve`, {
    method: 'PUT',
  });
}

export async function rejectLeaveRequest(id: number): Promise<LeaveRequestResponse> {
  return apiClient<LeaveRequestResponse>(`/leaves/${id}/reject`, {
    method: 'PUT',
  });
}
