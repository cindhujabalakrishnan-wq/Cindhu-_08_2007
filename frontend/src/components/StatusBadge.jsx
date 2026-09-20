const STATUS_STYLES = {
  ACTIVE: 'bg-success',
  PENDING: 'bg-warning text-dark',
  EXPIRED: 'bg-danger',
  EXPIRING: 'bg-warning text-dark',
  RENEWED: 'bg-info text-dark',
  CANCELLED: 'bg-secondary',
  CLAIMED: 'bg-primary',
  PAID: 'bg-success',
  FAILED: 'bg-danger',
  OVERDUE: 'bg-danger',
  UPCOMING: 'bg-info text-dark',
  COMPLETED: 'bg-success',
  READ: 'bg-secondary',
  UNREAD: 'bg-primary',
  ENABLED: 'bg-success',
  DISABLED: 'bg-secondary',
};

export default function StatusBadge({ status }) {
  const key = String(status || 'UNKNOWN').toUpperCase();
  const cls = STATUS_STYLES[key] || 'bg-secondary';
  return <span className={`badge ${cls}`}>{String(status || 'Unknown').replace(/_/g, ' ')}</span>;
}
