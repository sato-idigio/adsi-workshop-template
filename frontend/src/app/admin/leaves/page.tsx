'use client';

import { useAuth } from '@/hooks/useAuth';
import { useRouter } from 'next/navigation';
import { useEffect } from 'react';
import { withBasePath } from '@/lib/api-client';
import { LeaveApprovalList } from '@/components/leave/LeaveApprovalList';

export default function AdminLeavesPage() {
  const { user, isLoading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!isLoading && !user) {
      router.push('/login');
    }
    if (!isLoading && user && user.role !== 'ADMIN') {
      router.push('/');
    }
  }, [user, isLoading, router]);

  if (isLoading || !user || user.role !== 'ADMIN') return null;

  return (
    <div className="min-h-screen">
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-xl font-bold text-gray-900">休暇承認管理</h1>
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
        <h2 className="text-lg font-semibold mb-4">承認待ち休暇申請</h2>
        <LeaveApprovalList />
      </main>
    </div>
  );
}
