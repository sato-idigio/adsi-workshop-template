'use client';

import { useLeaveBalance } from '@/hooks/useLeave';

export function LeaveBalance() {
  const { data, error, isLoading } = useLeaveBalance();

  if (isLoading) return <div className="text-gray-500">読み込み中...</div>;
  if (error) return <div className="text-red-500">残日数の取得に失敗しました</div>;
  if (!data) return null;

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <h2 className="text-lg font-semibold mb-4">有給残日数（{data.fiscalYear}年度）</h2>
      <div className="grid grid-cols-3 gap-4 text-center">
        <div>
          <div className="text-2xl font-bold text-blue-600">{data.totalDays}</div>
          <div className="text-sm text-gray-500">付与日数</div>
        </div>
        <div>
          <div className="text-2xl font-bold text-orange-600">{data.usedDays}</div>
          <div className="text-sm text-gray-500">使用日数</div>
        </div>
        <div>
          <div className="text-2xl font-bold text-green-600">{data.remainingDays}</div>
          <div className="text-sm text-gray-500">残日数</div>
        </div>
      </div>
    </div>
  );
}
