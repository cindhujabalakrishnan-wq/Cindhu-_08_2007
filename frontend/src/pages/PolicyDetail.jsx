import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import policyService from '../services/policyService.js';
import paymentService from '../services/paymentService.js';
import documentService from '../services/documentService.js';
import renewalService from '../services/renewalService.js';
import { getErrorMessage } from '../services/api.js';
import Loader from '../components/Loader.jsx';
import ErrorAlert from '../components/ErrorAlert.jsx';
import EmptyState from '../components/EmptyState.jsx';
import StatusBadge from '../components/StatusBadge.jsx';

function formatDate(v) {
  if (!v) return '—';
  const d = new Date(v);
  return Number.isNaN(d.getTime()) ? String(v) : d.toLocaleDateString();
}

export default function PolicyDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [policy, setPolicy] = useState(null);
  const [payments, setPayments] = useState([]);
  const [documents, setDocuments] = useState([]);
  const [renewals, setRenewals] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionMsg, setActionMsg] = useState('');
  const [acting, setActing] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const p = await policyService.getById(id);
      setPolicy(p);
      const [pay, docs, ren] = await Promise.all([
        paymentService.getByPolicy(id).catch(() => ({ items: [] })),
        documentService.getByPolicy(id).catch(() => ({ items: [] })),
        renewalService.getByPolicy(id).catch(() => ({ items: [] })),
      ]);
      setPayments(pay.items || []);
      setDocuments(docs.items || []);
      setRenewals(ren.items || []);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to load policy.'));
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    load();
  }, [load]);

  const handleRenew = async () => {
    if (!policy) return;
    setActing(true);
    setActionMsg('');
    try {
      const expiry = policy.expiryDate || policy.endDate;
      const currentExpiry = expiry ? new Date(expiry) : new Date();
      const nextExpiry = new Date(currentExpiry);
      nextExpiry.setFullYear(nextExpiry.getFullYear() + 1);
      const renewal = await renewalService.create(id, {
        renewalDate: new Date().toISOString().slice(0, 10),
        newExpiryDate: nextExpiry.toISOString().slice(0, 10),
        renewalPremium: policy.premiumAmount,
      });
      if (renewal && renewal.id) {
        await renewalService.complete(renewal.id);
      }
      setActionMsg('Policy renewed successfully.');
      load();
    } catch (err) {
      setActionMsg(getErrorMessage(err, 'Renewal failed.'));
    } finally {
      setActing(false);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm('Delete this policy? This cannot be undone.')) return;
    setActing(true);
    try {
      await policyService.remove(id);
      navigate('/policies');
    } catch (err) {
      setActionMsg(getErrorMessage(err, 'Delete failed.'));
      setActing(false);
    }
  };

  if (loading) return <Loader text="Loading policy…" />;
  if (error) return <ErrorAlert message={error} onRetry={load} />;
  if (!policy) return <EmptyState title="Policy not found" hint="It may have been deleted." />;

  const rows = [
    ['Policy number', policy.policyNumber || `#${policy.id}`],
    ['Holder', policy.holderName || policy.name || '—'],
    ['Category', policy.category || policy.policyType?.name || '—'],
    ['Company', policy.company?.name || policy.companyName || '—'],
    ['Start', formatDate(policy.startDate)],
    ['Expiry', formatDate(policy.expiryDate || policy.endDate)],
    ['Premium', Number(policy.premiumAmount) ? Number(policy.premiumAmount).toLocaleString() : '—'],
    ['Notes', policy.notes || '—'],
  ];

  return (
    <div>
      <div className="d-flex align-items-center gap-2 mb-3 flex-wrap">
        <Link to="/policies" className="btn btn-sm btn-outline-secondary">← Policies</Link>
        <h2 className="mb-0 me-2">{policy.policyNumber || 'Policy'}</h2>
        <StatusBadge status={policy.status} />
        <div className="ms-auto d-flex gap-2">
          <Link to={`/policies/${id}/edit`} className="btn btn-sm btn-outline-primary">Edit</Link>
          <button className="btn btn-sm btn-success" disabled={acting} onClick={handleRenew}>Renew</button>
          <button className="btn btn-sm btn-outline-danger" disabled={acting} onClick={handleDelete}>Delete</button>
        </div>
      </div>
      {actionMsg && <div className="alert alert-info">{actionMsg}</div>}

      <div className="card card-soft mb-3">
        <div className="card-body">
          <div className="row">
            {rows.map(([k, v]) => (
              <div key={k} className="col-md-6 col-lg-3 mb-3">
                <div className="text-muted small">{k}</div>
                <div className="fw-semibold">{v}</div>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="row g-3">
        <div className="col-lg-4">
          <div className="card card-soft h-100">
            <div className="card-body">
              <h5>Payments ({payments.length})</h5>
              {payments.length === 0 ? <p className="text-muted">No payments recorded.</p> : (
                <ul className="list-group list-group-flush">
                  {payments.slice(0, 5).map((pay) => (
                    <li key={pay.id} className="list-group-item px-0 d-flex justify-content-between">
                      <span>{formatDate(pay.paidAt || pay.createdAt)} — <StatusBadge status={pay.status} /></span>
                      <strong>{Number(pay.amount) ? Number(pay.amount).toLocaleString() : '—'}</strong>
                    </li>
                  ))}
                </ul>
              )}
              <Link to="/payments" className="btn btn-sm btn-outline-primary mt-2">Manage payments</Link>
            </div>
          </div>
        </div>
        <div className="col-lg-4">
          <div className="card card-soft h-100">
            <div className="card-body">
              <h5>Documents ({documents.length})</h5>
              {documents.length === 0 ? <p className="text-muted">No documents uploaded.</p> : (
                <ul className="list-group list-group-flush">
                  {documents.slice(0, 5).map((d) => (
                    <li key={d.id} className="list-group-item px-0">{d.fileName || d.name || `Document #${d.id}`}</li>
                  ))}
                </ul>
              )}
              <Link to="/documents" className="btn btn-sm btn-outline-primary mt-2">Manage documents</Link>
            </div>
          </div>
        </div>
        <div className="col-lg-4">
          <div className="card card-soft h-100">
            <div className="card-body">
              <h5>Renewals ({renewals.length})</h5>
              {renewals.length === 0 ? <p className="text-muted">No renewal history.</p> : (
                <ul className="list-group list-group-flush">
                  {renewals.slice(0, 5).map((r) => (
                    <li key={r.id} className="list-group-item px-0 d-flex justify-content-between">
                      <span>{formatDate(r.renewedAt || r.createdAt)}</span>
                      <StatusBadge status={r.status} />
                    </li>
                  ))}
                </ul>
              )}
              <Link to="/renewals" className="btn btn-sm btn-outline-primary mt-2">View renewals</Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
