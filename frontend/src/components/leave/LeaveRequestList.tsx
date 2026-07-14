'use client';

import { useLeaveRequests } from '@/hooks/useLeave';
import type { ApprovalStatus, LeaveType } from '@/lib/types';

const LEAVE_TYPE_LABELS: Record<LeaveType, string> = {
  PAID: '有給休暇',
  HALF_AM: '午前半休',
  HALF_PM: '午後半休',
  SPECIAL: '特別休暇',
  COMPENSATORY: '代休',
};

const STATUS_LABELS: Record<ApprovalStatus, string> = {
  PENDING: '承認待ち',
  APPROVED: '承認済み',
  REJECTED: '却下',
};

const STATUS_COLORS: Record<ApprovalStatus, string> = {
  PENDING: 'bg-yellow-100 text-yellow-800',
  APPROVED: 'bg-green-100 text-green-800',
  REJECTED: 'bg-red-100 text-red-800',
};

export function LeaveRequestList() {
  const { data, error, isLoading } = useLeaveRequests();

  if (isLoading) return <div className="text-gray-500">読み込み中...</div>;
  if (error) return <div className="text-red-500">申請一覧の取得に失敗しました</div>;
  if (!data || data.content.length === 0) {
    return <div className="text-gray-500">休暇申請はありません</div>;
  }

  return (
    <div className="bg-white rounded-lg shadow overflow-hidden">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">種別</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">期間</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">日数</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">理由</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">ステータス</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-200">
          {data.content.map((request) => (
            <tr key={request.id}>
              <td className="px-4 py-3 text-sm">{LEAVE_TYPE_LABELS[request.leaveType]}</td>
              <td className="px-4 py-3 text-sm">
                {request.startDate === request.endDate
                  ? request.startDate
                  : `${request.startDate} 〜 ${request.endDate}`}
              </td>
              <td className="px-4 py-3 text-sm">{request.days}日</td>
              <td className="px-4 py-3 text-sm max-w-xs truncate">{request.reason}</td>
              <td className="px-4 py-3 text-sm">
                <span className={`px-2 py-1 rounded text-xs font-medium ${STATUS_COLORS[request.status]}`}>
                  {STATUS_LABELS[request.status]}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
