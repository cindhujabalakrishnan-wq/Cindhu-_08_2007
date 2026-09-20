import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import policyService from '../services/policyService.js';
import renewalService from '../services/renewalService.js';
import { getErrorMessage } from '../services/api.js';
import Loader from '../components/Loader.jsx';
import ErrorAlert from '../components/ErrorAlert.jsx';
import EmptyState from '../components/EmptyState.jsx';
import StatusBadge from '../components/StatusBadge.jsx';

function formatCurrency(value) {
  const n = Number(value);
  if (!Number.isFinite(n)) return '—';
  return n.toLocaleString(undefined, { style: 'currency', currency: 'USD' });
}

function formatDate(value) {
  if (!value) return '—';
  const d = new Date(value);
  return Number.isNaN(d.getTime()) ? String(value) : d.toLocaleDateString();
}

const CARDS = [
  { key: 'totalPolicies', label: 'Total Policies', icon: '📄', bg: '#e7f1ff' },
  { key: 'activePolicies', label: 'Active', icon: '✅', bg: '#e6f9ed' },
  { key: 'expiringSoon', label: 'Expiring Soon', icon: '⏳', bg: '#fff7e0' },
  { key: 'expiredPolicies', label: 'Expired', icon: '⛔', bg: '#fdecec' },
  { key: 'upcomingRenewals', label: 'Upcoming Renewals', icon: '🔁', bg: '#e8f6f8' },
  { key: 'totalPremium', label: 'Total Premium', icon: '💰', bg: '#f1eaff', money: true },
];

export default function Dashboard() {
  const [stats, setStats] = useState(null);
  const [renewals, setRenewals] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const [s, r] = await Promise.all([
        policyService.getStats(),
        renewalService.getUpcoming(30).catch(() => ({ items: [] })),
      ]);
      setStats(s || {});
      setRenewals(r.items || []);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to load dashboard.'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  if (loading) return <Loader text="Loading dashboard…" />;
  if (error) return <ErrorAlert message={error} onRetry={load} />;

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
        <h2 className="mb-0">Dashboard</h2>
        <Link to="/policies/new" className="btn btn-primary">+ New Policy</Link>
      </div>

      <div className="row g-3 mb-4">
        {CARDS.map((c) => (
          <div key={c.key} className="col-6 col-md-4 col-xl-2">
            <div className="card stat-card h-100">
              <div className="card-body d-flex gap-2 align-items-center">
                <span className="stat-icon" style={{ background: c.bg }}>{c.icon}</span>
                <div>
                  <div className="fw-bold fs-5">
                    {c.money ? formatCurrency(stats?.[c.key]) : (stats?.[c.key] ?? 0)}
                  </div>
                  <div className="text-muted small">{c.label}</div>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="card card-soft">
        <div className="card-body">
          <div className="d-flex justify-content-between align-items-center mb-3">
            <h5 className="mb-0">Upcoming Renewals (30 days)</h5>
            <Link to="/renewals" className="btn btn-sm btn-outline-primary">View all</Link>
          </div>
          {renewals.length === 0 ? (
            <EmptyState icon="🎉" title="No upcoming renewals" hint="All policies are up to date for the next 30 days." />
          ) : (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead>
                  <tr>
                    <th>Policy</th>
                    <th>Holder</th>
                    <th>Expiry</th>
                    <th>Premium</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {renewals.slice(0, 8).map((r) => (
                    <tr key={r.id || r.policyId || r.policyNumber}>
                      <td>
                        <Link to={r.policyId ? `/policies/${r.policyId}` : '/renewals'}>
                          {r.policyNumber || r.policy?.policyNumber || `#${r.policyId || ''}`}
                        </Link>
                      </td>
                      <td>{r.holderName || r.policy?.holderName || r.customerName || '—'}</td>
                      <td>{formatDate(r.expiryDate || r.endDate || r.policy?.expiryDate)}</td>
                      <td>{formatCurrency(r.premiumAmount ?? r.policy?.premiumAmount)}</td>
                      <td><StatusBadge status={r.status || 'UPCOMING'} /></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
