// components.jsx — AuditForge UI Primitives
const { useState, useEffect, useRef, useCallback, createContext, useContext } = React;

// ─── ICONS ───────────────────────────────────────────────────────────────────
const ICONS = {
  shield: `<path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>`,
  'shield-check': `<path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/><polyline points="9 12 11 14 15 10"/>`,
  search: `<circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>`,
  user: `<path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>`,
  plus: `<path d="M12 5v14M5 12h14"/>`,
  x: `<path d="M18 6 6 18M6 6l12 12"/>`,
  check: `<path d="M20 6 9 17l-5-5"/>`,
  'chevron-down': `<path d="m6 9 6 6 6-6"/>`,
  'chevron-right': `<path d="m9 18 6-6-6-6"/>`,
  'chevron-left': `<path d="m15 18-6-6 6-6"/>`,
  'chevron-up': `<path d="m18 15-6-6-6 6"/>`,
  'more-horizontal': `<circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/><circle cx="5" cy="12" r="1"/>`,
  settings: `<path d="M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z"/><circle cx="12" cy="12" r="3"/>`,
  'upload-cloud': `<polyline points="16 16 12 12 8 16"/><line x1="12" y1="12" x2="12" y2="21"/><path d="M20.39 18.39A5 5 0 0 0 18 9h-1.26A8 8 0 1 0 3 16.3"/>`,
  download: `<path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/>`,
  trash: `<path d="M3 6h18"/><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/>`,
  filter: `<polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/>`,
  'alert-triangle': `<path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"/><path d="M12 9v4"/><path d="M12 17h.01"/>`,
  'alert-circle': `<circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12" y2="16.01"/>`,
  'check-circle': `<path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/>`,
  'x-circle': `<circle cx="12" cy="12" r="10"/><path d="m15 9-6 6"/><path d="m9 9 6 6"/>`,
  info: `<circle cx="12" cy="12" r="10"/><path d="M12 16v-4"/><path d="M12 8h.01"/>`,
  lock: `<rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>`,
  eye: `<path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/>`,
  file: `<path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z"/><polyline points="14 2 14 8 20 8"/>`,
  'file-code': `<path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z"/><polyline points="14 2 14 8 20 8"/><path d="m10 13-2 2 2 2"/><path d="m14 17 2-2-2-2"/>`,
  'file-text': `<path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><line x1="10" y1="9" x2="8" y2="9"/>`,
  database: `<ellipse cx="12" cy="5" rx="9" ry="3"/><path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"/><path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"/>`,
  cloud: `<path d="M18 10h-1.26A8 8 0 1 0 9 20h9a5 5 0 0 0 0-10z"/>`,
  code: `<polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/>`,
  activity: `<polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>`,
  'refresh-cw': `<path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8"/><path d="M21 3v5h-5"/><path d="M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16"/><path d="M8 16H3v5"/>`,
  play: `<polygon points="5 3 19 12 5 21 5 3"/>`,
  square: `<rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>`,
  'external-link': `<path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"/><polyline points="15 3 21 3 21 9"/><line x1="10" y1="14" x2="21" y2="3"/>`,
  copy: `<rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>`,
  clock: `<circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>`,
  history: `<path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/><path d="M12 7v5l4 2"/>`,
  'layout-dashboard': `<rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/>`,
  list: `<line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/>`,
  server: `<rect x="2" y="2" width="20" height="8" rx="2" ry="2"/><rect x="2" y="14" width="20" height="8" rx="2" ry="2"/><line x1="6" y1="6" x2="6.01" y2="6"/><line x1="6" y1="18" x2="6.01" y2="18"/>`,
  key: `<path d="m21 2-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0 3 3L22 7l-3-3m-3.5 3.5L19 4"/>`,
  'arrow-up-down': `<path d="m21 16-4 4-4-4"/><path d="M17 20V4"/><path d="m3 8 4-4 4 4"/><path d="M7 4v16"/>`,
  terminal: `<polyline points="4 17 10 11 4 5"/><line x1="12" y1="19" x2="20" y2="19"/>`,
  link: `<path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>`,
  zap: `<polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/>`,
  'arrow-left': `<path d="M19 12H5"/><path d="m12 19-7-7 7-7"/>`,
  'arrow-right': `<path d="M5 12h14"/><path d="m12 5 7 7-7 7"/>`,
  edit: `<path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>`,
  bell: `<path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/>`,
  'book-open': `<path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/>`,
  cpu: `<rect x="4" y="4" width="16" height="16" rx="2"/><rect x="9" y="9" width="6" height="6"/><line x1="9" y1="1" x2="9" y2="4"/><line x1="15" y1="1" x2="15" y2="4"/><line x1="9" y1="20" x2="9" y2="23"/><line x1="15" y1="20" x2="15" y2="23"/><line x1="20" y1="9" x2="23" y2="9"/><line x1="20" y1="14" x2="23" y2="14"/><line x1="1" y1="9" x2="4" y2="9"/><line x1="1" y1="14" x2="4" y2="14"/>`
};

