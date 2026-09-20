import { useCallback, useEffect, useState } from 'react';
import adminService from '../../services/adminService.js';
import { getErrorMessage } from '../../services/api.js';
import Loader from '../../components/Loader.jsx';
import ErrorAlert from '../../components/ErrorAlert.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';
import Pagination from '../../components/Pagination.jsx';

function shortRole(role) {
  if (!role) return 'CUSTOMER';
  const r = String(role).toUpperCase();
  if (r === 'ROLE_ADMIN' || r === 'ADMIN') return 'ADMIN';
  return 'CUSTOMER';
}

function fullName(u) {
  const name = [u.firstName, u.lastName].filter(Boolean).join(' ').trim();
  return name || u.name || '—';
}

function enabledOf(u) {
  if (typeof u.enabled === 'boolean') return u.enabled;
  return (u.status || 'ENABLED') === 'ENABLED';
}

export default function AdminUsers() {
  const [items, setItems] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const res = await adminService.getUsers({ search: search || undefined, page, size: 10 });
      setItems(res.items);
      setTotalPages(res.totalPages);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to load users.'));
    } finally {
      setLoading(false);
    }
  }, [search, page]);

  useEffect(() => {
    const t = setTimeout(load, 250);
    return () => clearTimeout(t);
  }, [load]);

  const changeRole = async (u, role) => {
    setNotice('');
    try {
      await adminService.updateUserRole(u.id, role);
      setItems((l) => l.map((x) => (x.id === u.id ? { ...x, role: role === 'ADMIN' ? 'ROLE_ADMIN' : 'ROLE_CUSTOMER' } : x)));
      setNotice(`Role updated for ${u.email}.`);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to update role.'));
    }
  };

  const changeStatus = async (u) => {
    setNotice('');
    try {
      const next = !enabledOf(u);
      await adminService.updateUserStatus(u.id, next);
      setItems((l) => l.map((x) => (x.id === u.id ? { ...x, enabled: next } : x)));
      setNotice(`Status updated for ${u.email}.`);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to update status.'));
    }
  };

  const removeUser = async (u) => {
    if (!window.confirm(`Delete user ${u.email}?`)) return;
    try {
      await adminService.deleteUser(u.id);
      load();
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to delete user.'));
    }
  };

  return (
    <div>
      <h2 className="mb-3">Users</h2>
      {notice && <div className="alert alert-success">{notice}</div>}
      <div className="card card-soft mb-3">
        <div className="card-body">
          <input className="form-control" placeholder="Search name or email…" value={search} onChange={(e) => { setSearch(e.target.value); setPage(0); }} />
        </div>
      </div>
      {loading && <Loader text="Loading users…" />}
      {!loading && error && <ErrorAlert message={error} onRetry={load} />}
      {!loading && !error && items.length === 0 && (
        <div className="card card-soft"><div className="card-body">
          <EmptyState icon="👥" title="No users" hint="No users match this search." />
        </div></div>
      )}
      {!loading && !error && items.length > 0 && (
        <div className="card card-soft">
          <div className="card-body p-0">
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr><th>Name</th><th>Email</th><th>Role</th><th>Status</th><th className="text-end">Actions</th></tr>
                </thead>
                <tbody>
                  {items.map((u) => (
                    <tr key={u.id}>
                      <td>{fullName(u)}</td>
                      <td>{u.email}</td>
                      <td><StatusBadge status={shortRole(u.role)} /></td>
                      <td><StatusBadge status={enabledOf(u) ? 'ENABLED' : 'DISABLED'} /></td>
                      <td className="text-end text-nowrap">
                        <select className="form-select form-select-sm d-inline-block w-auto me-1" value={shortRole(u.role)} onChange={(e) => changeRole(u, e.target.value)}>
                          <option value="CUSTOMER">CUSTOMER</option>
                          <option value="ADMIN">ADMIN</option>
                        </select>
                        <button className="btn btn-sm btn-outline-secondary me-1" onClick={() => changeStatus(u)}>
                          {enabledOf(u) ? 'Disable' : 'Enable'}
                        </button>
                        <button className="btn btn-sm btn-outline-danger" onClick={() => removeUser(u)}>Delete</button>
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
