'use client';

import { useState } from 'react';
import { useAuth } from '@/hooks/useAuth';
import { useMonthlyAttendance } from '@/hooks/useAttendance';
import { useRouter } from 'next/navigation';
import { useEffect } from 'react';
import { withBasePath } from '@/lib/api-client';
import { MonthlyTable } from '@/components/attendance/MonthlyTable';

function getCurrentYearMonth(): string {
  const now = new Date();
  return `${now.getFullYear()}-${(now.getMonth() + 1).toString().padStart(2, '0')}`;
}

export default function AttendancePage() {
  const { user, isLoading: authLoading } = useAuth();
  const router = useRouter();
  const [yearMonth, setYearMonth] = useState(getCurrentYearMonth);
  const { records, isLoading } = useMonthlyAttendance(yearMonth);

  useEffect(() => {
    if (!authLoading && !user) {
      router.push(withBasePath('/login'));
    }
  }, [user, authLoading, router]);

  if (authLoading || !user) {
    return <div className="flex items-center justify-center min-h-screen">読み込み中...</div>;
  }

  return (
    <div className="min-h-screen">
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-xl font-bold text-gray-900">月次勤怠一覧</h1>
          <a href={withBasePath('/')} className="text-sm text-blue-600 hover:underline">
            ダッシュボードに戻る
          </a>
        </div>
      </header>
      <main className="max-w-7xl mx-auto px-4 py-8">
        <div className="bg-white rounded-lg shadow p-6">
          <div className="mb-6">
            <label htmlFor="yearMonth" className="block text-sm font-medium text-gray-700 mb-1">
              対象月
            </label>
            <input
              type="month"
              id="yearMonth"
              value={yearMonth}
              onChange={(e) => setYearMonth(e.target.value)}
              className="border border-gray-300 rounded-md px-3 py-2 text-sm"
            />
          </div>
          <MonthlyTable records={records} isLoading={isLoading} />
        </div>
      </main>
    </div>
  );
}
