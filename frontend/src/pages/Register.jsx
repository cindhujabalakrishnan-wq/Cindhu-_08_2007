import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { getErrorMessage } from '../services/api.js';

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: '', email: '', password: '', confirmPassword: '' });
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!form.name || !form.email || !form.password) {
      setError('Name, email and password are required.');
      return;
    }
    if (form.password.length < 6) {
      setError('Password must be at least 6 characters.');
      return;
    }
    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match.');
      return;
    }
    setSubmitting(true);
    try {
      await register({ name: form.name, email: form.email, password: form.password });
      navigate('/dashboard', { replace: true });
    } catch (err) {
      setError(getErrorMessage(err, 'Registration failed.'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="card auth-card">
        <div className="card-body p-4">
          <div className="text-center mb-4">
            <span className="brand-mark">🛡️</span>
            <h3 className="mt-3 mb-1">Create account</h3>
            <p className="text-muted">Manage policies with InsureTrack</p>
          </div>
          {error && <div className="alert alert-danger">{error}</div>}
          <form onSubmit={handleSubmit} noValidate>
            <div className="mb-3">
              <label className="form-label" htmlFor="name">Full name</label>
              <input id="name" name="name" className="form-control" value={form.name} onChange={handleChange} autoComplete="name" required />
            </div>
            <div className="mb-3">
              <label className="form-label" htmlFor="email">Email</label>
              <input id="email" name="email" type="email" className="form-control" value={form.email} onChange={handleChange} autoComplete="email" required />
            </div>
            <div className="row">
              <div className="col-md-6 mb-3">
                <label className="form-label" htmlFor="password">Password</label>
                <input id="password" name="password" type="password" className="form-control" value={form.password} onChange={handleChange} autoComplete="new-password" required />
              </div>
              <div className="col-md-6 mb-3">
                <label className="form-label" htmlFor="confirmPassword">Confirm</label>
                <input id="confirmPassword" name="confirmPassword" type="password" className="form-control" value={form.confirmPassword} onChange={handleChange} autoComplete="new-password" required />
              </div>
            </div>
            <button className="btn btn-primary w-100" disabled={submitting}>
              {submitting ? 'Creating…' : 'Register'}
            </button>
          </form>
          <p className="text-center mt-3 mb-0">
            Have an account? <Link to="/login">Sign in</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
