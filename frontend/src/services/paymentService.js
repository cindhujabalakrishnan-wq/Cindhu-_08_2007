import api, { normalizeList } from './api.js';

const paymentService = {
  getByPolicy: async (policyId, params = {}) => {
    const { data } = await api.get(`/policies/${policyId}/payments`, { params });
    return normalizeList(data, params.page || 0);
  },
  getSummary: async (policyId) => {
    const { data } = await api.get(`/policies/${policyId}/payments/summary`);
    return data?.data ?? data;
  },
  create: async (policyId, payload) => {
    const body = {
      amount: payload.amount,
      paymentDate: payload.paymentDate || new Date().toISOString().slice(0, 10),
      dueDate: payload.dueDate || null,
      paymentMethod: payload.paymentMethod || payload.method || 'CARD',
      transactionReference: payload.transactionReference || null,
      status: payload.status || 'COMPLETED',
      notes: payload.notes || null,
    };
    const { data } = await api.post(`/policies/${policyId}/payments`, body);
    return data?.data ?? data?.payment ?? data;
  },
};

export default paymentService;
