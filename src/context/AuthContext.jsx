import { createContext, useContext, useState, useEffect } from 'react';
import api, { setCredentials, clearCredentials } from '../api/axiosInstance';

const AuthContext = createContext(null);

// ── Credential persistence ────────────────────────────────────────────────────
// We store credentials in sessionStorage (not localStorage) so they are cleared
// when the browser tab closes, reducing exposure. The email is safe to store.
// The password is stored encoded — this is a trade-off needed because your
// backend uses HTTP Basic (stateless) rather than JWT or session cookies.
function saveCredentials(email, password) {
  sessionStorage.setItem('vl_email', email);
  sessionStorage.setItem('vl_pass', btoa(password)); // base64, not encryption
  setCredentials(email, password);
}

function loadCredentials() {
  const email = sessionStorage.getItem('vl_email');
  const pass  = sessionStorage.getItem('vl_pass');
  if (email && pass) {
    setCredentials(email, atob(pass));
    return { email, password: atob(pass) };
  }
  return null;
}

function removeCredentials() {
  sessionStorage.removeItem('vl_email');
  sessionStorage.removeItem('vl_pass');
  clearCredentials();
}

export function AuthProvider({ children }) {
  const [user, setUser]         = useState(() => {
    try {
      const stored = localStorage.getItem('vaultlock_user');
      return stored ? JSON.parse(stored) : null;
    } catch { return null; }
  });
  const [checking, setChecking] = useState(true);

  useEffect(() => {
    // Restore credentials into the axios interceptor on every page load/refresh
    // so requests made by other pages carry the Authorization header.
    loadCredentials();

    // Verify the session is still valid with the backend.
    api.get('/api/auth/me')
      .then(res => {
        setUser(res.data);
        localStorage.setItem('vaultlock_user', JSON.stringify(res.data));
      })
      .catch(() => {
        // Credentials rejected or backend restarted — force re-login.
        setUser(null);
        localStorage.removeItem('vaultlock_user');
        removeCredentials();
      })
      .finally(() => setChecking(false));
  }, []);

  const login = async (email, password) => {
    // Store credentials BEFORE making the login call so the request interceptor
    // attaches the Authorization header — login endpoint needs auth too.
    saveCredentials(email, password);
    try {
      const res = await api.post('/api/auth/login', { email, password });
      setUser(res.data);
      localStorage.setItem('vaultlock_user', JSON.stringify(res.data));
      return res.data;
    } catch (err) {
      // Login failed — remove credentials so bad creds aren't cached
      removeCredentials();
      throw err;
    }
  };

  const register = async (name, email, password) => {
    const res = await api.post('/api/auth/register', { name, email, password });
    return res.data;
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('vaultlock_user');
    removeCredentials();
  };

  const fetchProfile = async () => {
    const res = await api.get('/api/auth/me');
    setUser(res.data);
    localStorage.setItem('vaultlock_user', JSON.stringify(res.data));
    return res.data;
  };

  if (checking) {
    return (
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100vh', background: '#f1efe8' }}>
        <div style={{ textAlign: 'center' }}>
          <div style={{ width: '32px', height: '32px', border: '2px solid #d3d1c7', borderTopColor: '#0C447C', borderRadius: '50%', animation: 'spin 0.8s linear infinite', margin: '0 auto 12px' }} />
          <div style={{ fontSize: '13px', color: '#666663', fontFamily: 'DM Sans, sans-serif' }}>Restoring session...</div>
        </div>
        <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
      </div>
    );
  }

  return (
    <AuthContext.Provider value={{ user, login, register, logout, fetchProfile }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);