'use client';

import { createContext, useCallback, useEffect, useState } from 'react';
import type { ReactNode } from 'react';
import { apiClient, setAuthToken, getAuthToken } from '@/lib/api-client';
import type { EmployeeResponse, LoginRequest, LoginResponse } from '@/lib/types';

interface AuthContextType {
  user: EmployeeResponse | null;
  isLoading: boolean;
  login: (request: LoginRequest) => Promise<void>;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextType>({
  user: null,
  isLoading: true,
  login: async () => {},
  logout: () => {},
});

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<EmployeeResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const token = getAuthToken();
    if (token) {
      apiClient<EmployeeResponse>('/auth/me')
        .then(setUser)
        .catch(() => {
          setAuthToken(null);
          setUser(null);
        })
        .finally(() => setIsLoading(false));
    } else {
      setIsLoading(false);
    }
  }, []);

  const login = useCallback(async (request: LoginRequest) => {
    const response = await apiClient<LoginResponse>('/auth/login', {
      method: 'POST',
      body: request,
    });
    setAuthToken(response.token);
    setUser(response.employee);
  }, []);

  const logout = useCallback(() => {
    setAuthToken(null);
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, isLoading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}
