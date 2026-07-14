import { describe, it, expect, beforeEach } from 'vitest';
import { withBasePath, setAuthToken, getAuthToken } from '@/lib/api-client';

describe('api-client', () => {
  beforeEach(() => {
    setAuthToken(null);
    delete process.env.NEXT_PUBLIC_BASE_PATH;
  });

  describe('withBasePath', () => {
    it('NEXT_PUBLIC_BASE_PATHが未設定の場合はパスをそのまま返す', () => {
      expect(withBasePath('/api/health')).toBe('/api/health');
    });

    it('NEXT_PUBLIC_BASE_PATHが設定されている場合はプレフィックスを付ける', () => {
      process.env.NEXT_PUBLIC_BASE_PATH = '/codeeditor/default/absports/3000';
      expect(withBasePath('/api/health')).toBe('/codeeditor/default/absports/3000/api/health');
    });
  });

  describe('auth token', () => {
    it('トークンをセットして取得できる', () => {
      setAuthToken('test-token');
      expect(getAuthToken()).toBe('test-token');
    });

    it('nullをセットするとトークンがクリアされる', () => {
      setAuthToken('test-token');
      setAuthToken(null);
      expect(getAuthToken()).toBeNull();
    });
  });
});
