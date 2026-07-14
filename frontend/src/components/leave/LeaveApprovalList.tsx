'use client';

import { useState } from 'react';
import { useLeaveRequests, approveLeaveRequest, rejectLeaveRequest } from '@/hooks/useLeave';
import { LEAVE_TYPE_LABELS } from '@/lib/constants';

export function LeaveApprovalList() {
  const { data, error, isLoading, mutate } = useLeaveRequests('PENDING');
  const [processing, setProcessing] = useState<number | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);

  async function handleApprove(id: number) {
    setProcessing(id);
    setActionError(null);
    try {
      await approveLeaveRequest(id);
      mutate();
    } catch (err) {
      setActionError(err instanceof Error ? err.message : '承認に失敗しました');
    } finally {
      setProcessing(null);
    }
  }

  async function handleReject(id: number) {
    setProcessing(id);
    setActionError(null);
    try {
      await rejectLeaveRequest(id);
      mutate();
    } catch (err) {
      setActionError(err instanceof Error ? err.message : '却下に失敗しました');
    } finally {
      setProcessing(null);
    }
  }

  if (isLoading) return <div className="text-gray-500">読み込み中...</div>;
  if (error) return <div className="text-red-500">承認待ち一覧の取得に失敗しました</div>;
  if (!data || data.content.length === 0) {
    return <div className="text-gray-500">承認待ちの申請はありません</div>;
  }

  return (
    <div className="bg-white rounded-lg shadow overflow-hidden">
      {actionError && (
        <div className="px-4 py-3 bg-red-50 text-red-700 text-sm border-b border-red-200">
          {actionError}
        </div>
      )}
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">申請者</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">種別</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">期間</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">日数</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">理由</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-200">
          {data.content.map((request) => (
            <tr key={request.id}>
              <td className="px-4 py-3 text-sm">{request.employeeName}</td>
              <td className="px-4 py-3 text-sm">{LEAVE_TYPE_LABELS[request.leaveType]}</td>
              <td className="px-4 py-3 text-sm">
                {request.startDate === request.endDate
                  ? request.startDate
                  : `${request.startDate} 〜 ${request.endDate}`}
              </td>
              <td className="px-4 py-3 text-sm">{request.days}日</td>
              <td className="px-4 py-3 text-sm max-w-xs truncate">{request.reason}</td>
              <td className="px-4 py-3 text-sm space-x-2">
                <button
                  onClick={() => handleApprove(request.id)}
                  disabled={processing === request.id}
                  className="bg-green-600 text-white px-3 py-1 rounded text-xs hover:bg-green-700 disabled:opacity-50"
                >
                  承認
                </button>
                <button
                  onClick={() => handleReject(request.id)}
                  disabled={processing === request.id}
                  className="bg-red-600 text-white px-3 py-1 rounded text-xs hover:bg-red-700 disabled:opacity-50"
                >
                  却下
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