const Icon = ({ name, size = 16, style, className }) => {
  const d = ICONS[name];
  if (!d) return <span style={{ display: 'inline-block', width: size, height: size }} />;
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none"
    stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"
    style={{ flexShrink: 0, display: 'inline-block', ...style }} className={className}
    dangerouslySetInnerHTML={{ __html: d }} />);

};

// ─── SPINNER ─────────────────────────────────────────────────────────────────
const Spinner = ({ size = 14, color = 'currentColor' }) =>
<svg width={size} height={size} viewBox="0 0 24 24" fill="none"
stroke={color} strokeWidth="2.5" strokeLinecap="round"
className="animate-spin" style={{ flexShrink: 0 }}>
    <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83" />
  </svg>;


// ─── BUTTON ──────────────────────────────────────────────────────────────────
const variantMap = {
  primary: { bg: 'var(--accent-default)', color: 'var(--text-on-accent)', border: 'transparent', hoverBg: 'var(--accent-hover)' },
  secondary: { bg: 'transparent', color: 'var(--accent-default)', border: 'var(--accent-default)', hoverBg: 'var(--accent-subtle)' },
  ghost: { bg: 'transparent', color: 'var(--text-secondary)', border: 'transparent', hoverBg: 'var(--surface-hover)' },
  destructive: { bg: 'var(--severity-critical)', color: '#fff', border: 'transparent', hoverBg: '#e03d36' },
  outline: { bg: 'transparent', color: 'var(--text-primary)', border: 'var(--border-default)', hoverBg: 'var(--surface-hover)' }
};

const Btn = ({ children, variant = 'primary', size = 'md', icon, iconRight, loading = false, disabled = false, onClick, type = 'button', style, fullWidth }) => {
  const [hover, setHover] = useState(false);
  const v = variantMap[variant] || variantMap.primary;
  const pad = size === 'sm' ? '5px 10px' : size === 'lg' ? '9px 20px' : '6px 14px';
  const fs = size === 'sm' ? 'var(--text-sm)' : 'var(--text-base)';
  return (
    <button type={type} disabled={disabled || loading} onClick={onClick}
    onMouseEnter={() => setHover(true)} onMouseLeave={() => setHover(false)}
    style={{ ...{
        display: 'inline-flex', alignItems: 'center', gap: 'var(--sp-2)', fontFamily: 'var(--font-sans)',
        fontWeight: 500, fontSize: fs, lineHeight: 1, borderRadius: 'var(--radius-button)',
        border: `1px solid ${v.border}`, cursor: disabled || loading ? 'not-allowed' : 'pointer',
        opacity: disabled ? 0.45 : 1, padding: pad, whiteSpace: 'nowrap',
        background: hover && !disabled ? v.hoverBg : v.bg,
        color: v.color, transition: 'var(--transition)', outline: 'none',
        width: fullWidth ? '100%' : undefined, justifyContent: fullWidth ? 'center' : undefined,
        ...style, borderColor: "rgb(80, 72, 72)"
      }, color: "rgb(255, 255, 255)" }}>
      {loading ? <Spinner size={13} /> : icon ? <Icon name={icon} size={13} /> : null}
      {children}
      {iconRight && !loading ? <Icon name={iconRight} size={13} /> : null}
    </button>);

};

// ─── SEVERITY BADGE ──────────────────────────────────────────────────────────
const SEVERITY_CONFIG = {
  critical: { label: 'Critical', color: 'var(--severity-critical-text)', bg: 'var(--severity-critical-bg)' },
  high: { label: 'High', color: 'var(--severity-high-text)', bg: 'var(--severity-high-bg)' },
  medium: { label: 'Medium', color: 'var(--severity-medium-text)', bg: 'var(--severity-medium-bg)' },
  low: { label: 'Low', color: 'var(--severity-low-text)', bg: 'var(--severity-low-bg)' },
  resolved: { label: 'Resolved', color: 'var(--severity-resolved-text)', bg: 'var(--severity-resolved-bg)' }
};

const SeverityBadge = ({ level, dot = true }) => {
  const c = SEVERITY_CONFIG[level] || SEVERITY_CONFIG.low;
  return (
    <span style={{ display: 'inline-flex', alignItems: 'center', gap: 5,
      background: c.bg, color: c.color, fontSize: 'var(--text-sm)', fontWeight: 500,
      padding: '2px 8px', borderRadius: 'var(--radius-badge)', whiteSpace: 'nowrap' }}>
      {dot && <span style={{ width: 6, height: 6, borderRadius: '50%', background: c.color, flexShrink: 0 }} />}
      {c.label}
    </span>);

};

