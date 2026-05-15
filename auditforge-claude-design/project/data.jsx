// data.jsx — Mock data & App context for AuditForge
const { useState, useContext, createContext } = React;

// ─── MOCK SESSIONS ────────────────────────────────────────────────────────────
const SESSIONS = [
  {
    id: 's1', name: 'Q2 2026 HIPAA Compliance Audit', status: 'completed', score: 72,
    env: 'production', created: '2026-05-10T09:00:00Z', lastActivity: '2 days ago',
    description: 'Full HIPAA technical safeguards review of the patient portal and associated microservices.',
    findings: { critical:2, high:5, medium:9, low:6 },
  },
  {
    id: 's2', name: 'API Security Review — Payment Service', status: 'completed', score: 91,
    env: 'staging', created: '2026-05-08T14:00:00Z', lastActivity: '4 days ago',
    description: 'OWASP API Top 10 assessment of the payment processing API prior to production release.',
    findings: { critical:0, high:2, medium:3, low:4 },
  },
  {
    id: 's3', name: 'SOC2 Type II Readiness Assessment', status: 'scanning', score: null,
    env: 'production', created: '2026-05-15T08:30:00Z', lastActivity: 'just now',
    description: 'Pre-audit SOC2 readiness check across all production infrastructure and service configurations.',
    findings: { critical:1, high:3, medium:0, low:0 },
  },
  {
    id: 's4', name: 'Cloud Infrastructure — AWS Production', status: 'failed', score: null,
    env: 'production', created: '2026-05-12T11:00:00Z', lastActivity: '3 days ago',
    description: 'Cloud configuration review for AWS production environment against CIS benchmarks.',
    findings: { critical:0, high:0, medium:0, low:0 },
  },
  {
    id: 's5', name: 'OWASP API Top 10 Review — v3 Auth Service', status: 'created', score: null,
    env: 'development', created: '2026-05-15T07:00:00Z', lastActivity: '5 hours ago',
    description: '',
    findings: { critical:0, high:0, medium:0, low:0 },
  },
];

