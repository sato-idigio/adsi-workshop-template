'use client';

import type { AttendanceRecordResponse } from '@/lib/types';

interface TodayStatusProps {
  record: AttendanceRecordResponse | null;
}

function formatTime(dateTimeStr: string | null): string {
  if (!dateTimeStr) return '--:--';
  const date = new Date(dateTimeStr);
  return date.toLocaleTimeString('ja-JP', { hour: '2-digit', minute: '2-digit' });
}

function formatMinutes(minutes: number | null): string {
  if (minutes == null) return '-';
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return `${h}時間${m > 0 ? `${m}分` : ''}`;
}

export function TodayStatus({ record }: TodayStatusProps) {
  if (!record) {
    return (
      <div className="text-gray-500 text-sm">本日の打刻はまだありません</div>
    );
  }

  return (
    <div className="space-y-2">
      <div className="flex gap-6 text-sm">
        <div>
          <span className="text-gray-500">出勤:</span>{' '}
          <span className="font-medium">{formatTime(record.clockIn)}</span>
        </div>
        <div>
          <span className="text-gray-500">退勤:</span>{' '}
          <span className="font-medium">{formatTime(record.clockOut)}</span>
        </div>
      </div>
      {record.workMinutes != null && (
        <div className="flex gap-6 text-sm text-gray-600">
          <div>勤務: {formatMinutes(record.workMinutes)}</div>
          {record.overtimeMinutes != null && record.overtimeMinutes > 0 && (
            <div className="text-orange-600">残業: {formatMinutes(record.overtimeMinutes)}</div>
          )}
          {record.nightMinutes != null && record.nightMinutes > 0 && (
            <div className="text-purple-600">深夜: {formatMinutes(record.nightMinutes)}</div>
          )}
        </div>
      )}
    </div>
  );
}
