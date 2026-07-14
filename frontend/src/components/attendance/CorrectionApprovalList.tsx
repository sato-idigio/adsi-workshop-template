'use client';

import { useState } from 'react';
import { approveCorrection, rejectCorrection } from '@/hooks/useCorrection';
import type { CorrectionRequestResponse } from '@/lib/types';

interface CorrectionApprovalListProps {
  corrections: CorrectionRequestResponse[];
  isLoading: boolean;
  onAction: () => void;
}

function formatDateTime(dateTimeStr: string | null): string {
  if (!dateTimeStr) return '-';
  const date = new Date(dateTimeStr);
  return date.toLocaleString('ja-JP', {
    month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit',
  });
}

export function CorrectionApprovalList({ corrections, isLoading, onAction }: CorrectionApprovalListProps) {
  const [processingId, setProcessingId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleApprove = async (id: number) => {
    setProcessingId(id);
    setError(null);
    try {
      await approveCorrection(id);
      onAction();
    } catch (e) {
      setError(e instanceof Error ? e.message : '承認に失敗しました');
    } finally {
      setProcessingId(null);
    }
  };

  const handleReject = async (id: number) => {
    setProcessingId(id);
    setError(null);
    try {
      await rejectCorrection(id);
      onAction();
    } catch (e) {
      setError(e instanceof Error ? e.message : '却下に失敗しました');
    } finally {
      setProcessingId(null);
    }
  };

  if (isLoading) {
    return <div className="text-gray-500">読み込み中...</div>;
  }

  if (corrections.length === 0) {
    return <div className="text-gray-500">承認待ちの申請はありません</div>;
  }

  return (
    <div className="space-y-3">
      {error && <p className="text-red-600 text-sm">{error}</p>}
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500">申請者</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500">日付</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500">現在出勤</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500">修正後出勤</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500">現在退勤</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500">修正後退勤</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500">理由</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500">操作</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {corrections.map((c) => (
              <tr key={c.id}>
                <td className="px-4 py-3 text-sm">{c.employeeName}</td>
                <td className="px-4 py-3 text-sm">{c.date}</td>
                <td className="px-4 py-3 text-sm">{formatDateTime(c.currentClockIn)}</td>
                <td className="px-4 py-3 text-sm font-medium text-blue-600">
                  {formatDateTime(c.requestedClockIn)}
                </td>
                <td className="px-4 py-3 text-sm">{formatDateTime(c.currentClockOut)}</td>
                <td className="px-4 py-3 text-sm font-medium text-blue-600">
                  {formatDateTime(c.requestedClockOut)}
                </td>
                <td className="px-4 py-3 text-sm max-w-xs truncate">{c.reason}</td>
                <td className="px-4 py-3 text-sm">
                  <div className="flex gap-2">
                    <button
                      onClick={() => handleApprove(c.id)}
                      disabled={processingId === c.id}
                      className="px-3 py-1 bg-green-600 text-white rounded text-xs hover:bg-green-700 disabled:bg-gray-300"
                    >
                      承認
                    </button>
                    <button
                      onClick={() => handleReject(c.id)}
                      disabled={processingId === c.id}
                      className="px-3 py-1 bg-red-600 text-white rounded text-xs hover:bg-red-700 disabled:bg-gray-300"
                    >
                      却下
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
