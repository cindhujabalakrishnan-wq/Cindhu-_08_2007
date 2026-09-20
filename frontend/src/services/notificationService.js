import api, { normalizeList } from './api.js';

const notificationService = {
  list: async (params = {}) => {
    const { data } = await api.get('/notifications', { params });
    return normalizeList(data, params.page || 0);
  },
  getUnread: async () => {
    const { data } = await api.get('/notifications/unread');
    return normalizeList(data);
  },
  getUnreadCount: async () => {
    const res = await notificationService.getUnread();
    return (res.items || []).length;
  },
  markRead: async (id) => {
    const { data } = await api.put(`/notifications/${id}/read`);
    return data;
  },
  markAllRead: async () => {
    const { data } = await api.put('/notifications/read-all');
    return data;
  },
};

export default notificationService;
