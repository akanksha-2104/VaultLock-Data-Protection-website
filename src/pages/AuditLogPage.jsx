import { useEffect, useState } from 'react';
import api from '../api/axiosInstance';
import { AppLayout, AlertBanner, StatCard, SectionCard, PageHeader } from '../components/UI';

const DOT_COLORS = {
  LOGIN:          '#3B6D11',
  REGISTER:       '#3B6D11',
  UPLOAD:         '#185FA5',
  DOWNLOAD:       '#854F0B',
  VIEW:           '#854F0B',
  EDIT:           '#533AB7',
  DELETE:         '#A32D2D',
  LOGIN_FAILED:   '#A32D2D',
  DECOY_REDIRECT: '#533AB7',
};

function formatAction(action = '') {
  return action.replace(/_/g, ' ').toLowerCase().replace(/^\w/, c => c.toUpperCase());
}

function formatTime(ts) {
  if (!ts) return '—';
  const d = new Date(ts);
  const now = new Date();
  const diffMs = now - d;
  const diffH = diffMs / 3600000;
  if (diffH < 24) return d.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' });
  if (diffH < 48) return 'Yesterday ' + d.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' });
  return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short' });
}

export default function AuditLogPage() {
  const [logs, setLogs]     = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError]   = useState('');

  useEffect(() => {
    api.get('/api/audit-logs')
      .then(res => {
        setLogs(Array.isArray(res.data) ? res.data : []);
        setError('');
      })
      .catch(err => {
        if (err.response?.status !== 401) {
          setError('Failed to load audit logs. Make sure the backend is running.');
        }
      })
      .finally(() => setLoading(false));
  }, []);

  const counts = {
    total:    logs.length,
    logins:   logs.filter(l => l.action === 'LOGIN').length,
    failed:   logs.filter(l => l.action === 'LOGIN_FAILED').length,
    decoy:    logs.filter(l => l.action === 'DECOY_REDIRECT').length,
  };

  return (
    <AppLayout>
      <PageHeader title="Audit Log" subtitle="Every action taken in your account is recorded here" />

      {error && <AlertBanner type="danger">{error}</AlertBanner>}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '10px', marginBottom: '1.5rem' }}>
        <StatCard label="Total events"    value={counts.total}  />
        <StatCard label="Logins"          value={counts.logins} badge="Successful"  badgeType="success" />
        <StatCard label="Failed logins"   value={counts.failed} badge="Suspicious"  badgeType="danger" />
        <StatCard label="Decoy triggers"  value={counts.decoy}  badge="Security"    badgeType="warning" />
      </div>

      <SectionCard header={<span style={{ fontSize: '13px', fontWeight: '500' }}>Activity timeline</span>}>
        {loading ? (
          <div style={{ padding: '2rem', textAlign: 'center', fontSize: '13px', color: 'var(--text-secondary)' }}>Loading...</div>
        ) : logs.length === 0 ? (
          <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-secondary)', fontSize: '13px' }}>No activity recorded yet.</div>
        ) : (
          logs.map(log => (
            <div key={log.id} style={s.row}>
              <div style={{ ...s.dot, background: DOT_COLORS[log.action] || '#888780' }} />
              <div style={{ flex: 1 }}>
                <div style={s.action}>
                  {formatAction(log.action)}
                  {log.filename && (
                    <span style={{ color: 'var(--text-secondary)', fontWeight: '400' }}> — {log.filename}</span>
                  )}
                </div>
                <div style={s.meta}>IP: {log.ipAddress}</div>
              </div>
              <div style={s.time}>{formatTime(log.timestamp)}</div>
            </div>
          ))
        )}
      </SectionCard>
    </AppLayout>
  );
}

const s = {
  row: { display: 'flex', alignItems: 'flex-start', gap: '12px', padding: '10px 1.25rem', borderTop: '0.5px solid rgba(0,0,0,0.06)' },
  dot: { width: '8px', height: '8px', borderRadius: '50%', marginTop: '5px', flexShrink: 0 },
  action: { fontSize: '13px', fontWeight: '500', color: 'var(--text-primary)' },
  meta: { fontSize: '11px', color: 'var(--text-secondary)', marginTop: '2px' },
  time: { fontFamily: 'var(--mono)', fontSize: '11px', color: 'var(--text-tertiary)', marginLeft: 'auto', flexShrink: 0 },
};
