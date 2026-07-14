'use client';

import { useState } from 'react';
import { createCorrection } from '@/hooks/useCorrection';
import type { CorrectionRequestCreate } from '@/lib/types';

interface CorrectionFormProps {
  onSuccess: () => void;
}

export function CorrectionForm({ onSuccess }: CorrectionFormProps) {
  const [attendanceRecordId, setAttendanceRecordId] = useState('');
  const [requestedClockIn, setRequestedClockIn] = useState('');
  const [requestedClockOut, setRequestedClockOut] = useState('');
  const [reason, setReason] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const request: CorrectionRequestCreate = {
        attendanceRecordId: Number(attendanceRecordId),
        requestedClockIn: requestedClockIn || null,
        requestedClockOut: requestedClockOut || null,
        reason,
      };
      await createCorrection(request);
      setAttendanceRecordId('');
      setRequestedClockIn('');
      setRequestedClockOut('');
      setReason('');
      onSuccess();
    } catch (e) {
      setError(e instanceof Error ? e.message : '申請に失敗しました');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label htmlFor="recordId" className="block text-sm font-medium text-gray-700">
          打刻レコードID
        </label>
        <input
          id="recordId"
          type="number"
          value={attendanceRecordId}
          onChange={(e) => setAttendanceRecordId(e.target.value)}
          required
          className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
        />
      </div>
      <div>
        <label htmlFor="clockIn" className="block text-sm font-medium text-gray-700">
          修正後の出勤時刻
        </label>
        <input
          id="clockIn"
          type="datetime-local"
          value={requestedClockIn}
          onChange={(e) => setRequestedClockIn(e.target.value)}
          className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
        />
      </div>
      <div>
        <label htmlFor="clockOut" className="block text-sm font-medium text-gray-700">
          修正後の退勤時刻
        </label>
        <input
          id="clockOut"
          type="datetime-local"
          value={requestedClockOut}
          onChange={(e) => setRequestedClockOut(e.target.value)}
          className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
        />
      </div>
      <div>
        <label htmlFor="reason" className="block text-sm font-medium text-gray-700">
          理由
        </label>
        <textarea
          id="reason"
          value={reason}
          onChange={(e) => setReason(e.target.value)}
          required
          maxLength={500}
          rows={3}
          className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
        />
      </div>
      {error && <p className="text-red-600 text-sm">{error}</p>}
      <button
        type="submit"
        disabled={loading}
        className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-gray-300 text-sm"
      >
        {loading ? '送信中...' : '修正申請'}
      </button>
    </form>
  );
}
