import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MonthlyReportTable } from '@/components/admin/MonthlyReportTable';
import type { MonthlySummaryResponse } from '@/lib/types';

describe('MonthlyReportTable', () => {
  const mockData: MonthlySummaryResponse[] = [
    {
      employeeId: 1,
      employeeName: '田中 太郎',
      departmentName: '開発部',
      yearMonth: '2026-07',
      totalWorkMinutes: 9600,
      overtimeMinutes: 300,
      nightMinutes: 60,
      holidayWorkMinutes: 480,
      paidLeaveDays: 1.5,
      workingDays: 20,
    },
  ];

  it('テーブルヘッダーが正しく表示される', () => {
    render(
      <MonthlyReportTable data={mockData} totalPages={1} currentPage={0} onPageChange={vi.fn()} />
    );

    expect(screen.getByText('社員名')).toBeInTheDocument();
    expect(screen.getByText('部署')).toBeInTheDocument();
    expect(screen.getByText('総労働時間')).toBeInTheDocument();
    expect(screen.getByText('残業時間')).toBeInTheDocument();
    expect(screen.getByText('有給取得')).toBeInTheDocument();
    expect(screen.getByText('出勤日数')).toBeInTheDocument();
  });

  it('社員データが正しく表示される', () => {
    render(
      <MonthlyReportTable data={mockData} totalPages={1} currentPage={0} onPageChange={vi.fn()} />
    );

    expect(screen.getByText('田中 太郎')).toBeInTheDocument();
    expect(screen.getByText('開発部')).toBeInTheDocument();
    expect(screen.getByText('160:00')).toBeInTheDocument();
    expect(screen.getByText('5:00')).toBeInTheDocument();
    expect(screen.getByText('1.5日')).toBeInTheDocument();
    expect(screen.getByText('20日')).toBeInTheDocument();
  });

  it('データが空の場合はメッセージを表示する', () => {
    render(
      <MonthlyReportTable data={[]} totalPages={0} currentPage={0} onPageChange={vi.fn()} />
    );

    expect(screen.getByText(/データがありません/)).toBeInTheDocument();
  });

  it('複数ページある場合はページネーションを表示する', () => {
    render(
      <MonthlyReportTable data={mockData} totalPages={3} currentPage={1} onPageChange={vi.fn()} />
    );

    expect(screen.getByText('前へ')).toBeInTheDocument();
    expect(screen.getByText('次へ')).toBeInTheDocument();
    expect(screen.getByText('2 / 3')).toBeInTheDocument();
  });
});