const SeverityDot = ({ level }) => {
  const c = SEVERITY_CONFIG[level] || SEVERITY_CONFIG.low;
  return <span style={{ display: 'inline-block', width: 8, height: 8, borderRadius: '50%', background: c.color, flexShrink: 0 }} />;
};

// Severity count chips (e.g. "3 critical · 7 high")
const SeverityChips = ({ counts }) =>
<div style={{ display: 'flex', gap: 'var(--sp-1)', flexWrap: 'wrap' }}>
    {['critical', 'high', 'medium', 'low'].map((l) => counts[l] > 0 &&
  <span key={l} style={{
    display: 'inline-flex', alignItems: 'center', gap: 4,
    background: `var(--severity-${l}-bg)`, color: `var(--severity-${l}-text)`,
    fontSize: 'var(--text-xs)', fontWeight: 500, padding: '2px 6px',
    borderRadius: 'var(--radius-badge)', whiteSpace: 'nowrap'
  }}>
        <span style={{ width: 5, height: 5, borderRadius: '50%', background: `var(--severity-${l}-text)`, flexShrink: 0 }} />
        {counts[l]}
      </span>
  )}
  </div>;


// ─── STATUS BADGE ────────────────────────────────────────────────────────────
const STATUS_CONFIG = {
  created: { label: 'Created', color: 'var(--status-neutral)', bg: 'var(--status-neutral-bg)' },
  scanning: { label: 'Scanning', color: 'var(--status-info)', bg: 'var(--status-info-bg)' },
  completed: { label: 'Completed', color: 'var(--status-success)', bg: 'var(--status-success-bg)' },
  failed: { label: 'Failed', color: 'var(--status-error)', bg: 'var(--status-error-bg)' },
  open: { label: 'Open', color: 'var(--status-error)', bg: 'var(--status-error-bg)' },
  acknowledged: { label: 'Acknowledged', color: 'var(--status-warning)', bg: 'var(--status-warning-bg)' },
  false_positive: { label: 'False positive', color: 'var(--status-neutral)', bg: 'var(--status-neutral-bg)' },
  resolved: { label: 'Resolved', color: 'var(--status-success)', bg: 'var(--status-success-bg)' }
};

const StatusBadge = ({ status }) => {
  const c = STATUS_CONFIG[status] || STATUS_CONFIG.open;
  return (
    <span style={{ display: 'inline-flex', alignItems: 'center', gap: 5,
      background: c.bg, color: c.color, fontSize: 'var(--text-sm)', fontWeight: 500,
      padding: '2px 8px', borderRadius: 'var(--radius-badge)', whiteSpace: 'nowrap' }}>
      {status === 'scanning' && <span className="animate-pulse-opacity" style={{ width: 5, height: 5, borderRadius: '50%', background: c.color }} />}
      {c.label}
    </span>);

};

// Score badge (color-coded by value)
const ScoreBadge = ({ score, large }) => {
  if (score == null) return <span style={{ color: 'var(--text-tertiary)', fontSize: 'var(--text-sm)' }}>—</span>;
  const color = score >= 80 ? 'var(--status-success)' : score >= 50 ? 'var(--status-warning)' : 'var(--status-error)';
  const bg = score >= 80 ? 'var(--status-success-bg)' : score >= 50 ? 'var(--status-warning-bg)' : 'var(--status-error-bg)';
  return (
    <span style={{ display: 'inline-flex', alignItems: 'baseline', gap: 2,
      background: bg, color, fontWeight: 600, borderRadius: 'var(--radius-badge)', padding: large ? '4px 10px' : '2px 8px',
      fontSize: large ? 'var(--text-lg)' : 'var(--text-base)' }}>
      {score}<span style={{ fontSize: 'var(--text-sm)', fontWeight: 400 }}>/100</span>
    </span>);

};

// ─── FORM INPUTS ─────────────────────────────────────────────────────────────
const inputBase = (focus, error) => ({
  width: '100%', fontFamily: 'var(--font-sans)', fontSize: 'var(--text-base)',
  background: 'var(--surface-sunken)', color: 'var(--text-primary)',
  border: `1px solid ${error ? 'var(--status-error)' : focus ? 'var(--border-focus)' : 'var(--border-default)'}`,
  borderRadius: 'var(--radius-input)', padding: '6px 10px', outline: 'none',
  transition: 'border-color var(--transition)', lineHeight: '1.5',
  boxShadow: focus ? '0 0 0 3px var(--accent-subtle)' : 'none'
});

