import { useEffect, useState } from 'react';

export default function DecoyPage() {
  const [sessionId] = useState(() => Math.random().toString(36).substring(2, 18).toUpperCase());
  const [dots, setDots] = useState('');

  useEffect(() => {
    const timer = setInterval(() => {
      setDots(d => d.length >= 3 ? '' : d + '.');
    }, 500);
    return () => clearInterval(timer);
  }, []);

  return (
    <div style={s.wrap}>
      <div style={s.card}>
        <div style={s.spinner} />
        <div style={s.title}>Verifying your identity{dots}</div>
        <div style={s.sub}>
          Please wait while we validate your credentials and establish a secure session.
        </div>
        <div style={s.sessionId}>Session ID: {sessionId}</div>
        <div style={s.steps}>
          <div style={s.step}>
            <div style={{ ...s.stepDot, background: '#3B6D11' }} />
            Authenticating credentials
          </div>
          <div style={s.step}>
            <div style={{ ...s.stepDot, background: '#185FA5', animation: 'pulse 1.5s infinite' }} />
            Establishing secure tunnel
          </div>
          <div style={s.step}>
            <div style={{ ...s.stepDot, background: '#D3D1C7' }} />
            Loading your workspace
          </div>
        </div>
      </div>
      <style>{`
        @keyframes spin { to { transform: rotate(360deg); } }
        @keyframes pulse { 0%,100%{opacity:1} 50%{opacity:0.3} }
      `}</style>
    </div>
  );
}

const s = {
  wrap: { display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '100vh', background: '#ffffff' },
  card: { maxWidth: '380px', width: '100%', padding: '2.5rem', border: '0.5px solid rgba(0,0,0,0.08)', borderRadius: '16px', textAlign: 'center' },
  spinner: { width: '40px', height: '40px', border: '2px solid rgba(0,0,0,0.08)', borderTopColor: '#378ADD', borderRadius: '50%', animation: 'spin 1s linear infinite', margin: '0 auto 1.5rem' },
  title: { fontFamily: 'var(--mono)', fontSize: '16px', fontWeight: '700', color: 'var(--text-primary)', marginBottom: '8px' },
  sub: { fontSize: '13px', color: 'var(--text-secondary)', lineHeight: '1.6' },
  sessionId: { marginTop: '1.5rem', fontSize: '11px', color: 'var(--text-tertiary)', fontFamily: 'var(--mono)' },
  steps: { marginTop: '1.5rem', textAlign: 'left', display: 'flex', flexDirection: 'column', gap: '10px' },
  step: { display: 'flex', alignItems: 'center', gap: '10px', fontSize: '12px', color: 'var(--text-secondary)' },
  stepDot: { width: '8px', height: '8px', borderRadius: '50%', flexShrink: 0 },
};
