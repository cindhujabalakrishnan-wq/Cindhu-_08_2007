import { useCallback, useEffect, useState } from 'react';
import adminService from '../../services/adminService.js';
import { getErrorMessage } from '../../services/api.js';
import Loader from '../../components/Loader.jsx';
import ErrorAlert from '../../components/ErrorAlert.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';
import Pagination from '../../components/Pagination.jsx';

function formatDate(v) {
  if (!v) return '—';
  const d = new Date(v);
  return Number.isNaN(d.getTime()) ? String(v) : d.toLocaleDateString();
}

export default function AdminRenewals() {
  const [items, setItems] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [status, setStatus] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const res = await adminService.getRenewals({ status: status || undefined, page, size: 10 });
      setItems(res.items);
      setTotalPages(res.totalPages);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to load renewals.'));
    } finally {
      setLoading(false);
    }
  }, [status, page]);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <div>
      <h2 className="mb-3">All Renewals</h2>
      <div className="card card-soft mb-3">
        <div className="card-body">
          <div className="col-md-4">
            <select className="form-select" value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); }}>
              <option value="">All statuses</option>
              <option value="UPCOMING">Upcoming</option>
              <option value="COMPLETED">Completed</option>
              <option value="OVERDUE">Overdue</option>
            </select>
          </div>
        </div>
      </div>
      {loading && <Loader text="Loading renewals…" />}
      {!loading && error && <ErrorAlert message={error} onRetry={load} />}
      {!loading && !error && items.length === 0 && (
        <div className="card card-soft"><div className="card-body">
          <EmptyState icon="🔁" title="No renewals" hint="No renewals match this filter." />
        </div></div>
      )}
      {!loading && !error && items.length > 0 && (
        <div className="card card-soft">
          <div className="card-body p-0">
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr><th>Policy</th><th>Holder</th><th>Owner</th><th>Expiry</th><th>Status</th></tr>
                </thead>
                <tbody>
                  {items.map((r) => (
                    <tr key={r.id || r.policyId}>
                      <td>{r.policyNumber || r.policy?.policyNumber || `#${r.policyId || ''}`}</td>
                      <td>{r.holderName || r.policy?.holderName || '—'}</td>
                      <td>{r.ownerEmail || r.policy?.ownerEmail || '—'}</td>
                      <td>{formatDate(r.expiryDate || r.policy?.expiryDate)}</td>
                      <td><StatusBadge status={r.status} /></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
          <div className="card-footer bg-white">
            <Pagination page={page} totalPages={totalPages} onChange={setPage} />
          </div>
        </div>
      )}
    </div>
  );
}
