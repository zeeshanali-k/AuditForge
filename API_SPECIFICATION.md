# AuditForge API Specification

**Version:** 1.0
**Base URL:** `http://localhost:8080/api/v1` (configurable)
**Protocol:** HTTPS in production, HTTP for local dev
**Content-Type:** `application/json` unless otherwise stated

> **Note:** "AuditForge" is a working name. Replace with your final product name in implementation.

---

## Table of Contents

1. [Conventions](#1-conventions)
2. [Authentication](#2-authentication)
3. [Sessions](#3-sessions)
4. [Uploads](#4-uploads)
5. [Connectors](#5-connectors)
6. [Policies](#6-policies)
7. [Scans](#7-scans)
8. [Findings](#8-findings)
9. [Reports](#9-reports)
10. [Audit Trail](#10-audit-trail)
11. [Health](#11-health)
12. [Data Models Reference](#12-data-models-reference)

---

## 1. Conventions

### Request/Response Format
All requests and responses use JSON unless explicitly stated (e.g., file uploads use `multipart/form-data`).

### Timestamps
ISO 8601 UTC, e.g., `"2026-05-14T10:30:00Z"`.

### Identifiers
All resource IDs are UUIDs (v4), e.g., `"550e8400-e29b-41d4-a716-446655440000"`.

### Pagination
List endpoints accept query parameters:
- `page` (int, default `1`)
- `page_size` (int, default `20`, max `100`)

Paginated responses include a wrapper:
```json
{
  "data": [ /* array of items */ ],
  "pagination": {
    "page": 1,
    "page_size": 20,
    "total_items": 145,
    "total_pages": 8
  }
}
```

### Sorting & Filtering
- Sort: `?sort=created_at:desc` (field name + direction)
- Filter: documented per endpoint

### Error Response Format
All error responses follow this schema:
```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable explanation",
    "details": { /* optional, error-specific */ }
  }
}
```

### Standard HTTP Status Codes

| Code | Meaning |
|------|---------|
| 200 | OK |
| 201 | Created |
| 202 | Accepted (async operation started) |
| 204 | No Content |
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 409 | Conflict |
| 413 | Payload Too Large |
| 422 | Unprocessable Entity (validation) |
| 429 | Too Many Requests |
| 500 | Internal Server Error |
| 503 | Service Unavailable |

### Standard Error Codes

| Code | When |
|------|------|
| `BAD_REQUEST` | Malformed request |
| `UNAUTHORIZED` | Missing or invalid auth token |
| `FORBIDDEN` | Authenticated but not permitted |
| `NOT_FOUND` | Resource does not exist |
| `VALIDATION_ERROR` | Field-level validation failed |
| `CONFLICT` | State conflict (e.g., scan already running) |
| `PAYLOAD_TOO_LARGE` | File exceeds size limit |
| `INTERNAL_ERROR` | Server failure |
| `SCAN_FAILED` | Scan completed with errors |
| `RATE_LIMITED` | Too many requests |

---

## 2. Authentication

### POST `/auth/login`

Exchange credentials for a JWT bearer token.

**Auth:** Not required

**Request body:**
```json
{
  "email": "user@example.com",
  "password": "string"
}
```

**Response 200:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expires_at": "2026-05-15T10:30:00Z",
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "name": "Jane Doe",
    "role": "auditor"
  }
}
```

**Errors:** `401 UNAUTHORIZED` on invalid credentials.

---

### POST `/auth/logout`

Invalidate the current token.

**Auth:** Required

**Response 204:** No content.

---

### Authorization Header

All authenticated endpoints require:
```
Authorization: Bearer <token>
```

---

## 3. Sessions

A **session** represents one audit run, scoped to a set of uploaded artifacts and a chosen policy pack.

### POST `/sessions`

Create a new audit session.

**Auth:** Required

**Request body:**
```json
{
  "name": "Q2 2026 Compliance Audit",
  "description": "Pre-deployment audit for the patient portal release",
  "target_environment": "production"
}
```

**Field rules:**
- `name`: required, 3–120 chars
- `description`: optional, max 1000 chars
- `target_environment`: optional, enum: `development | staging | production`

**Response 201:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Q2 2026 Compliance Audit",
  "description": "...",
  "target_environment": "production",
  "status": "created",
  "created_at": "2026-05-14T10:30:00Z",
  "updated_at": "2026-05-14T10:30:00Z",
  "created_by": "uuid-of-user"
}
```

**Errors:** `422` if validation fails.

---

### GET `/sessions`

List sessions for the authenticated user.

**Auth:** Required

**Query parameters:**
- `page`, `page_size` (pagination)
- `status` (filter): `created | scanning | completed | failed`
- `sort` (default `created_at:desc`)

**Response 200:**
```json
{
  "data": [
    {
      "id": "uuid",
      "name": "Q2 2026 Compliance Audit",
      "status": "completed",
      "finding_count": 23,
      "overall_score": 78,
      "created_at": "2026-05-14T10:30:00Z"
    }
  ],
  "pagination": { ... }
}
```

---

### GET `/sessions/{session_id}`

Retrieve a specific session.

**Auth:** Required

**Response 200:** Full `Session` object (see Data Models).

**Errors:** `404` if not found.

---

### DELETE `/sessions/{session_id}`

Delete a session and all associated data. Audit trail records remain for compliance per retention policy.

**Auth:** Required

**Response 204:** No content.

**Errors:** `404`, `409` if scan in progress.

---

## 4. Uploads

### POST `/sessions/{session_id}/uploads`

Upload one or more files to a session.

**Auth:** Required

**Content-Type:** `multipart/form-data`

**Form fields:**
- `file` (binary, required): the file
- `upload_type` (text, required): enum — `codebase | openapi_spec | config | db_schema | env_file | terraform | kubernetes`
- `metadata` (text, optional): JSON-encoded extra context, e.g. `{"language":"python"}`

**Constraints:**
- Max file size: 100 MB
- Allowed file types per `upload_type`:
  - `codebase`: `.zip`, `.tar.gz`
  - `openapi_spec`: `.json`, `.yaml`, `.yml`
  - `config`: `.yaml`, `.yml`, `.json`, `.toml`, `.ini`
  - `db_schema`: `.sql`
  - `env_file`: `.env`
  - `terraform`: `.tf`, `.zip`
  - `kubernetes`: `.yaml`, `.yml`, `.zip`

**Response 201:**
```json
{
  "id": "uuid",
  "session_id": "uuid",
  "filename": "patient-portal.zip",
  "size_bytes": 4523112,
  "upload_type": "codebase",
  "uploaded_at": "2026-05-14T10:35:00Z",
  "checksum_sha256": "a1b2c3...",
  "metadata": { "language": "python" }
}
```

**Errors:** `413 PAYLOAD_TOO_LARGE`, `422` if type mismatch, `404` if session doesn't exist.

---

### GET `/sessions/{session_id}/uploads`

List all uploads for a session.

**Auth:** Required

**Response 200:**
```json
{
  "data": [
    {
      "id": "uuid",
      "filename": "patient-portal.zip",
      "upload_type": "codebase",
      "size_bytes": 4523112,
      "uploaded_at": "2026-05-14T10:35:00Z"
    }
  ]
}
```

---

### DELETE `/sessions/{session_id}/uploads/{upload_id}`

Remove a single upload from the session.

**Auth:** Required

**Response 204:** No content.

**Errors:** `404`, `409` if scan is using the upload.

---

## 5. Connectors

Connectors are the modules that read each upload type and produce normalized artifacts. Read-only — connectors are registered server-side.

### GET `/connectors`

List all available connectors.

**Auth:** Required

**Response 200:**
```json
{
  "data": [
    {
      "id": "code-scanner",
      "name": "Source Code Scanner",
      "version": "1.2.0",
      "description": "Scans codebases for hardcoded credentials, insecure patterns, and dependency vulnerabilities.",
      "supported_inputs": ["codebase"],
      "supported_languages": ["python", "javascript", "typescript", "go", "java", "kotlin"]
    },
    {
      "id": "openapi-auditor",
      "name": "OpenAPI Spec Auditor",
      "version": "1.0.0",
      "description": "Audits OpenAPI/Swagger specs for missing auth, weak schemes, and insecure transport.",
      "supported_inputs": ["openapi_spec"],
      "supported_languages": null
    }
  ]
}
```

---

### GET `/connectors/{connector_id}`

Get detailed information about a connector, including the policies it can evaluate.

**Auth:** Required

**Response 200:**
```json
{
  "id": "code-scanner",
  "name": "Source Code Scanner",
  "version": "1.2.0",
  "description": "...",
  "supported_inputs": ["codebase"],
  "supported_languages": ["python", "javascript", "..."],
  "artifact_types_produced": [
    "credential_entry",
    "import_statement",
    "function_definition",
    "api_call",
    "config_value"
  ]
}
```

---

## 6. Policies

### GET `/policies/packs`

List available policy packs (pre-built rule bundles for compliance frameworks).

**Auth:** Required

**Query parameters:**
- `framework` (filter): `OWASP | HIPAA | SOC2 | PCI-DSS | GDPR | custom`

**Response 200:**
```json
{
  "data": [
    {
      "id": "owasp-api-top10-2023",
      "name": "OWASP API Top 10 (2023)",
      "framework": "OWASP",
      "version": "2023",
      "description": "Industry-standard API security risks",
      "rule_count": 23,
      "is_custom": false
    },
    {
      "id": "hipaa-technical-safeguards",
      "name": "HIPAA Technical Safeguards",
      "framework": "HIPAA",
      "version": "2024-01",
      "description": "164.312 technical safeguards for PHI",
      "rule_count": 18,
      "is_custom": false
    }
  ]
}
```

---

### GET `/policies/packs/{pack_id}`

Get full pack details including all contained rules.

**Auth:** Required

**Response 200:**
```json
{
  "id": "owasp-api-top10-2023",
  "name": "OWASP API Top 10 (2023)",
  "framework": "OWASP",
  "version": "2023",
  "description": "...",
  "rules": [
    {
      "id": "owasp-api1-broken-object-auth",
      "name": "Broken Object Level Authorization",
      "severity": "critical",
      "category": "authorization",
      "description": "...",
      "check_type": "ai_evaluation",
      "compliance_mapping": ["OWASP-API1:2023"]
    }
  ]
}
```

---

### GET `/policies/rules`

List individual rules across all packs.

**Auth:** Required

**Query parameters:**
- `pack_id` (filter)
- `severity` (filter): `critical | high | medium | low`
- `category` (filter): `authentication | authorization | encryption | input_validation | logging | configuration`

**Response 200:** Paginated list of `Rule` objects.

---

### POST `/policies/rules`

Create a custom rule. (Advanced feature — may be hidden behind a feature flag in MVP.)

**Auth:** Required (admin role)

**Request body:**
```json
{
  "name": "Block hardcoded API keys",
  "description": "Detect API keys matching common prefixes",
  "severity": "high",
  "category": "configuration",
  "check_type": "pattern",
  "pattern": "(sk_live_|api_key_|AKIA)[A-Za-z0-9]{16,}",
  "applies_to_artifacts": ["config_value", "credential_entry"]
}
```

**Response 201:** Created `Rule` object.

---

## 7. Scans

A **scan** is a single execution that processes a session's uploads against a policy pack.

### POST `/sessions/{session_id}/scans`

Trigger a new scan. Asynchronous — returns immediately with a scan ID.

**Auth:** Required

**Request body:**
```json
{
  "policy_pack_ids": ["owasp-api-top10-2023", "hipaa-technical-safeguards"],
  "additional_rule_ids": ["custom-rule-uuid"],
  "config": {
    "enable_ai_analysis": true,
    "max_findings_per_rule": 100
  }
}
```

**Response 202:**
```json
{
  "id": "scan-uuid",
  "session_id": "session-uuid",
  "status": "queued",
  "started_at": null,
  "policy_pack_ids": ["owasp-api-top10-2023", "hipaa-technical-safeguards"],
  "estimated_duration_seconds": 45
}
```

**Errors:** `409` if a scan is already running for this session, `422` if no uploads or invalid pack.

---

### GET `/sessions/{session_id}/scans/{scan_id}`

Get current state of a scan.

**Auth:** Required

**Response 200:**
```json
{
  "id": "scan-uuid",
  "session_id": "session-uuid",
  "status": "running",
  "started_at": "2026-05-14T10:40:00Z",
  "completed_at": null,
  "progress_percent": 62,
  "current_step": "Analyzing API spec",
  "findings_so_far": 8,
  "policy_pack_ids": ["owasp-api-top10-2023"],
  "connectors_used": ["openapi-auditor", "code-scanner"]
}
```

**Status enum:** `queued | running | completed | failed | cancelled`

---

### GET `/sessions/{session_id}/scans/{scan_id}/progress`

Server-Sent Events stream of scan progress. Use this for live UI updates.

**Auth:** Required (token in query param `?token=...` for SSE since browsers can't set headers on EventSource)

**Content-Type:** `text/event-stream`

**Event format:**
```
event: progress
data: {"progress_percent":35,"current_step":"Scanning codebase","findings_so_far":3}

event: finding
data: {"id":"finding-uuid","severity":"high","rule_id":"...","title":"..."}

event: completed
data: {"scan_id":"...","total_findings":23,"overall_score":78}

event: error
data: {"code":"SCAN_FAILED","message":"..."}
```

**KMP Note:** Use Ktor Client's SSE plugin in `commonMain`.

---

### POST `/sessions/{session_id}/scans/{scan_id}/cancel`

Cancel a running scan.

**Auth:** Required

**Response 200:**
```json
{
  "id": "scan-uuid",
  "status": "cancelled",
  "cancelled_at": "2026-05-14T10:45:00Z"
}
```

---

## 8. Findings

A **finding** is one detected issue.

### GET `/sessions/{session_id}/findings`

List findings for a session.

**Auth:** Required

**Query parameters:**
- `severity` (filter): `critical | high | medium | low` (repeatable)
- `category` (filter)
- `rule_id` (filter)
- `status` (filter): `open | acknowledged | false_positive | resolved`
- `sort` (default `severity:desc,created_at:asc`)
- `page`, `page_size`

**Response 200:**
```json
{
  "data": [
    {
      "id": "finding-uuid",
      "session_id": "session-uuid",
      "scan_id": "scan-uuid",
      "rule_id": "owasp-api2-broken-auth",
      "rule_name": "Broken Authentication",
      "severity": "critical",
      "category": "authentication",
      "title": "API endpoint missing authentication",
      "description": "The endpoint POST /api/v1/users/{id}/delete does not require authentication, allowing any caller to delete user records.",
      "evidence": {
        "source_type": "openapi_spec",
        "source_path": "patient-portal-api.yaml",
        "line_number": 142,
        "snippet": "delete:\n  summary: Delete user\n  responses:\n    '204':\n      description: Deleted",
        "context": "No 'security' key present on this endpoint"
      },
      "remediation": {
        "summary": "Add an authentication requirement to this endpoint.",
        "code_example": "delete:\n  summary: Delete user\n  security:\n    - BearerAuth: []\n  responses: ...",
        "references": [
          "https://owasp.org/API-Security/editions/2023/en/0xa2-broken-authentication/"
        ]
      },
      "compliance_refs": [
        "OWASP-API2:2023",
        "HIPAA-164.312(a)(1)"
      ],
      "status": "open",
      "created_at": "2026-05-14T10:42:00Z"
    }
  ],
  "pagination": { ... }
}
```

---

### GET `/sessions/{session_id}/findings/{finding_id}`

Get full details for one finding.

**Auth:** Required

**Response 200:** Single `Finding` object (same shape as above).

---

### PATCH `/sessions/{session_id}/findings/{finding_id}`

Update finding status (e.g., mark as false positive or resolved).

**Auth:** Required

**Request body:**
```json
{
  "status": "false_positive",
  "comment": "This endpoint is only callable from internal network"
}
```

**Response 200:** Updated `Finding` object.

---

### GET `/sessions/{session_id}/findings/summary`

Aggregate counts and the overall compliance score.

**Auth:** Required

**Response 200:**
```json
{
  "session_id": "session-uuid",
  "scan_id": "scan-uuid",
  "total_findings": 23,
  "by_severity": {
    "critical": 3,
    "high": 7,
    "medium": 9,
    "low": 4
  },
  "by_status": {
    "open": 18,
    "acknowledged": 2,
    "false_positive": 1,
    "resolved": 2
  },
  "by_category": {
    "authentication": 6,
    "authorization": 4,
    "encryption": 3,
    "input_validation": 5,
    "configuration": 5
  },
  "overall_score": 78,
  "score_breakdown": {
    "rules_passed": 47,
    "rules_failed": 23,
    "rules_total": 70
  },
  "policy_coverage": [
    {
      "pack_id": "owasp-api-top10-2023",
      "rules_evaluated": 23,
      "rules_passed": 16,
      "rules_failed": 7
    }
  ]
}
```

---

## 9. Reports

### POST `/sessions/{session_id}/reports`

Generate a new report file (typically PDF).

**Auth:** Required

**Request body:**
```json
{
  "format": "pdf",
  "include_resolved": false,
  "include_remediation": true,
  "compliance_framework": "HIPAA",
  "sign": true
}
```

**Field rules:**
- `format`: enum — `pdf | json | csv | html`
- `compliance_framework`: optional, restricts the report to one framework's findings
- `sign`: if `true`, includes a cryptographic signature hash

**Response 202:**
```json
{
  "id": "report-uuid",
  "session_id": "session-uuid",
  "status": "generating",
  "format": "pdf",
  "requested_at": "2026-05-14T10:50:00Z"
}
```

---

### GET `/sessions/{session_id}/reports/{report_id}`

Get report metadata and status.

**Auth:** Required

**Response 200:**
```json
{
  "id": "report-uuid",
  "session_id": "session-uuid",
  "status": "ready",
  "format": "pdf",
  "file_size_bytes": 245678,
  "generated_at": "2026-05-14T10:50:15Z",
  "finding_count": 23,
  "overall_score": 78,
  "signed": true,
  "signature_sha256": "abc123...",
  "download_url": "/api/v1/sessions/uuid/reports/uuid/download"
}
```

**Status enum:** `generating | ready | failed`

---

### GET `/sessions/{session_id}/reports/{report_id}/download`

Download the generated report file.

**Auth:** Required

**Response 200:**
- Content-Type matches format (`application/pdf`, `application/json`, `text/csv`, `text/html`)
- Content-Disposition: `attachment; filename="audit-report-{session-name}-{date}.pdf"`

**Errors:** `404` if report not generated, `409` if report still generating.

---

### GET `/sessions/{session_id}/reports`

List all reports generated for a session.

**Auth:** Required

**Response 200:** Paginated list of `Report` objects.

---

## 10. Audit Trail

The audit trail is a tamper-evident log of every action taken in the system.

### GET `/sessions/{session_id}/audit-trail`

Get the audit trail for a session.

**Auth:** Required

**Query parameters:**
- `event_type` (filter): `session_created | upload_added | scan_started | scan_completed | finding_created | report_generated | policy_changed | user_action`
- `from_timestamp`, `to_timestamp` (filter ISO 8601)
- `page`, `page_size`

**Response 200:**
```json
{
  "data": [
    {
      "id": "event-uuid",
      "session_id": "session-uuid",
      "timestamp": "2026-05-14T10:30:00Z",
      "event_type": "session_created",
      "actor": {
        "type": "user",
        "id": "user-uuid",
        "name": "Jane Doe"
      },
      "details": {
        "session_name": "Q2 2026 Compliance Audit"
      },
      "previous_hash": "0000000000...",
      "current_hash": "a1b2c3d4..."
    }
  ],
  "pagination": { ... }
}
```

**Note:** `previous_hash` chains records together. Verification is performed by re-hashing the previous record's content and confirming the chain.

---

### GET `/sessions/{session_id}/audit-trail/verify`

Run integrity verification on the audit chain.

**Auth:** Required

**Response 200:**
```json
{
  "session_id": "session-uuid",
  "total_events": 156,
  "chain_intact": true,
  "verified_at": "2026-05-14T11:00:00Z"
}
```

If `chain_intact` is `false`, the response includes the index of the first broken link.

---

## 11. Health

### GET `/health`

Basic health probe — no auth required.

**Response 200:**
```json
{
  "status": "ok",
  "version": "1.0.0",
  "uptime_seconds": 12345,
  "checks": {
    "database": "ok",
    "gemini_api": "ok",
    "storage": "ok"
  }
}
```

If any check is degraded, returns `503` with the same body.

---

## 12. Data Models Reference

Canonical shapes for shared objects. The TypeScript-like syntax is for readability; in implementation use Kotlin data classes with `@Serializable`.

### Session
```typescript
{
  id: UUID
  name: string                           // 3-120 chars
  description: string | null
  target_environment: "development" | "staging" | "production" | null
  status: "created" | "scanning" | "completed" | "failed"
  finding_count: int                     // populated after first scan
  overall_score: int | null              // 0-100
  created_at: ISO8601
  updated_at: ISO8601
  created_by: UUID                       // user id
}
```

### Upload
```typescript
{
  id: UUID
  session_id: UUID
  filename: string
  size_bytes: int
  upload_type: "codebase" | "openapi_spec" | "config" | "db_schema" | "env_file" | "terraform" | "kubernetes"
  uploaded_at: ISO8601
  checksum_sha256: string
  metadata: object | null
}
```

### Connector
```typescript
{
  id: string                             // e.g. "code-scanner"
  name: string
  version: string                        // semver
  description: string
  supported_inputs: string[]             // upload_type values
  supported_languages: string[] | null
  artifact_types_produced: string[]
}
```

### PolicyPack
```typescript
{
  id: string                             // e.g. "owasp-api-top10-2023"
  name: string
  framework: "OWASP" | "HIPAA" | "SOC2" | "PCI-DSS" | "GDPR" | "custom"
  version: string
  description: string
  rule_count: int
  is_custom: boolean
  rules: Rule[]                          // present only on GET /packs/{id}
}
```

### Rule
```typescript
{
  id: string
  name: string
  description: string
  severity: "critical" | "high" | "medium" | "low"
  category: "authentication" | "authorization" | "encryption" | "input_validation" | "logging" | "configuration"
  check_type: "pattern" | "ai_evaluation" | "schema_validation" | "hybrid"
  pattern: string | null                 // regex if check_type is "pattern"
  applies_to_artifacts: string[]
  compliance_mapping: string[]
}
```

### Scan
```typescript
{
  id: UUID
  session_id: UUID
  status: "queued" | "running" | "completed" | "failed" | "cancelled"
  started_at: ISO8601 | null
  completed_at: ISO8601 | null
  progress_percent: int                  // 0-100
  current_step: string | null
  findings_so_far: int
  policy_pack_ids: string[]
  connectors_used: string[]
  estimated_duration_seconds: int | null
}
```

### Finding
```typescript
{
  id: UUID
  session_id: UUID
  scan_id: UUID
  rule_id: string
  rule_name: string
  severity: "critical" | "high" | "medium" | "low"
  category: string
  title: string
  description: string
  evidence: {
    source_type: string                  // matches upload_type
    source_path: string
    line_number: int | null
    snippet: string
    context: string | null
  }
  remediation: {
    summary: string
    code_example: string | null
    references: string[]                 // URLs
  }
  compliance_refs: string[]
  status: "open" | "acknowledged" | "false_positive" | "resolved"
  comment: string | null
  created_at: ISO8601
  updated_at: ISO8601
}
```

### Report
```typescript
{
  id: UUID
  session_id: UUID
  status: "generating" | "ready" | "failed"
  format: "pdf" | "json" | "csv" | "html"
  file_size_bytes: int | null
  generated_at: ISO8601 | null
  finding_count: int | null
  overall_score: int | null
  signed: boolean
  signature_sha256: string | null
  download_url: string | null
  compliance_framework: string | null
}
```

### AuditEvent
```typescript
{
  id: UUID
  session_id: UUID
  timestamp: ISO8601
  event_type: string
  actor: {
    type: "user" | "system"
    id: UUID | null
    name: string
  }
  details: object
  previous_hash: string                  // sha256 of previous event
  current_hash: string                   // sha256 of this event
}
```

### User
```typescript
{
  id: UUID
  email: string
  name: string
  role: "admin" | "auditor" | "viewer"
  created_at: ISO8601
}
```

---

## Appendix A: Implementation Notes

### For Backend
- All write endpoints emit an audit event automatically — do not require the caller to log them
- Long-running operations (scans, report generation) MUST be async and return `202`
- Rate limit: 100 requests / minute per user, 10 scans / hour per session
- File upload uses streaming, not in-memory loading

### For Frontend (KMP)
- Cache the connector list and policy pack list locally — they change rarely
- Use the SSE progress endpoint, not polling, for live scan UI
- Token refresh strategy: re-login when receiving `401`; do not auto-refresh in MVP
- For Wasm target, handle file uploads via the browser File API (`org.w3c.files.File`); for JVM, use `java.io.File`
- Implement a shared `ApiResult<T>` sealed type for handling success/error uniformly across screens

### For Both
- Treat the spec as a contract. Breaking changes require API version bump (`/api/v2/`)
- Field additions to responses are non-breaking; removals or type changes are breaking
- All datetime fields are UTC; the frontend formats to local time on display

---

*End of API Specification v1*
