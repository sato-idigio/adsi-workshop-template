'use client';

import { useState } from 'react';
import { clockIn, clockOut } from '@/hooks/useAttendance';
import type { AttendanceRecordResponse } from '@/lib/types';

interface ClockButtonProps {
  today: AttendanceRecordResponse | null;
  onSuccess: () => void;
}

export function ClockButton({ today, onSuccess }: ClockButtonProps) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const hasClockedIn = today?.clockIn != null;
  const hasClockedOut = today?.clockOut != null;

  const handleClockIn = async () => {
    setLoading(true);
    setError(null);
    try {
      await clockIn();
      onSuccess();
    } catch (e) {
      setError(e instanceof Error ? e.message : '打刻に失敗しました');
    } finally {
      setLoading(false);
    }
  };

  const handleClockOut = async () => {
    setLoading(true);
    setError(null);
    try {
      await clockOut();
      onSuccess();
    } catch (e) {
      setError(e instanceof Error ? e.message : '打刻に失敗しました');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-3">
      <div className="flex gap-3">
        <button
          onClick={handleClockIn}
          disabled={loading || hasClockedIn}
          className="px-6 py-3 bg-blue-600 text-white rounded-lg font-medium
                     hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed
                     transition-colors"
        >
          {hasClockedIn ? '出勤済み' : '出勤'}
        </button>
        <button
          onClick={handleClockOut}
          disabled={loading || !hasClockedIn || hasClockedOut}
          className="px-6 py-3 bg-green-600 text-white rounded-lg font-medium
                     hover:bg-green-700 disabled:bg-gray-300 disabled:cursor-not-allowed
                     transition-colors"
        >
          {hasClockedOut ? '退勤済み' : '退勤'}
        </button>
      </div>
      {error && <p className="text-red-600 text-sm">{error}</p>}
    </div>
  );
}
