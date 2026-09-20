import api, { normalizeList } from './api.js';

const policyService = {
  list: async (params = {}) => {
    const { data } = await api.get('/policies', { params });
    return normalizeList(data, params.page || 0);
  },
  getById: async (id) => {
    const { data } = await api.get(`/policies/${id}`);
    return data?.data ?? data?.policy ?? data;
  },
  create: async (payload) => {
    const { data } = await api.post('/policies', payload);
    return data?.data ?? data?.policy ?? data;
  },
  update: async (id, payload) => {
    const { data } = await api.put(`/policies/${id}`, payload);
    return data?.data ?? data?.policy ?? data;
  },
  remove: async (id) => {
    const { data } = await api.delete(`/policies/${id}`);
    return data;
  },
  getStats: async () => {
    const { data } = await api.get('/policies/dashboard-summary');
    return data?.data ?? data?.stats ?? data;
  },
  getExpiring: async (days = 30) => {
    const { data } = await api.get('/renewals/expiring', { params: { days } });
    return normalizeList(data);
  },
};

export default policyService;
