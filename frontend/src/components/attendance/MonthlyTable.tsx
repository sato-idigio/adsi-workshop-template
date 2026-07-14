'use client';

import type { AttendanceRecordResponse } from '@/lib/types';

interface MonthlyTableProps {
  records: AttendanceRecordResponse[];
  isLoading: boolean;
}

function formatTime(dateTimeStr: string | null): string {
  if (!dateTimeStr) return '-';
  const date = new Date(dateTimeStr);
  return date.toLocaleTimeString('ja-JP', { hour: '2-digit', minute: '2-digit' });
}

function formatMinutes(minutes: number | null): string {
  if (minutes == null) return '-';
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return `${h}:${m.toString().padStart(2, '0')}`;
}

function formatDate(dateStr: string): string {
  const date = new Date(dateStr);
  const dayNames = ['日', '月', '火', '水', '木', '金', '土'];
  const day = date.getDay();
  return `${date.getDate()}日 (${dayNames[day]})`;
}

export function MonthlyTable({ records, isLoading }: MonthlyTableProps) {
  if (isLoading) {
    return <div className="text-gray-500">読み込み中...</div>;
  }

  if (records.length === 0) {
    return <div className="text-gray-500">この月の勤怠データはありません</div>;
  }

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">日付</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">出勤</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">退勤</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">勤務</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">残業</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">深夜</th>
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {records.map((record) => (
            <tr key={record.id}>
              <td className="px-4 py-3 text-sm">{formatDate(record.date)}</td>
              <td className="px-4 py-3 text-sm">{formatTime(record.clockIn)}</td>
              <td className="px-4 py-3 text-sm">{formatTime(record.clockOut)}</td>
              <td className="px-4 py-3 text-sm">{formatMinutes(record.workMinutes)}</td>
              <td className="px-4 py-3 text-sm text-orange-600">
                {record.overtimeMinutes && record.overtimeMinutes > 0
                  ? formatMinutes(record.overtimeMinutes)
                  : '-'}
              </td>
              <td className="px-4 py-3 text-sm text-purple-600">
                {record.nightMinutes && record.nightMinutes > 0
                  ? formatMinutes(record.nightMinutes)
                  : '-'}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
