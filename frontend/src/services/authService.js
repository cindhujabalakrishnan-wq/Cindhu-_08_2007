import api from './api.js';

const authService = {
  login: async (credentials) => {
    const { data } = await api.post('/auth/login', credentials);
    return data;
  },
  register: async (payload) => {
    const { data } = await api.post('/auth/register', payload);
    return data;
  },
  me: async () => {
    const { data } = await api.get('/auth/me');
    return data?.user || data;
  },
};

export default authService;