// ─── MOCK FINDINGS ────────────────────────────────────────────────────────────
const FINDINGS = {
  s1: [
    {
      id:'f1', sessionId:'s1', severity:'critical', status:'open',
      title:'PHI transmitted over unencrypted HTTP',
      category:'Data Protection', rule:'HIPAA §164.312(e)(1)', age:'5 days',
      location:'src/api/patient.py', line:247,
      complianceRefs:['HIPAA §164.312(e)(1)', 'HIPAA §164.312(a)(2)(iv)', 'NIST 800-53 SC-8'],
      description:`Patient health information (PHI) is being transmitted from the patient portal API to the analytics backend over an unencrypted HTTP connection. Any network-level observer can read, modify, or replay this traffic. HIPAA §164.312(e)(1) requires that covered entities implement technical security measures to guard against unauthorized access to ePHI transmitted over electronic communications networks.`,
      evidence: `# patient.py — analytics forwarder
def forward_to_analytics(patient_record):
    # TODO: switch to internal HTTPS endpoint
    url = "http://analytics-internal.svc/ingest"
    payload = {
        "patient_id": patient_record.id,
        "dob": patient_record.date_of_birth,
        "diagnosis": patient_record.icd_codes,
        "provider_id": patient_record.assigned_provider
    }
    response = requests.post(url, json=payload)
    return response.status_code`,
      highlightLines:[3,4],
      remediation:`Replace the HTTP endpoint with an HTTPS endpoint and verify the server certificate:

url = "https://analytics-internal.svc/ingest"
response = requests.post(url, json=payload, verify="/etc/ssl/certs/ca-bundle.crt")`,
      references:['https://www.hhs.gov/hipaa/for-professionals/security/guidance/index.html', 'https://csrc.nist.gov/publications/detail/sp/800-53/rev-5/final'],
    },
    {
      id:'f2', sessionId:'s1', severity:'critical', status:'open',
      title:'PHI written in plaintext to application logs',
      category:'Logging & Monitoring', rule:'HIPAA §164.312(b)', age:'5 days',
      location:'src/services/auth_service.py', line:89,
      complianceRefs:['HIPAA §164.312(b)', 'OWASP API3:2023'],
      description:`The authentication service logs full patient records including diagnosis codes and prescription data at the DEBUG level. These logs are retained for 90 days in an unencrypted log aggregation system accessible to all engineering staff. This constitutes improper disclosure of PHI and violates minimum necessary use principles.`,
      evidence:`def authenticate_patient(patient_id, token):
    patient = db.get_patient(patient_id)
    logger.debug(f"Auth attempt: {patient}")   # logs full object
    if not verify_token(patient.token_hash, token):
        logger.warning(f"Failed login for {patient.ssn}")
        raise AuthError("Invalid token")`,
      highlightLines:[3,5],
      remediation:`Log only non-sensitive identifiers:

logger.debug(f"Auth attempt for patient_id={patient_id}")
if not verify_token(patient.token_hash, token):
    logger.warning(f"Failed login for patient_id={patient_id}")`,
      references:['https://owasp.org/API-Security/editions/2023/en/0xa3-broken-object-property-level-authorization/'],
    },
    {
      id:'f3', sessionId:'s1', severity:'high', status:'acknowledged',
      title:'Missing authentication on /api/v1/patients endpoint',
      category:'Access Control', rule:'HIPAA §164.312(a)(1)', age:'5 days',
      location:'src/api/routes.py', line:42,
      complianceRefs:['HIPAA §164.312(a)(1)', 'OWASP API1:2023'],
      description:`The /api/v1/patients list endpoint does not require an authenticated session. Any unauthenticated HTTP client can retrieve the full patient roster. This endpoint was discovered to return 1,240 patient records in testing.`,
      evidence:`@app.route("/api/v1/patients", methods=["GET"])
def list_patients():
    # Missing @require_auth decorator
    patients = db.query(Patient).limit(5000).all()
    return jsonify([p.to_dict() for p in patients])`,
      highlightLines:[1,3],
      remediation:`Add the authentication decorator and restrict scope:

@app.route("/api/v1/patients", methods=["GET"])
@require_auth(roles=["provider", "admin"])
def list_patients():
    patients = db.query(Patient).filter_by(provider_id=current_user.id).all()`,
      references:['https://owasp.org/API-Security/editions/2023/en/0xa1-broken-object-level-authorization/'],
    },
    {
      id:'f4', sessionId:'s1', severity:'high', status:'open',
      title:'Weak session token — insufficient entropy',
      category:'Authentication', rule:'NIST 800-63B', age:'5 days',
      location:'src/auth/tokens.py', line:18,
      complianceRefs:['NIST 800-63B §4.2.3', 'OWASP API2:2023'],
      description:`Session tokens are generated using Python's random module seeded with the current timestamp. This produces predictable tokens that can be brute-forced. The token space is approximately 2^32, far below the NIST minimum of 128 bits of entropy.`,
      evidence:`import random
import time

def generate_session_token():
    random.seed(int(time.time()))  # predictable seed
    return hex(random.getrandbits(32))[2:]`,
      highlightLines:[5,6],
      remediation:`Use secrets.token_hex for cryptographically secure tokens:

import secrets

def generate_session_token():
    return secrets.token_hex(32)  # 256 bits of entropy`,
      references:['https://pages.nist.gov/800-63-3/sp800-63b.html'],
    },
    {
      id:'f5', sessionId:'s1', severity:'high', status:'open',
      title:'SQL injection via unsanitized patient search parameter',
      category:'Injection', rule:'OWASP API8:2023', age:'5 days',
      location:'src/api/search.py', line:31,
      complianceRefs:['OWASP API8:2023', 'CWE-89'],
      description:`The patient search endpoint constructs a SQL query by directly interpolating the user-supplied "name" query parameter into the SQL string without parameterization. This allows an attacker to exfiltrate the entire database or modify records.`,
      evidence:`@app.route("/api/v1/search")
def search_patients():
    name = request.args.get("name", "")
    query = f"SELECT * FROM patients WHERE name LIKE '%{name}%'"
    results = db.execute(query).fetchall()`,
      highlightLines:[4],
      remediation:`Use parameterized queries:

query = "SELECT * FROM patients WHERE name LIKE :name"
results = db.execute(query, {"name": f"%{name}%"}).fetchall()`,
      references:['https://owasp.org/API-Security/'],
    },
    {
      id:'f6', sessionId:'s1', severity:'medium', status:'open',
      title:'Missing rate limiting on authentication endpoint',
      category:'Rate Limiting', rule:'OWASP API4:2023', age:'5 days',
      location:'src/api/auth.py', line:15,
      complianceRefs:['OWASP API4:2023'],
      description:`The /api/v1/auth/login endpoint does not implement rate limiting. An attacker can submit unlimited authentication attempts, enabling credential stuffing and brute-force attacks.`,
      evidence:`@app.route("/api/v1/auth/login", methods=["POST"])
def login():
    # No rate limiting configured
    email = request.json.get("email")
    password = request.json.get("password")`,
      highlightLines:[3],
      remediation:`Apply a rate limiter using Flask-Limiter:

from flask_limiter import Limiter
limiter = Limiter(app, default_limits=["5 per minute"])

@app.route("/api/v1/auth/login", methods=["POST"])
@limiter.limit("5 per minute")
def login():`,
      references:['https://owasp.org/API-Security/'],
    },
    {
      id:'f7', sessionId:'s1', severity:'medium', status:'resolved',
      title:'Insecure direct object reference on /api/v1/records/{id}',
      category:'Access Control', rule:'OWASP API1:2023', age:'5 days',
      location:'src/api/records.py', line:58,
      complianceRefs:['OWASP API1:2023', 'HIPAA §164.312(a)(1)'],
      description:`The record retrieval endpoint does not verify that the requesting user has access to the requested record ID. Any authenticated user can enumerate all record IDs and retrieve any record.`,
      evidence:`@app.route("/api/v1/records/<int:record_id>")
@require_auth
def get_record(record_id):
    # No ownership check
    return db.query(MedicalRecord).get(record_id).to_dict()`,
      highlightLines:[4],
      remediation:`Add ownership verification before returning data.`,
      references:['https://owasp.org/API-Security/'],
    },
    {
      id:'f8', sessionId:'s1', severity:'medium', status:'open',
      title:'Overly verbose error messages expose stack traces',
      category:'Security Misconfiguration', rule:'OWASP API7:2023', age:'5 days',
      location:'src/app.py', line:12,
      complianceRefs:['OWASP API7:2023'],
      description:`The application is running in DEBUG mode in production, causing Flask to return full stack traces and environment variable dumps in HTTP error responses.`,
      evidence:`app = Flask(__name__)
app.config["DEBUG"] = True   # should be False in production
app.config["PROPAGATE_EXCEPTIONS"] = True`,
      highlightLines:[2],
      remediation:`Disable debug mode and use a generic error handler in production.`,
      references:[],
    },
    { id:'f9',  sessionId:'s1', severity:'medium', status:'open', title:'JWT secret stored as plaintext environment variable', category:'Secrets Management', rule:'CWE-798', age:'5 days', location:'src/config.py', line:5, complianceRefs:['CWE-798'], description:'The JWT signing secret is stored as a plaintext environment variable exposed in CI/CD logs.', evidence:'JWT_SECRET = os.getenv("JWT_SECRET", "supersecret123")', highlightLines:[1], remediation:'Store secrets in a dedicated secret manager such as AWS Secrets Manager or HashiCorp Vault.', references:[] },
    { id:'f10', sessionId:'s1', severity:'low', status:'open', title:'Missing Strict-Transport-Security header', category:'Security Headers', rule:'OWASP API7:2023', age:'5 days', location:'src/middleware.py', line:22, complianceRefs:['OWASP API7:2023'], description:'The HSTS header is absent, allowing clients to connect over HTTP.', evidence:'# No HSTS header added', highlightLines:[], remediation:'Add HSTS header with a minimum max-age of 31536000.', references:[] },
    { id:'f11', sessionId:'s1', severity:'low', status:'open', title:'Missing Content-Security-Policy header', category:'Security Headers', rule:'OWASP API7:2023', age:'5 days', location:'src/middleware.py', line:23, complianceRefs:[], description:'CSP header is not configured, increasing XSS risk.', evidence:'# No CSP header', highlightLines:[], remediation:'Define a strict CSP policy appropriate for your application.', references:[] },
  ],
  s2: [
    { id:'f20', sessionId:'s2', severity:'high', status:'open', title:'Mass assignment vulnerability on /v1/account endpoint', category:'Access Control', rule:'OWASP API6:2023', age:'7 days', location:'src/controllers/account.js', line:34, complianceRefs:['OWASP API6:2023'], description:'The account update endpoint accepts and persists all request body fields without a whitelist, allowing users to elevate their own privileges.', evidence:'const account = await Account.update(req.body);', highlightLines:[1], remediation:'Use explicit field allowlisting before persisting data.', references:[] },
    { id:'f21', sessionId:'s2', severity:'high', status:'acknowledged', title:'Unrestricted file upload accepts executable MIME types', category:'Input Validation', rule:'OWASP API8:2023', age:'7 days', location:'src/routes/upload.js', line:18, complianceRefs:['OWASP API8:2023'], description:'The file upload endpoint does not validate MIME type or file extension, allowing upload of executable files.', evidence:`if (!file) return res.status(400).json({ error: 'No file' });
// Missing: MIME type and extension validation
await storage.save(file);`, highlightLines:[2], remediation:'Validate MIME type and restrict allowed extensions to a safe allowlist.', references:[] },
    { id:'f22', sessionId:'s2', severity:'medium', status:'open', title:'Missing pagination on /v1/transactions', category:'Resource Management', rule:'OWASP API4:2023', age:'7 days', location:'src/routes/transactions.js', line:10, complianceRefs:['OWASP API4:2023'], description:'The transactions endpoint returns all records without pagination, enabling resource exhaustion.', evidence:'const txns = await Transaction.findAll();', highlightLines:[1], remediation:'Add LIMIT/OFFSET pagination with a maximum page size of 100.', references:[] },
  ],
};

