// screens-misc.jsx — Live Scan & Finding Detail
const { useState, useEffect, useRef } = React;

// ─── LIVE SCAN SCREEN ─────────────────────────────────────────────────────────
const SCAN_STEPS = [
  'Parsing patient-portal.yaml…',
  'Analyzing auth_service.py…',
  'Checking database.conf for misconfigurations…',
  'Evaluating patient-data-schema.sql…',
  'Scanning aws-infra-config.json…',
  'Running HIPAA Technical Safeguards ruleset…',
  'Running OWASP API Top 10 ruleset…',
  'Correlating findings across artifacts…',
  'Computing compliance score…',
  'Finalizing report…',
];

const LIVE_FINDINGS = [
  { severity:'critical', title:'PHI transmitted over unencrypted HTTP', location:'patient-portal.yaml:142' },
  { severity:'critical', title:'PHI written in plaintext to application logs', location:'auth_service.py:89' },
  { severity:'high',     title:'Missing authentication on /api/v1/patients', location:'auth_service.py:42' },
  { severity:'high',     title:'Weak session token — insufficient entropy',  location:'auth_service.py:18' },
  { severity:'high',     title:'SQL injection via unsanitized search param', location:'auth_service.py:31' },
  { severity:'medium',   title:'Missing rate limiting on auth endpoint',     location:'auth_service.py:15' },
  { severity:'medium',   title:'JWT secret stored as plaintext env var',     location:'database.conf:5' },
  { severity:'medium',   title:'Overly verbose error messages in production',location:'auth_service.py:12' },
  { severity:'low',      title:'Missing Strict-Transport-Security header',   location:'aws-infra-config.json:44' },
  { severity:'low',      title:'Missing Content-Security-Policy header',     location:'aws-infra-config.json:45' },
];

