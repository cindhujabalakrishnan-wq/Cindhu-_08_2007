import { useCallback, useEffect, useState } from 'react';
import policyTypeService from '../../services/policyTypeService.js';
import { getErrorMessage } from '../../services/api.js';
import Loader from '../../components/Loader.jsx';
import ErrorAlert from '../../components/ErrorAlert.jsx';
import EmptyState from '../../components/EmptyState.jsx';

export default function AdminPolicyTypes() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [form, setForm] = useState({ name: '', description: '' });
  const [editing, setEditing] = useState(null);
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const res = await policyTypeService.list({ size: 100 });
      setItems(res.items);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to load policy types.'));
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
      setFormError('Type name is required.');
      return;
    }
    setSaving(true);
    try {
      if (editing) await policyTypeService.update(editing, form);
      else await policyTypeService.create(form);
      setForm({ name: '', description: '' });
      setEditing(null);
      load();
    } catch (err) {
      setFormError(getErrorMessage(err, 'Failed to save policy type.'));
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (t) => {
    if (!window.confirm(`Delete policy type "${t.name}"?`)) return;
    try {
      await policyTypeService.remove(t.id);
      load();
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to delete policy type.'));
    }
  };

  return (
    <div>
      <h2 className="mb-3">Policy Types</h2>
      <div className="card card-soft mb-3">
        <div className="card-body">
          <h5>{editing ? 'Edit policy type' : 'New policy type'}</h5>
          {formError && <div className="alert alert-danger">{formError}</div>}
          <form onSubmit={handleSubmit}>
            <div className="row g-2">
              <div className="col-md-4">
                <input className="form-control" placeholder="Type name *" value={form.name} onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))} required />
              </div>
              <div className="col-md-6">
                <input className="form-control" placeholder="Description" value={form.description} onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))} />
              </div>
              <div className="col-md-2 d-flex gap-2">
                <button className="btn btn-primary flex-grow-1" disabled={saving}>{saving ? 'Saving…' : editing ? 'Update' : 'Add'}</button>
                {editing && <button type="button" className="btn btn-outline-secondary" onClick={() => { setEditing(null); setForm({ name: '', description: '' }); }}>✕</button>}
              </div>
            </div>
          </form>
        </div>
      </div>

      {loading && <Loader text="Loading policy types…" />}
      {!loading && error && <ErrorAlert message={error} onRetry={load} />}
      {!loading && !error && items.length === 0 && (
        <div className="card card-soft"><div className="card-body">
          <EmptyState icon="🏷️" title="No policy types" hint="Add the first policy type above." />
        </div></div>
      )}
      {!loading && !error && items.length > 0 && (
        <div className="card card-soft">
          <div className="card-body p-0">
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light"><tr><th>Name</th><th>Description</th><th className="text-end">Actions</th></tr></thead>
                <tbody>
                  {items.map((t) => (
                    <tr key={t.id}>
                      <td>{t.name}</td>
                      <td>{t.description || '—'}</td>
                      <td className="text-end text-nowrap">
                        <button className="btn btn-sm btn-outline-primary me-1" onClick={() => { setEditing(t.id); setForm({ name: t.name || '', description: t.description || '' }); }}>Edit</button>
                        <button className="btn btn-sm btn-outline-danger" onClick={() => handleDelete(t)}>Delete</button>
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
