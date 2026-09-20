import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
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

export default function AdminPolicies() {
  const [items, setItems] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const res = await adminService.getPolicies({ search: search || undefined, page, size: 10 });
      setItems(res.items);
      setTotalPages(res.totalPages);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to load policies.'));
    } finally {
      setLoading(false);
    }
  }, [search, page]);

  useEffect(() => {
    const t = setTimeout(load, 250);
    return () => clearTimeout(t);
  }, [load]);

  return (
    <div>
      <h2 className="mb-3">All Policies</h2>
      <div className="card card-soft mb-3">
        <div className="card-body">
          <input className="form-control" placeholder="Search policies…" value={search} onChange={(e) => { setSearch(e.target.value); setPage(0); }} />
        </div>
      </div>
      {loading && <Loader text="Loading policies…" />}
      {!loading && error && <ErrorAlert message={error} onRetry={load} />}
      {!loading && !error && items.length === 0 && (
        <div className="card card-soft"><div className="card-body">
          <EmptyState icon="📄" title="No policies" hint="No policies found." />
        </div></div>
      )}
      {!loading && !error && items.length > 0 && (
        <div className="card card-soft">
          <div className="card-body p-0">
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr><th>Policy #</th><th>Holder</th><th>Owner</th><th>Expiry</th><th>Premium</th><th>Status</th><th className="text-end">Actions</th></tr>
                </thead>
                <tbody>
                  {items.map((p) => (
                    <tr key={p.id}>
                      <td>{p.policyNumber || `#${p.id}`}</td>
                      <td>{p.holderName || '—'}</td>
                      <td>{p.ownerEmail || p.user?.email || '—'}</td>
                      <td>{formatDate(p.expiryDate || p.endDate)}</td>
                      <td>{Number(p.premiumAmount) ? Number(p.premiumAmount).toLocaleString() : '—'}</td>
                      <td><StatusBadge status={p.status} /></td>
                      <td className="text-end"><Link to={`/policies/${p.id}`} className="btn btn-sm btn-outline-primary">View</Link></td>
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