// ─── MOCK UPLOADS ─────────────────────────────────────────────────────────────
const UPLOADS = {
  s1: [
    { id:'u1', name:'patient-portal.yaml',  type:'spec',   size:'42 KB',  date:'5 days ago' },
    { id:'u2', name:'auth_service.py',       type:'code',   size:'128 KB', date:'5 days ago' },
    { id:'u3', name:'database.conf',         type:'config', size:'8 KB',   date:'4 days ago' },
    { id:'u4', name:'patient-data-schema.sql',type:'db',    size:'2.1 MB', date:'4 days ago' },
    { id:'u5', name:'aws-infra-config.json', type:'config', size:'14 KB',  date:'3 days ago' },
  ],
  s2: [
    { id:'u10', name:'payment-api-spec.yaml', type:'spec', size:'38 KB', date:'7 days ago' },
    { id:'u11', name:'auth.js',               type:'code', size:'54 KB', date:'7 days ago' },
  ],
  s3: [], s4: [], s5: [],
};

// ─── MOCK POLICIES ────────────────────────────────────────────────────────────
const ALL_POLICIES = [
  { id:'p1', name:'HIPAA Technical Safeguards',     framework:'HIPAA',  ruleCount:45, selected:true,
    rules:[
      {id:'r1', name:'Encryption in transit',         severity:'critical', category:'Data Protection'},
      {id:'r2', name:'PHI logging controls',           severity:'critical', category:'Logging'},
      {id:'r3', name:'Access control enforcement',     severity:'high',     category:'Access Control'},
      {id:'r4', name:'Audit log integrity',            severity:'high',     category:'Logging'},
      {id:'r5', name:'Minimum necessary access',       severity:'medium',   category:'Access Control'},
    ]
  },
  { id:'p2', name:'OWASP API Top 10 (2023)',         framework:'OWASP',  ruleCount:34, selected:true,
    rules:[
      {id:'r10', name:'Broken object-level auth',      severity:'critical', category:'Access Control'},
      {id:'r11', name:'Broken authentication',          severity:'critical', category:'Authentication'},
      {id:'r12', name:'Broken object property auth',    severity:'high',     category:'Access Control'},
      {id:'r13', name:'Unrestricted resource consumption',severity:'medium', category:'Rate Limiting'},
    ]
  },
  { id:'p3', name:'SOC2 CC6 — Logical Access',      framework:'SOC2',   ruleCount:28, selected:false, rules:[] },
  { id:'p4', name:'SOC2 CC7 — System Operations',   framework:'SOC2',   ruleCount:19, selected:false, rules:[] },
  { id:'p5', name:'CIS AWS Foundations Benchmark',   framework:'CIS',    ruleCount:67, selected:false, rules:[] },
  { id:'p6', name:'NIST 800-53 Access Control',      framework:'NIST',   ruleCount:52, selected:false, rules:[] },
];

