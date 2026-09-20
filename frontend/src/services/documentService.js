import api, { normalizeList } from './api.js';

const documentService = {
  getByPolicy: async (policyId) => {
    const { data } = await api.get(`/policies/${policyId}/documents`);
    return normalizeList(data);
  },
  upload: async (policyId, file, meta = {}) => {
    const form = new FormData();
    form.append('file', file);
    form.append('documentType', meta.documentType || 'OTHER');
    const { data } = await api.post(`/policies/${policyId}/documents`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return data?.data ?? data?.document ?? data;
  },
  extractPreview: async (policyId, file) => {
    const form = new FormData();
    form.append('file', file);
    const { data } = await api.post(`/policies/${policyId}/documents/extract-preview`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return data?.data ?? data;
  },
  download: async (id, filename = 'document') => {
    const response = await api.get(`/documents/${id}/download`, { responseType: 'blob' });
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', filename);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },
  remove: async (id) => {
    const { data } = await api.delete(`/documents/${id}`);
    return data;
  },
};

export default documentService;
