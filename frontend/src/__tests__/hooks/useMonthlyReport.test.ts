import { describe, it, expect, vi, beforeEach } from 'vitest';

vi.mock('@/lib/api-client', () => ({
  apiClient: vi.fn(),
  withBasePath: (path: string) => path,
}));

vi.mock('swr', () => ({
  default: vi.fn((key: string | null) => {
    if (!key) return { data: undefined, error: undefined, isLoading: false, mutate: vi.fn() };
    return { data: undefined, error: undefined, isLoading: true, mutate: vi.fn() };
  }),
}));

import { apiClient } from '@/lib/api-client';
import { generateMonthlyReport } from '@/hooks/useMonthlyReport';

describe('useMonthlyReport', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('generateMonthlyReport', () => {
    it('POST リクエストを送信する', async () => {
      const mockApiClient = vi.mocked(apiClient);
      mockApiClient.mockResolvedValue(undefined);

      await generateMonthlyReport('2026-07');

      expect(mockApiClient).toHaveBeenCalledWith(
        '/reports/monthly/generate?yearMonth=2026-07',
        { method: 'POST' }
      );
    });
  });
});
