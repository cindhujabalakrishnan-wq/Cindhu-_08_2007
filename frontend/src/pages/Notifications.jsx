import { useCallback, useEffect, useState } from 'react';
import notificationService from '../services/notificationService.js';
import { getErrorMessage } from '../services/api.js';
import Loader from '../components/Loader.jsx';
import ErrorAlert from '../components/ErrorAlert.jsx';
import EmptyState from '../components/EmptyState.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import Pagination from '../components/Pagination.jsx';

function formatDate(v) {
  if (!v) return '—';
  const d = new Date(v);
  return Number.isNaN(d.getTime()) ? String(v) : d.toLocaleString();
}

export default function Notifications() {
  const [items, setItems] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [showUnreadOnly, setShowUnreadOnly] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const res = await notificationService.list({ page, size: 10 });
      setItems(res.items);
      setTotalPages(res.totalPages);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to load notifications.'));
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    load();
  }, [load]);

  const visible = showUnreadOnly ? items.filter((n) => !n.read && !n.readAt) : items;

  const handleMarkRead = async (n) => {
    try {
      await notificationService.markRead(n.id);
      setItems((list) => list.map((x) => (x.id === n.id ? { ...x, read: true, readAt: new Date().toISOString() } : x)));
    } catch {
      setError('Failed to mark notification as read.');
    }
  };

  const handleMarkAll = async () => {
    try {
      await notificationService.markAllRead();
      setItems((list) => list.map((x) => ({ ...x, read: true, readAt: x.readAt || new Date().toISOString() })));
    } catch {
      setError('Failed to mark all as read.');
    }
  };

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
        <h2 className="mb-0">Notifications</h2>
        <div className="d-flex gap-2">
          <button className={`btn btn-sm ${showUnreadOnly ? 'btn-primary' : 'btn-outline-primary'}`} onClick={() => setShowUnreadOnly((v) => !v)}>
            {showUnreadOnly ? 'Showing unread' : 'Show unread only'}
          </button>
          <button className="btn btn-sm btn-outline-success" onClick={handleMarkAll}>Mark all read</button>
        </div>
      </div>

      {loading && <Loader text="Loading notifications…" />}
      {!loading && error && <ErrorAlert message={error} onRetry={load} />}
      {!loading && !error && visible.length === 0 && (
        <div className="card card-soft"><div className="card-body">
          <EmptyState icon="🔔" title="All caught up" hint="No notifications to show." />
        </div></div>
      )}
      {!loading && !error && visible.length > 0 && (
        <>
          <div className="list-group mb-3">
            {visible.map((n) => {
              const isRead = Boolean(n.read || n.readAt);
              return (
                <div key={n.id} className={`list-group-item ${isRead ? '' : 'list-group-item-primary'}`}>
                  <div className="d-flex justify-content-between align-items-start gap-2">
                    <div>
                      <div className="fw-semibold">{n.title || n.subject || 'Notification'}</div>
                      <div className="text-muted small">{n.message || n.body || ''}</div>
                      <div className="text-muted small mt-1">{formatDate(n.createdAt)}</div>
                    </div>
                    <div className="d-flex flex-column align-items-end gap-1">
                      <StatusBadge status={isRead ? 'READ' : 'UNREAD'} />
                      {!isRead && (
                        <button className="btn btn-sm btn-outline-primary" onClick={() => handleMarkRead(n)}>
                          Mark read
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
          <Pagination page={page} totalPages={totalPages} onChange={setPage} />
        </>
      )}
    </div>
  );
}