// ─── MOCK AUDIT EVENTS ────────────────────────────────────────────────────────
const AUDIT_EVENTS = {
  s1: [
    { id:'e8', ts:'2026-05-13 16:22', type:'report_downloaded', actor:'auditor@external.com', summary:'Downloaded signed PDF report', icon:'download', hash:'a9f3c2d1', prevHash:'7e4b8a91', payload:'{"report_id":"r1","format":"PDF","ip":"203.0.113.42"}', expanded:false },
    { id:'e7', ts:'2026-05-12 09:05', type:'report_generated',  actor:'jane.doe@acme.com',    summary:'Generated signed PDF report (22 findings, score 72)', icon:'file-text', hash:'7e4b8a91', prevHash:'2d6f0e33', payload:'{"format":"PDF","finding_count":22,"score":72,"signed":true}', expanded:false },
    { id:'e6', ts:'2026-05-11 14:30', type:'finding_updated',   actor:'john.smith@acme.com',  summary:'Acknowledged finding: Missing authentication on /api/v1/patients', icon:'check', hash:'2d6f0e33', prevHash:'b1c4a728', payload:'{"finding_id":"f3","old_status":"open","new_status":"acknowledged"}', expanded:false },
    { id:'e5', ts:'2026-05-10 11:45', type:'scan_completed',    actor:'System',               summary:'Scan completed — 22 findings detected, score 72/100', icon:'check-circle', hash:'b1c4a728', prevHash:'f8e9d012', payload:'{"finding_count":22,"score":72,"duration_seconds":1382}', expanded:false },
    { id:'e4', ts:'2026-05-10 11:22', type:'scan_started',      actor:'System',               summary:'Scan started with 2 policy packs (HIPAA, OWASP API Top 10)', icon:'play', hash:'f8e9d012', prevHash:'c3a7b561', payload:'{"policy_packs":["p1","p2"],"target":"production"}', expanded:false },
    { id:'e3', ts:'2026-05-10 10:16', type:'file_uploaded',     actor:'jane.doe@acme.com',    summary:'Uploaded auth_service.py (128 KB)', icon:'upload-cloud', hash:'c3a7b561', prevHash:'91d4e820', payload:'{"filename":"auth_service.py","size_bytes":131072,"type":"code"}', expanded:false },
    { id:'e2', ts:'2026-05-10 10:15', type:'file_uploaded',     actor:'jane.doe@acme.com',    summary:'Uploaded patient-portal.yaml (42 KB)', icon:'upload-cloud', hash:'91d4e820', prevHash:'4f2c6b19', payload:'{"filename":"patient-portal.yaml","size_bytes":43008,"type":"spec"}', expanded:false },
    { id:'e1', ts:'2026-05-10 09:00', type:'session_created',   actor:'jane.doe@acme.com',    summary:'Created audit session "Q2 2026 HIPAA Compliance Audit"', icon:'plus', hash:'4f2c6b19', prevHash:null, payload:'{"session_name":"Q2 2026 HIPAA Compliance Audit","env":"production"}', expanded:false },
  ],
};

