import api, { normalizeList } from './api.js';

const adminService = {
  getDashboardStats: async () => {
    const { data } = await api.get('/admin/dashboard');
    return data?.data ?? data?.stats ?? data;
  },
  getUsers: async (params = {}) => {
    const { data } = await api.get('/admin/users', { params });
    return normalizeList(data, params.page || 0);
  },
  updateUserRole: async (id, role) => {
    const { data } = await api.put(`/admin/users/${id}/role`, { role });
    return data?.data ?? data?.user ?? data;
  },
  updateUserStatus: async (id, enabled) => {
    const value = typeof enabled === 'boolean' ? enabled : enabled !== 'DISABLED';
    const { data } = await api.put(`/admin/users/${id}/status`, { enabled: value });
    return data?.data ?? data?.user ?? data;
  },
  deleteUser: async (id) => {
    const { data } = await api.delete(`/admin/users/${id}`);
    return data;
  },
  getPolicies: async (params = {}) => {
    const { data } = await api.get('/admin/policies', { params });
    return normalizeList(data, params.page || 0);
  },
  getRenewals: async (params = {}) => {
    const { data } = await api.get('/admin/renewals', { params });
    return normalizeList(data, params.page || 0);
  },
  getAuditLogs: async (params = {}) => {
    const { data } = await api.get('/admin/audit-logs', { params });
    return normalizeList(data, params.page || 0);
  },
  getProfile: async () => {
    const { data } = await api.get('/users/me');
    return data?.data ?? data?.user ?? data;
  },
  updateProfile: async (payload) => {
    const { data } = await api.put('/users/me', payload);
    return data?.data ?? data?.user ?? data;
  },
};

export default adminService;
