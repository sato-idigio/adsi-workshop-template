import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { ReportFilter } from '@/components/admin/ReportFilter';

const mockApiClient = vi.fn();
vi.mock('@/lib/api-client', () => ({
  apiClient: (...args: unknown[]) => mockApiClient(...args),
  withBasePath: (path: string) => path,
}));

describe('ReportFilter', () => {
  const defaultProps = {
    yearMonth: '2026-07',
    departmentId: undefined as number | undefined,
    onYearMonthChange: vi.fn(),
    onDepartmentChange: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
    mockApiClient.mockResolvedValue([
      { id: 1, name: '開発部' },
      { id: 2, name: '営業部' },
    ]);
  });

  it('部署一覧を取得して表示する', async () => {
    render(<ReportFilter {...defaultProps} />);

    await waitFor(() => {
      expect(screen.getByText('開発部')).toBeInTheDocument();
      expect(screen.getByText('営業部')).toBeInTheDocument();
    });

    expect(mockApiClient).toHaveBeenCalledWith('/departments');
  });

  it('yearMonth の変更で onYearMonthChange が呼ばれる', () => {
    render(<ReportFilter {...defaultProps} />);

    const input = screen.getByLabelText('対象年月');
    fireEvent.change(input, { target: { value: '2026-08' } });

    expect(defaultProps.onYearMonthChange).toHaveBeenCalledWith('2026-08');
  });

  it('部署選択の変更で onDepartmentChange が呼ばれる', async () => {
    render(<ReportFilter {...defaultProps} />);

    await waitFor(() => {
      expect(screen.getByText('開発部')).toBeInTheDocument();
    });

    const select = screen.getByLabelText('部署');
    fireEvent.change(select, { target: { value: '1' } });

    expect(defaultProps.onDepartmentChange).toHaveBeenCalledWith(1);
  });

  it('全部署を選択すると undefined が渡される', async () => {
    render(<ReportFilter {...defaultProps} departmentId={1} />);

    await waitFor(() => {
      expect(screen.getByText('開発部')).toBeInTheDocument();
    });

    const select = screen.getByLabelText('部署');
    fireEvent.change(select, { target: { value: '' } });

    expect(defaultProps.onDepartmentChange).toHaveBeenCalledWith(undefined);
  });

  it('部署取得失敗時にエラーメッセージを表示する', async () => {
    mockApiClient.mockRejectedValue(new Error('Network error'));

    render(<ReportFilter {...defaultProps} />);

    await waitFor(() => {
      expect(screen.getByText('部署一覧の取得に失敗しました')).toBeInTheDocument();
    });
  });
});
