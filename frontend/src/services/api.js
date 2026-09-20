import axios from 'axios';

/**
 * Expected backend contract (Spring Boot):
 *   baseURL = {VITE_API_BASE_URL}/api/v1   (e.g. http://localhost:8080/api/v1)
 *   Auth:        POST /auth/login, POST /auth/register, GET /auth/me
 *   Policies:    GET|POST /policies, GET /policies/dashboard-summary,
 *                GET|PUT|DELETE /policies/:id
 *   Renewals:    GET /renewals, GET /renewals/upcoming, GET /renewals/expiring?days=,
 *                GET /renewals/expired, POST /renewals/:policyId, PUT /renewals/:id/complete
 *   Payments:    GET|POST /policies/:policyId/payments, GET /policies/:policyId/payments/summary
 *   Documents:   GET|POST /policies/:policyId/documents,
 *                GET /documents/:id/download (blob), DELETE /documents/:id
 *   Notify:      GET /notifications, GET /notifications/unread,
 *                PUT /notifications/:id/read, PUT /notifications/read-all
 *   Users:       GET|PUT /users/me, GET|PUT /profile
 *   Companies:   GET|POST /insurance-companies, PUT|DELETE /insurance-companies/:id
 *   PolicyTypes: GET|POST /policy-types, PUT|DELETE /policy-types/:id
 *   Admin:       GET /admin/dashboard, GET /admin/users,
 *                PUT /admin/users/:id/role, PUT /admin/users/:id/status,
 *                DELETE /admin/users/:id, GET /admin/policies,
 *                GET /admin/renewals, GET /admin/audit-logs
 */

function resolveApiBase() {
  const raw = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/+$/, '');
  if (raw.endsWith('/api/v1')) return raw;
  if (raw.endsWith('/api')) return `${raw}/v1`;
  return `${raw}/api/v1`;
}

const api = axios.create({
  baseURL: resolveApiBase(),
  timeout: 20000,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status;
    if (status === 401 && !window.location.pathname.startsWith('/login')) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  },
);

/** Normalize any axios/backend error into a human-readable message. */
export function getErrorMessage(error, fallback = 'Something went wrong. Please try again.') {
  const data = error?.response?.data;
  if (!data) return error?.message || fallback;
  if (typeof data === 'string') return data;
  return (
    data.message ||
    data.error ||
    (Array.isArray(data.errors) ? data.errors.join(', ') : null) ||
    (data.fieldErrors
      ? Object.entries(data.fieldErrors)
          .map(([k, v]) => `${k}: ${Array.isArray(v) ? v.join(', ') : v}`)
          .join('; ')
      : null) ||
    fallback
  );
}

/**
 * Normalize list responses: supports plain arrays, Spring Page
 * ({content, totalElements, totalPages, number}), and {data, total} shapes.
 */
export function normalizeList(data, page = 0) {
  if (Array.isArray(data)) {
    return { items: data, totalElements: data.length, totalPages: 1, page };
  }
  if (data && Array.isArray(data.content)) {
    return {
      items: data.content,
      totalElements: data.totalElements ?? data.content.length,
      totalPages: data.totalPages ?? 1,
      page: data.number ?? page,
    };
  }
  if (data && Array.isArray(data.items)) {
    return {
      items: data.items,
      totalElements: data.total ?? data.items.length,
      totalPages: data.totalPages ?? 1,
      page: data.page ?? page,
    };
  }
  if (data && Array.isArray(data.data)) {
    return { items: data.data, totalElements: data.total ?? data.data.length, totalPages: 1, page };
  }
  return { items: [], totalElements: 0, totalPages: 1, page };
}

export default api;
