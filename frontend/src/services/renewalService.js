import api, { normalizeList } from './api.js';

const renewalService = {
  list: async (params = {}) => {
    const { data } = await api.get('/renewals', { params });
    return normalizeList(data, params.page || 0);
  },
  getUpcoming: async (days = 30, params = {}) => {
    if (Number(days) <= 60) {
      const { data } = await api.get('/renewals/expiring', { params: { days, ...params } });
      return normalizeList(data, params.page || 0);
    }
    const { data } = await api.get('/renewals/upcoming', { params });
    return normalizeList(data, params.page || 0);
  },
  getExpired: async (params = {}) => {
    const { data } = await api.get('/renewals/expired', { params });
    return normalizeList(data, params.page || 0);
  },
  create: async (policyId, payload = {}) => {
    const { data } = await api.post(`/renewals/${policyId}`, payload);
    return data?.data ?? data?.renewal ?? data;
  },
  complete: async (id) => {
    const { data } = await api.put(`/renewals/${id}/complete`);
    return data?.data ?? data?.renewal ?? data;
  },
  getByPolicy: async (policyId) => {
    const { data } = await api.get('/renewals/upcoming');
    const all = normalizeList(data);
    return { ...all, items: (all.items || []).filter((r) => String(r.policyId) === String(policyId)) };
  },
};

export default renewalService;
