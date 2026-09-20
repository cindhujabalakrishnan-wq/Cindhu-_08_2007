import { useCallback, useEffect, useState } from 'react';
import adminService from '../../services/adminService.js';
import { getErrorMessage } from '../../services/api.js';
import Loader from '../../components/Loader.jsx';
import ErrorAlert from '../../components/ErrorAlert.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';
import Pagination from '../../components/Pagination.jsx';

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
      setItems((l) => l.map((x) => (x.id === u.id ? { ...x, role } : x)));
      setNotice(`Role updated for ${u.email}.`);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to update role.'));
    }
  };

  const changeStatus = async (u, status) => {
    setNotice('');
    try {
      await adminService.updateUserStatus(u.id, status);
      setItems((l) => l.map((x) => (x.id === u.id ? { ...x, status } : x)));
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
                      <td>{u.name || '—'}</td>
                      <td>{u.email}</td>
                      <td><StatusBadge status={u.role} /></td>
                      <td><StatusBadge status={u.status || 'ENABLED'} /></td>
                      <td className="text-end text-nowrap">
                        <select className="form-select form-select-sm d-inline-block w-auto me-1" value={u.role || 'USER'} onChange={(e) => changeRole(u, e.target.value)}>
                          <option value="USER">USER</option>
                          <option value="ADMIN">ADMIN</option>
                        </select>
                        <button className="btn btn-sm btn-outline-secondary me-1" onClick={() => changeStatus(u, (u.status || 'ENABLED') === 'ENABLED' ? 'DISABLED' : 'ENABLED')}>
                          {(u.status || 'ENABLED') === 'ENABLED' ? 'Disable' : 'Enable'}
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
