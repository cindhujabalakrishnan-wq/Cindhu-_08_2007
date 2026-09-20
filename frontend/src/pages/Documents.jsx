import { useCallback, useEffect, useState } from 'react';
import documentService from '../services/documentService.js';
import policyService from '../services/policyService.js';
import { getErrorMessage } from '../services/api.js';
import Loader from '../components/Loader.jsx';
import ErrorAlert from '../components/ErrorAlert.jsx';
import EmptyState from '../components/EmptyState.jsx';
import Pagination from '../components/Pagination.jsx';

function formatDate(v) {
  if (!v) return '—';
  const d = new Date(v);
  return Number.isNaN(d.getTime()) ? String(v) : d.toLocaleDateString();
}

export default function Documents() {
  const [items, setItems] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [policyFilter, setPolicyFilter] = useState('');
  const [policies, setPolicies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [uploadPolicy, setUploadPolicy] = useState('');
  const [file, setFile] = useState(null);
  const [uploadMsg, setUploadMsg] = useState('');
  const [uploading, setUploading] = useState(false);

  useEffect(() => {
    policyService.list({ size: 200 }).then((r) => {
      const list = r.items || [];
      setPolicies(list);
      if (list.length > 0) {
        setPolicyFilter((cur) => cur || String(list[0].id));
        setUploadPolicy((cur) => cur || String(list[0].id));
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
      const res = await documentService.getByPolicy(policyFilter);
      setItems(res.items);
      setTotalPages(res.totalPages);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to load documents.'));
    } finally {
      setLoading(false);
    }
  }, [page, policyFilter]);

  useEffect(() => {
    load();
  }, [load]);

  const handleUpload = async (e) => {
    e.preventDefault();
    setUploadMsg('');
    if (!uploadPolicy || !file) {
      setUploadMsg('Select a policy and a file to upload.');
      return;
    }
    setUploading(true);
    try {
      await documentService.upload(uploadPolicy, file, { documentType: 'OTHER' });
      setUploadMsg('Upload successful.');
      setFile(null);
      e.target.reset();
      load();
    } catch (err) {
      setUploadMsg(getErrorMessage(err, 'Upload failed.'));
    } finally {
      setUploading(false);
    }
  };

  const handleDownload = async (doc) => {
    try {
      await documentService.download(doc.id, doc.fileName || doc.name || `document-${doc.id}`);
    } catch {
      setError('Download failed. Please try again.');
    }
  };

  const handleDelete = async (doc) => {
    if (!window.confirm(`Delete "${doc.fileName || doc.name || doc.id}"?`)) return;
    try {
      await documentService.remove(doc.id);
      load();
    } catch (err) {
      setError(getErrorMessage(err, 'Delete failed.'));
    }
  };

  return (
    <div>
      <h2 className="mb-3">Documents</h2>

      <div className="card card-soft mb-3">
        <div className="card-body">
          <h5>Upload document</h5>
          {uploadMsg && <div className="alert alert-info">{uploadMsg}</div>}
          <form onSubmit={handleUpload}>
            <div className="row g-2">
              <div className="col-md-4">
                <select className="form-select" value={uploadPolicy} onChange={(e) => setUploadPolicy(e.target.value)} required>
                  <option value="">Select policy…</option>
                  {policies.map((p) => <option key={p.id} value={p.id}>{p.policyNumber || `#${p.id}`} — {p.holderName}</option>)}
                </select>
              </div>
              <div className="col-md-5">
                <input type="file" className="form-control" onChange={(e) => setFile(e.target.files?.[0] || null)} required />
              </div>
              <div className="col-md-3">
                <button className="btn btn-primary w-100" disabled={uploading}>{uploading ? 'Uploading…' : 'Upload'}</button>
              </div>
            </div>
          </form>
        </div>
      </div>

      <div className="card card-soft mb-3">
        <div className="card-body">
          <div className="col-md-4">
            <select className="form-select" value={policyFilter} onChange={(e) => { setPolicyFilter(e.target.value); setPage(0); }}>
              <option value="">All policies</option>
              {policies.map((p) => <option key={p.id} value={p.id}>{p.policyNumber || `#${p.id}`} — {p.holderName}</option>)}
            </select>
          </div>
        </div>
      </div>

      {loading && <Loader text="Loading documents…" />}
      {!loading && error && <ErrorAlert message={error} onRetry={load} />}
      {!loading && !error && items.length === 0 && (
        <div className="card card-soft"><div className="card-body">
          <EmptyState icon="📁" title="No documents" hint="Upload the first document above." />
        </div></div>
      )}
      {!loading && !error && items.length > 0 && (
        <div className="card card-soft">
          <div className="card-body p-0">
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr><th>File</th><th>Policy</th><th>Type</th><th>Uploaded</th><th className="text-end">Actions</th></tr>
                </thead>
                <tbody>
                  {items.map((d) => (
                    <tr key={d.id}>
                      <td>{d.fileName || d.name || `#${d.id}`}</td>
                      <td>{d.policyNumber || d.policy?.policyNumber || d.policyId || '—'}</td>
                      <td>{d.contentType || d.type || '—'}</td>
                      <td>{formatDate(d.uploadedAt || d.createdAt)}</td>
                      <td className="text-end text-nowrap">
                        <button className="btn btn-sm btn-outline-primary me-1" onClick={() => handleDownload(d)}>Download</button>
                        <button className="btn btn-sm btn-outline-danger" onClick={() => handleDelete(d)}>Delete</button>
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
