export default function EmptyState({ icon = '📭', title = 'Nothing here yet', hint = 'There is no data to show.', action = null }) {
  return (
    <div className="text-center py-5">
      <div style={{ fontSize: '2.5rem' }}>{icon}</div>
      <h5 className="mt-3">{title}</h5>
      <p className="text-muted">{hint}</p>
      {action}
    </div>
  );
}
