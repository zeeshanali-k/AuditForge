// screens-session.jsx — Session Detail with all tabs
const { useState, useEffect, useRef } = React;

const FILE_TYPE_ICONS = { code:'file-code', config:'file-text', db:'database', spec:'book-open', file:'file' };
const FRAMEWORK_COLORS = { HIPAA:'#388BFD', OWASP:'#F85149', SOC2:'#3FB950', CIS:'#FB8F44', NIST:'#E3B341' };

// ─── SESSION DETAIL SCREEN ────────────────────────────────────────────────────
const SessionDetailScreen = ({ sessionId, initialTab='findings', navigate }) => {
  const { sessions, findings, uploads, reports, auditEvents } = useApp();
  const toast = useToast();
  const session = sessions.find(s => s.id === sessionId);
  const [tab, setTab] = useState(initialTab);
  const [editingName, setEditingName] = useState(false);
  const [nameVal, setNameVal] = useState(session?.name || '');

  useEffect(() => { if (session) setNameVal(session.name); }, [sessionId]);
  useEffect(() => { setTab(initialTab); }, [initialTab]);

  if (!session) return <EmptyState title="Session not found" description="This session may have been deleted."/>;

  const sessionFindings = findings[sessionId] || [];
  const sessionUploads  = uploads[sessionId]  || [];
  const sessionReports  = reports[sessionId]  || [];
  const sessionEvents   = auditEvents[sessionId] || [];
  const totalFindings   = Object.values(session.findings).reduce((a,b)=>a+b,0);

  const tabs = [
    { id:'uploads',    label:'Uploads',    icon:'upload-cloud', count: sessionUploads.length },
    { id:'policies',   label:'Policies',   icon:'book-open'  },
    { id:'scans',      label:'Scans',      icon:'activity'   },
    { id:'findings',   label:'Findings',   icon:'zap',        count: totalFindings },
    { id:'reports',    label:'Reports',    icon:'file-text',  count: sessionReports.length },
    { id:'audit-trail',label:'Audit trail',icon:'history'    },
  ];

  return (
    <div style={{ display:'flex', flexDirection:'column', height:'100%', minHeight:0 }}>
      {/* Header */}
      <div style={{ padding:'var(--sp-6) var(--sp-6) 0', borderBottom:'1px solid var(--border-default)', flexShrink:0 }}>
        <Breadcrumb items={[{label:'Sessions', onClick:()=>navigate('dashboard')},{label: session.name}]} />
        <div style={{ display:'flex', alignItems:'flex-start', justifyContent:'space-between', marginTop:'var(--sp-4)', marginBottom:'var(--sp-4)', gap:'var(--sp-4)' }}>
          <div style={{ display:'flex', alignItems:'center', gap:'var(--sp-3)', minWidth:0, flex:1 }}>
            {editingName
              ? <input autoFocus value={nameVal}
                  onChange={e=>setNameVal(e.target.value)}
                  onBlur={()=>setEditingName(false)}
                  onKeyDown={e=>{ if(e.key==='Enter'||e.key==='Escape') setEditingName(false); }}
                  style={{ fontSize:'var(--text-xl)', fontWeight:500, background:'transparent', border:'none', borderBottom:'2px solid var(--border-focus)', color:'var(--text-primary)', fontFamily:'var(--font-sans)', outline:'none', minWidth:0, flex:1 }}/>
              : <h1 onClick={()=>setEditingName(true)} title="Click to edit"
                  style={{ fontSize:'var(--text-xl)', fontWeight:500, color:'var(--text-primary)', cursor:'text', overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap', lineHeight:'var(--lh-heading)' }}>
                  {nameVal}
                  <Icon name="edit" size={13} style={{ marginLeft:8, color:'var(--text-tertiary)', opacity:0 }} className="edit-icon"/>
                </h1>
            }
            <StatusBadge status={session.status}/>
            {session.score != null && <ScoreBadge score={session.score} large/>}
          </div>
          <div style={{ display:'flex', gap:'var(--sp-2)', flexShrink:0 }}>
            {session.status === 'completed' || session.status === 'created'
              ? <Btn variant="primary" icon="play" onClick={()=>navigate('scan',{sessionId})}>Run scan</Btn>
              : null}
            <DropdownMenu
              trigger={<Btn variant="outline" icon="more-horizontal"/>}
              items={[
                { label:'Export settings', icon:'download', onClick:()=>toast('Settings exported','info') },
                'divider',
                { label:'Delete session', icon:'trash', destructive:true, onClick:()=>toast('Use the session list to delete','warning') },
              ]}
            />
          </div>
        </div>
        <TabStrip tabs={tabs} active={tab} onChange={setTab}/>
      </div>

      {/* Tab content */}
      <div style={{ flex:1, overflowY:'auto', minHeight:0 }}>
        {tab === 'uploads'     && <UploadsTab sessionId={sessionId} uploads={sessionUploads} navigate={navigate}/>}
        {tab === 'policies'    && <PoliciesTab sessionId={sessionId}/>}
        {tab === 'scans'       && <ScansTab session={session} navigate={navigate}/>}
        {tab === 'findings'    && <FindingsTab sessionId={sessionId} findings={sessionFindings} session={session} navigate={navigate}/>}
        {tab === 'reports'     && <ReportsTab sessionId={sessionId} session={session} reports={sessionReports}/>}
        {tab === 'audit-trail' && <AuditTrailTab events={sessionEvents}/>}
      </div>
    </div>
  );
};

// ─── UPLOADS TAB ─────────────────────────────────────────────────────────────
const UploadsTab = ({ sessionId, uploads, navigate }) => {
  const { addUpload, deleteUpload } = useApp();
  const toast = useToast();
  const [dragging, setDragging] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [confirmId, setConfirmId] = useState(null);
  const inputRef = useRef(null);

  const handleDrop = (e) => {
    e.preventDefault(); setDragging(false);
    const files = Array.from(e.dataTransfer?.files || []);
    if (files.length) processFiles(files);
  };

  const processFiles = (files) => {
    setUploading(true); setProgress(0);
    const interval = setInterval(() => setProgress(p => { if (p >= 100) { clearInterval(interval); return 100; } return p + 8; }), 80);
    setTimeout(() => {
      files.forEach(f => addUpload(sessionId, f));
      setUploading(false); setProgress(0);
      toast(`${files.length} file${files.length>1?'s':''} uploaded`, 'success');
    }, 1200);
  };

  return (
    <div style={{ padding:'var(--sp-6)' }}>
      {/* Drop zone or progress */}
      {uploading
        ? <div style={{ border:'1px solid var(--border-default)', borderRadius:'var(--radius-card)', padding:'var(--sp-8)', marginBottom:'var(--sp-6)', background:'var(--surface-secondary)', display:'flex', flexDirection:'column', gap:'var(--sp-3)' }}>
            <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
              <span style={{ fontSize:'var(--text-base)', color:'var(--text-primary)' }}>Uploading…</span>
              <Btn variant="ghost" size="sm" icon="x" onClick={()=>{ setUploading(false); setProgress(0); }}>Cancel</Btn>
            </div>
            <div style={{ height:4, background:'var(--border-default)', borderRadius:2, overflow:'hidden' }}>
              <div style={{ height:'100%', width:`${progress}%`, background:'var(--accent-default)', borderRadius:2, transition:'width 80ms linear' }}/>
            </div>
            <span style={{ fontSize:'var(--text-sm)', color:'var(--text-tertiary)' }}>{progress}% complete</span>
          </div>
        : <div onDragOver={e=>{e.preventDefault();setDragging(true)}} onDragLeave={()=>setDragging(false)} onDrop={handleDrop}
            onClick={()=>inputRef.current?.click()}
            style={{ border:`1.5px dashed ${dragging?'var(--accent-default)':'var(--border-strong)'}`, borderRadius:'var(--radius-card)', padding:'var(--sp-8)', marginBottom:'var(--sp-6)', background: dragging?'var(--accent-subtle)':'var(--surface-secondary)', cursor:'pointer', textAlign:'center', transition:'var(--transition)', display:'flex', flexDirection:'column', alignItems:'center', gap:'var(--sp-3)' }}>
            <Icon name="upload-cloud" size={24} style={{ color: dragging?'var(--accent-default)':'var(--text-tertiary)' }}/>
            <div>
              <p style={{ fontSize:'var(--text-base)', color: dragging?'var(--accent-default)':'var(--text-secondary)', fontWeight:500 }}>Drop files here or click to browse</p>
              <p style={{ fontSize:'var(--text-sm)', color:'var(--text-tertiary)', marginTop:4 }}>Supported: Python, JS, Go, YAML, JSON, SQL, config files</p>
            </div>
            <input ref={inputRef} type="file" multiple style={{ display:'none' }} onChange={e=>processFiles(Array.from(e.target.files))}/>
          </div>
      }

      {/* File list */}
      {uploads.length > 0 && (
        <Card hover={false} style={{ overflow:'hidden' }}>
          <div style={{ display:'grid', gridTemplateColumns:'minmax(200px,1fr) 80px 120px 130px 40px', gap:'var(--sp-4)', padding:'var(--sp-2) var(--sp-4)', background:'var(--surface-sunken)', borderBottom:'1px solid var(--border-default)' }}>
            {['Filename','Type','Size','Uploaded',''].map((h,i)=>(
              <span key={i} style={{ fontSize:'var(--text-xs)', fontWeight:500, color:'var(--text-tertiary)', textTransform:'uppercase', letterSpacing:'0.05em' }}>{h}</span>
            ))}
          </div>
          {uploads.map(u => (
            <div key={u.id} style={{ display:'grid', gridTemplateColumns:'minmax(200px,1fr) 80px 120px 130px 40px', gap:'var(--sp-4)', padding:'var(--sp-3) var(--sp-4)', borderBottom:'1px solid var(--border-default)', alignItems:'center' }}
              onMouseEnter={e=>e.currentTarget.style.background='var(--surface-hover)'}
              onMouseLeave={e=>e.currentTarget.style.background='transparent'}>
              <div style={{ display:'flex', alignItems:'center', gap:'var(--sp-2)', minWidth:0 }}>
                <Icon name={FILE_TYPE_ICONS[u.type]||'file'} size={14} style={{ color:'var(--text-tertiary)', flexShrink:0 }}/>
                <span style={{ fontSize:'var(--text-base)', color:'var(--text-primary)', overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap', fontFamily: u.type==='code'?'var(--font-mono)':undefined }}>{u.name}</span>
              </div>
              <span style={{ fontSize:'var(--text-sm)', color:'var(--text-secondary)', textTransform:'capitalize' }}>{u.type}</span>
              <span style={{ fontSize:'var(--text-sm)', color:'var(--text-secondary)', fontFamily:'var(--font-mono)' }}>{u.size}</span>
              <span style={{ fontSize:'var(--text-sm)', color:'var(--text-tertiary)' }}>{u.date}</span>
              <button onClick={()=>setConfirmId(u.id)} style={{ background:'none', border:'none', cursor:'pointer', color:'var(--text-tertiary)', padding:4, borderRadius:4, display:'flex', alignItems:'center' }}
                onMouseEnter={e=>e.currentTarget.style.color='var(--severity-critical)'}
                onMouseLeave={e=>e.currentTarget.style.color='var(--text-tertiary)'}>
                <Icon name="trash" size={13}/>
              </button>
            </div>
          ))}
        </Card>
      )}
      {uploads.length === 0 && !uploading && <EmptyState title="No files uploaded yet" description="Upload your source code, API specs, database schemas, or configuration files to begin a scan."/>}
      <ConfirmModal open={!!confirmId} onClose={()=>setConfirmId(null)} onConfirm={()=>{ deleteUpload(sessionId,confirmId); setConfirmId(null); toast('File deleted','info'); }} title="Delete upload" confirmLabel="Delete file" message="This file will be removed from the session. Any scans that used it will retain their existing findings."/>
    </div>
  );
};

// ─── POLICIES TAB ─────────────────────────────────────────────────────────────
const PoliciesTab = ({ sessionId }) => {
  const { policies, ALL_POLICIES, setPolicies } = useApp();
  const toast = useToast();
  const sessionPolicies = policies[sessionId] || ALL_POLICIES;
  const [expanded, setExpanded] = useState({});
  const [hasChanges, setHasChanges] = useState(false);

  const toggle = (policyId) => {
    setPolicies(p => ({ ...p, [sessionId]: (p[sessionId]||ALL_POLICIES).map(x => x.id===policyId?{...x,selected:!x.selected}:x) }));
    setHasChanges(true);
  };
  const selected = sessionPolicies.filter(p=>p.selected);
  const groups = {};
  sessionPolicies.forEach(p => { if(!groups[p.framework]) groups[p.framework]=[]; groups[p.framework].push(p); });

  return (
    <div style={{ padding:'var(--sp-6)', display:'grid', gridTemplateColumns:'1fr 1.5fr', gap:'var(--sp-6)', alignItems:'start', maxWidth:1100 }}>
      {/* Left: browse */}
      <div>
        <p style={{ fontSize:'var(--text-sm)', color:'var(--text-tertiary)', marginBottom:'var(--sp-4)' }}>Select the policy packs to apply to this session's scans.</p>
        {Object.entries(groups).map(([fw, packs]) => (
          <div key={fw} style={{ marginBottom:'var(--sp-6)' }}>
            <div style={{ display:'flex', alignItems:'center', gap:'var(--sp-2)', marginBottom:'var(--sp-3)' }}>
              <span style={{ width:8, height:8, borderRadius:2, background: FRAMEWORK_COLORS[fw]||'var(--border-strong)', flexShrink:0 }}/>
              <span style={{ fontSize:'var(--text-sm)', fontWeight:500, color:'var(--text-secondary)', textTransform:'uppercase', letterSpacing:'0.05em' }}>{fw}</span>
            </div>
            {packs.map(p => (
              <div key={p.id} onClick={()=>toggle(p.id)} style={{ display:'flex', alignItems:'center', gap:'var(--sp-3)', padding:'var(--sp-3)', borderRadius:'var(--radius-card)', cursor:'pointer', marginBottom:'var(--sp-1)', transition:'background var(--transition)' }}
                onMouseEnter={e=>e.currentTarget.style.background='var(--surface-hover)'}
                onMouseLeave={e=>e.currentTarget.style.background='transparent'}>
                <input type="checkbox" checked={p.selected} onChange={()=>{}} style={{ width:14, height:14, accentColor:'var(--accent-default)', flexShrink:0 }}/>
                <div style={{ flex:1, minWidth:0 }}>
                  <p style={{ fontSize:'var(--text-base)', color:'var(--text-primary)', overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap' }}>{p.name}</p>
                  <p style={{ fontSize:'var(--text-sm)', color:'var(--text-tertiary)' }}>{p.ruleCount} rules</p>
                </div>
              </div>
            ))}
          </div>
        ))}
      </div>

      {/* Right: selected packs */}
      <div>
        <div style={{ display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'var(--sp-4)' }}>
          <p style={{ fontSize:'var(--text-sm)', color:'var(--text-tertiary)' }}>{selected.length} pack{selected.length!==1?'s':''} selected</p>
          {hasChanges && <Btn variant="primary" size="sm" onClick={()=>{ setHasChanges(false); toast('Policy selection saved','success'); }}>Save selection</Btn>}
        </div>
        {selected.length === 0
          ? <div style={{ border:'1px dashed var(--border-strong)', borderRadius:'var(--radius-card)', padding:'var(--sp-8)', textAlign:'center' }}>
              <p style={{ color:'var(--text-tertiary)', fontSize:'var(--text-base)' }}>No packs selected</p>
            </div>
          : selected.map(p => (
              <Card key={p.id} hover={false} style={{ marginBottom:'var(--sp-3)', padding:'var(--sp-4)' }}>
                <div style={{ display:'flex', alignItems:'flex-start', justifyContent:'space-between', marginBottom: expanded[p.id] && p.rules.length ? 'var(--sp-3)' : 0 }}>
                  <div>
                    <div style={{ display:'flex', alignItems:'center', gap:'var(--sp-2)', marginBottom:4 }}>
                      <span style={{ fontSize:'var(--text-xs)', fontWeight:600, color:FRAMEWORK_COLORS[p.framework]||'var(--accent-default)', letterSpacing:'0.05em' }}>{p.framework}</span>
                    </div>
                    <p style={{ fontSize:'var(--text-base)', fontWeight:500, color:'var(--text-primary)' }}>{p.name}</p>
                    <p style={{ fontSize:'var(--text-sm)', color:'var(--text-tertiary)', marginTop:2 }}>{p.ruleCount} rules</p>
                  </div>
                  <div style={{ display:'flex', gap:'var(--sp-1)' }}>
                    {p.rules.length > 0 && <Btn variant="ghost" size="sm" iconRight={expanded[p.id]?'chevron-up':'chevron-down'} onClick={()=>setExpanded(x=>({...x,[p.id]:!x[p.id]}))}>View rules</Btn>}
                    <Btn variant="ghost" size="sm" icon="x" onClick={()=>toggle(p.id)}/>
                  </div>
                </div>
                {expanded[p.id] && p.rules.length > 0 && (
                  <div style={{ borderTop:'1px solid var(--border-default)', paddingTop:'var(--sp-3)', display:'flex', flexDirection:'column', gap:'var(--sp-1)' }}>
                    {p.rules.map(r => (
                      <div key={r.id} style={{ display:'flex', alignItems:'center', gap:'var(--sp-3)', padding:'var(--sp-1) 0' }}>
                        <SeverityDot level={r.severity}/>
                        <span style={{ fontSize:'var(--text-sm)', color:'var(--text-primary)', flex:1 }}>{r.name}</span>
                        <span style={{ fontSize:'var(--text-xs)', color:'var(--text-tertiary)' }}>{r.category}</span>
                      </div>
                    ))}
                  </div>
                )}
              </Card>
          ))
        }
      </div>
    </div>
  );
};

// ─── SCANS TAB ────────────────────────────────────────────────────────────────
const ScansTab = ({ session, navigate }) => {
  const hasScan = session.status === 'completed' || session.status === 'failed';
  return (
    <div style={{ padding:'var(--sp-6)' }}>
      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:'var(--sp-6)' }}>
        <h2 style={{ fontSize:'var(--text-md)', fontWeight:500 }}>Scan history</h2>
        <Btn variant="primary" icon="play" onClick={()=>navigate('scan',{sessionId:session.id})}>Run scan</Btn>
      </div>
      {hasScan ? (
        <Card hover={false} style={{ overflow:'hidden' }}>
          <div style={{ display:'grid', gridTemplateColumns:'160px 100px 1fr 100px 80px', gap:'var(--sp-4)', padding:'var(--sp-2) var(--sp-4)', background:'var(--surface-sunken)', borderBottom:'1px solid var(--border-default)' }}>
            {['Started','Status','Policies','Findings','Duration'].map((h,i)=>(
              <span key={i} style={{ fontSize:'var(--text-xs)', fontWeight:500, color:'var(--text-tertiary)', textTransform:'uppercase', letterSpacing:'0.05em' }}>{h}</span>
            ))}
          </div>
          <div style={{ display:'grid', gridTemplateColumns:'160px 100px 1fr 100px 80px', gap:'var(--sp-4)', padding:'var(--sp-3) var(--sp-4)', alignItems:'center', borderBottom:'1px solid var(--border-default)' }}>
            <span style={{ fontSize:'var(--text-sm)', fontFamily:'var(--font-mono)', color:'var(--text-secondary)' }}>2026-05-10 11:22</span>
            <StatusBadge status={session.status==='failed'?'failed':'completed'}/>
            <span style={{ fontSize:'var(--text-sm)', color:'var(--text-secondary)' }}>HIPAA Technical Safeguards, OWASP API Top 10</span>
            <span style={{ fontSize:'var(--text-base)', fontWeight:500, color:'var(--text-primary)' }}>22</span>
            <span style={{ fontSize:'var(--text-sm)', color:'var(--text-tertiary)', fontFamily:'var(--font-mono)' }}>23m 02s</span>
          </div>
        </Card>
      ) : <EmptyState title="No scans yet" description="Run a scan to analyze uploaded artifacts against selected policy packs." action={<Btn variant="primary" icon="play" onClick={()=>navigate('scan',{sessionId:session.id})}>Run scan</Btn>}/>}
    </div>
  );
};

// ─── FINDINGS TAB ─────────────────────────────────────────────────────────────
const FindingsTab = ({ sessionId, findings, session, navigate }) => {
  const { updateFindingStatus } = useApp();
  const toast = useToast();
  const [search, setSearch] = useState('');
  const [severityFilter, setSeverityFilter] = useState([]);
  const [statusFilter, setStatusFilter] = useState('all');
  const [selected, setSelected] = useState([]);
  const [page, setPage] = useState(1);
  const PER = 8;

  const filtered = findings.filter(f => {
    if (severityFilter.length && !severityFilter.includes(f.severity)) return false;
    if (statusFilter !== 'all' && f.status !== statusFilter) return false;
    if (search && !f.title.toLowerCase().includes(search.toLowerCase()) && !f.location.toLowerCase().includes(search.toLowerCase())) return false;
    return true;
  });
  const paged = filtered.slice((page-1)*PER, page*PER);
  const totalFindings = Object.values(session.findings).reduce((a,b)=>a+b,0);
  const allSelected = paged.length > 0 && paged.every(f => selected.includes(f.id));

  const toggleSev = (s) => setSeverityFilter(f => f.includes(s) ? f.filter(x=>x!==s) : [...f,s]);
  const clearFilters = () => { setSearch(''); setSeverityFilter([]); setStatusFilter('all'); };
  const hasFilters = search || severityFilter.length || statusFilter !== 'all';

  const batchUpdate = (newStatus) => {
    selected.forEach(id => updateFindingStatus(sessionId, id, newStatus));
    toast(`${selected.length} finding${selected.length>1?'s':''} marked as ${newStatus}`, 'success');
    setSelected([]);
  };

  return (
    <div style={{ padding:'var(--sp-6)' }}>
      {/* Summary widget */}
      {findings.length > 0 && (
        <div style={{ display:'grid', gridTemplateColumns:'auto 1fr auto', gap:'var(--sp-6)', padding:'var(--sp-4) var(--sp-6)', background:'var(--surface-secondary)', border:'1px solid var(--border-default)', borderRadius:'var(--radius-card)', marginBottom:'var(--sp-6)', alignItems:'center' }}>
          <div style={{ textAlign:'center' }}>
            <div style={{ fontSize:'var(--text-xl)', fontWeight:600, color:'var(--text-primary)', lineHeight:1 }}>{totalFindings}</div>
            <div style={{ fontSize:'var(--text-sm)', color:'var(--text-tertiary)', marginTop:4 }}>Total findings</div>
          </div>
          <div>
            <div style={{ display:'flex', gap:'var(--sp-4)', marginBottom:'var(--sp-2)' }}>
              {['critical','high','medium','low'].map(l => (
                <div key={l} style={{ textAlign:'center' }}>
                  <div style={{ fontSize:'var(--text-lg)', fontWeight:600, color:`var(--severity-${l}-text)` }}>{session.findings[l]||0}</div>
                  <div style={{ fontSize:'var(--text-xs)', color:'var(--text-tertiary)', textTransform:'capitalize' }}>{l}</div>
                </div>
              ))}
            </div>
            <SeverityBar counts={session.findings}/>
          </div>
          <div style={{ textAlign:'center' }}>
            <ScoreBadge score={session.score} large/>
            <div style={{ fontSize:'var(--text-sm)', color:'var(--text-tertiary)', marginTop:6 }}>Compliance score</div>
          </div>
        </div>
      )}

      {/* Filter bar */}
      <div style={{ display:'flex', alignItems:'center', gap:'var(--sp-3)', marginBottom:'var(--sp-4)', flexWrap:'wrap' }}>
        <Input icon="search" value={search} onChange={e=>setSearch(e.target.value)} placeholder="Search findings…" style={{ maxWidth:240 }}/>
        <div style={{ display:'flex', gap:'var(--sp-1)' }}>
          {['critical','high','medium','low'].map(l => (
            <button key={l} onClick={()=>toggleSev(l)} style={{
              padding:'5px 10px', borderRadius:'var(--radius-badge)', border:'1px solid',
              borderColor: severityFilter.includes(l) ? `var(--severity-${l})` : 'var(--border-default)',
              background: severityFilter.includes(l) ? `var(--severity-${l}-bg)` : 'transparent',
              color: severityFilter.includes(l) ? `var(--severity-${l}-text)` : 'var(--text-secondary)',
              fontSize:'var(--text-sm)', fontWeight:500, cursor:'pointer', transition:'var(--transition)', fontFamily:'var(--font-sans)', textTransform:'capitalize'
            }}>{l}</button>
          ))}
        </div>
        <SelectInput value={statusFilter} onChange={e=>setStatusFilter(e.target.value)} options={[{value:'all',label:'All statuses'},{value:'open',label:'Open'},{value:'acknowledged',label:'Acknowledged'},{value:'resolved',label:'Resolved'},{value:'false_positive',label:'False positive'}]}/>
        {hasFilters && <button onClick={clearFilters} style={{ background:'none', border:'none', cursor:'pointer', color:'var(--accent-default)', fontSize:'var(--text-sm)', fontFamily:'var(--font-sans)' }}>Clear filters</button>}
        {selected.length > 0 && (
          <div style={{ display:'flex', gap:'var(--sp-2)', marginLeft:'auto' }}>
            <span style={{ fontSize:'var(--text-sm)', color:'var(--text-secondary)', alignSelf:'center' }}>{selected.length} selected</span>
            <Btn variant="outline" size="sm" onClick={()=>batchUpdate('acknowledged')}>Acknowledge</Btn>
            <Btn variant="outline" size="sm" onClick={()=>batchUpdate('false_positive')}>Mark false positive</Btn>
          </div>
        )}
      </div>

      {/* Findings table */}
      {findings.length === 0
        ? <EmptyState title="No findings yet" description="Run a scan to detect compliance issues in your uploaded artifacts." action={<Btn variant="primary" icon="play" onClick={()=>navigate('scan',{sessionId})}>Run scan</Btn>}/>
        : <Card hover={false} style={{ overflow:'hidden' }}>
            <div style={{ display:'grid', gridTemplateColumns:'32px 110px minmax(200px,1fr) 110px minmax(120px,200px) 110px 80px', gap:'var(--sp-3)', padding:'var(--sp-2) var(--sp-4)', background:'var(--surface-sunken)', borderBottom:'1px solid var(--border-default)' }}>
              <div style={{ display:'flex', alignItems:'center' }}>
                <input type="checkbox" checked={allSelected} onChange={e=>setSelected(e.target.checked?paged.map(f=>f.id):[])} style={{ width:13, height:13, accentColor:'var(--accent-default)' }}/>
              </div>
              {['Severity','Title','Category','Location','Status','Age'].map((h,i)=>(
                <span key={i} style={{ fontSize:'var(--text-xs)', fontWeight:500, color:'var(--text-tertiary)', textTransform:'uppercase', letterSpacing:'0.05em', display:'flex', alignItems:'center', gap:4 }}>{h}</span>
              ))}
            </div>
            {filtered.length === 0
              ? <EmptyState title="No findings match" description="Try adjusting the filters." action={<button onClick={clearFilters} style={{ background:'none', border:'none', cursor:'pointer', color:'var(--accent-default)', fontSize:'var(--text-sm)', fontFamily:'var(--font-sans)' }}>Clear filters</button>}/>
              : paged.map(f => (
                  <FindingRow key={f.id} finding={f} selected={selected.includes(f.id)} onSelect={()=>setSelected(s=>s.includes(f.id)?s.filter(x=>x!==f.id):[...s,f.id])} onClick={()=>navigate('finding',{sessionId,findingId:f.id})}/>
                ))
            }
            <div style={{ borderTop:'1px solid var(--border-default)' }}>
              <Pagination page={page} total={filtered.length} perPage={PER} onChange={setPage}/>
            </div>
          </Card>
      }
    </div>
  );
};

const FindingRow = ({ finding:f, selected, onSelect, onClick }) => {
  const [hov, setHov] = useState(false);
  return (
    <div onMouseEnter={()=>setHov(true)} onMouseLeave={()=>setHov(false)} style={{ display:'grid', gridTemplateColumns:'32px 110px minmax(200px,1fr) 110px minmax(120px,200px) 110px 80px', gap:'var(--sp-3)', padding:'var(--sp-3) var(--sp-4)', borderBottom:'1px solid var(--border-default)', alignItems:'center', cursor:'pointer', background: selected ? 'var(--accent-subtle)' : hov ? 'var(--surface-hover)' : 'transparent', transition:'background var(--transition)' }}>
      <div onClick={e=>{e.stopPropagation();onSelect();}} style={{ display:'flex', alignItems:'center' }}>
        <input type="checkbox" checked={selected} onChange={onSelect} style={{ width:13, height:13, accentColor:'var(--accent-default)', cursor:'pointer' }}/>
      </div>
      <div onClick={onClick}><SeverityBadge level={f.severity}/></div>
      <div onClick={onClick} style={{ minWidth:0 }}>
        <p style={{ fontSize:'var(--text-base)', color:'var(--text-primary)', overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap', fontWeight:500 }}>{f.title}</p>
      </div>
      <div onClick={onClick}><span style={{ fontSize:'var(--text-sm)', color:'var(--text-secondary)' }}>{f.category}</span></div>
      <div onClick={onClick} style={{ minWidth:0 }}>
        <span style={{ fontSize:'var(--text-sm)', color:'var(--text-tertiary)', fontFamily:'var(--font-mono)', overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap', display:'block' }}>{f.location}:{f.line}</span>
      </div>
      <div onClick={onClick}><StatusBadge status={f.status}/></div>
      <div onClick={onClick}><span style={{ fontSize:'var(--text-sm)', color:'var(--text-tertiary)' }}>{f.age}</span></div>
    </div>
  );
};

// ─── REPORTS TAB ─────────────────────────────────────────────────────────────
const FORMAT_ICONS = { PDF:'file-text', JSON:'code', CSV:'list', HTML:'terminal' };

const ReportsTab = ({ sessionId, session, reports }) => {
  const { addReport } = useApp();
  const toast = useToast();
  const [showGen, setShowGen] = useState(false);

  return (
    <div style={{ padding:'var(--sp-6)' }}>
      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:'var(--sp-6)' }}>
        <h2 style={{ fontSize:'var(--text-md)', fontWeight:500 }}>Generated reports</h2>
        <Btn variant="primary" icon="file-text" onClick={()=>setShowGen(true)}>Generate report</Btn>
      </div>
      {reports.length === 0
        ? <EmptyState title="No reports yet" description="Generate your first report to share with auditors." action={<Btn variant="primary" icon="file-text" onClick={()=>setShowGen(true)}>Generate report</Btn>}/>
        : <Card hover={false} style={{ overflow:'hidden' }}>
            {reports.map((r,i) => (
              <div key={r.id} style={{ display:'flex', alignItems:'center', gap:'var(--sp-4)', padding:'var(--sp-3) var(--sp-4)', borderBottom: i<reports.length-1 ? '1px solid var(--border-default)' : 'none' }}>
                <Icon name={FORMAT_ICONS[r.format]||'file'} size={16} style={{ color:'var(--text-secondary)' }}/>
                <span style={{ fontSize:'var(--text-base)', fontWeight:500, color:'var(--text-primary)', width:50 }}>{r.format}</span>
                <span style={{ fontSize:'var(--text-sm)', color:'var(--text-tertiary)', flex:1 }}>{r.date} · {r.findings} findings · Score {r.score ?? '—'}/100</span>
                {r.signed && <span style={{ display:'flex', alignItems:'center', gap:4, fontSize:'var(--text-sm)', color:'var(--status-success)' }}><Icon name="lock" size={12}/>Signed</span>}
                <Btn variant="ghost" size="sm" icon="download" onClick={()=>toast(`Downloading ${r.format} report…`,'info')}>Download</Btn>
              </div>
            ))}
          </Card>
      }
      <GenerateReportModal open={showGen} onClose={()=>setShowGen(false)} onGenerate={(fmt)=>{ addReport(sessionId, fmt); setShowGen(false); toast('Report generated','success'); }}/>
    </div>
  );
};

const GenerateReportModal = ({ open, onClose, onGenerate }) => {
  const [format, setFormat] = useState('PDF');
  const [framework, setFramework] = useState('all');
  const [includeResolved, setIncludeResolved] = useState(false);
  const [includeRemediation, setIncludeRemediation] = useState(true);
  const [signReport, setSignReport] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [done, setDone] = useState(false);

  const handleGenerate = () => {
    setGenerating(true);
    setTimeout(() => { setGenerating(false); setDone(true); }, 1800);
  };

  const handleClose = () => { setDone(false); setGenerating(false); onClose(); };

  return (
    <Modal open={open} onClose={handleClose} title="Generate report" width={500}
      footer={<>
        <Btn variant="ghost" onClick={handleClose}>Cancel</Btn>
        {done
          ? <Btn variant="primary" icon="download" onClick={()=>{ onGenerate(format); handleClose(); }}>Download report</Btn>
          : <Btn variant="primary" onClick={handleGenerate} loading={generating}>Generate</Btn>
        }
      </>}>
      <div style={{ display:'flex', flexDirection:'column', gap:'var(--sp-5)' }}>
        {done && <div style={{ display:'flex', alignItems:'center', gap:'var(--sp-3)', padding:'var(--sp-4)', background:'var(--status-success-bg)', borderRadius:'var(--radius-card)', border:'1px solid var(--status-success)' }}>
          <Icon name="check-circle" size={16} style={{ color:'var(--status-success)' }}/>
          <span style={{ fontSize:'var(--text-base)', color:'var(--text-primary)' }}>Report generated successfully. Click download to save.</span>
        </div>}
        <div>
          <label style={{ fontSize:'var(--text-sm)', fontWeight:500, color:'var(--text-secondary)', display:'block', marginBottom:'var(--sp-2)' }}>Format</label>
          <SegmentedControl options={['PDF','JSON','CSV','HTML'].map(v=>({value:v,label:v}))} value={format} onChange={setFormat}/>
        </div>
        <SelectInput label="Compliance framework" value={framework} onChange={e=>setFramework(e.target.value)} options={[{value:'all',label:'All frameworks'},{value:'hipaa',label:'HIPAA'},{value:'soc2',label:'SOC2'},{value:'owasp',label:'OWASP API'}]}/>
        <div style={{ display:'flex', flexDirection:'column', gap:'var(--sp-3)' }}>
          <Toggle label="Include resolved findings" checked={includeResolved} onChange={()=>setIncludeResolved(x=>!x)}/>
          <Toggle label="Include remediation guidance" checked={includeRemediation} onChange={()=>setIncludeRemediation(x=>!x)}/>
          <Toggle label="Sign report (recommended)" checked={signReport} onChange={()=>setSignReport(x=>!x)}/>
        </div>
      </div>
    </Modal>
  );
};

// ─── AUDIT TRAIL TAB ─────────────────────────────────────────────────────────
const AuditTrailTab = ({ events }) => {
  const [expanded, setExpanded] = useState({});
  const [verifyOpen, setVerifyOpen] = useState(false);
  const [verifying, setVerifying] = useState(false);
  const [verified, setVerified] = useState(null);

  const handleVerify = () => {
    setVerifying(true); setVerified(null);
    setTimeout(() => { setVerifying(false); setVerified(true); }, 1500);
  };

  return (
    <div style={{ padding:'var(--sp-6)' }}>
      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:'var(--sp-6)' }}>
        <h2 style={{ fontSize:'var(--text-md)', fontWeight:500 }}>Audit trail</h2>
        <Btn variant="outline" icon="shield-check" onClick={()=>setVerifyOpen(true)}>Verify chain integrity</Btn>
      </div>
      {events.length === 0
        ? <EmptyState title="No events recorded" description="Session activity will appear here as you upload files, run scans, and generate reports."/>
        : <Card hover={false} style={{ overflow:'hidden' }}>
            {events.map((ev, i) => (
              <div key={ev.id} style={{ borderBottom: i < events.length-1 ? '1px solid var(--border-default)' : 'none' }}>
                <div style={{ display:'grid', gridTemplateColumns:'150px 28px 1fr 28px', gap:'var(--sp-3)', padding:'var(--sp-3) var(--sp-4)', alignItems:'center', cursor:'pointer', transition:'background var(--transition)' }}
                  onClick={()=>setExpanded(x=>({...x,[ev.id]:!x[ev.id]}))}
                  onMouseEnter={e=>e.currentTarget.style.background='var(--surface-hover)'}
                  onMouseLeave={e=>e.currentTarget.style.background='transparent'}>
                  <span style={{ fontSize:'var(--text-sm)', color:'var(--text-tertiary)', fontFamily:'var(--font-mono)', whiteSpace:'nowrap' }}>{ev.ts}</span>
                  <Icon name={ev.icon} size={14} style={{ color:'var(--text-secondary)' }}/>
                  <div>
                    <span style={{ fontSize:'var(--text-base)', color:'var(--text-primary)' }}>{ev.summary}</span>
                    <span style={{ fontSize:'var(--text-sm)', color:'var(--text-tertiary)', marginLeft:'var(--sp-2)' }}>· {ev.actor}</span>
                  </div>
                  <Icon name={expanded[ev.id]?'chevron-up':'chevron-right'} size={13} style={{ color:'var(--text-tertiary)' }}/>
                </div>
                {expanded[ev.id] && (
                  <div style={{ padding:'var(--sp-4) var(--sp-4) var(--sp-4) calc(150px + var(--sp-3) + 28px + var(--sp-3))', background:'var(--surface-sunken)', display:'flex', flexDirection:'column', gap:'var(--sp-3)' }}>
                    <div>
                      <p style={{ fontSize:'var(--text-sm)', color:'var(--text-tertiary)', marginBottom:'var(--sp-1)' }}>Payload</p>
                      <code style={{ fontSize:'var(--text-sm)', fontFamily:'var(--font-mono)', color:'var(--text-primary)', background:'var(--surface-elevated)', padding:'var(--sp-2) var(--sp-3)', borderRadius:'var(--radius-badge)', border:'1px solid var(--border-default)', display:'block', wordBreak:'break-all' }}>{ev.payload}</code>
                    </div>
                    <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr', gap:'var(--sp-4)' }}>
                      {[['Previous hash', ev.prevHash||'(genesis)'],['Current hash', ev.hash]].map(([label, val])=>(
                        <div key={label}>
                          <p style={{ fontSize:'var(--text-sm)', color:'var(--text-tertiary)', marginBottom:'var(--sp-1)' }}>{label}</p>
                          <div style={{ display:'flex', alignItems:'center', gap:'var(--sp-1)' }}>
                            <code style={{ fontSize:'var(--text-sm)', fontFamily:'var(--font-mono)', color:'var(--text-primary)' }}>{val}</code>
                            {val && val !== '(genesis)' && <button onClick={()=>navigator.clipboard?.writeText(val)} style={{ background:'none', border:'none', cursor:'pointer', color:'var(--text-tertiary)', padding:2, display:'flex' }}><Icon name="copy" size={11}/></button>}
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            ))}
          </Card>
      }

      {/* Verify modal */}
      <Modal open={verifyOpen} onClose={()=>{ setVerifyOpen(false); setVerified(null); }} title="Verify chain integrity" width={440}
        footer={<Btn variant="primary" onClick={()=>setVerifyOpen(false)}>Close</Btn>}>
        {!verified && !verifying && (
          <div style={{ textAlign:'center', padding:'var(--sp-6) 0' }}>
            <p style={{ color:'var(--text-secondary)', marginBottom:'var(--sp-4)' }}>Verify that the cryptographic hash chain for this session's audit trail has not been tampered with.</p>
            <Btn variant="primary" onClick={handleVerify}>Run verification</Btn>
          </div>
        )}
        {verifying && (
          <div style={{ textAlign:'center', padding:'var(--sp-8) 0' }}>
            <Spinner size={24} color="var(--accent-default)"/>
            <p style={{ color:'var(--text-secondary)', marginTop:'var(--sp-4)' }}>Verifying {events.length} events…</p>
          </div>
        )}
        {verified && (
          <div style={{ display:'flex', flexDirection:'column', alignItems:'center', gap:'var(--sp-4)', padding:'var(--sp-6) 0' }}>
            <div style={{ width:48, height:48, borderRadius:'50%', background:'var(--status-success-bg)', display:'flex', alignItems:'center', justifyContent:'center' }}>
              <Icon name="check-circle" size={24} style={{ color:'var(--status-success)' }}/>
            </div>
            <div style={{ textAlign:'center' }}>
              <p style={{ fontSize:'var(--text-md)', fontWeight:500, color:'var(--text-primary)', marginBottom:4 }}>Chain integrity verified</p>
              <p style={{ fontSize:'var(--text-sm)', color:'var(--text-tertiary)' }}>{events.length} events verified · No tampering detected</p>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
};

Object.assign(window, { SessionDetailScreen, UploadsTab, PoliciesTab, ScansTab, FindingsTab, ReportsTab, AuditTrailTab, GenerateReportModal });
