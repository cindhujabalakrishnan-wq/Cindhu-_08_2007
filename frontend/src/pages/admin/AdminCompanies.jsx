import { useCallback, useEffect, useState } from 'react';
import companyService from '../../services/companyService.js';
import { getErrorMessage } from '../../services/api.js';
import Loader from '../../components/Loader.jsx';
import ErrorAlert from '../../components/ErrorAlert.jsx';
import EmptyState from '../../components/EmptyState.jsx';

export default function AdminCompanies() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [form, setForm] = useState({ name: '', contactEmail: '' });
  const [editing, setEditing] = useState(null);
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const res = await companyService.list({ size: 100 });
      setItems(res.items);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to load companies.'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError('');
    if (!form.name.trim()) {
      setFormError('Company name is required.');
      return;
    }
    setSaving(true);
    try {
      if (editing) await companyService.update(editing, form);
      else await companyService.create(form);
      setForm({ name: '', contactEmail: '' });
      setEditing(null);
      load();
    } catch (err) {
      setFormError(getErrorMessage(err, 'Failed to save company.'));
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (c) => {
    if (!window.confirm(`Delete company "${c.name}"?`)) return;
    try {
      await companyService.remove(c.id);
      load();
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to delete company.'));
    }
  };

  return (
    <div>
      <h2 className="mb-3">Companies</h2>
      <div className="card card-soft mb-3">
        <div className="card-body">
          <h5>{editing ? 'Edit company' : 'New company'}</h5>
          {formError && <div className="alert alert-danger">{formError}</div>}
          <form onSubmit={handleSubmit}>
            <div className="row g-2">
              <div className="col-md-5">
                <input className="form-control" placeholder="Company name *" value={form.name} onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))} required />
              </div>
              <div className="col-md-5">
                <input type="email" className="form-control" placeholder="Contact email" value={form.contactEmail} onChange={(e) => setForm((f) => ({ ...f, contactEmail: e.target.value }))} />
              </div>
              <div className="col-md-2 d-flex gap-2">
                <button className="btn btn-primary flex-grow-1" disabled={saving}>{saving ? 'Saving…' : editing ? 'Update' : 'Add'}</button>
                {editing && <button type="button" className="btn btn-outline-secondary" onClick={() => { setEditing(null); setForm({ name: '', contactEmail: '' }); }}>✕</button>}
              </div>
            </div>
          </form>
        </div>
      </div>

      {loading && <Loader text="Loading companies…" />}
      {!loading && error && <ErrorAlert message={error} onRetry={load} />}
      {!loading && !error && items.length === 0 && (
        <div className="card card-soft"><div className="card-body">
          <EmptyState icon="🏢" title="No companies" hint="Add the first company above." />
        </div></div>
      )}
      {!loading && !error && items.length > 0 && (
        <div className="card card-soft">
          <div className="card-body p-0">
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light"><tr><th>Name</th><th>Contact</th><th className="text-end">Actions</th></tr></thead>
                <tbody>
                  {items.map((c) => (
                    <tr key={c.id}>
                      <td>{c.name}</td>
                      <td>{c.contactEmail || '—'}</td>
                      <td className="text-end text-nowrap">
                        <button className="btn btn-sm btn-outline-primary me-1" onClick={() => { setEditing(c.id); setForm({ name: c.name || '', contactEmail: c.contactEmail || '' }); }}>Edit</button>
                        <button className="btn btn-sm btn-outline-danger" onClick={() => handleDelete(c)}>Delete</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
