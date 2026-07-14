'use client';

import type { CorrectionRequestResponse } from '@/lib/types';

interface CorrectionListProps {
  corrections: CorrectionRequestResponse[];
  isLoading: boolean;
}

function formatDateTime(dateTimeStr: string | null): string {
  if (!dateTimeStr) return '-';
  const date = new Date(dateTimeStr);
  return date.toLocaleString('ja-JP', {
    month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit',
  });
}

function statusBadge(status: string) {
  const styles: Record<string, string> = {
    PENDING: 'bg-yellow-100 text-yellow-800',
    APPROVED: 'bg-green-100 text-green-800',
    REJECTED: 'bg-red-100 text-red-800',
  };
  const labels: Record<string, string> = {
    PENDING: '承認待ち',
    APPROVED: '承認済み',
    REJECTED: '却下',
  };
  return (
    <span className={`px-2 py-1 rounded-full text-xs font-medium ${styles[status] || ''}`}>
      {labels[status] || status}
    </span>
  );
}

export function CorrectionList({ corrections, isLoading }: CorrectionListProps) {
  if (isLoading) {
    return <div className="text-gray-500">読み込み中...</div>;
  }

  if (corrections.length === 0) {
    return <div className="text-gray-500">修正申請はありません</div>;
  }

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500">日付</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500">修正後出勤</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500">修正後退勤</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500">理由</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500">ステータス</th>
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {corrections.map((c) => (
            <tr key={c.id}>
              <td className="px-4 py-3 text-sm">{c.date}</td>
              <td className="px-4 py-3 text-sm">{formatDateTime(c.requestedClockIn)}</td>
              <td className="px-4 py-3 text-sm">{formatDateTime(c.requestedClockOut)}</td>
              <td className="px-4 py-3 text-sm max-w-xs truncate">{c.reason}</td>
              <td className="px-4 py-3 text-sm">{statusBadge(c.status)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
