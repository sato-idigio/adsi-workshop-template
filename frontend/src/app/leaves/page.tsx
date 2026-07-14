'use client';

import { useAuth } from '@/hooks/useAuth';
import { useRouter } from 'next/navigation';
import { useEffect } from 'react';
import { withBasePath } from '@/lib/api-client';
import { LeaveBalance } from '@/components/leave/LeaveBalance';
import { LeaveRequestForm } from '@/components/leave/LeaveRequestForm';
import { LeaveRequestList } from '@/components/leave/LeaveRequestList';
import { useSWRConfig } from 'swr';

export default function LeavesPage() {
  const { user, isLoading } = useAuth();
  const router = useRouter();
  const { mutate } = useSWRConfig();

  useEffect(() => {
    if (!isLoading && !user) {
      router.push('/login');
    }
  }, [user, isLoading, router]);

  if (isLoading || !user) return null;

  function handleSuccess() {
    mutate((key: string) => typeof key === 'string' && key.startsWith('/leaves'), undefined, { revalidate: true });
  }

  return (
    <div className="min-h-screen">
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-xl font-bold text-gray-900">休暇管理</h1>
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
      <main className="max-w-7xl mx-auto px-4 py-8 space-y-6">
        <LeaveBalance />
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <LeaveRequestForm onSuccess={handleSuccess} />
          <div className="space-y-4">
            <h2 className="text-lg font-semibold">申請履歴</h2>
            <LeaveRequestList />
          </div>
        </div>
      </main>
    </div>
  );
}