const LiveScanScreen = ({ sessionId, navigate }) => {
  const { sessions } = useApp();
  const session = sessions.find(s=>s.id===sessionId);
  const [progress, setProgress] = useState(0);
  const [stepIdx, setStepIdx]   = useState(0);
  const [liveFeed, setLiveFeed] = useState([]);
  const [counts, setCounts]     = useState({ critical:0, high:0, medium:0, low:0 });
  const [cancelled, setCancelled] = useState(false);
  const [complete, setComplete]   = useState(false);
  const feedRef = useRef(null);
  const intervalRef = useRef(null);

  useEffect(() => {
    if (cancelled) return;

    // Progress ticker
    intervalRef.current = setInterval(() => {
      setProgress(p => {
        const next = p + (Math.random() * 1.4 + 0.4);
        if (next >= 100) {
          clearInterval(intervalRef.current);
          setComplete(true);
          return 100;
        }
        return next;
      });
    }, 120);

    // Step ticker
    const stepTimer = setInterval(() => setStepIdx(i => Math.min(i+1, SCAN_STEPS.length-1)), 2200);

    // Finding injector
    let findingIdx = 0;
    const findingTimer = setInterval(() => {
      if (findingIdx >= LIVE_FINDINGS.length) { clearInterval(findingTimer); return; }
      const f = LIVE_FINDINGS[findingIdx++];
      setLiveFeed(feed => [{ ...f, id: Date.now(), ts:'just now' }, ...feed.slice(0,49)]);
      setCounts(c => ({ ...c, [f.severity]: c[f.severity]+1 }));
    }, 800);

    return () => { clearInterval(intervalRef.current); clearInterval(stepTimer); clearInterval(findingTimer); };
  }, [cancelled]);

  // Scroll feed to top when new findings arrive
  useEffect(() => { if (feedRef.current) feedRef.current.scrollTop = 0; }, [liveFeed.length]);

  const cancel = () => { clearInterval(intervalRef.current); setCancelled(true); navigate('session',{sessionId,tab:'scans'}); };

  return (
    <div style={{ padding:'var(--sp-6)', display:'flex', flexDirection:'column', gap:'var(--sp-6)', maxWidth:1100, margin:'0 auto' }}>
      {/* Scan header */}
      <div style={{ display:'flex', alignItems:'center', justifyContent:'space-between' }}>
        <div style={{ display:'flex', alignItems:'center', gap:'var(--sp-3)' }}>
          {!complete
            ? <span className="animate-pulse-opacity" style={{ width:10, height:10, borderRadius:'50%', background:'var(--accent-default)', flexShrink:0, display:'inline-block' }}/>
            : <Icon name="check-circle" size={16} style={{ color:'var(--status-success)' }}/>
          }
          <div>
            <h1 style={{ fontSize:'var(--text-lg)', fontWeight:500, color:'var(--text-primary)' }}>
              {complete ? 'Scan complete' : `Scanning — ${session?.name || 'Audit session'}`}
            </h1>
            <p style={{ fontSize:'var(--text-sm)', color:'var(--text-tertiary)', marginTop:2 }}>
              {complete ? `${liveFeed.length} findings detected · Score 72/100` : SCAN_STEPS[stepIdx]}
            </p>
          </div>
        </div>
        {!complete
          ? <Btn variant="destructive" icon="square" onClick={cancel}>Cancel scan</Btn>
          : <Btn variant="primary" onClick={()=>navigate('session',{sessionId,tab:'findings'})}>Review findings</Btn>
        }
      </div>

      {/* Progress bar */}
      <div style={{ display:'flex', flexDirection:'column', gap:'var(--sp-2)' }}>
        <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
          <span style={{ fontSize:'var(--text-sm)', color:'var(--text-secondary)' }}>
            {complete ? 'Analysis complete' : SCAN_STEPS[stepIdx]}
          </span>
          <span style={{ fontSize:'var(--text-sm)', fontFamily:'var(--font-mono)', color:'var(--text-secondary)', fontWeight:500 }}>{Math.round(progress)}%</span>
        </div>
        <div style={{ height:6, background:'var(--border-default)', borderRadius:3, overflow:'hidden' }}>
          <div style={{ height:'100%', width:`${progress}%`, background: complete ? 'var(--status-success)' : 'var(--accent-default)', borderRadius:3, transition:'width 120ms linear' }}/>
        </div>
      </div>

      {/* Completion banner */}
      {complete && (
        <div className="animate-slide-in" style={{ display:'flex', alignItems:'center', gap:'var(--sp-4)', padding:'var(--sp-4) var(--sp-6)', background:'var(--status-success-bg)', border:'1px solid var(--status-success)', borderRadius:'var(--radius-card)' }}>
          <Icon name="check-circle" size={20} style={{ color:'var(--status-success)', flexShrink:0 }}/>
          <div style={{ flex:1 }}>
            <p style={{ fontSize:'var(--text-base)', fontWeight:500, color:'var(--text-primary)' }}>Scan complete · {liveFeed.length} findings · Score 72/100</p>
            <p style={{ fontSize:'var(--text-sm)', color:'var(--text-secondary)', marginTop:2 }}>Review the findings below, then generate a signed compliance report.</p>
          </div>
          <Btn variant="primary" onClick={()=>navigate('session',{sessionId,tab:'findings'})}>Review findings</Btn>
        </div>
      )}

      {/* Severity counters + live feed */}
      <div style={{ display:'grid', gridTemplateColumns:'1fr 1.4fr', gap:'var(--sp-6)', alignItems:'start' }}>
        {/* Counters */}
        <div style={{ display:'flex', flexDirection:'column', gap:'var(--sp-4)' }}>
          <h2 style={{ fontSize:'var(--text-base)', fontWeight:500, color:'var(--text-secondary)' }}>Findings detected</h2>
          <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr', gap:'var(--sp-3)' }}>
            {['critical','high','medium','low'].map(l => (
              <div key={l} style={{ background:'var(--surface-secondary)', border:`1px solid var(--severity-${l}-bg)`, borderRadius:'var(--radius-card)', padding:'var(--sp-4)', display:'flex', flexDirection:'column', gap:'var(--sp-1)' }}>
                <span style={{ fontSize:'var(--text-xl)', fontWeight:600, color:`var(--severity-${l}-text)`, lineHeight:1, fontFamily:'var(--font-mono)' }} key={counts[l]}>
                  {counts[l]}
                </span>
                <span style={{ fontSize:'var(--text-sm)', color:'var(--text-tertiary)', textTransform:'capitalize' }}>{l}</span>
              </div>
            ))}
          </div>
          <SeverityBar counts={counts}/>
          <div style={{ fontSize:'var(--text-sm)', color:'var(--text-tertiary)', display:'flex', flexDirection:'column', gap:'var(--sp-2)' }}>
            <div style={{ display:'flex', gap:'var(--sp-2)' }}><Icon name="cpu" size={13}/> Google Gemini 1.5 Pro</div>
            <div style={{ display:'flex', gap:'var(--sp-2)' }}><Icon name="shield-check" size={13}/> HIPAA + OWASP API Top 10</div>
            <div style={{ display:'flex', gap:'var(--sp-2)' }}><Icon name="clock" size={13}/> Started 11:22 AM</div>
          </div>
        </div>

        {/* Live findings feed */}
        <div>
          <h2 style={{ fontSize:'var(--text-base)', fontWeight:500, color:'var(--text-secondary)', marginBottom:'var(--sp-3)' }}>Live findings feed</h2>
          <Card hover={false} style={{ overflow:'hidden' }}>
            <div ref={feedRef} style={{ maxHeight:380, overflowY:'auto' }}>
              {liveFeed.length === 0
                ? <div style={{ padding:'var(--sp-8)', textAlign:'center', color:'var(--text-tertiary)', fontSize:'var(--text-sm)' }}>
                    {complete ? 'No findings detected' : 'Waiting for findings…'}
                  </div>
                : liveFeed.map(f => (
                    <div key={f.id} className="animate-slide-in" style={{ display:'flex', alignItems:'flex-start', gap:'var(--sp-3)', padding:'var(--sp-3) var(--sp-4)', borderBottom:'1px solid var(--border-default)' }}>
                      <SeverityDot level={f.severity}/>
                      <div style={{ flex:1, minWidth:0 }}>
                        <p style={{ fontSize:'var(--text-sm)', color:'var(--text-primary)', fontWeight:500, overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap' }}>{f.title}</p>
                        <p style={{ fontSize:'var(--text-xs)', color:'var(--text-tertiary)', fontFamily:'var(--font-mono)', marginTop:2 }}>{f.location}</p>
                      </div>
                      <span style={{ fontSize:'var(--text-xs)', color:'var(--text-tertiary)', flexShrink:0 }}>just now</span>
                    </div>
                  ))
              }
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
};

// ─── FINDING DETAIL SCREEN ────────────────────────────────────────────────────
const FindingDetailScreen = ({ sessionId, findingId, navigate }) => {
  const { sessions, findings, updateFindingStatus } = useApp();
  const toast = useToast();
  const sessionFindings = findings[sessionId] || [];
  const finding = sessionFindings.find(f => f.id === findingId);
  const session = sessions.find(s => s.id === sessionId);
  const idx = sessionFindings.findIndex(f => f.id === findingId);
  const prev = idx > 0 ? sessionFindings[idx-1] : null;
  const next = idx < sessionFindings.length-1 ? sessionFindings[idx+1] : null;
  const [status, setStatus] = useState(finding?.status || 'open');
  const [comment, setComment] = useState('');

  useEffect(() => { if (finding) setStatus(finding.status); }, [findingId]);

  const handleStatusChange = (newStatus) => {
    setStatus(newStatus);
    updateFindingStatus(sessionId, findingId, newStatus);
    toast(`Finding marked as ${newStatus}`, 'success');
  };

  if (!finding) return <EmptyState title="Finding not found" action={<Btn onClick={()=>navigate('session',{sessionId,tab:'findings'})}>Back to findings</Btn>}/>;

  return (
    <div style={{ display:'flex', flexDirection:'column', height:'100%', minHeight:0 }}>
      {/* Header */}
      <div style={{ padding:'var(--sp-4) var(--sp-6)', borderBottom:'1px solid var(--border-default)', flexShrink:0 }}>
        <Breadcrumb items={[
          {label:'Sessions', onClick:()=>navigate('dashboard')},
          {label:session?.name||sessionId, onClick:()=>navigate('session',{sessionId,tab:'findings'})},
          {label:'Findings', onClick:()=>navigate('session',{sessionId,tab:'findings'})},
          {label:finding.title},
        ]}/>
        <div style={{ display:'flex', alignItems:'center', gap:'var(--sp-3)', marginTop:'var(--sp-3)' }}>
          <SeverityBadge level={finding.severity}/>
          <h1 style={{ fontSize:'var(--text-lg)', fontWeight:500, color:'var(--text-primary)', flex:1, overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap' }}>{finding.title}</h1>
          <SelectInput value={status} onChange={e=>handleStatusChange(e.target.value)}
            options={[{value:'open',label:'Open'},{value:'acknowledged',label:'Acknowledged'},{value:'false_positive',label:'False positive'},{value:'resolved',label:'Resolved'}]}/>
        </div>
      </div>

      {/* Body */}
      <div style={{ flex:1, overflowY:'auto', minHeight:0 }}>
        <div style={{ display:'grid', gridTemplateColumns:'300px 1fr', gap:0, minHeight:'100%' }}>
          {/* Left: Metadata */}
          <div style={{ borderRight:'1px solid var(--border-default)', padding:'var(--sp-6)', display:'flex', flexDirection:'column', gap:'var(--sp-6)' }}>
            <MetaSection title="Detection rule">
              <span style={{ fontSize:'var(--text-sm)', fontFamily:'var(--font-mono)', color:'var(--accent-default)' }}>{finding.rule}</span>
            </MetaSection>
            <MetaSection title="Severity">
              <SeverityBadge level={finding.severity}/>
            </MetaSection>
            <MetaSection title="Category">
              <span style={{ fontSize:'var(--text-base)', color:'var(--text-primary)' }}>{finding.category}</span>
            </MetaSection>
            <MetaSection title="Status">
              <StatusBadge status={status}/>
            </MetaSection>
            <MetaSection title="Source location">
              <span style={{ fontSize:'var(--text-sm)', fontFamily:'var(--font-mono)', color:'var(--text-primary)', wordBreak:'break-all' }}>{finding.location}:{finding.line}</span>
            </MetaSection>
            <MetaSection title="Compliance references">
              <div style={{ display:'flex', flexWrap:'wrap', gap:'var(--sp-1)' }}>
                {finding.complianceRefs?.map(ref => (
                  <span key={ref} style={{ fontSize:'var(--text-xs)', fontFamily:'var(--font-mono)', background:'var(--accent-subtle)', color:'var(--accent-default)', padding:'2px 6px', borderRadius:'var(--radius-badge)', cursor:'pointer', border:'1px solid var(--accent-default)', opacity:0.8 }}>{ref}</span>
                ))}
              </div>
            </MetaSection>
            <MetaSection title="Age">
              <span style={{ fontSize:'var(--text-base)', color:'var(--text-secondary)' }}>{finding.age}</span>
            </MetaSection>

            {/* Comments */}
            <div>
              <p style={{ fontSize:'var(--text-sm)', fontWeight:500, color:'var(--text-tertiary)', textTransform:'uppercase', letterSpacing:'0.05em', marginBottom:'var(--sp-3)' }}>Comments</p>
              <Textarea value={comment} onChange={e=>setComment(e.target.value)} placeholder="Add a comment…" rows={3}/>
              {comment && <div style={{ marginTop:'var(--sp-2)', display:'flex', justifyContent:'flex-end' }}>
                <Btn variant="primary" size="sm" onClick={()=>{ toast('Comment added','success'); setComment(''); }}>Add comment</Btn>
              </div>}
            </div>
          </div>

          {/* Right: Evidence + Description + Remediation */}
          <div style={{ padding:'var(--sp-6)', display:'flex', flexDirection:'column', gap:'var(--sp-6)' }}>
            {/* Evidence */}
            <section>
              <h2 style={{ fontSize:'var(--text-md)', fontWeight:500, color:'var(--text-primary)', marginBottom:'var(--sp-3)', display:'flex', alignItems:'center', gap:'var(--sp-2)' }}>
                <Icon name="code" size={15} style={{ color:'var(--text-tertiary)' }}/> Evidence
              </h2>
              <CodeBlock code={finding.evidence} highlightLines={finding.highlightLines} maxHeight={220}/>
            </section>

            {/* Description */}
            <section>
              <h2 style={{ fontSize:'var(--text-md)', fontWeight:500, color:'var(--text-primary)', marginBottom:'var(--sp-3)', display:'flex', alignItems:'center', gap:'var(--sp-2)' }}>
                <Icon name="info" size={15} style={{ color:'var(--text-tertiary)' }}/> Description
              </h2>
              <p style={{ fontSize:'var(--text-base)', color:'var(--text-secondary)', lineHeight:'var(--lh-body)' }}>{finding.description}</p>
            </section>

            {/* Remediation */}
            {finding.remediation && (
              <section>
                <h2 style={{ fontSize:'var(--text-md)', fontWeight:500, color:'var(--text-primary)', marginBottom:'var(--sp-3)', display:'flex', alignItems:'center', gap:'var(--sp-2)' }}>
                  <Icon name="zap" size={15} style={{ color:'var(--status-warning)' }}/> Remediation
                </h2>
                <CodeBlock code={finding.remediation} maxHeight={200}/>
              </section>
            )}

            {/* External references */}
            {finding.references?.length > 0 && (
              <section>
                <h2 style={{ fontSize:'var(--text-md)', fontWeight:500, color:'var(--text-primary)', marginBottom:'var(--sp-3)', display:'flex', alignItems:'center', gap:'var(--sp-2)' }}>
                  <Icon name="external-link" size={15} style={{ color:'var(--text-tertiary)' }}/> References
                </h2>
                <div style={{ display:'flex', flexDirection:'column', gap:'var(--sp-1)' }}>
                  {finding.references.map((ref,i) => (
                    <a key={i} href={ref} target="_blank" rel="noreferrer" style={{ fontSize:'var(--text-sm)', color:'var(--accent-default)', fontFamily:'var(--font-mono)', wordBreak:'break-all', display:'flex', alignItems:'center', gap:'var(--sp-1)', textDecoration:'none' }}
                      onMouseEnter={e=>e.currentTarget.style.textDecoration='underline'}
                      onMouseLeave={e=>e.currentTarget.style.textDecoration='none'}>
                      {ref}<Icon name="external-link" size={11}/>
                    </a>
                  ))}
                </div>
              </section>
            )}
          </div>
        </div>
      </div>

      {/* Bottom action bar */}
      <div style={{ borderTop:'1px solid var(--border-default)', padding:'var(--sp-3) var(--sp-6)', display:'flex', alignItems:'center', justifyContent:'space-between', background:'var(--surface-secondary)', flexShrink:0 }}>
        <Btn variant="ghost" icon="arrow-left" disabled={!prev} onClick={()=>prev&&navigate('finding',{sessionId,findingId:prev.id})}>Previous</Btn>
        <div style={{ display:'flex', alignItems:'center', gap:'var(--sp-2)', fontSize:'var(--text-sm)', color:'var(--text-tertiary)' }}>
          <span>{idx+1} of {sessionFindings.length}</span>
        </div>
        <Btn variant="ghost" iconRight="arrow-right" disabled={!next} onClick={()=>next&&navigate('finding',{sessionId,findingId:next.id})}>Next</Btn>
      </div>
    </div>
  );
};

const MetaSection = ({ title, children }) => (
  <div>
    <p style={{ fontSize:'var(--text-xs)', fontWeight:500, color:'var(--text-tertiary)', textTransform:'uppercase', letterSpacing:'0.05em', marginBottom:'var(--sp-2)' }}>{title}</p>
    {children}
  </div>
);

Object.assign(window, { LiveScanScreen, FindingDetailScreen });
