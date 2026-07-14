'use client';

import { useEffect, useState } from 'react';
import { apiClient } from '@/lib/api-client';
import type { DepartmentResponse } from '@/lib/types';

interface ReportFilterProps {
  yearMonth: string;
  departmentId: number | undefined;
  onYearMonthChange: (yearMonth: string) => void;
  onDepartmentChange: (departmentId: number | undefined) => void;
}

export function ReportFilter({ yearMonth, departmentId, onYearMonthChange, onDepartmentChange }: ReportFilterProps) {
  const [departments, setDepartments] = useState<DepartmentResponse[]>([]);
  const [fetchError, setFetchError] = useState<string | null>(null);

  useEffect(() => {
    apiClient<DepartmentResponse[]>('/departments')
      .then(setDepartments)
      .catch(() => {
        setFetchError('部署一覧の取得に失敗しました');
      });
  }, []);

  return (
    <div className="flex flex-wrap gap-4 items-end mb-6">
      <div>
        <label htmlFor="yearMonth" className="block text-sm font-medium text-gray-700 mb-1">
          対象年月
        </label>
        <input
          type="month"
          id="yearMonth"
          value={yearMonth}
          onChange={(e) => onYearMonthChange(e.target.value)}
          className="border border-gray-300 rounded-md px-3 py-2 text-sm"
        />
      </div>
      <div>
        <label htmlFor="department" className="block text-sm font-medium text-gray-700 mb-1">
          部署
        </label>
        {fetchError ? (
          <p className="text-sm text-red-600">{fetchError}</p>
        ) : (
          <select
            id="department"
            value={departmentId ?? ''}
            onChange={(e) => onDepartmentChange(e.target.value ? Number(e.target.value) : undefined)}
            className="border border-gray-300 rounded-md px-3 py-2 text-sm"
          >
            <option value="">全部署</option>
            {departments.map((dept) => (
              <option key={dept.id} value={dept.id}>
                {dept.name}
              </option>
            ))}
          </select>
        )}
      </div>
    </div>
  );
}
