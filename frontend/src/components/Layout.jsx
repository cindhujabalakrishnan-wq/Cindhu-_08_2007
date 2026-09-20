import { useEffect, useState } from 'react';
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import notificationService from '../services/notificationService.js';

const userLinks = [
  { to: '/dashboard', label: 'Dashboard', icon: '📊' },
  { to: '/policies', label: 'Policies', icon: '📄' },
  { to: '/renewals', label: 'Renewals', icon: '🔁' },
  { to: '/payments', label: 'Payments', icon: '💳' },
  { to: '/documents', label: 'Documents', icon: '📁' },
  { to: '/notifications', label: 'Notifications', icon: '🔔' },
];

const adminLinks = [
  { to: '/admin', label: 'Admin Home', icon: '🛠️' },
  { to: '/admin/users', label: 'Users', icon: '👥' },
  { to: '/admin/policies', label: 'All Policies', icon: '🗂️' },
  { to: '/admin/renewals', label: 'All Renewals', icon: '⏰' },
  { to: '/admin/companies', label: 'Companies', icon: '🏢' },
  { to: '/admin/policy-types', label: 'Policy Types', icon: '🏷️' },
  { to: '/admin/audit-logs', label: 'Audit Logs', icon: '📝' },
];

export default function Layout() {
  const { user, isAdmin, logout } = useAuth();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [unread, setUnread] = useState(0);

  useEffect(() => {
    let mounted = true;
    notificationService
      .getUnreadCount()
      .then((count) => {
        if (mounted) setUnread(Number(count) || 0);
      })
      .catch(() => {});
    return () => {
      mounted = false;
    };
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const closeSidebar = () => setSidebarOpen(false);

  return (
    <div className="app-shell">
      <aside className={`sidebar p-3 ${sidebarOpen ? 'open' : ''}`}>
        <Link to="/dashboard" className="sidebar-brand d-flex align-items-center gap-2 mb-4" onClick={closeSidebar}>
          <span className="brand-mark" style={{ width: 38, height: 38, fontSize: '1.2rem' }}>
            🛡️
          </span>
          InsureTrack
        </Link>
        <nav className="nav flex-column gap-1">
          {userLinks.map((l) => (
            <NavLink key={l.to} to={l.to} className="nav-link" onClick={closeSidebar}>
              <span className="me-2">{l.icon}</span>
              {l.label}
              {l.to === '/notifications' && unread > 0 && (
                <span className="badge bg-danger ms-2">{unread}</span>
              )}
            </NavLink>
          ))}
        </nav>
        {isAdmin && (
          <>
            <div className="text-uppercase text-secondary small mt-4 mb-2 px-2">Administration</div>
            <nav className="nav flex-column gap-1">
              {adminLinks.map((l) => (
                <NavLink key={l.to} to={l.to} end={l.to === '/admin'} className="nav-link" onClick={closeSidebar}>
                  <span className="me-2">{l.icon}</span>
                  {l.label}
                </NavLink>
              ))}
            </nav>
          </>
        )}
        <div className="mt-auto pt-3 border-top border-secondary">
          <div className="small text-truncate">{user?.name || user?.email || 'User'}</div>
          <div className="small text-secondary mb-2">{user?.role || ''}</div>
          <button className="btn btn-sm btn-outline-light w-100" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </aside>
      <div className={`sidebar-backdrop ${sidebarOpen ? 'show' : ''}`} onClick={closeSidebar} />

      <div className="main-area">
        <header className="topnav d-flex align-items-center gap-2 px-3 py-2">
          <button className="btn btn-outline-secondary btn-sm d-lg-none" onClick={() => setSidebarOpen((v) => !v)} aria-label="Toggle menu">
            ☰
          </button>
          <Link to="/dashboard" className="fw-bold text-decoration-none text-dark d-lg-none">
            🛡️ InsureTrack
          </Link>
          <div className="ms-auto d-flex align-items-center gap-2">
            <Link to="/notifications" className="btn btn-outline-secondary btn-sm position-relative">
              🔔
              {unread > 0 && (
                <span className="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">
                  {unread}
                </span>
              )}
            </Link>
            <Link to="/profile" className="btn btn-outline-primary btn-sm">
              {user?.name ? user.name.split(' ')[0] : 'Profile'}
            </Link>
          </div>
        </header>
        <main className="page-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