const Input = ({ label, error, hint, icon, type = 'text', value, onChange, placeholder, disabled, autoFocus, name }) => {
  const [focus, setFocus] = useState(false);
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--sp-1)' }}>
      {label && <label style={{ fontSize: 'var(--text-sm)', fontWeight: 500, color: 'var(--text-secondary)' }}>{label}</label>}
      <div style={{ position: 'relative' }}>
        {icon && <span style={{ position: 'absolute', left: 10, top: '50%', transform: 'translateY(-50%)', color: 'var(--text-tertiary)', pointerEvents: 'none' }}>
          <Icon name={icon} size={14} />
        </span>}
        <input type={type} value={value} onChange={onChange} placeholder={placeholder}
        disabled={disabled} autoFocus={autoFocus} name={name}
        onFocus={() => setFocus(true)} onBlur={() => setFocus(false)}
        style={{ ...inputBase(focus, error), paddingLeft: icon ? 32 : 10 }} />
      </div>
      {error && <span style={{ fontSize: 'var(--text-sm)', color: 'var(--status-error)' }}>{error}</span>}
      {hint && !error && <span style={{ fontSize: 'var(--text-sm)', color: 'var(--text-tertiary)' }}>{hint}</span>}
    </div>);

};

const Textarea = ({ label, error, value, onChange, placeholder, rows = 3, disabled }) => {
  const [focus, setFocus] = useState(false);
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--sp-1)' }}>
      {label && <label style={{ fontSize: 'var(--text-sm)', fontWeight: 500, color: 'var(--text-secondary)' }}>{label}</label>}
      <textarea value={value} onChange={onChange} placeholder={placeholder} rows={rows} disabled={disabled}
      onFocus={() => setFocus(true)} onBlur={() => setFocus(false)}
      style={{ ...inputBase(focus, error), resize: 'vertical', fontFamily: 'var(--font-sans)' }} />
      {error && <span style={{ fontSize: 'var(--text-sm)', color: 'var(--status-error)' }}>{error}</span>}
    </div>);

};

const SelectInput = ({ label, value, onChange, options, disabled }) => {
  const [focus, setFocus] = useState(false);
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--sp-1)' }}>
      {label && <label style={{ fontSize: 'var(--text-sm)', fontWeight: 500, color: 'var(--text-secondary)' }}>{label}</label>}
      <select value={value} onChange={onChange} disabled={disabled}
      onFocus={() => setFocus(true)} onBlur={() => setFocus(false)}
      style={{ ...inputBase(focus, false), appearance: 'none', cursor: 'pointer',
        backgroundImage: `url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%238B949E' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='m6 9 6 6 6-6'/%3E%3C/svg%3E")`,
        backgroundRepeat: 'no-repeat', backgroundPosition: 'right 10px center', paddingRight: 32 }}>
        {options.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
      </select>
    </div>);

};

const Checkbox = ({ label, checked, onChange, indeterminate }) => {
  const ref = useRef(null);
  useEffect(() => {if (ref.current) ref.current.indeterminate = !!indeterminate;}, [indeterminate]);
  return (
    <label style={{ display: 'inline-flex', alignItems: 'center', gap: 'var(--sp-2)', cursor: 'pointer', fontSize: 'var(--text-base)', color: 'var(--text-primary)', userSelect: 'none' }}>
      <input ref={ref} type="checkbox" checked={checked} onChange={onChange}
      style={{ width: 14, height: 14, accentColor: 'var(--accent-default)', cursor: 'pointer' }} />
      {label}
    </label>);

};

const Radio = ({ label, value, checked, onChange, name }) =>
<label style={{ display: 'inline-flex', alignItems: 'center', gap: 'var(--sp-2)', cursor: 'pointer', fontSize: 'var(--text-base)', color: 'var(--text-primary)', userSelect: 'none' }}>
    <input type="radio" value={value} checked={checked} onChange={onChange} name={name}
  style={{ width: 14, height: 14, accentColor: 'var(--accent-default)', cursor: 'pointer' }} />
    {label}
  </label>;


const Toggle = ({ label, checked, onChange }) =>
<label style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 'var(--sp-4)', cursor: 'pointer', userSelect: 'none' }}>
    <span style={{ fontSize: 'var(--text-base)', color: 'var(--text-primary)' }}>{label}</span>
    <div onClick={onChange} style={{
    width: 36, height: 20, borderRadius: 10, cursor: 'pointer', position: 'relative',
    background: checked ? 'var(--accent-default)' : 'var(--border-strong)',
    transition: 'background var(--transition)', flexShrink: 0
  }}>
      <div style={{
      position: 'absolute', top: 2, left: checked ? 18 : 2,
      width: 16, height: 16, borderRadius: '50%', background: '#fff',
      transition: 'left var(--transition)', boxShadow: '0 1px 3px rgba(0,0,0,0.3)'
    }} />
    </div>
  </label>;


const SegmentedControl = ({ options, value, onChange }) =>
<div style={{ display: 'inline-flex', background: 'var(--surface-sunken)', borderRadius: 'var(--radius-button)', padding: 2, gap: 2 }}>
    {options.map((o) =>
  <button key={o.value} onClick={() => onChange(o.value)} style={{
    padding: '5px 12px', borderRadius: 4, border: 'none', cursor: 'pointer', fontFamily: 'var(--font-sans)',
    fontSize: 'var(--text-sm)', fontWeight: 500, transition: 'var(--transition)',
    background: value === o.value ? 'var(--surface-elevated)' : 'transparent',
    color: value === o.value ? 'var(--text-primary)' : 'var(--text-secondary)',
    boxShadow: value === o.value ? 'var(--shadow-sm)' : 'none'
  }}>{o.label}</button>
  )}
  </div>;


