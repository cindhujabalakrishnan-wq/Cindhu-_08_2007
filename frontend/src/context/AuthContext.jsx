import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import authService from '../services/authService.js';

const AuthContext = createContext(null);

function readStoredUser() {
  try {
    const raw = localStorage.getItem('user');
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('token'));
  const [user, setUser] = useState(() => readStoredUser());
  const [loading, setLoading] = useState(true);

  const persist = (nextToken, nextUser) => {
    setToken(nextToken);
    setUser(nextUser);
    if (nextToken) localStorage.setItem('token', nextToken);
    else localStorage.removeItem('token');
    if (nextUser) localStorage.setItem('user', JSON.stringify(nextUser));
    else localStorage.removeItem('user');
  };

  const refreshMe = useCallback(async () => {
    const stored = localStorage.getItem('token');
    if (!stored) {
      setLoading(false);
      return null;
    }
    try {
      const me = await authService.me();
      persist(localStorage.getItem('token'), me);
      return me;
    } catch {
      persist(null, null);
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refreshMe();
  }, [refreshMe]);

  const login = useCallback(async (credentials) => {
    const data = await authService.login(credentials);
    persist(data.token || data.accessToken, data.user || data);
    return data;
  }, []);

  const register = useCallback(async (payload) => {
    const data = await authService.register(payload);
    if (data?.token || data?.accessToken) {
      persist(data.token || data.accessToken, data.user || null);
    }
    return data;
  }, []);

  const logout = useCallback(() => {
    persist(null, null);
  }, []);

  const value = useMemo(
    () => ({
      token,
      user,
      role: user?.role || null,
      isAuthenticated: Boolean(token),
      isAdmin: user?.role === 'ADMIN',
      loading,
      login,
      register,
      logout,
      refreshMe,
    }),
    [token, user, loading, login, register, logout, refreshMe],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
