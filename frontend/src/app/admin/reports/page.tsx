'use client';

import { useAuth } from '@/hooks/useAuth';
import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import { withBasePath } from '@/lib/api-client';
import { ReportFilter } from '@/components/admin/ReportFilter';
import { MonthlyReportTable } from '@/components/admin/MonthlyReportTable';
import { useMonthlyReport, generateMonthlyReport } from '@/hooks/useMonthlyReport';

function getCurrentYearMonth(): string {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  return `${year}-${month}`;
}

export default function AdminReportsPage() {
  const { user, isLoading } = useAuth();
  const router = useRouter();
  const [yearMonth, setYearMonth] = useState(getCurrentYearMonth());
  const [departmentId, setDepartmentId] = useState<number | undefined>(undefined);
  const [page, setPage] = useState(0);
  const [generating, setGenerating] = useState(false);

  const { data, mutate } = useMonthlyReport(yearMonth, departmentId, page);

  useEffect(() => {
    if (!isLoading && !user) {
      router.push('/login');
    }
    if (!isLoading && user && user.role !== 'ADMIN') {
      router.push('/');
    }
  }, [user, isLoading, router]);

  const handleGenerate = async () => {
    setGenerating(true);
    try {
      await generateMonthlyReport(yearMonth);
      await mutate();
    } catch {
      alert('集計生成に失敗しました');
    } finally {
      setGenerating(false);
    }
  };

  if (isLoading || !user || user.role !== 'ADMIN') return null;

  return (
    <div className="min-h-screen">
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-xl font-bold text-gray-900">月次勤怠レポート</h1>
          <div className="flex items-center gap-4">
            <a href={withBasePath('/')} className="text-sm text-blue-600 hover:underline">
              ダッシュボード
            </a>
            <span className="text-sm text-gray-600">
              {user.lastName} {user.firstName}
            </span>
          </div>
        </div>
      </header>
      <main className="max-w-7xl mx-auto px-4 py-8">
        <div className="flex justify-between items-start mb-6">
          <ReportFilter
            yearMonth={yearMonth}
            departmentId={departmentId}
            onYearMonthChange={(ym) => { setYearMonth(ym); setPage(0); }}
            onDepartmentChange={(id) => { setDepartmentId(id); setPage(0); }}
          />
          <button
            onClick={handleGenerate}
            disabled={generating}
            className="px-4 py-2 bg-blue-600 text-white text-sm rounded-md hover:bg-blue-700 disabled:opacity-50"
          >
            {generating ? '生成中...' : '集計生成'}
          </button>
        </div>
        <MonthlyReportTable
          data={data?.content ?? []}
          totalPages={data?.totalPages ?? 0}
          currentPage={page}
          onPageChange={setPage}
        />
      </main>
    </div>
  );
}