// ─── MODAL ───────────────────────────────────────────────────────────────────
const Modal = ({ open, onClose, title, children, width = 480, footer }) => {
  useEffect(() => {
    const handler = (e) => {if (e.key === 'Escape') onClose();};
    if (open) document.addEventListener('keydown', handler);
    return () => document.removeEventListener('keydown', handler);
  }, [open, onClose]);
  if (!open) return null;
  return (
    <div style={{ position: 'fixed', inset: 0, zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center',
      background: 'var(--surface-overlay)', backdropFilter: 'blur(2px)' }} onClick={(e) => {if (e.target === e.currentTarget) onClose();}}>
      <div className="animate-fade-in" style={{ background: 'var(--surface-elevated)', borderRadius: 'var(--radius-modal)',
        width: 'min(90vw,' + width + 'px)', maxHeight: '85vh', overflow: 'auto',
        border: '1px solid var(--border-strong)', boxShadow: 'var(--shadow-lg)' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          padding: 'var(--sp-4) var(--sp-6)', borderBottom: '1px solid var(--border-default)' }}>
          <h2 style={{ fontSize: 'var(--text-md)', fontWeight: 500, color: 'var(--text-primary)' }}>{title}</h2>
          <button onClick={onClose} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-tertiary)', padding: 4, borderRadius: 4, display: 'flex', alignItems: 'center' }}>
            <Icon name="x" size={16} />
          </button>
        </div>
        <div style={{ padding: 'var(--sp-6)' }}>{children}</div>
        {footer && <div style={{ padding: 'var(--sp-4) var(--sp-6)', borderTop: '1px solid var(--border-default)', display: 'flex', justifyContent: 'flex-end', gap: 'var(--sp-2)' }}>{footer}</div>}
      </div>
    </div>);

};

// ─── SLIDE-OVER ──────────────────────────────────────────────────────────────
const SlideOver = ({ open, onClose, title, children, footer, width = 480 }) => {
  useEffect(() => {
    const h = (e) => {if (e.key === 'Escape') onClose();};
    if (open) document.addEventListener('keydown', h);
    return () => document.removeEventListener('keydown', h);
  }, [open, onClose]);
  return (
    <div style={{ position: 'fixed', inset: 0, zIndex: 1000, display: 'flex', justifyContent: 'flex-end',
      background: open ? 'var(--surface-overlay)' : 'transparent',
      pointerEvents: open ? 'all' : 'none', transition: 'background var(--transition-slow)' }}
    onClick={(e) => {if (e.target === e.currentTarget) onClose();}}>
      <div style={{ background: 'var(--surface-elevated)', height: '100%', width: `min(95vw,${width}px)`,
        borderLeft: '1px solid var(--border-strong)', display: 'flex', flexDirection: 'column',
        transform: open ? 'translateX(0)' : 'translateX(100%)', transition: 'transform var(--transition-slow)',
        boxShadow: open ? 'var(--shadow-lg)' : 'none' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          padding: 'var(--sp-4) var(--sp-6)', borderBottom: '1px solid var(--border-default)', flexShrink: 0 }}>
          <h2 style={{ fontSize: 'var(--text-md)', fontWeight: 500 }}>{title}</h2>
          <button onClick={onClose} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-tertiary)', padding: 4, borderRadius: 4, display: 'flex', alignItems: 'center' }}>
            <Icon name="x" size={16} />
          </button>
        </div>
        <div style={{ flex: 1, overflowY: 'auto', padding: 'var(--sp-6)' }}>{children}</div>
        {footer && <div style={{ padding: 'var(--sp-4) var(--sp-6)', borderTop: '1px solid var(--border-default)', display: 'flex', justifyContent: 'flex-end', gap: 'var(--sp-2)', flexShrink: 0 }}>{footer}</div>}
      </div>
    </div>);

};

// Confirm modal (destructive actions)
const ConfirmModal = ({ open, onClose, onConfirm, title, message, confirmLabel = 'Delete', loading }) =>
<Modal open={open} onClose={onClose} title={title} width={400}
footer={<>
      <Btn variant="ghost" onClick={onClose}>Cancel</Btn>
      <Btn variant="destructive" onClick={onConfirm} loading={loading}>{confirmLabel}</Btn>
    </>}>
    <p style={{ color: 'var(--text-secondary)', lineHeight: 'var(--lh-body)' }}>{message}</p>
  </Modal>;


