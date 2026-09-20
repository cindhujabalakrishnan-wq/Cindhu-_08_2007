import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import adminService from '../../services/adminService.js';
import { getErrorMessage } from '../../services/api.js';
import Loader from '../../components/Loader.jsx';
import ErrorAlert from '../../components/ErrorAlert.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';

function formatDate(v) {
  if (!v) return '—';
  const d = new Date(v);
  return Number.isNaN(d.getTime()) ? String(v) : d.toLocaleDateString();
}

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      setStats(await adminService.getDashboardStats());
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to load admin stats.'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  if (loading) return <Loader text="Loading admin dashboard…" />;
  if (error) return <ErrorAlert message={error} onRetry={load} />;

  const cards = [
    { label: 'Total Users', value: stats?.totalUsers ?? 0, icon: '👥', bg: '#e7f1ff', to: '/admin/users' },
    { label: 'Total Policies', value: stats?.totalPolicies ?? 0, icon: '📄', bg: '#e6f9ed', to: '/admin/policies' },
    { label: 'Active Policies', value: stats?.activePolicies ?? 0, icon: '✅', bg: '#e6f9ed', to: '/admin/policies' },
    { label: 'Expiring Soon', value: stats?.expiringPolicies ?? stats?.expiringSoon ?? 0, icon: '⏳', bg: '#fff7e0', to: '/admin/renewals' },
    { label: 'Expired', value: stats?.expiredPolicies ?? 0, icon: '⛔', bg: '#fdecec', to: '/admin/renewals' },
    { label: 'Total Premium', value: Number(stats?.totalPremium) ? Number(stats.totalPremium).toLocaleString() : (stats?.totalPremium ?? 0), icon: '💰', bg: '#f1eaff', to: '/admin/policies' },
  ];

  const recentPolicies = stats?.recentPolicies || [];
  const recentUsers = stats?.recentUsers || [];
  const upcomingRenewals = stats?.upcomingRenewals || [];

  return (
    <div>
      <h2 className="mb-3">Admin Dashboard</h2>
      <div className="row g-3 mb-4">
        {cards.map((c) => (
          <div key={c.label} className="col-6 col-md-4 col-xl-2">
            <Link to={c.to} className="text-decoration-none text-dark">
              <div className="card stat-card h-100">
                <div className="card-body d-flex gap-2 align-items-center">
                  <span className="stat-icon" style={{ background: c.bg }}>{c.icon}</span>
                  <div>
                    <div className="fw-bold fs-5">{c.value}</div>
                    <div className="text-muted small">{c.label}</div>
                  </div>
                </div>
              </div>
            </Link>
          </div>
        ))}
      </div>

      <div className="row g-3">
        <div className="col-lg-4">
          <div className="card card-soft h-100">
            <div className="card-body">
              <h5>Recent Policies</h5>
              {recentPolicies.length === 0 ? <EmptyState icon="📄" title="No recent policies" hint="New policies will appear here." /> : (
                <ul className="list-group list-group-flush">
                  {recentPolicies.slice(0, 5).map((p) => (
                    <li key={p.id} className="list-group-item px-0 d-flex justify-content-between">
                      <span>{p.policyNumber || `#${p.id}`} — {p.holderName || ''}</span>
                      <StatusBadge status={p.status} />
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>
        </div>
        <div className="col-lg-4">
          <div className="card card-soft h-100">
            <div className="card-body">
              <h5>Recent Users</h5>
              {recentUsers.length === 0 ? <EmptyState icon="👥" title="No users yet" hint="Registered users will appear here." /> : (
                <ul className="list-group list-group-flush">
                  {recentUsers.slice(0, 5).map((u) => (
                    <li key={u.id} className="list-group-item px-0 d-flex justify-content-between">
                      <span>{u.name || u.email}</span>
                      <StatusBadge status={u.role || u.status} />
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>
        </div>
        <div className="col-lg-4">
          <div className="card card-soft h-100">
            <div className="card-body">
              <h5>Upcoming Renewals</h5>
              {upcomingRenewals.length === 0 ? <EmptyState icon="🔁" title="None upcoming" hint="Renewals due soon will appear here." /> : (
                <ul className="list-group list-group-flush">
                  {upcomingRenewals.slice(0, 5).map((r) => (
                    <li key={r.id || r.policyId} className="list-group-item px-0 d-flex justify-content-between">
                      <span>{r.policyNumber || r.policy?.policyNumber || `#${r.policyId || ''}`}</span>
                      <span className="text-muted small">{formatDate(r.expiryDate || r.policy?.expiryDate)}</span>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
