import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import policyService from '../services/policyService.js';
import companyService from '../services/companyService.js';
import { getErrorMessage } from '../services/api.js';
import Loader from '../components/Loader.jsx';
import ErrorAlert from '../components/ErrorAlert.jsx';
import EmptyState from '../components/EmptyState.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import Pagination from '../components/Pagination.jsx';

const PAGE_SIZE = 10;

function formatDate(v) {
  if (!v) return '—';
  const d = new Date(v);
  return Number.isNaN(d.getTime()) ? String(v) : d.toLocaleDateString();
}

export default function PolicyList() {
  const [items, setItems] = useState([]);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('');
  const [companyId, setCompanyId] = useState('');
  const [companies, setCompanies] = useState([]);
  const [sortBy, setSortBy] = useState('expiryDate');
  const [sortDir, setSortDir] = useState('asc');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    companyService
      .list()
      .then((res) => setCompanies(res.items || []))
      .catch(() => {});
  }, []);

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const res = await policyService.list({
        search: search || undefined,
        status: status || undefined,
        companyId: companyId || undefined,
        sortBy,
        sortDir,
        page,
        size: PAGE_SIZE,
      });
      setItems(res.items);
      setTotalPages(res.totalPages);
      setTotalElements(res.totalElements);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to load policies.'));
    } finally {
      setLoading(false);
    }
  }, [search, status, companyId, sortBy, sortDir, page]);

  useEffect(() => {
    const t = setTimeout(load, 250);
    return () => clearTimeout(t);
  }, [load]);

  const toggleSort = (field) => {
    if (sortBy === field) setSortDir((d) => (d === 'asc' ? 'desc' : 'asc'));
    else {
      setSortBy(field);
      setSortDir('asc');
    }
    setPage(0);
  };

  const sortIcon = (field) => (sortBy !== field ? '' : sortDir === 'asc' ? ' ▲' : ' ▼');

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
        <h2 className="mb-0">Policies {totalElements > 0 && <small className="text-muted">({totalElements})</small>}</h2>
        <Link to="/policies/new" className="btn btn-primary">+ New Policy</Link>
      </div>

      <div className="card card-soft mb-3">
        <div className="card-body">
          <div className="row g-2">
            <div className="col-md-4">
              <input className="form-control" placeholder="Search number, name…" value={search} onChange={(e) => { setSearch(e.target.value); setPage(0); }} />
            </div>
            <div className="col-md-3">
              <select className="form-select" value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); }}>
                <option value="">All statuses</option>
                <option value="ACTIVE">Active</option>
                <option value="PENDING">Pending</option>
                <option value="EXPIRED">Expired</option>
                <option value="RENEWED">Renewed</option>
                <option value="CANCELLED">Cancelled</option>
              </select>
            </div>
            <div className="col-md-3">
              <select className="form-select" value={companyId} onChange={(e) => { setCompanyId(e.target.value); setPage(0); }}>
                <option value="">All companies</option>
                {companies.map((c) => (
                  <option key={c.id} value={c.id}>{c.name}</option>
                ))}
              </select>
            </div>
            <div className="col-md-2">
              <button className="btn btn-outline-secondary w-100" onClick={() => { setSearch(''); setStatus(''); setCompanyId(''); setPage(0); }}>
                Clear
              </button>
            </div>
          </div>
        </div>
      </div>

      {loading && <Loader text="Loading policies…" />}
      {!loading && error && <ErrorAlert message={error} onRetry={load} />}
      {!loading && !error && items.length === 0 && (
        <div className="card card-soft">
          <div className="card-body">
            <EmptyState icon="📄" title="No policies found" hint="Try adjusting filters or create a new policy." action={<Link to="/policies/new" className="btn btn-primary">Create policy</Link>} />
          </div>
        </div>
      )}

      {!loading && !error && items.length > 0 && (
        <div className="card card-soft">
          <div className="card-body p-0">
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr>
                    <th><button className="btn btn-link p-0 text-decoration-none text-dark fw-bold" onClick={() => toggleSort('policyNumber')}>Policy #{sortIcon('policyNumber')}</button></th>
                    <th>Name</th>
                    <th>Category</th>
                    <th>Company</th>
                    <th><button className="btn btn-link p-0 text-decoration-none text-dark fw-bold" onClick={() => toggleSort('startDate')}>Start{sortIcon('startDate')}</button></th>
                    <th><button className="btn btn-link p-0 text-decoration-none text-dark fw-bold" onClick={() => toggleSort('expiryDate')}>Expiry{sortIcon('expiryDate')}</button></th>
                    <th><button className="btn btn-link p-0 text-decoration-none text-dark fw-bold" onClick={() => toggleSort('premiumAmount')}>Premium{sortIcon('premiumAmount')}</button></th>
                    <th>Status</th>
                    <th className="text-end">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {items.map((p) => (
                    <tr key={p.id}>
                      <td><Link to={`/policies/${p.id}`}>{p.policyNumber || `#${p.id}`}</Link></td>
                      <td>{p.holderName || p.name || '—'}</td>
                      <td>{p.category || p.policyType?.name || '—'}</td>
                      <td>{p.company?.name || p.companyName || '—'}</td>
                      <td>{formatDate(p.startDate)}</td>
                      <td>{formatDate(p.expiryDate || p.endDate)}</td>
                      <td>{Number(p.premiumAmount) ? Number(p.premiumAmount).toLocaleString() : '—'}</td>
                      <td><StatusBadge status={p.status} /></td>
                      <td className="text-end text-nowrap">
                        <Link to={`/policies/${p.id}`} className="btn btn-sm btn-outline-primary me-1">View</Link>
                        <Link to={`/policies/${p.id}/edit`} className="btn btn-sm btn-outline-secondary">Edit</Link>
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
