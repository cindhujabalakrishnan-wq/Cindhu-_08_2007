import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '80vh' }}>
      <div className="text-center">
        <div style={{ fontSize: '4rem' }}>🧭</div>
        <h1 className="display-5 fw-bold">404</h1>
        <p className="text-muted">The page you are looking for does not exist.</p>
        <Link to="/dashboard" className="btn btn-primary">Go to dashboard</Link>
      </div>
    </div>
  );
}
