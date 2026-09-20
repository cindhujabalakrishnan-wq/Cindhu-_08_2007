import { useEffect, useState } from 'react';
import adminService from '../services/adminService.js';
import { useAuth } from '../context/AuthContext.jsx';
import { getErrorMessage } from '../services/api.js';
import Loader from '../components/Loader.jsx';
import ErrorAlert from '../components/ErrorAlert.jsx';

export default function Profile() {
  const { refreshMe } = useAuth();
  const [form, setForm] = useState({ name: '', email: '', phone: '' });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    (async () => {
      setLoading(true);
      try {
        const me = await adminService.getProfile();
        setForm({ name: me.name || '', email: me.email || '', phone: me.phone || '' });
      } catch (err) {
        setError(getErrorMessage(err, 'Failed to load profile.'));
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('');
    setError('');
    if (!form.name.trim() || !form.email.trim()) {
      setError('Name and email are required.');
      return;
    }
    setSaving(true);
    try {
      await adminService.updateProfile({ name: form.name, email: form.email, phone: form.phone });
      await refreshMe();
      setMessage('Profile updated successfully.');
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to update profile.'));
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <Loader text="Loading profile…" />;

  return (
    <div>
      <h2 className="mb-3">Profile</h2>
      {error && <ErrorAlert message={error} />}
      {message && <div className="alert alert-success">{message}</div>}
      <div className="card card-soft">
        <div className="card-body">
          <form onSubmit={handleSubmit} noValidate>
            <div className="mb-3">
              <label className="form-label" htmlFor="name">Full name</label>
              <input id="name" className="form-control" value={form.name} onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))} required />
            </div>
            <div className="mb-3">
              <label className="form-label" htmlFor="email">Email</label>
              <input id="email" type="email" className="form-control" value={form.email} onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))} required />
            </div>
            <div className="mb-3">
              <label className="form-label" htmlFor="phone">Phone</label>
              <input id="phone" className="form-control" value={form.phone} onChange={(e) => setForm((f) => ({ ...f, phone: e.target.value }))} placeholder="Optional" />
            </div>
            <button className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Save changes'}</button>
          </form>
        </div>
      </div>
    </div>
  );
}
