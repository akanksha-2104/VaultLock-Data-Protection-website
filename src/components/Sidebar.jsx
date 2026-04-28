import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const NAV_ITEMS = [
  {
    path: '/files',
    label: 'My Files',
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" width="15" height="15">
        <path d="M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z" />
        <polyline points="13 2 13 9 20 9" />
      </svg>
    ),
  },
  {
    path: '/upload',
    label: 'Upload',
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" width="15" height="15">
        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
        <polyline points="17 8 12 3 7 8" />
        <line x1="12" y1="3" x2="12" y2="15" />
      </svg>
    ),
  },
  {
    path: '/logs',
    label: 'Audit Log',
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" width="15" height="15">
        <path d="M9 11l3 3L22 4" />
        <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" />
      </svg>
    ),
  },
  {
    path: '/locations',
    label: 'Login History',
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" width="15" height="15">
        <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" />
        <circle cx="12" cy="10" r="3" />
      </svg>
    ),
  },
];

export default function Sidebar() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();

  const name = user?.name || user?.email?.split('@')[0] || 'User';
  const email = user?.email || '';
  const initials = name.slice(0, 2).toUpperCase();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <aside style={styles.sidebar}>
      <div style={styles.logo}>
        <div style={styles.logoIcon}>
          <svg viewBox="0 0 24 24" fill="white" width="14" height="14">
            <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z" />
          </svg>
        </div>
        <span style={styles.logoText}>VAULTLOCK</span>
      </div>

      <div style={styles.navSection}>Workspace</div>

      {NAV_ITEMS.map((item) => {
        const active = location.pathname === item.path;
        return (
          <button
            key={item.path}
            style={{ ...styles.navItem, ...(active ? styles.navItemActive : {}) }}
            onClick={() => navigate(item.path)}
          >
            <span style={{ color: active ? '#0C447C' : 'var(--text-secondary)' }}>{item.icon}</span>
            {item.label}
          </button>
        );
      })}

      <div style={styles.sidebarBottom}>
        <div style={styles.userBadge}>
          <div style={styles.avatar}>{initials}</div>
          <div>
            <div style={styles.userName}>{name}</div>
            <div style={styles.userEmail}>{email}</div>
          </div>
        </div>
        <button style={styles.logoutBtn} onClick={handleLogout}>Sign out</button>
      </div>
    </aside>
  );
}

const styles = {
  sidebar: {
    width: '200px',
    background: '#ffffff',
    borderRight: '0.5px solid rgba(0,0,0,0.08)',
    display: 'flex',
    flexDirection: 'column',
    padding: '1rem 0',
    flexShrink: 0,
    height: '100vh',
    position: 'sticky',
    top: 0,
  },
  logo: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    padding: '0 1rem 1rem',
    borderBottom: '0.5px solid rgba(0,0,0,0.08)',
    marginBottom: '0.75rem',
  },
  logoIcon: {
    width: '28px',
    height: '28px',
    background: '#0C447C',
    borderRadius: '6px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
  logoText: {
    fontFamily: 'var(--mono)',
    fontSize: '11px',
    fontWeight: '700',
    color: 'var(--text-primary)',
    letterSpacing: '0.5px',
  },
  navSection: {
    fontSize: '10px',
    textTransform: 'uppercase',
    letterSpacing: '0.8px',
    color: 'var(--text-tertiary)',
    padding: '0.75rem 1rem 0.25rem',
    fontWeight: '500',
  },
  navItem: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
    padding: '9px 1rem',
    fontSize: '13px',
    fontWeight: '400',
    color: 'var(--text-secondary)',
    background: 'transparent',
    border: 'none',
    borderLeft: '2px solid transparent',
    width: '100%',
    textAlign: 'left',
    cursor: 'pointer',
    transition: 'all 0.15s',
  },
  navItemActive: {
    background: '#E6F1FB',
    color: '#0C447C',
    borderLeftColor: '#0C447C',
    fontWeight: '500',
  },
  sidebarBottom: {
    marginTop: 'auto',
    padding: '0.75rem 1rem',
    borderTop: '0.5px solid rgba(0,0,0,0.08)',
  },
  userBadge: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    marginBottom: '8px',
  },
  avatar: {
    width: '30px',
    height: '30px',
    borderRadius: '50%',
    background: '#E6F1FB',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: '11px',
    fontWeight: '600',
    color: '#0C447C',
    flexShrink: 0,
  },
  userName: {
    fontSize: '12px',
    fontWeight: '500',
    color: 'var(--text-primary)',
  },
  userEmail: {
    fontSize: '10px',
    color: 'var(--text-secondary)',
  },
  logoutBtn: {
    width: '100%',
    padding: '7px',
    border: '0.5px solid rgba(0,0,0,0.1)',
    borderRadius: '6px',
    background: 'transparent',
    fontSize: '12px',
    color: 'var(--text-secondary)',
    cursor: 'pointer',
  },
};
