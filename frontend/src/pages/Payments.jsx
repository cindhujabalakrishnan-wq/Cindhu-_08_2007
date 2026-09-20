import { useCallback, useEffect, useState } from 'react';
import paymentService from '../services/paymentService.js';
import policyService from '../services/policyService.js';
import { getErrorMessage } from '../services/api.js';
import Loader from '../components/Loader.jsx';
import ErrorAlert from '../components/ErrorAlert.jsx';
import EmptyState from '../components/EmptyState.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import Pagination from '../components/Pagination.jsx';

function formatDate(v) {
  if (!v) return '—';
  const d = new Date(v);
  return Number.isNaN(d.getTime()) ? String(v) : d.toLocaleDateString();
}

export default function Payments() {
  const [items, setItems] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [policyFilter, setPolicyFilter] = useState('');
  const [policies, setPolicies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ policyId: '', amount: '', method: 'CREDIT_CARD', notes: '' });
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    policyService.list({ size: 200 }).then((r) => {
      const list = r.items || [];
      setPolicies(list);
      if (list.length > 0) {
        setPolicyFilter((cur) => cur || String(list[0].id));
        setForm((f) => ({ ...f, policyId: f.policyId || String(list[0].id) }));
      }
    }).catch(() => {});
  }, []);

  const load = useCallback(async () => {
    if (!policyFilter) {
      setLoading(false);
      setItems([]);
      return;
    }
    setLoading(true);
    setError('');
    try {
      const res = await paymentService.getByPolicy(policyFilter, { page, size: 10 });
      setItems(res.items);
      setTotalPages(res.totalPages);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to load payments.'));
    } finally {
      setLoading(false);
    }
  }, [page, policyFilter]);

  useEffect(() => {
    load();
  }, [load]);

  const handleCreate = async (e) => {
    e.preventDefault();
    setFormError('');
    if (!form.policyId || !form.amount || Number(form.amount) <= 0) {
      setFormError('Policy and a positive amount are required.');
      return;
    }
    setSaving(true);
    try {
      await paymentService.create(form.policyId, { ...form, amount: Number(form.amount) });
      setShowForm(false);
      setForm({ policyId: '', amount: '', method: 'CREDIT_CARD', notes: '' });
      setPage(0);
      load();
    } catch (err) {
      setFormError(getErrorMessage(err, 'Failed to record payment.'));
    } finally {
      setSaving(false);
    }
  };

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
        <h2 className="mb-0">Payments</h2>
        <button className="btn btn-primary" onClick={() => setShowForm((v) => !v)}>
          {showForm ? 'Close' : '+ Record payment'}
        </button>
      </div>

      {showForm && (
        <div className="card card-soft mb-3">
          <div className="card-body">
            <h5>New payment</h5>
            {formError && <div className="alert alert-danger">{formError}</div>}
            <form onSubmit={handleCreate}>
              <div className="row g-2">
                <div className="col-md-4">
                  <select className="form-select" value={form.policyId} onChange={(e) => setForm((f) => ({ ...f, policyId: e.target.value }))} required>
                    <option value="">Select policy…</option>
                    {policies.map((p) => <option key={p.id} value={p.id}>{p.policyNumber || `#${p.id}`} — {p.holderName}</option>)}
                  </select>
                </div>
                <div className="col-md-2">
                  <input type="number" min="0" step="0.01" className="form-control" placeholder="Amount" value={form.amount} onChange={(e) => setForm((f) => ({ ...f, amount: e.target.value }))} required />
                </div>
                <div className="col-md-2">
                  <select className="form-select" value={form.method} onChange={(e) => setForm((f) => ({ ...f, method: e.target.value }))}>
                    <option value="CREDIT_CARD">Credit card</option>
                    <option value="DEBIT_CARD">Debit card</option>
                    <option value="BANK_TRANSFER">Bank transfer</option>
                    <option value="UPI">UPI</option>
                    <option value="NET_BANKING">Net banking</option>
                    <option value="CASH">Cash</option>
                    <option value="CHEQUE">Cheque</option>
                  </select>
                </div>
                <div className="col-md-2">
                  <input className="form-control" placeholder="Notes" value={form.notes} onChange={(e) => setForm((f) => ({ ...f, notes: e.target.value }))} />
                </div>
                <div className="col-md-2">
                  <button className="btn btn-success w-100" disabled={saving}>{saving ? 'Saving…' : 'Save'}</button>
                </div>
              </div>
            </form>
          </div>
        </div>
      )}

      <div className="card card-soft mb-3">
        <div className="card-body">
          <div className="row g-2">
            <div className="col-md-4">
              <select className="form-select" value={policyFilter} onChange={(e) => { setPolicyFilter(e.target.value); setPage(0); }}>
                <option value="">All policies</option>
                {policies.map((p) => <option key={p.id} value={p.id}>{p.policyNumber || `#${p.id}`} — {p.holderName}</option>)}
              </select>
            </div>
          </div>
        </div>
      </div>

      {loading && <Loader text="Loading payments…" />}
      {!loading && error && <ErrorAlert message={error} onRetry={load} />}
      {!loading && !error && items.length === 0 && (
        <div className="card card-soft"><div className="card-body">
          <EmptyState icon="💳" title="No payments" hint="Record your first payment using the button above." />
        </div></div>
      )}
      {!loading && !error && items.length > 0 && (
        <div className="card card-soft">
          <div className="card-body p-0">
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr><th>ID</th><th>Policy</th><th>Amount</th><th>Method</th><th>Date</th><th>Status</th></tr>
                </thead>
                <tbody>
                  {items.map((pay) => (
                    <tr key={pay.id}>
                      <td>#{pay.id}</td>
                      <td>{pay.policyNumber || pay.policy?.policyNumber || pay.policyId || '—'}</td>
                      <td>{Number(pay.amount) ? Number(pay.amount).toLocaleString() : '—'}</td>
                      <td>{pay.method || '—'}</td>
                      <td>{formatDate(pay.paidAt || pay.createdAt)}</td>
                      <td><StatusBadge status={pay.status} /></td>
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
