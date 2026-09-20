import api, { normalizeList } from './api.js';

const companyService = {
  list: async (params = {}) => {
    const { data } = await api.get('/insurance-companies', { params });
    return normalizeList(data, params.page || 0);
  },
  getById: async (id) => {
    const { data } = await api.get(`/insurance-companies/${id}`);
    return data?.data ?? data?.company ?? data;
  },
  create: async (payload) => {
    const { data } = await api.post('/insurance-companies', payload);
    return data?.data ?? data?.company ?? data;
  },
  update: async (id, payload) => {
    const { data } = await api.put(`/insurance-companies/${id}`, payload);
    return data?.data ?? data?.company ?? data;
  },
  remove: async (id) => {
    const { data } = await api.delete(`/insurance-companies/${id}`);
    return data;
  },
};

export default companyService;
