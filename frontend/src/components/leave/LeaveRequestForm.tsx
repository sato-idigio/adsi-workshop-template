'use client';

import { useState } from 'react';
import { createLeaveRequest } from '@/hooks/useLeave';
import { LEAVE_TYPE_LABELS } from '@/lib/constants';
import type { LeaveType } from '@/lib/types';

interface LeaveRequestFormProps {
  onSuccess: () => void;
}

export function LeaveRequestForm({ onSuccess }: LeaveRequestFormProps) {
  const [leaveType, setLeaveType] = useState<LeaveType>('PAID');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [reason, setReason] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const isHalf = leaveType === 'HALF_AM' || leaveType === 'HALF_PM';

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError('');

    const effectiveEndDate = isHalf ? startDate : endDate;
    if (!isHalf && endDate < startDate) {
      setError('終了日は開始日以降の日付を指定してください');
      return;
    }

    setSubmitting(true);
    try {
      await createLeaveRequest({
        leaveType,
        startDate,
        endDate: effectiveEndDate,
        reason,
      });
      setStartDate('');
      setEndDate('');
      setReason('');
      onSuccess();
    } catch (err) {
      setError(err instanceof Error ? err.message : '申請に失敗しました');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow p-6 space-y-4">
      <h2 className="text-lg font-semibold">休暇申請</h2>

      {error && <div className="text-red-500 text-sm">{error}</div>}

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">休暇種別</label>
        <select
          value={leaveType}
          onChange={(e) => setLeaveType(e.target.value as LeaveType)}
          className="w-full border border-gray-300 rounded px-3 py-2"
        >
          {Object.entries(LEAVE_TYPE_LABELS).map(([value, label]) => (
            <option key={value} value={value}>{label}</option>
          ))}
        </select>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">開始日</label>
        <input
          type="date"
          value={startDate}
          onChange={(e) => setStartDate(e.target.value)}
          required
          className="w-full border border-gray-300 rounded px-3 py-2"
        />
      </div>

      {!isHalf && (
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">終了日</label>
          <input
            type="date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            required
            min={startDate || undefined}
            className="w-full border border-gray-300 rounded px-3 py-2"
          />
        </div>
      )}

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">理由</label>
        <textarea
          value={reason}
          onChange={(e) => setReason(e.target.value)}
          required
          maxLength={500}
          rows={3}
          className="w-full border border-gray-300 rounded px-3 py-2"
        />
      </div>

      <button
        type="submit"
        disabled={submitting}
        className="w-full bg-blue-600 text-white py-2 px-4 rounded hover:bg-blue-700 disabled:opacity-50"
      >
        {submitting ? '送信中...' : '申請する'}
      </button>
    </form>
  );
}
