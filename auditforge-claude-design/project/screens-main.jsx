// screens-main.jsx — Login, Dashboard, Settings screens
const { useState, useEffect, useRef } = React;

// ─── LOGIN SCREEN ─────────────────────────────────────────────────────────────
const LoginScreen = ({ onLogin }) => {
  const [email, setEmail] = useState('jane.doe@acme.com');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [remember, setRemember] = useState(false);

  const handle = (e) => {
    e.preventDefault();
    if (!email) { setError('Email is required'); return; }
    if (!password) { setError('Password is required'); return; }
    setLoading(true); setError('');
    setTimeout(() => { setLoading(false); onLogin(); }, 900);
  };

  return (
    <div style={{ minHeight:'100vh', background:'var(--surface-primary)', display:'flex', flexDirection:'column', alignItems:'center', justifyContent:'center', padding:'var(--sp-6)' }}>
      <div style={{ marginBottom:'var(--sp-8)', textAlign:'center' }}>
        <div style={{ display:'inline-flex', alignItems:'center', gap:'var(--sp-2)', marginBottom:'var(--sp-2)' }}>
          <Icon name="shield-check" size={20} style={{ color:'var(--accent-default)' }}/>
          <span style={{ fontSize:'var(--text-md)', fontWeight:600, color:'var(--text-primary)', letterSpacing:'-0.3px' }}>AuditForge</span>
        </div>
        <p style={{ fontSize:'var(--text-sm)', color:'var(--text-tertiary)' }}>Enterprise compliance audit platform</p>
      </div>

      <div style={{ width:'100%', maxWidth:380, background:'var(--surface-secondary)', border:'1px solid var(--border-default)', borderRadius:'var(--radius-card)', padding:'var(--sp-8)' }}>
        <h1 style={{ fontSize:'var(--text-lg)', fontWeight:500, marginBottom:'var(--sp-6)', color:'var(--text-primary)' }}>Sign in</h1>
        <form onSubmit={handle} style={{ display:'flex', flexDirection:'column', gap:'var(--sp-4)' }}>
          <Input label="Work email" type="email" value={email} onChange={e=>setEmail(e.target.value)} placeholder="you@company.com" icon="user" autoFocus />
          <Input label="Password" type="password" value={password} onChange={e=>setPassword(e.target.value)} placeholder="••••••••" icon="lock" error={error} />
          <Checkbox label="Remember this device" checked={remember} onChange={e=>setRemember(e.target.checked)} />
          <Btn type="submit" variant="primary" loading={loading} fullWidth style={{ marginTop:'var(--sp-2)' }}>Sign in</Btn>
        </form>
      </div>
      <p style={{ marginTop:'var(--sp-4)', fontSize:'var(--text-sm)', color:'var(--text-tertiary)' }}>
        Secure access · SSO available via your IT admin
      </p>
    </div>
  );
};

// ─── DASHBOARD ────────────────────────────────────────────────────────────────
const ENV_COLORS = { production:'var(--status-error-bg)', staging:'var(--status-warning-bg)', development:'var(--status-info-bg)' };
const ENV_TEXT   = { production:'var(--status-error)',    staging:'var(--status-warning)',    development:'var(--status-info)' };

