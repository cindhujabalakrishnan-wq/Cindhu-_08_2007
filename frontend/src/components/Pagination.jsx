export default function Pagination({ page = 0, totalPages = 1, onChange }) {
  if (!totalPages || totalPages <= 1) return null;
  const current = Number(page) || 0;

  const go = (p) => {
    if (p < 0 || p >= totalPages || p === current) return;
    onChange(p);
  };

  const pages = [];
  const start = Math.max(0, current - 2);
  const end = Math.min(totalPages - 1, current + 2);
  for (let i = start; i <= end; i += 1) pages.push(i);

  return (
    <nav aria-label="Pagination">
      <ul className="pagination justify-content-center">
        <li className={`page-item ${current === 0 ? 'disabled' : ''}`}>
          <button className="page-link" onClick={() => go(current - 1)}>
            Previous
          </button>
        </li>
        {start > 0 && (
          <>
            <li className="page-item">
              <button className="page-link" onClick={() => go(0)}>1</button>
            </li>
            {start > 1 && <li className="page-item disabled"><span className="page-link">…</span></li>}
          </>
        )}
        {pages.map((p) => (
          <li key={p} className={`page-item ${p === current ? 'active' : ''}`}>
            <button className="page-link" onClick={() => go(p)}>{p + 1}</button>
          </li>
        ))}
        {end < totalPages - 1 && (
          <>
            {end < totalPages - 2 && <li className="page-item disabled"><span className="page-link">…</span></li>}
            <li className="page-item">
              <button className="page-link" onClick={() => go(totalPages - 1)}>{totalPages}</button>
            </li>
          </>
        )}
        <li className={`page-item ${current >= totalPages - 1 ? 'disabled' : ''}`}>
          <button className="page-link" onClick={() => go(current + 1)}>
            Next
          </button>
        </li>
      </ul>
    </nav>
  );
}
