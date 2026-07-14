'use client';

import { useEffect, useState } from 'react';
import { useAuth } from '@/hooks/useAuth';
import { apiClient, withBasePath } from '@/lib/api-client';
import { useRouter } from 'next/navigation';
import type { DepartmentTreeResponse } from '@/lib/types';

function DepartmentNode({ dept, depth = 0 }: { dept: DepartmentTreeResponse; depth?: number }) {
  return (
    <>
      <tr className="hover:bg-gray-50">
        <td className="px-4 py-3" style={{ paddingLeft: `${depth * 24 + 16}px` }}>
          {depth > 0 && <span className="text-gray-400 mr-2">└</span>}
          {dept.name}
        </td>
        <td className="px-4 py-3">レベル {dept.level}</td>
      </tr>
      {dept.children.map((child) => (
        <DepartmentNode key={child.id} dept={child} depth={depth + 1} />
      ))}
    </>
  );
}

export default function DepartmentsPage() {
  const { user, isLoading: authLoading } = useAuth();
  const router = useRouter();
  const [departments, setDepartments] = useState<DepartmentTreeResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (!authLoading && (!user || user.role !== 'ADMIN')) {
      router.push('/');
      return;
    }
    if (user) {
      loadDepartments();
    }
  }, [user, authLoading, router]);

  const loadDepartments = async () => {
    try {
      const data = await apiClient<DepartmentTreeResponse[]>('/departments');
      setDepartments(data);
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
          <h1 className="text-xl font-bold">部署管理</h1>
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
                <th className="px-4 py-3 text-left">部署名</th>
                <th className="px-4 py-3 text-left">階層</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {departments.map((dept) => (
                <DepartmentNode key={dept.id} dept={dept} />
              ))}
            </tbody>
          </table>
        </div>
      </main>
    </div>
  );
}