const SessionRow = ({ session, onClick, onDelete }) => {
  const [hov, setHov] = useState(false);
  return (
    <div onMouseEnter={()=>setHov(true)} onMouseLeave={()=>setHov(false)}
      style={{ display:'grid', gridTemplateColumns:'minmax(200px,1fr) 100px 200px 80px 130px auto', alignItems:'center', gap:'var(--sp-4)', padding:'var(--sp-3) var(--sp-4)', borderBottom:'1px solid var(--border-default)', cursor:'pointer', background: hov ? 'var(--surface-hover)' : 'transparent', transition:'background var(--transition)' }}
      onClick={onClick}>
      <div style={{ minWidth:0 }}>
        <div style={{ display:'flex', alignItems:'center', gap:'var(--sp-2)', marginBottom:3 }}>
          <span style={{ fontSize:'var(--text-base)', fontWeight:500, color:'var(--text-primary)', overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap' }}>{session.name}</span>
          <span style={{ fontSize:'var(--text-xs)', fontWeight:500, color:ENV_TEXT[session.env], background:ENV_COLORS[session.env], padding:'1px 6px', borderRadius:3, whiteSpace:'nowrap', flexShrink:0 }}>{session.env}</span>
        </div>
        <span style={{ fontSize:'var(--text-sm)', color:'var(--text-tertiary)' }}>{session.lastActivity}</span>
      </div>
      <div><StatusBadge status={session.status}/></div>
      <div><SeverityChips counts={session.findings}/></div>
      <div><ScoreBadge score={session.score}/></div>
      <div style={{ fontSize:'var(--text-sm)', color:'var(--text-tertiary)' }}>
        {session.created ? new Date(session.created).toLocaleDateString('en-US',{month:'short',day:'numeric'}) : '—'}
      </div>
      <div onClick={e=>e.stopPropagation()}>
        <DropdownMenu
          trigger={<Btn variant="ghost" size="sm" icon="more-horizontal" style={{ opacity: hov?1:0, transition:'opacity var(--transition)' }}/>}
          items={[
            { label:'View detail', icon:'eye',   onClick:onClick },
            'divider',
            { label:'Delete session', icon:'trash', destructive:true, onClick:()=>onDelete(session.id) },
          ]}
        />
      </div>
    </div>
  );
};

const DashboardScreen = ({ navigate }) => {
  const { sessions, addSession, deleteSession } = useApp();
  const toast = useToast();
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [page, setPage] = useState(1);
  const [showCreate, setShowCreate] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(null);
  const PER_PAGE = 10;

  const filtered = sessions.filter(s => {
    if (statusFilter !== 'all' && s.status !== statusFilter) return false;
    if (search && !s.name.toLowerCase().includes(search.toLowerCase())) return false;
    return true;
  });

  const paged = filtered.slice((page-1)*PER_PAGE, page*PER_PAGE);

  const handleCreate = (data) => {
    const id = addSession(data);
    setShowCreate(false);
    toast('Session created successfully', 'success');
    navigate('session', { sessionId:id, tab:'uploads' });
  };

  const handleDelete = (id) => {
    deleteSession(id);
    setConfirmDelete(null);
    toast('Session deleted', 'info');
  };

  return (
    <div style={{ padding:'var(--sp-6)', maxWidth:1200, margin:'0 auto' }}>
      {/* Page header */}
      <div style={{ display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'var(--sp-6)' }}>
        <div>
          <h1 style={{ fontSize:'var(--text-xl)', fontWeight:500, color:'var(--text-primary)', lineHeight:'var(--lh-heading)' }}>Audit sessions</h1>
          <p style={{ fontSize:'var(--text-sm)', color:'var(--text-tertiary)', marginTop:4 }}>{sessions.length} sessions total</p>
        </div>
        <Btn variant="primary" icon="plus" onClick={()=>setShowCreate(true)}>Create session</Btn>
      </div>

      {/* Filter bar */}
      <div style={{ display:'flex', alignItems:'center', gap:'var(--sp-3)', marginBottom:'var(--sp-4)', flexWrap:'wrap' }}>
        <Input icon="search" value={search} onChange={e=>setSearch(e.target.value)} placeholder="Search sessions…" style={{ maxWidth:280 }}/>
        <div style={{ display:'flex', gap:'var(--sp-1)' }}>
          {['all','created','scanning','completed','failed'].map(s => (
            <button key={s} onClick={()=>{ setStatusFilter(s); setPage(1); }} style={{
              padding:'5px 10px', borderRadius:'var(--radius-badge)', border:'1px solid',
              borderColor: statusFilter===s ? 'var(--accent-default)' : 'var(--border-default)',
              background: statusFilter===s ? 'var(--accent-subtle)' : 'transparent',
              color: statusFilter===s ? 'var(--accent-default)' : 'var(--text-secondary)',
              fontSize:'var(--text-sm)', fontWeight:500, cursor:'pointer', transition:'var(--transition)', fontFamily:'var(--font-sans)'
            }}>{s === 'all' ? 'All' : s.charAt(0).toUpperCase()+s.slice(1)}</button>
          ))}
        </div>
      </div>

      {/* Table */}
      <Card hover={false} style={{ overflow:'hidden' }}>
        {/* Table header */}
        <div style={{ display:'grid', gridTemplateColumns:'minmax(200px,1fr) 100px 200px 80px 130px 40px', gap:'var(--sp-4)', padding:'var(--sp-2) var(--sp-4)', borderBottom:'1px solid var(--border-default)', background:'var(--surface-sunken)' }}>
          {['Session','Status','Severity','Score','Created',''].map((h,i) => (
            <span key={i} style={{ fontSize:'var(--text-xs)', fontWeight:500, color:'var(--text-tertiary)', textTransform:'uppercase', letterSpacing:'0.05em' }}>{h}</span>
          ))}
        </div>
        {paged.length === 0
          ? <EmptyState title={search||statusFilter!=='all' ? 'No sessions match' : 'No sessions yet'} description={search||statusFilter!=='all' ? 'Try adjusting your filters.' : 'Create your first audit session to get started.'} action={!search&&statusFilter==='all'&&<Btn variant="primary" icon="plus" onClick={()=>setShowCreate(true)}>Create session</Btn>}/>
          : paged.map(s => <SessionRow key={s.id} session={s} onClick={()=>navigate('session',{sessionId:s.id,tab:'findings'})} onDelete={id=>setConfirmDelete(id)}/>)
        }
        <div style={{ borderTop:'1px solid var(--border-default)' }}>
          <Pagination page={page} total={filtered.length} perPage={PER_PAGE} onChange={setPage}/>
        </div>
      </Card>

      {/* Create session slide-over */}
      <CreateSessionSlideOver open={showCreate} onClose={()=>setShowCreate(false)} onCreate={handleCreate}/>

      {/* Confirm delete */}
      <ConfirmModal open={!!confirmDelete} onClose={()=>setConfirmDelete(null)} onConfirm={()=>handleDelete(confirmDelete)}
        title="Delete session" confirmLabel="Delete session"
        message="This will permanently delete the session, all uploaded files, findings, and reports. This action cannot be undone."/>
    </div>
  );
};

// ─── CREATE SESSION SLIDE-OVER ────────────────────────────────────────────────
const CreateSessionSlideOver = ({ open, onClose, onCreate }) => {
  const [name, setName]   = useState('');
  const [desc, setDesc]   = useState('');
  const [env, setEnv]     = useState('production');
  const [errors, setErrors] = useState({});

  const validate = () => {
    const e = {};
    if (!name.trim()) e.name = 'Session name is required';
    if (name.trim().length > 120) e.name = 'Name must be under 120 characters';
    if (name.trim().length > 0 && name.trim().length < 3) e.name = 'Name must be at least 3 characters';
    return e;
  };

  const handleCreate = () => {
    const e = validate();
    if (Object.keys(e).length) { setErrors(e); return; }
    onCreate({ name: name.trim(), description: desc.trim(), env });
    setName(''); setDesc(''); setEnv('production'); setErrors({});
  };

  return (
    <SlideOver open={open} onClose={onClose} title="Create audit session" width={460}
      footer={<>
        <Btn variant="ghost" onClick={onClose}>Cancel</Btn>
        <Btn variant="primary" onClick={handleCreate}>Create session</Btn>
      </>}>
      <div style={{ display:'flex', flexDirection:'column', gap:'var(--sp-6)' }}>
        <Input label="Session name *" value={name} onChange={e=>setName(e.target.value)} placeholder="e.g. Q3 2026 HIPAA Compliance Audit" error={errors.name} hint="3–120 characters" autoFocus />
        <Textarea label="Description" value={desc} onChange={e=>setDesc(e.target.value)} placeholder="Describe the scope and purpose of this audit session…" rows={3}/>
        <div>
          <label style={{ fontSize:'var(--text-sm)', fontWeight:500, color:'var(--text-secondary)', display:'block', marginBottom:'var(--sp-2)' }}>Target environment</label>
          <div style={{ display:'flex', flexDirection:'column', gap:'var(--sp-2)' }}>
            {['development','staging','production'].map(e => (
              <Radio key={e} label={e.charAt(0).toUpperCase()+e.slice(1)} value={e} checked={env===e} onChange={()=>setEnv(e)} name="env"/>
            ))}
          </div>
        </div>
        <div style={{ background:'var(--surface-sunken)', borderRadius:'var(--radius-card)', padding:'var(--sp-4)', border:'1px solid var(--border-default)' }}>
          <p style={{ fontSize:'var(--text-sm)', color:'var(--text-tertiary)', lineHeight:'var(--lh-body)' }}>
            After creating the session, upload your artifacts (code files, API specs, configurations) and select compliance policy packs before running a scan.
          </p>
        </div>
      </div>
    </SlideOver>
  );
};

// ─── SETTINGS SCREEN ──────────────────────────────────────────────────────────
const SettingsScreen = ({ theme, setTheme }) => {
  const toast = useToast();
  const [apiUrl, setApiUrl] = useState('https://api.auditforge.io/v1');
  const [testStatus, setTestStatus] = useState(null);
  const [testing, setTesting] = useState(false);
  const [name, setName] = useState('Jane Doe');
  const [currentPw, setCurrentPw] = useState('');
  const [newPw, setNewPw] = useState('');

  const testConn = () => {
    setTesting(true); setTestStatus(null);
    setTimeout(() => { setTesting(false); setTestStatus('success'); toast('Connection successful', 'success'); }, 1200);
  };

  const Section = ({ title, children }) => (
    <div style={{ marginBottom:'var(--sp-8)' }}>
      <h2 style={{ fontSize:'var(--text-md)', fontWeight:500, color:'var(--text-primary)', paddingBottom:'var(--sp-3)', borderBottom:'1px solid var(--border-default)', marginBottom:'var(--sp-6)' }}>{title}</h2>
      {children}
    </div>
  );

  const Field = ({ label, children }) => (
    <div style={{ display:'grid', gridTemplateColumns:'200px 1fr', gap:'var(--sp-8)', alignItems:'start', marginBottom:'var(--sp-4)' }}>
      <label style={{ fontSize:'var(--text-base)', color:'var(--text-secondary)', paddingTop:7 }}>{label}</label>
      <div>{children}</div>
    </div>
  );

  return (
    <div style={{ padding:'var(--sp-6)', maxWidth:720 }}>
      <h1 style={{ fontSize:'var(--text-xl)', fontWeight:500, marginBottom:'var(--sp-8)' }}>Settings</h1>

      <Section title="Profile">
        <Field label="Full name"><Input value={name} onChange={e=>setName(e.target.value)}/></Field>
        <Field label="Email"><div style={{ padding:'6px 10px', fontSize:'var(--text-base)', color:'var(--text-tertiary)', background:'var(--surface-sunken)', borderRadius:'var(--radius-input)', border:'1px solid var(--border-default)' }}>jane.doe@acme.com</div></Field>
        <Field label="Current password"><Input type="password" value={currentPw} onChange={e=>setCurrentPw(e.target.value)} placeholder="••••••••"/></Field>
        <Field label="New password"><Input type="password" value={newPw} onChange={e=>setNewPw(e.target.value)} placeholder="••••••••"/></Field>
        <div style={{ display:'flex', justifyContent:'flex-end' }}>
          <Btn variant="primary" onClick={()=>toast('Profile updated','success')}>Save changes</Btn>
        </div>
      </Section>

      <Section title="API configuration">
        <Field label="Base URL">
          <div style={{ display:'flex', gap:'var(--sp-2)' }}>
            <Input value={apiUrl} onChange={e=>setApiUrl(e.target.value)} style={{ flex:1 }}/>
            <Btn variant="outline" onClick={testConn} loading={testing} icon={testStatus==='success'?'check-circle':undefined} style={{ flexShrink:0, color: testStatus==='success'?'var(--status-success)':undefined }}>
              {testStatus==='success' ? 'Connected' : 'Test connection'}
            </Btn>
          </div>
        </Field>
      </Section>

      <Section title="Appearance">
        <Field label="Theme">
          <div style={{ display:'flex', gap:'var(--sp-3)' }}>
            {[
              { id:'dark',   label:'Dark',   preview:'#0D1117' },
              { id:'light',  label:'Light',  preview:'#FFFFFF' },
            ].map(t => (
              <div key={t.id} onClick={()=>setTheme(t.id)} style={{ cursor:'pointer', display:'flex', flexDirection:'column', gap:'var(--sp-2)', alignItems:'center' }}>
                <div style={{ width:80, height:52, borderRadius:'var(--radius-card)', background:t.preview,
                  border:`2px solid ${theme===t.id?'var(--accent-default)':'var(--border-default)'}`,
                  display:'flex', alignItems:'center', justifyContent:'center', transition:'border-color var(--transition)' }}>
                  <div style={{ width:48, height:28, borderRadius:4, background: t.id==='dark'?'#161B22':'#F6F8FA', border:`1px solid ${t.id==='dark'?'#21262D':'#D0D7DE'}` }}/>
                </div>
                <span style={{ fontSize:'var(--text-sm)', fontWeight: theme===t.id ? 500 : 400, color: theme===t.id ? 'var(--accent-default)' : 'var(--text-secondary)' }}>{t.label}</span>
              </div>
            ))}
          </div>
        </Field>
      </Section>

      <Section title="About">
        <div style={{ display:'flex', flexDirection:'column', gap:'var(--sp-2)' }}>
          {[['Version','2.4.1'],['License','Enterprise'],['Support','security@auditforge.io']].map(([k,v]) => (
            <div key={k} style={{ display:'flex', gap:'var(--sp-8)', fontSize:'var(--text-base)' }}>
              <span style={{ color:'var(--text-secondary)', width:200 }}>{k}</span>
              <span style={{ color:'var(--text-primary)' }}>{v}</span>
            </div>
          ))}
        </div>
      </Section>
    </div>
  );
};

Object.assign(window, { LoginScreen, DashboardScreen, CreateSessionSlideOver, SettingsScreen });
