import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import policyService from '../services/policyService.js';
import companyService from '../services/companyService.js';
import policyTypeService from '../services/policyTypeService.js';
import { getErrorMessage } from '../services/api.js';
import Loader from '../components/Loader.jsx';
import ErrorAlert from '../components/ErrorAlert.jsx';

const EMPTY = {
  policyNumber: '',
  holderName: '',
  category: '',
  companyId: '',
  policyTypeId: '',
  startDate: '',
  expiryDate: '',
  premiumAmount: '',
  status: 'ACTIVE',
  notes: '',
};

export default function PolicyForm() {
  const { id } = useParams();
  const isEdit = Boolean(id);
  const navigate = useNavigate();
  const [form, setForm] = useState(EMPTY);
  const [errors, setErrors] = useState({});
  const [companies, setCompanies] = useState([]);
  const [types, setTypes] = useState([]);
  const [loading, setLoading] = useState(isEdit);
  const [loadError, setLoadError] = useState('');
  const [saveError, setSaveError] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    companyService.list().then((r) => setCompanies(r.items || [])).catch(() => {});
    policyTypeService.list().then((r) => setTypes(r.items || [])).catch(() => {});
  }, []);

  useEffect(() => {
    if (!isEdit) return;
    (async () => {
      setLoading(true);
      try {
        const p = await policyService.getById(id);
        setForm({
          policyNumber: p.policyNumber || '',
          holderName: p.holderName || p.name || '',
          category: p.category || '',
          companyId: p.company?.id || p.companyId || '',
          policyTypeId: p.policyType?.id || p.policyTypeId || '',
          startDate: (p.startDate || '').slice(0, 10),
          expiryDate: (p.expiryDate || p.endDate || '').slice(0, 10),
          premiumAmount: p.premiumAmount ?? '',
          status: p.status || 'ACTIVE',
          notes: p.notes || '',
        });
      } catch (err) {
        setLoadError(getErrorMessage(err, 'Failed to load policy.'));
      } finally {
        setLoading(false);
      }
    })();
  }, [id, isEdit]);

  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }));

  const validate = () => {
    const e = {};
    if (!form.holderName.trim()) e.holderName = 'Policy holder name is required.';
    if (!form.startDate) e.startDate = 'Start date is required.';
    if (!form.expiryDate) e.expiryDate = 'Expiry date is required.';
    if (form.startDate && form.expiryDate && form.expiryDate <= form.startDate) {
      e.expiryDate = 'Expiry date must be after start date.';
    }
    if (form.premiumAmount === '' || Number(form.premiumAmount) <= 0) {
      e.premiumAmount = 'Premium must be greater than 0.';
    }
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSubmit = async (ev) => {
    ev.preventDefault();
    setSaveError('');
    if (!validate()) return;
    setSaving(true);
    try {
      const payload = {
        ...form,
        companyId: form.companyId || null,
        policyTypeId: form.policyTypeId || null,
        premiumAmount: Number(form.premiumAmount),
      };
      const saved = isEdit ? await policyService.update(id, payload) : await policyService.create(payload);
      navigate(`/policies/${saved.id || id}`, { replace: true });
    } catch (err) {
      setSaveError(getErrorMessage(err, 'Failed to save policy.'));
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <Loader text="Loading policy…" />;
  if (loadError) return <ErrorAlert message={loadError} />;

  return (
    <div>
      <div className="d-flex align-items-center gap-2 mb-3">
        <Link to={isEdit ? `/policies/${id}` : '/policies'} className="btn btn-sm btn-outline-secondary">← Back</Link>
        <h2 className="mb-0">{isEdit ? 'Edit Policy' : 'New Policy'}</h2>
      </div>
      {saveError && <ErrorAlert message={saveError} />}
      <form onSubmit={handleSubmit} noValidate>
        <div className="card card-soft mb-3">
          <div className="card-body">
            <div className="row g-3">
              <div className="col-md-6">
                <label className="form-label">Policy number</label>
                <input className="form-control" value={form.policyNumber} onChange={(e) => set('policyNumber', e.target.value)} placeholder="Auto-generated if blank" />
              </div>
              <div className="col-md-6">
                <label className="form-label">Policy holder name *</label>
                <input className={`form-control ${errors.holderName ? 'is-invalid' : ''}`} value={form.holderName} onChange={(e) => set('holderName', e.target.value)} />
                {errors.holderName && <div className="invalid-feedback">{errors.holderName}</div>}
              </div>
              <div className="col-md-6">
                <label className="form-label">Company</label>
                <select className="form-select" value={form.companyId} onChange={(e) => set('companyId', e.target.value)}>
                  <option value="">Select company…</option>
                  {companies.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
                </select>
              </div>
              <div className="col-md-6">
                <label className="form-label">Policy type</label>
                <select className="form-select" value={form.policyTypeId} onChange={(e) => set('policyTypeId', e.target.value)}>
                  <option value="">Select type…</option>
                  {types.map((t) => <option key={t.id} value={t.id}>{t.name}</option>)}
                </select>
              </div>
              <div className="col-md-6">
                <label className="form-label">Category</label>
                <input className="form-control" value={form.category} onChange={(e) => set('category', e.target.value)} placeholder="e.g. Health, Motor, Life" />
              </div>
              <div className="col-md-6">
                <label className="form-label">Status</label>
                <select className="form-select" value={form.status} onChange={(e) => set('status', e.target.value)}>
                  <option value="ACTIVE">Active</option>
                  <option value="PENDING">Pending</option>
                  <option value="EXPIRED">Expired</option>
                  <option value="RENEWED">Renewed</option>
                  <option value="CANCELLED">Cancelled</option>
                </select>
              </div>
              <div className="col-md-4">
                <label className="form-label">Start date *</label>
                <input type="date" className={`form-control ${errors.startDate ? 'is-invalid' : ''}`} value={form.startDate} onChange={(e) => set('startDate', e.target.value)} />
                {errors.startDate && <div className="invalid-feedback">{errors.startDate}</div>}
              </div>
              <div className="col-md-4">
                <label className="form-label">Expiry date *</label>
                <input type="date" className={`form-control ${errors.expiryDate ? 'is-invalid' : ''}`} value={form.expiryDate} onChange={(e) => set('expiryDate', e.target.value)} />
                {errors.expiryDate && <div className="invalid-feedback">{errors.expiryDate}</div>}
              </div>
              <div className="col-md-4">
                <label className="form-label">Premium amount *</label>
                <input type="number" min="0" step="0.01" className={`form-control ${errors.premiumAmount ? 'is-invalid' : ''}`} value={form.premiumAmount} onChange={(e) => set('premiumAmount', e.target.value)} />
                {errors.premiumAmount && <div className="invalid-feedback">{errors.premiumAmount}</div>}
              </div>
              <div className="col-12">
                <label className="form-label">Notes</label>
                <textarea className="form-control" rows="3" value={form.notes} onChange={(e) => set('notes', e.target.value)} />
              </div>
            </div>
          </div>
          <div className="card-footer bg-white d-flex justify-content-end gap-2">
            <Link to={isEdit ? `/policies/${id}` : '/policies'} className="btn btn-outline-secondary">Cancel</Link>
            <button className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : isEdit ? 'Save changes' : 'Create policy'}</button>
          </div>
        </div>
      </form>
    </div>
  );
}
