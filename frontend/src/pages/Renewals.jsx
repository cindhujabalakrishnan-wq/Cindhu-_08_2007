import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import renewalService from '../services/renewalService.js';
import { getErrorMessage } from '../services/api.js';
import Loader from '../components/Loader.jsx';
import ErrorAlert from '../components/ErrorAlert.jsx';
import EmptyState from '../components/EmptyState.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import Pagination from '../components/Pagination.jsx';

const FILTERS = [
  { key: '7', label: 'Next 7 days', days: 7 },
  { key: '30', label: 'Next 30 days', days: 30 },
  { key: '60', label: 'Next 60 days', days: 60 },
  { key: 'expired', label: 'Expired', days: null },
];

function formatDate(v) {
  if (!v) return '—';
  const d = new Date(v);
  return Number.isNaN(d.getTime()) ? String(v) : d.toLocaleDateString();
}

export default function Renewals() {
  const [filter, setFilter] = useState('30');
  const [items, setItems] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      let res;
      if (filter === 'expired') res = await renewalService.getExpired({ page, size: 10 });
      else res = await renewalService.getUpcoming(Number(filter), { page, size: 10 });
      setItems(res.items);
      setTotalPages(res.totalPages);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to load renewals.'));
    } finally {
      setLoading(false);
    }
  }, [filter, page]);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <div>
      <h2 className="mb-3">Renewals</h2>
      <div className="btn-group mb-3 flex-wrap" role="group" aria-label="Renewal filters">
        {FILTERS.map((f) => (
          <button
            key={f.key}
            className={`btn ${filter === f.key ? 'btn-primary' : 'btn-outline-primary'}`}
            onClick={() => { setFilter(f.key); setPage(0); }}
          >
            {f.label}
          </button>
        ))}
      </div>

      {loading && <Loader text="Loading renewals…" />}
      {!loading && error && <ErrorAlert message={error} onRetry={load} />}
      {!loading && !error && items.length === 0 && (
        <div className="card card-soft"><div className="card-body">
          <EmptyState icon="🎉" title="No renewals" hint="Nothing matches this filter." />
        </div></div>
      )}
      {!loading && !error && items.length > 0 && (
        <div className="card card-soft">
          <div className="card-body p-0">
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr><th>Policy</th><th>Holder</th><th>Expiry</th><th>Premium</th><th>Status</th><th className="text-end">Actions</th></tr>
                </thead>
                <tbody>
                  {items.map((r) => (
                    <tr key={r.id || r.policyId}>
                      <td>{r.policyNumber || r.policy?.policyNumber || `#${r.policyId || ''}`}</td>
                      <td>{r.holderName || r.policy?.holderName || '—'}</td>
                      <td>{formatDate(r.expiryDate || r.endDate || r.policy?.expiryDate)}</td>
                      <td>{Number(r.premiumAmount ?? r.policy?.premiumAmount) ? Number(r.premiumAmount ?? r.policy?.premiumAmount).toLocaleString() : '—'}</td>
                      <td><StatusBadge status={r.status} /></td>
                      <td className="text-end">
                        {r.policyId && <Link to={`/policies/${r.policyId}`} className="btn btn-sm btn-outline-primary">Open policy</Link>}
                      </td>
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
