'use client';

import { useAuth } from '@/hooks/useAuth';
import { useRouter } from 'next/navigation';
import { useEffect } from 'react';
import { withBasePath } from '@/lib/api-client';

export default function DashboardPage() {
  const { user, isLoading, logout } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!isLoading && !user) {
      router.push('/login');
    }
  }, [user, isLoading, router]);

  if (isLoading) {
    return <div className="flex items-center justify-center min-h-screen">読み込み中...</div>;
  }

  if (!user) {
    return null;
  }

  return (
    <div className="min-h-screen">
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-xl font-bold text-gray-900">勤怠管理システム</h1>
          <div className="flex items-center gap-4">
            <span className="text-sm text-gray-600">
              {user.lastName} {user.firstName}
            </span>
            {user.role === 'ADMIN' && (
              <nav className="flex gap-2">
                <a href={withBasePath('/admin/employees')} className="text-sm text-blue-600 hover:underline">
                  社員管理
                </a>
                <a href={withBasePath('/admin/departments')} className="text-sm text-blue-600 hover:underline">
                  部署管理
                </a>
              </nav>
            )}
            <button
              onClick={logout}
              className="text-sm text-gray-500 hover:text-gray-700"
            >
              ログアウト
            </button>
          </div>
        </div>
      </header>
      <main className="max-w-7xl mx-auto px-4 py-8">
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold mb-4">ダッシュボード</h2>
          <p className="text-gray-600">
            ようこそ、{user.lastName} {user.firstName} さん
          </p>
          <p className="text-sm text-gray-500 mt-2">
            部署: {user.department.name} / 役職: {user.position || '-'}
          </p>
        </div>
      </main>
    </div>
  );
}
