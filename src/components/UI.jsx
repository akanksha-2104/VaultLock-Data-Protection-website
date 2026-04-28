/* ── AlertBanner ── */
export function AlertBanner({ type = 'info', children }) {
  const colors = {
    success: { bg: '#EAF3DE', color: '#3B6D11' },
    warning: { bg: '#FAEEDA', color: '#633806' },
    danger:  { bg: '#FCEBEB', color: '#A32D2D' },
    info:    { bg: '#E6F1FB', color: '#0C447C' },
  };
  const c = colors[type] || colors.info;
  return (
    <div style={{ padding: '10px 16px', borderRadius: '8px', fontSize: '13px', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '8px', background: c.bg, color: c.color }}>
      {children}
    </div>
  );
}

/* ── StatCard ── */
export function StatCard({ label, value, badge, badgeType = 'info' }) {
  const badgeColors = {
    info:    { bg: '#E6F1FB', color: '#0C447C' },
    success: { bg: '#EAF3DE', color: '#3B6D11' },
    warning: { bg: '#FAEEDA', color: '#854F0B' },
    danger:  { bg: '#FCEBEB', color: '#A32D2D' },
    neutral: { bg: '#F1EFE8', color: '#444441' },
  };
  const bc = badgeColors[badgeType] || badgeColors.info;
  return (
    <div style={{ background: '#f7f7f5', borderRadius: '10px', padding: '1rem' }}>
      <div style={{ fontSize: '11px', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.5px', fontWeight: '500' }}>{label}</div>
      <div style={{ fontFamily: 'var(--mono)', fontSize: '22px', fontWeight: '700', color: 'var(--text-primary)', marginTop: '4px' }}>{value}</div>
      {badge && (
        <div style={{ display: 'inline-block', fontSize: '10px', padding: '2px 7px', borderRadius: '20px', marginTop: '4px', background: bc.bg, color: bc.color }}>{badge}</div>
      )}
    </div>
  );
}

/* ── TagPill ── */
export function TagPill({ tag }) {
  const map = {
    PRIVATE:    { bg: '#F1EFE8', color: '#444441' },
    PUBLIC:     { bg: '#EAF3DE', color: '#3B6D11' },
    RESTRICTED: { bg: '#FAEEDA', color: '#854F0B' },
  };
  const c = map[tag] || map.PRIVATE;
  return (
    <span style={{ display: 'inline-flex', alignItems: 'center', padding: '2px 8px', borderRadius: '20px', fontSize: '11px', fontWeight: '500', background: c.bg, color: c.color }}>
      {tag}
    </span>
  );
}

/* ── SectionCard ── */
export function SectionCard({ header, children }) {
  return (
    <div style={{ background: '#ffffff', border: '0.5px solid rgba(0,0,0,0.08)', borderRadius: '12px', overflow: 'hidden', marginBottom: '1.25rem' }}>
      {header && (
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '1rem 1.25rem', borderBottom: '0.5px solid rgba(0,0,0,0.08)' }}>
          {header}
        </div>
      )}
      {children}
    </div>
  );
}

/* ── IconButton ── */
export function IconButton({ onClick, title, children, danger = false }) {
  return (
    <button
      onClick={onClick}
      title={title}
      style={{
        width: '28px', height: '28px',
        border: '0.5px solid rgba(0,0,0,0.1)',
        borderRadius: '6px', background: 'transparent',
        cursor: 'pointer', display: 'flex',
        alignItems: 'center', justifyContent: 'center',
        color: danger ? '#A32D2D' : 'var(--text-secondary)',
      }}
    >
      {children}
    </button>
  );
}

/* ── PrimaryButton ── */
export function PrimaryButton({ onClick, children, style = {} }) {
  return (
    <button
      onClick={onClick}
      style={{
        padding: '9px 16px', background: '#0C447C', color: 'white',
        border: 'none', borderRadius: '8px', fontSize: '13px',
        fontWeight: '500', cursor: 'pointer', ...style,
      }}
    >
      {children}
    </button>
  );
}

/* ── SecondaryButton ── */
export function SecondaryButton({ onClick, children, style = {} }) {
  return (
    <button
      onClick={onClick}
      style={{
        padding: '9px 16px', background: 'transparent', color: 'var(--text-secondary)',
        border: '0.5px solid rgba(0,0,0,0.1)', borderRadius: '8px', fontSize: '13px',
        fontWeight: '400', cursor: 'pointer', ...style,
      }}
    >
      {children}
    </button>
  );
}

/* ── PageHeader ── */
export function PageHeader({ title, subtitle }) {
  return (
    <div style={{ marginBottom: '1.5rem' }}>
      <div style={{ fontFamily: 'var(--mono)', fontSize: '18px', fontWeight: '700', color: 'var(--text-primary)' }}>{title}</div>
      {subtitle && <div style={{ fontSize: '13px', color: 'var(--text-secondary)', marginTop: '2px' }}>{subtitle}</div>}
    </div>
  );
}

/* ── AppLayout ── */
import Sidebar from './Sidebar';
export function AppLayout({ children }) {
  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      <Sidebar />
      <main style={{ flex: 1, padding: '1.5rem', overflowY: 'auto' }}>
        {children}
      </main>
    </div>
  );
}
