import api from './api.js';

/**
 * Backend wraps payloads in an ApiResponse envelope:
 *   { success, message, data: { token, tokenType, user } }
 * These helpers always return the inner payload so callers can rely on
 * `result.token` and `result.user` directly.
 */
function unwrap(body) {
  return body?.data ?? body;
}

const authService = {
  login: async (credentials) => {
    const { data } = await api.post('/auth/login', credentials);
    return unwrap(data);
  },
  register: async (payload) => {
    const { data } = await api.post('/auth/register', payload);
    return unwrap(data);
  },
  me: async () => {
    const { data } = await api.get('/auth/me');
    const inner = unwrap(data);
    return inner?.user ?? inner;
  },
};

export default authService;
