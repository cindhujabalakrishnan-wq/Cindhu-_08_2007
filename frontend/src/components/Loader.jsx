export default function Loader({ fullPage = false, text = 'Loading…' }) {
  const inner = (
    <div className="d-flex flex-column align-items-center gap-2 py-5">
      <div className="spinner-border text-primary" role="status">
        <span className="visually-hidden">{text}</span>
      </div>
      <div className="text-muted small">{text}</div>
    </div>
  );
  if (fullPage) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '60vh' }}>
        {inner}
      </div>
    );
  }
  return inner;
}
