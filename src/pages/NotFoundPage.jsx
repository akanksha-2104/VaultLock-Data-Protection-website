import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function NotFoundPage() {
  const navigate = useNavigate();
  const { user } = useAuth();

  return (
    <div style={s.wrap}>
      <div style={s.card}>
        <div style={s.logoRow}>
          <div style={s.logoIcon}>
            <svg viewBox="0 0 24 24" fill="white" width="18" height="18">
              <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z" />
            </svg>
          </div>
          <span style={s.logoText}>VAULTLOCK</span>
        </div>
        <div style={s.code}>404</div>
        <div style={s.title}>Page not found</div>
        <div style={s.sub}>The page you are looking for does not exist or you do not have access.</div>
        <button style={s.btn} onClick={() => navigate(user ? '/files' : '/login')}>
          {user ? '← Back to files' : '← Sign in'}
        </button>
      </div>
    </div>
  );
}

const s = {
  wrap: { display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '100vh', background: 'var(--bg-tertiary)', padding: '2rem' },
  card: { background: '#ffffff', border: '0.5px solid rgba(0,0,0,0.08)', borderRadius: '16px', padding: '2.5rem', width: '100%', maxWidth: '400px', textAlign: 'center' },
  logoRow: { display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px', marginBottom: '2rem' },
  logoIcon: { width: '30px', height: '30px', background: '#0C447C', borderRadius: '6px', display: 'flex', alignItems: 'center', justifyContent: 'center' },
  logoText: { fontFamily: 'var(--mono)', fontSize: '13px', fontWeight: '700', color: 'var(--text-primary)' },
  code: { fontFamily: 'var(--mono)', fontSize: '64px', fontWeight: '700', color: '#E6F1FB', lineHeight: 1, marginBottom: '0.5rem' },
  title: { fontFamily: 'var(--mono)', fontSize: '18px', fontWeight: '700', color: 'var(--text-primary)', marginBottom: '0.5rem' },
  sub: { fontSize: '13px', color: 'var(--text-secondary)', lineHeight: '1.6', marginBottom: '1.5rem' },
  btn: { padding: '10px 20px', background: '#0C447C', color: 'white', border: 'none', borderRadius: '8px', fontSize: '13px', fontWeight: '500', cursor: 'pointer' },
};
