import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import authService from '../services/authService.js';

const AuthContext = createContext(null);

function normalizeRole(role) {
  if (!role) return null;
  return String(role).startsWith('ROLE_') ? String(role).slice(5) : String(role);
}

function normalizeUser(user) {
  if (!user) return null;
  return { ...user, role: normalizeRole(user.role) };
}

function readStoredToken() {
  const raw = localStorage.getItem('token');
  return raw && raw !== 'undefined' && raw !== 'null' ? raw : null;
}

function readStoredUser() {
  try {
    const raw = localStorage.getItem('user');
    return normalizeUser(raw ? JSON.parse(raw) : null);
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => readStoredToken());
  const [user, setUser] = useState(() => readStoredUser());
  const [loading, setLoading] = useState(true);

  const persist = (nextToken, nextUser) => {
    const cleanUser = normalizeUser(nextUser);
    setToken(nextToken || null);
    setUser(cleanUser);
    if (nextToken) localStorage.setItem('token', nextToken);
    else localStorage.removeItem('token');
    if (cleanUser) localStorage.setItem('user', JSON.stringify(cleanUser));
    else localStorage.removeItem('user');
  };

  const refreshMe = useCallback(async () => {
    const stored = readStoredToken();
    if (!stored) {
      persist(null, null);
      setLoading(false);
      return null;
    }
    try {
      const me = await authService.me();
      persist(stored, me);
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
    const token = data?.token || data?.accessToken;
    if (!token) {
      throw new Error(data?.message || 'Login failed. Check your credentials.');
    }
    persist(token, data.user || data);
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