// ─── TOAST ───────────────────────────────────────────────────────────────────
const ToastCtx = createContext(null);
const useToast = () => useContext(ToastCtx);
const TOAST_ICONS = { success: 'check-circle', error: 'x-circle', warning: 'alert-triangle', info: 'info' };
const TOAST_COLORS = { success: 'var(--status-success)', error: 'var(--status-error)', warning: 'var(--status-warning)', info: 'var(--status-info)' };

const ToastProvider = ({ children }) => {
  const [toasts, setToasts] = useState([]);
  const add = useCallback((msg, type = 'info', duration = 4000) => {
    const id = Date.now();
    setToasts((t) => [...t, { id, msg, type }]);
    setTimeout(() => setToasts((t) => t.filter((x) => x.id !== id)), duration);
  }, []);
  return (
    <ToastCtx.Provider value={add}>
      {children}
      <div style={{ position: 'fixed', bottom: 20, right: 20, zIndex: 2000, display: 'flex', flexDirection: 'column', gap: 'var(--sp-2)', maxWidth: 380 }}>
        {toasts.map((t) =>
        <div key={t.id} className="animate-slide-in" style={{
          display: 'flex', alignItems: 'flex-start', gap: 'var(--sp-3)',
          background: 'var(--surface-elevated)', border: '1px solid var(--border-strong)',
          borderRadius: 'var(--radius-card)', padding: 'var(--sp-3) var(--sp-4)',
          boxShadow: 'var(--shadow-md)', color: 'var(--text-primary)'
        }}>
            <Icon name={TOAST_ICONS[t.type]} size={15} style={{ color: TOAST_COLORS[t.type], marginTop: 1 }} />
            <span style={{ fontSize: 'var(--text-base)', lineHeight: 'var(--lh-body)' }}>{t.msg}</span>
            <button onClick={() => setToasts((ts) => ts.filter((x) => x.id !== t.id))}
          style={{ marginLeft: 'auto', background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-tertiary)', padding: 0, display: 'flex', alignItems: 'center', flexShrink: 0 }}>
              <Icon name="x" size={13} />
            </button>
          </div>
        )}
      </div>
    </ToastCtx.Provider>);

};

// ─── CARD ────────────────────────────────────────────────────────────────────
const Card = ({ children, style, onClick, hover = true }) => {
  const [hov, setHov] = useState(false);
  return (
    <div onMouseEnter={() => hover && setHov(true)} onMouseLeave={() => hover && setHov(false)}
    onClick={onClick}
    style={{ background: 'var(--surface-secondary)', border: `1px solid ${hov && onClick ? 'var(--border-strong)' : 'var(--border-default)'}`,
      borderRadius: 'var(--radius-card)', cursor: onClick ? 'pointer' : 'default',
      transition: 'border-color var(--transition)', ...style }}>
      {children}
    </div>);

};

// ─── EMPTY STATE ─────────────────────────────────────────────────────────────
const EmptyState = ({ title, description, action }) =>
<div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
  padding: 'var(--sp-16) var(--sp-8)', gap: 'var(--sp-3)', textAlign: 'center' }}>
    <p style={{ fontSize: 'var(--text-md)', fontWeight: 500, color: 'var(--text-secondary)' }}>{title}</p>
    {description && <p style={{ fontSize: 'var(--text-base)', color: 'var(--text-tertiary)', maxWidth: 340, lineHeight: 'var(--lh-body)' }}>{description}</p>}
    {action && <div style={{ marginTop: 'var(--sp-2)' }}>{action}</div>}
  </div>;


// ─── SKELETON ────────────────────────────────────────────────────────────────
const Skeleton = ({ width, height = 14, radius = 4, style }) =>
<div style={{ width: width || '100%', height, borderRadius: radius,
  background: 'var(--border-default)', animation: 'pulse-opacity 1.5s ease-in-out infinite', ...style }} />;


