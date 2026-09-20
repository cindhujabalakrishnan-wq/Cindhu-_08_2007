import api, { normalizeList } from './api.js';

const policyTypeService = {
  list: async (params = {}) => {
    const { data } = await api.get('/policy-types', { params });
    return normalizeList(data, params.page || 0);
  },
  getById: async (id) => {
    const { data } = await api.get(`/policy-types/${id}`);
    return data?.policyType || data;
  },
  create: async (payload) => {
    const { data } = await api.post('/policy-types', payload);
    return data?.policyType || data;
  },
  update: async (id, payload) => {
    const { data } = await api.put(`/policy-types/${id}`, payload);
    return data?.policyType || data;
  },
  remove: async (id) => {
    const { data } = await api.delete(`/policy-types/${id}`);
    return data;
  },
};

export default policyTypeService;
