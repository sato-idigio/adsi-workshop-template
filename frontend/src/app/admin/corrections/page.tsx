'use client';

import { useAuth } from '@/hooks/useAuth';
import { useCorrectionList } from '@/hooks/useCorrection';
import { useRouter } from 'next/navigation';
import { useEffect } from 'react';
import { withBasePath } from '@/lib/api-client';
import { CorrectionApprovalList } from '@/components/attendance/CorrectionApprovalList';

export default function AdminCorrectionsPage() {
  const { user, isLoading: authLoading } = useAuth();
  const router = useRouter();
  const { data, isLoading, mutate } = useCorrectionList('PENDING');

  useEffect(() => {
    if (!authLoading && !user) {
      router.push(withBasePath('/login'));
    }
    if (!authLoading && user && user.role !== 'ADMIN') {
      router.push(withBasePath('/'));
    }
  }, [user, authLoading, router]);

  if (authLoading || !user || user.role !== 'ADMIN') {
    return <div className="flex items-center justify-center min-h-screen">読み込み中...</div>;
  }

  return (
    <div className="min-h-screen">
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-xl font-bold text-gray-900">打刻修正承認</h1>
          <a href={withBasePath('/')} className="text-sm text-blue-600 hover:underline">
            ダッシュボードに戻る
          </a>
        </div>
      </header>
      <main className="max-w-7xl mx-auto px-4 py-8">
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold mb-4">承認待ち申請</h2>
          <CorrectionApprovalList
            corrections={data.content}
            isLoading={isLoading}
            onAction={() => mutate()}
          />
        </div>
      </main>
    </div>
  );
}
