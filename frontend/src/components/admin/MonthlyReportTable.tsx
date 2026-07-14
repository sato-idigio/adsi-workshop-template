'use client';

import type { MonthlySummaryResponse } from '@/lib/types';

interface MonthlyReportTableProps {
  data: MonthlySummaryResponse[];
  totalPages: number;
  currentPage: number;
  onPageChange: (page: number) => void;
}

function formatMinutes(minutes: number): string {
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  return `${hours}:${String(mins).padStart(2, '0')}`;
}

export function MonthlyReportTable({ data, totalPages, currentPage, onPageChange }: MonthlyReportTableProps) {
  if (data.length === 0) {
    return <p className="text-gray-500 text-sm">データがありません。「集計生成」を実行してください。</p>;
  }

  return (
    <div>
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">社員名</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">部署</th>
              <th className="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">総労働時間</th>
              <th className="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">残業時間</th>
              <th className="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">深夜時間</th>
              <th className="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">休日出勤</th>
              <th className="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">有給取得</th>
              <th className="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">出勤日数</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {data.map((row) => (
              <tr key={row.employeeId}>
                <td className="px-4 py-3 text-sm text-gray-900">{row.employeeName}</td>
                <td className="px-4 py-3 text-sm text-gray-600">{row.departmentName}</td>
                <td className="px-4 py-3 text-sm text-gray-900 text-right">{formatMinutes(row.totalWorkMinutes)}</td>
                <td className="px-4 py-3 text-sm text-gray-900 text-right">{formatMinutes(row.overtimeMinutes)}</td>
                <td className="px-4 py-3 text-sm text-gray-900 text-right">{formatMinutes(row.nightMinutes)}</td>
                <td className="px-4 py-3 text-sm text-gray-900 text-right">{formatMinutes(row.holidayWorkMinutes)}</td>
                <td className="px-4 py-3 text-sm text-gray-900 text-right">{row.paidLeaveDays}日</td>
                <td className="px-4 py-3 text-sm text-gray-900 text-right">{row.workingDays}日</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {totalPages > 1 && (
        <div className="flex justify-center gap-2 mt-4">
          <button
            onClick={() => onPageChange(currentPage - 1)}
            disabled={currentPage === 0}
            className="px-3 py-1 text-sm border rounded disabled:opacity-50"
          >
            前へ
          </button>
          <span className="px-3 py-1 text-sm">
            {currentPage + 1} / {totalPages}
          </span>
          <button
            onClick={() => onPageChange(currentPage + 1)}
            disabled={currentPage >= totalPages - 1}
            className="px-3 py-1 text-sm border rounded disabled:opacity-50"
          >
            次へ
          </button>
        </div>
      )}
    </div>
  );
}