// ─── CODE BLOCK ──────────────────────────────────────────────────────────────
const highlightLine = (line) => {
  let result = line.
  replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').
  replace(/(#[^\n]*|\/\/[^\n]*)$/g, '<span style="color:#6E7681;font-style:italic">$1</span>').
  replace(/("(?:[^"\\]|\\.)*"|'(?:[^'\\]|\\.)*'|`(?:[^`\\]|\\.)*`)/g, '<span style="color:#a5d6ff">$1</span>').
  replace(/\b(def|class|import|from|return|if|else|elif|for|while|with|as|and|or|not|in|is|None|True|False|async|await|try|except|raise|pass|break|continue|lambda|yield|const|let|var|function|=>|export|default|new|typeof|instanceof|null|undefined|true|false|void)\b/g, '<span style="color:#ff7b72">$1</span>').
  replace(/\b(\d+\.?\d*)\b/g, '<span style="color:#f2cc60">$1</span>');
  return result;
};

const CodeBlock = ({ code, language, highlightLines = [], copyable = true, lineNumbers = true, maxHeight = 300 }) => {
  const [copied, setCopied] = useState(false);
  const lines = (code || '').split('\n');
  const copy = () => {navigator.clipboard?.writeText(code);setCopied(true);setTimeout(() => setCopied(false), 1500);};
  return (
    <div style={{ position: 'relative', borderRadius: 'var(--radius-card)', border: '1px solid var(--border-default)',
      background: 'var(--surface-sunken)', fontFamily: 'var(--font-mono)', fontSize: 'var(--text-sm)', overflow: 'hidden' }}>
      {copyable && <button onClick={copy} style={{
        position: 'absolute', top: 8, right: 8, background: 'var(--surface-elevated)', border: '1px solid var(--border-default)',
        borderRadius: 4, cursor: 'pointer', color: 'var(--text-secondary)', padding: '3px 8px', fontSize: 'var(--text-xs)',
        display: 'flex', alignItems: 'center', gap: 4, zIndex: 1
      }}><Icon name="copy" size={11} />{copied ? 'Copied!' : 'Copy'}</button>}
      <div style={{ overflowY: 'auto', maxHeight, overflowX: 'auto' }}>
        <table style={{ borderCollapse: 'collapse', width: '100%' }}>
          <tbody>
            {lines.map((line, i) => {
              const isHl = highlightLines.includes(i + 1);
              return (
                <tr key={i} style={{ background: isHl ? 'rgba(248,81,73,0.12)' : 'transparent' }}>
                  {lineNumbers && <td style={{ userSelect: 'none', paddingLeft: 12, paddingRight: 10, color: 'var(--text-tertiary)', textAlign: 'right', minWidth: 36,
                    fontSize: 'var(--text-xs)', verticalAlign: 'top', paddingTop: 2, paddingBottom: 2,
                    borderRight: isHl ? '2px solid var(--severity-critical)' : '1px solid var(--border-default)' }}>
                    {i + 1}
                  </td>}
                  <td style={{ padding: '2px 16px', whiteSpace: 'pre', color: 'var(--text-primary)' }}
                  dangerouslySetInnerHTML={{ __html: highlightLine(line) }} />
                </tr>);

            })}
          </tbody>
        </table>
      </div>
    </div>);

};

// ─── BREADCRUMB ──────────────────────────────────────────────────────────────
const Breadcrumb = ({ items }) =>
<div style={{ display: 'flex', alignItems: 'center', gap: 'var(--sp-1)', fontSize: 'var(--text-sm)', color: 'var(--text-tertiary)' }}>
    {items.map((item, i) =>
  <React.Fragment key={i}>
        {i > 0 && <Icon name="chevron-right" size={12} style={{ opacity: 0.5 }} />}
        {item.onClick ?
    <button onClick={item.onClick} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-secondary)', padding: 0, fontFamily: 'var(--font-sans)', fontSize: 'var(--text-sm)', textDecoration: 'none' }}
    onMouseEnter={(e) => e.target.style.color = 'var(--text-primary)'} onMouseLeave={(e) => e.target.style.color = 'var(--text-secondary)'}>{item.label}</button> :
    <span style={{ color: i === items.length - 1 ? 'var(--text-primary)' : 'var(--text-secondary)' }}>{item.label}</span>}
      </React.Fragment>
  )}
  </div>;


// ─── TAB STRIP ───────────────────────────────────────────────────────────────
const TabStrip = ({ tabs, active, onChange }) =>
<div style={{ display: 'flex', borderBottom: '1px solid var(--border-default)', gap: 0 }}>
    {tabs.map((tab) => {
    const isActive = tab.id === active;
    return (
      <button key={tab.id} onClick={() => onChange(tab.id)} style={{
        padding: '10px 16px', background: 'none', border: 'none', cursor: 'pointer',
        fontFamily: 'var(--font-sans)', fontSize: 'var(--text-sm)', fontWeight: isActive ? 500 : 400,
        color: isActive ? 'var(--text-primary)' : 'var(--text-secondary)',
        borderBottom: isActive ? '2px solid var(--accent-default)' : '2px solid transparent',
        marginBottom: -1, transition: 'color var(--transition)', display: 'flex', alignItems: 'center', gap: 'var(--sp-2)'
      }}>
          {tab.icon && <Icon name={tab.icon} size={13} />}
          {tab.label}
          {tab.count != null && <span style={{ background: 'var(--border-strong)', borderRadius: 8, padding: '0 5px', fontSize: 'var(--text-xs)', color: 'var(--text-tertiary)', fontWeight: 400 }}>{tab.count}</span>}
        </button>);

  })}
  </div>;


// ─── FILTER CHIP ─────────────────────────────────────────────────────────────
const FilterChip = ({ label, onRemove }) =>
<span style={{ display: 'inline-flex', alignItems: 'center', gap: 'var(--sp-1)',
  background: 'var(--accent-subtle)', color: 'var(--accent-default)',
  fontSize: 'var(--text-sm)', fontWeight: 500, padding: '3px 8px',
  borderRadius: 'var(--radius-badge)', border: '1px solid var(--accent-default)' }}>
    {label}
    <button onClick={onRemove} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'inherit', padding: 0, display: 'flex', alignItems: 'center', marginLeft: 2 }}>
      <Icon name="x" size={11} />
    </button>
  </span>;


