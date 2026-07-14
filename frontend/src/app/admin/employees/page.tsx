'use client';

import { useEffect, useState } from 'react';
import { useAuth } from '@/hooks/useAuth';
import { apiClient, withBasePath } from '@/lib/api-client';
import { useRouter } from 'next/navigation';
import type { EmployeeResponse, PagedResponse } from '@/lib/types';
import Button from '@/components/ui/Button';

export default function EmployeesPage() {
  const { user, isLoading: authLoading } = useAuth();
  const router = useRouter();
  const [employees, setEmployees] = useState<EmployeeResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (!authLoading && (!user || user.role !== 'ADMIN')) {
      router.push('/');
      return;
    }
    if (user) {
      loadEmployees();
    }
  }, [user, authLoading, router]);

  const loadEmployees = async () => {
    try {
      const data = await apiClient<PagedResponse<EmployeeResponse>>('/employees?size=50');
      setEmployees(data.content);
    } catch {
      // handled by apiClient
    } finally {
      setIsLoading(false);
    }
  };

  if (authLoading || isLoading) {
    return <div className="flex items-center justify-center min-h-screen">読み込み中...</div>;
  }

  return (
    <div className="min-h-screen">
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-xl font-bold">社員管理</h1>
          <a href={withBasePath('/')} className="text-sm text-blue-600 hover:underline">
            ダッシュボードへ戻る
          </a>
        </div>
      </header>
      <main className="max-w-7xl mx-auto px-4 py-8">
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-3 text-left">社員番号</th>
                <th className="px-4 py-3 text-left">氏名</th>
                <th className="px-4 py-3 text-left">メール</th>
                <th className="px-4 py-3 text-left">部署</th>
                <th className="px-4 py-3 text-left">役職</th>
                <th className="px-4 py-3 text-left">ロール</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {employees.map((emp) => (
                <tr key={emp.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3">{emp.employeeNumber}</td>
                  <td className="px-4 py-3">{emp.lastName} {emp.firstName}</td>
                  <td className="px-4 py-3">{emp.email}</td>
                  <td className="px-4 py-3">{emp.department.name}</td>
                  <td className="px-4 py-3">{emp.position || '-'}</td>
                  <td className="px-4 py-3">{emp.role}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {employees.length === 0 && (
            <p className="text-center py-8 text-gray-500">社員データがありません</p>
          )}
        </div>
      </main>
    </div>
  );
}
