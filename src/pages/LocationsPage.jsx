import { useEffect, useState } from 'react';
import api from '../api/axiosInstance';
import { AppLayout, AlertBanner, SectionCard, PageHeader } from '../components/UI';

function formatTime(ts) {
  if (!ts) return '—';
  const d = new Date(ts);
  const now = new Date();
  const diffH = (now - d) / 3600000;
  if (diffH < 24) return 'Today ' + d.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' });
  if (diffH < 48) return 'Yesterday ' + d.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' });
  return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

export default function LocationsPage() {
  const [locations, setLocations] = useState([]);
  const [loading, setLoading]     = useState(true);
  const [error, setError]         = useState('');

  useEffect(() => {
    api.get('/api/login-locations')
      .then(res => {
        setLocations(Array.isArray(res.data) ? res.data : []);
        setError('');
      })
      .catch(err => {
        if (err.response?.status !== 401) {
          setError('Failed to load login history. Make sure the backend is running.');
        }
      })
      .finally(() => setLoading(false));
  }, []);

  const hasNewLocation = locations.some(l => l.newLocationDetected);

  return (
    <AppLayout>
      <PageHeader title="Login History" subtitle="Track where and when your account was accessed" />

      {error && <AlertBanner type="danger">{error}</AlertBanner>}
      {hasNewLocation && (
        <AlertBanner type="warning">
          ⚠ A login was detected from a new location. If this wasn't you, change your password immediately.
        </AlertBanner>
      )}

      <SectionCard header={<span style={{ fontSize: '13px', fontWeight: '500' }}>Recent logins ({locations.length})</span>}>
        {loading ? (
          <div style={{ padding: '2rem', textAlign: 'center', fontSize: '13px', color: 'var(--text-secondary)' }}>Loading...</div>
        ) : locations.length === 0 ? (
          <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-secondary)', fontSize: '13px' }}>No login history found.</div>
        ) : (
          locations.map(loc => (
            <div key={loc.id} style={s.row}>
              <div style={s.icon}>
                <svg viewBox="0 0 24 24" fill="none" stroke="#0C447C" strokeWidth="2" width="14" height="14">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" />
                  <circle cx="12" cy="10" r="3" />
                </svg>
              </div>
              <div style={{ flex: 1 }}>
                <div style={s.location}>{loc.location || 'Unknown location'}</div>
                <div style={s.meta}>{loc.ipAddress} · {loc.deviceInfo}</div>
                {loc.previousLocation && (
                  <div style={{ ...s.meta, marginTop: '2px' }}>Previous: {loc.previousLocation}</div>
                )}
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '6px', flexShrink: 0 }}>
                <div style={s.time}>{formatTime(loc.loginTime)}</div>
                {loc.newLocationDetected && (
                  <div style={s.newBadge}>New location</div>
                )}
              </div>
            </div>
          ))
        )}
      </SectionCard>
    </AppLayout>
  );
}

const s = {
  row: { display: 'flex', alignItems: 'flex-start', gap: '12px', padding: '12px 1.25rem', borderTop: '0.5px solid rgba(0,0,0,0.06)' },
  icon: { width: '32px', height: '32px', borderRadius: '8px', background: '#E6F1FB', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 },
  location: { fontSize: '13px', fontWeight: '500', color: 'var(--text-primary)' },
  meta: { fontSize: '11px', color: 'var(--text-secondary)', marginTop: '2px' },
  time: { fontFamily: 'var(--mono)', fontSize: '11px', color: 'var(--text-tertiary)' },
  newBadge: { background: '#FAEEDA', color: '#854F0B', fontSize: '10px', padding: '2px 8px', borderRadius: '20px', fontWeight: '500' },
};