// ─── SEVERITY BAR ────────────────────────────────────────────────────────────
const SeverityBar = ({ counts }) => {
  const total = (counts.critical || 0) + (counts.high || 0) + (counts.medium || 0) + (counts.low || 0);
  if (!total) return null;
  const segs = ['critical', 'high', 'medium', 'low'].map((l) => ({ level: l, pct: (counts[l] || 0) / total * 100 })).filter((s) => s.pct > 0);
  return (
    <div style={{ display: 'flex', height: 6, borderRadius: 3, overflow: 'hidden', gap: 1 }}>
      {segs.map((s) => <div key={s.level} style={{ width: `${s.pct}%`, background: `var(--severity-${s.level})`, borderRadius: 2 }} />)}
    </div>);

};

// ─── PAGINATION ──────────────────────────────────────────────────────────────
const Pagination = ({ page, total, perPage, onChange }) => {
  const pages = Math.ceil(total / perPage);
  if (pages <= 1) return null;
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--sp-2)', justifyContent: 'center', padding: 'var(--sp-4) 0' }}>
      <Btn variant="ghost" size="sm" icon="arrow-left" disabled={page === 1} onClick={() => onChange(page - 1)} />
      <span style={{ fontSize: 'var(--text-sm)', color: 'var(--text-secondary)' }}>Page {page} of {pages}</span>
      <Btn variant="ghost" size="sm" iconRight="arrow-right" disabled={page === pages} onClick={() => onChange(page + 1)}>Next</Btn>
    </div>);

};

// ─── DROPDOWN MENU ───────────────────────────────────────────────────────────
const DropdownMenu = ({ trigger, items }) => {
  const [open, setOpen] = useState(false);
  const ref = useRef(null);
  useEffect(() => {
    const h = (e) => {if (ref.current && !ref.current.contains(e.target)) setOpen(false);};
    document.addEventListener('mousedown', h);
    return () => document.removeEventListener('mousedown', h);
  }, []);
  return (
    <div ref={ref} style={{ position: 'relative', display: 'inline-flex' }}>
      <div onClick={() => setOpen((o) => !o)}>{trigger}</div>
      {open &&
      <div className="animate-fade-in" style={{ position: 'absolute', top: 'calc(100% + 4px)', right: 0, minWidth: 180,
        background: 'var(--surface-elevated)', border: '1px solid var(--border-strong)',
        borderRadius: 'var(--radius-card)', boxShadow: 'var(--shadow-md)', zIndex: 100, overflow: 'hidden', padding: 'var(--sp-1)' }}>
          {items.map((item, i) => item === 'divider' ?
        <div key={i} style={{ height: 1, background: 'var(--border-default)', margin: 'var(--sp-1) 0' }} /> :
        <button key={i} onClick={() => {item.onClick();setOpen(false);}}
        style={{ display: 'flex', alignItems: 'center', gap: 'var(--sp-2)', width: '100%', padding: '7px 12px',
          background: 'none', border: 'none', cursor: 'pointer', fontFamily: 'var(--font-sans)',
          fontSize: 'var(--text-base)', color: item.destructive ? 'var(--severity-critical-text)' : 'var(--text-primary)',
          textAlign: 'left', borderRadius: 4, transition: 'background var(--transition)' }}
        onMouseEnter={(e) => e.currentTarget.style.background = 'var(--surface-hover)'}
        onMouseLeave={(e) => e.currentTarget.style.background = 'none'}>
                {item.icon && <Icon name={item.icon} size={13} />}
                {item.label}
              </button>
        )}
        </div>
      }
    </div>);

};

// ─── SECTION HEADER ──────────────────────────────────────────────────────────
const SectionHeader = ({ title, action }) =>
<div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 'var(--sp-4)' }}>
    <h2 style={{ fontSize: 'var(--text-md)', fontWeight: 500, color: 'var(--text-primary)' }}>{title}</h2>
    {action}
  </div>;


// Export everything to window
Object.assign(window, {
  Icon, Spinner, Btn, SeverityBadge, SeverityDot, SeverityChips, StatusBadge, ScoreBadge,
  Input, Textarea, SelectInput, Checkbox, Radio, Toggle, SegmentedControl,
  Modal, SlideOver, ConfirmModal,
  ToastCtx, ToastProvider, useToast,
  Card, EmptyState, Skeleton, CodeBlock,
  Breadcrumb, TabStrip, FilterChip, SeverityBar, Pagination, DropdownMenu, SectionHeader,
  SEVERITY_CONFIG, STATUS_CONFIG
});