// ─── MOCK REPORTS ─────────────────────────────────────────────────────────────
const REPORTS = {
  s1: [
    { id:'r1', format:'PDF',  date:'2026-05-12', findings:22, score:72, signed:true },
    { id:'r2', format:'JSON', date:'2026-05-12', findings:22, score:72, signed:false },
  ],
  s2: [
    { id:'r10', format:'PDF', date:'2026-05-09', findings:9, score:91, signed:true },
  ],
  s3:[], s4:[], s5:[],
};

// ─── APP CONTEXT ──────────────────────────────────────────────────────────────
const AppContext = createContext(null);
const useApp = () => useContext(AppContext);

const AppProvider = ({ children }) => {
  const [sessions, setSessions]   = useState(SESSIONS);
  const [findings, setFindings]   = useState(FINDINGS);
  const [uploads,  setUploads]    = useState(UPLOADS);
  const [policies, setPolicies]   = useState(() => {
    const map = {};
    SESSIONS.forEach(s => { map[s.id] = ALL_POLICIES.map(p => ({ ...p, selected: s.id==='s1' ? (p.id==='p1'||p.id==='p2') : false })); });
    return map;
  });
  const [reports,  setReports]    = useState(REPORTS);
  const [auditEvents, setAuditEvents] = useState(AUDIT_EVENTS);

  const addSession = (sessionData) => {
    const id = 's' + Date.now();
    const newSession = { id, status:'created', score:null, lastActivity:'just now', findings:{critical:0,high:0,medium:0,low:0}, ...sessionData };
    setSessions(s => [newSession, ...s]);
    setFindings(f => ({ ...f, [id]:[] }));
    setUploads(u => ({ ...u, [id]:[] }));
    setReports(r => ({ ...r, [id]:[] }));
    setAuditEvents(e => ({ ...e, [id]:[] }));
    return id;
  };

  const deleteSession = (id) => setSessions(s => s.filter(x => x.id !== id));

  const updateFindingStatus = (sessionId, findingId, newStatus) => {
    setFindings(f => ({
      ...f,
      [sessionId]: (f[sessionId]||[]).map(x => x.id === findingId ? { ...x, status: newStatus } : x)
    }));
  };

  const addUpload = (sessionId, file) => {
    const newFile = { id:'u'+Date.now(), name:file.name, type:detectType(file.name), size:formatSize(file.size), date:'just now' };
    setUploads(u => ({ ...u, [sessionId]: [newFile, ...(u[sessionId]||[])] }));
  };

  const deleteUpload = (sessionId, uploadId) => setUploads(u => ({ ...u, [sessionId]: (u[sessionId]||[]).filter(x=>x.id!==uploadId) }));

  const addReport = (sessionId, format) => {
    const session = sessions.find(s=>s.id===sessionId);
    const totalFindings = Object.values(session?.findings||{}).reduce((a,b)=>a+b,0);
    const newReport = { id:'r'+Date.now(), format, date: new Date().toISOString().slice(0,10), findings:totalFindings, score:session?.score, signed:format==='PDF' };
    setReports(r => ({ ...r, [sessionId]: [newReport, ...(r[sessionId]||[])] }));
  };

  const detectType = (name) => {
    const ext = name.split('.').pop().toLowerCase();
    if (['py','js','ts','go','java','rb','rs'].includes(ext)) return 'code';
    if (['yaml','yml','json','toml','ini','conf'].includes(ext)) return 'config';
    if (['sql'].includes(ext)) return 'db';
    if (['yaml','yml','json'].includes(ext) && name.includes('spec')) return 'spec';
    return 'file';
  };
  const formatSize = (bytes) => bytes > 1024*1024 ? (bytes/1024/1024).toFixed(1)+' MB' : bytes > 1024 ? Math.round(bytes/1024)+' KB' : bytes+' B';

  return (
    <AppContext.Provider value={{ sessions, findings, uploads, policies, reports, auditEvents, addSession, deleteSession, updateFindingStatus, addUpload, deleteUpload, addReport, setPolicies, ALL_POLICIES }}>
      {children}
    </AppContext.Provider>
  );
};

Object.assign(window, { AppContext, AppProvider, useApp, ALL_POLICIES, SESSIONS, FINDINGS, UPLOADS, REPORTS, AUDIT_EVENTS });
