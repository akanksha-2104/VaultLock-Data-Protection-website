import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
  const [email, setEmail]       = useState('');
  const [password, setPassword] = useState('');
  const [error, setError]       = useState('');
  const [loading, setLoading]   = useState(false);
  const { login } = useAuth();
  const navigate  = useNavigate();

  const handleLogin = async () => {
    if (!email || !password) { setError('Please fill in all fields.'); return; }
    setLoading(true);
    setError('');
    try {
      const data = await login(email, password);
      if (data.newLocationDetected) {
        // Show warning but still navigate — LocationsPage will show the banner
      }
      navigate('/files');
    } catch (err) {
      const status = err.response?.status;
      const msg    = err.response?.data?.message || err.response?.data || '';

      if (status === 403 || (typeof msg === 'string' && msg.toLowerCase().includes('decoy'))) {
        navigate('/decoy');
        return;
      }
      const remaining = err.response?.data?.remainingAttempts;
      if (remaining !== undefined) {
        setError(`Invalid credentials. ${remaining} attempt(s) remaining before security lockout.`);
      } else {
        setError(typeof msg === 'string' ? msg : 'Invalid email or password.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e) => { if (e.key === 'Enter') handleLogin(); };

  return (
    <div style={s.wrap}>
      <div style={s.card}>
        <div style={s.logoRow}>
          <div style={s.logoIcon}>
            <svg viewBox="0 0 24 24" fill="white" width="20" height="20">
              <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z" />
            </svg>
          </div>
          <div>
            <div style={s.logoText}>VAULTLOCK</div>
            <div style={s.logoSub}>Secure File System</div>
          </div>
        </div>

        <div style={s.title}>Sign in</div>
        <div style={s.subtitle}>Access your secure file vault</div>

        {error && (
          <div style={s.errorBox}>⚠ {error}</div>
        )}

        <div style={s.field}>
          <label style={s.label}>Email</label>
          <input
            type="email" value={email} placeholder="you@company.com"
            onChange={e => setEmail(e.target.value)} onKeyDown={handleKeyDown}
            style={s.input}
          />
        </div>

        <div style={s.field}>
          <label style={s.label}>Password</label>
          <input
            type="password" value={password} placeholder="••••••••"
            onChange={e => setPassword(e.target.value)} onKeyDown={handleKeyDown}
            style={s.input}
          />
        </div>

        <button style={s.btn} onClick={handleLogin} disabled={loading}>
          {loading ? 'Signing in...' : 'Sign in →'}
        </button>

        <div style={s.switchRow}>
          No account?{' '}
          <span style={s.link} onClick={() => navigate('/register')}>Create one</span>
        </div>
      </div>
    </div>
  );
}

const s = {
  wrap: { display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '100vh', padding: '2rem', background: 'var(--bg-tertiary)' },
  card: { background: '#ffffff', border: '0.5px solid rgba(0,0,0,0.08)', borderRadius: '16px', padding: '2.5rem', width: '100%', maxWidth: '420px' },
  logoRow: { display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '2rem' },
  logoIcon: { width: '36px', height: '36px', background: '#0C447C', borderRadius: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center' },
  logoText: { fontFamily: 'var(--mono)', fontSize: '14px', fontWeight: '700', color: 'var(--text-primary)', letterSpacing: '-0.5px' },
  logoSub: { fontSize: '11px', color: 'var(--text-secondary)' },
  title: { fontFamily: 'var(--mono)', fontSize: '22px', fontWeight: '700', color: 'var(--text-primary)', marginBottom: '4px' },
  subtitle: { fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '1.5rem' },
  errorBox: { background: '#FCEBEB', color: '#A32D2D', padding: '10px 14px', borderRadius: '8px', fontSize: '13px', marginBottom: '1rem' },
  field: { marginBottom: '1rem' },
  label: { display: 'block', fontSize: '11px', fontWeight: '500', color: 'var(--text-secondary)', marginBottom: '6px', textTransform: 'uppercase', letterSpacing: '0.5px' },
  input: { width: '100%', padding: '10px 12px', border: '0.5px solid rgba(0,0,0,0.12)', borderRadius: '8px', fontSize: '14px', background: '#ffffff', color: 'var(--text-primary)' },
  btn: { width: '100%', padding: '11px', background: '#0C447C', color: 'white', border: 'none', borderRadius: '8px', fontSize: '14px', fontWeight: '500', cursor: 'pointer', marginTop: '0.5rem' },
  switchRow: { textAlign: 'center', marginTop: '1.5rem', fontSize: '13px', color: 'var(--text-secondary)' },
  link: { color: '#185FA5', cursor: 'pointer', fontWeight: '500' },
};
