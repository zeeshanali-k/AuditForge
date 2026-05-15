# Software Requirements Specification

**Project:** AuditForge — AI-Powered Compliance Audit Platform
**Version:** 1.0
**Status:** Draft
**Last Updated:** 2026-05-14

> **Note:** "AuditForge" is a working name. Replace with your final product name.

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [Overall Description](#2-overall-description)
3. [External Interface Requirements](#3-external-interface-requirements)
4. [System Features (Functional Requirements)](#4-system-features-functional-requirements)
5. [Non-Functional Requirements](#5-non-functional-requirements)
6. [Other Requirements](#6-other-requirements)
7. [Appendices](#7-appendices)

---

## 1. Introduction

### 1.1 Purpose

This document specifies the requirements for AuditForge, an AI-powered platform that audits enterprise software systems (codebases, APIs, databases, cloud configurations) against industry compliance standards. The platform produces actionable findings, remediation guidance, and signed audit reports suitable for regulators and security teams.

This SRS is the authoritative reference for what the system must do. It is intended to guide engineering, testing, and demonstration during the hackathon and beyond.

### 1.2 Document Conventions

- **MUST / SHALL** — mandatory requirement
- **SHOULD** — recommended requirement
- **MAY** — optional requirement
- **REQ-FR-XXX** — Functional Requirement identifier
- **REQ-NFR-XXX** — Non-Functional Requirement identifier
- Acronyms are listed in Appendix A

### 1.3 Intended Audience

| Audience | Use |
|----------|-----|
| Engineering team | Implementation reference |
| Hackathon judges | Product capability overview |
| Future maintainers | Architectural intent and constraints |
| Product / business stakeholders | Scope, value proposition, and roadmap |

### 1.4 Project Scope

AuditForge ingests artifacts from enterprise systems through pluggable **connectors**, evaluates them against pluggable **policy packs** (OWASP, HIPAA, SOC2, PCI-DSS), augments deterministic checks with **AI-powered analysis** (via Google Gemini), and produces a **standardized findings model**, a **live dashboard**, **PDF reports**, and a **tamper-evident audit trail**.

**In scope (Phase 1 / hackathon):**
- Code, OpenAPI, and database config connectors
- OWASP API Top 10 + HIPAA + SOC2 starter packs
- Gemini-powered analysis layer
- Web + Desktop client via Kotlin Multiplatform
- PDF report generation with cryptographic signing
- Single-tenant operation

**Out of scope (Phase 1):**
- Live database connections
- Continuous monitoring / scheduled scans
- Multi-tenant SaaS deployment
- CI/CD pipeline plugins (GitHub Actions, Jenkins)
- Mobile clients
- Custom policy-pack editor UI

### 1.5 References

- OWASP API Security Top 10 (2023)
- HIPAA Security Rule, 45 CFR §164.302–164.318
- SOC 2 Trust Services Criteria
- IEEE 830-1998 SRS template (structural reference)
- API Specification document (`API_SPECIFICATION.md`)

---

## 2. Overall Description

### 2.1 Product Perspective

AuditForge is a **new, standalone product**. It is not a replacement for any existing system but sits alongside developer workflows as a pre-deployment compliance gate. Unlike static analyzers like Snyk or Checkmarx, AuditForge focuses on **compliance traceability** — every finding maps to a specific regulatory clause, and every action is recorded in a tamper-evident audit trail.

The platform has three architectural tiers:

1. **Connector tier** — pluggable modules that ingest different system types
2. **Core engine** — normalization, policy evaluation, AI analysis
3. **Presentation tier** — web/desktop client, reports, alerts, REST API

### 2.2 Product Functions

At a high level, AuditForge enables users to:

- Create and manage **audit sessions** scoped to a specific deployment or artifact set
- **Upload artifacts** of various types (code, specs, configs) for analysis
- **Select policy packs** corresponding to one or more compliance frameworks
- **Run scans** that produce findings asynchronously, with live progress feedback
- **Review findings** filtered by severity, category, or compliance reference
- **Generate signed PDF reports** suitable for auditors and regulators
- **Inspect the audit trail** for any session
- **Verify audit trail integrity** via cryptographic chain validation

### 2.3 User Classes and Characteristics

| User Class | Expertise | Primary Tasks |
|------------|-----------|---------------|
| **Security Engineer** | High | Runs scans, reviews findings, triages false positives |
| **Compliance Officer** | Medium technical | Selects policy packs, reviews reports, signs off on releases |
| **Developer** | High technical, low compliance | Views findings for their code, applies remediation |
| **Auditor (read-only)** | Low technical, high compliance | Downloads reports, verifies audit trail integrity |
| **Admin** | High technical | Manages users, custom policies, system configuration |

### 2.4 Operating Environment

**Server (backend):**
- OS: Linux (Ubuntu 22.04+ recommended) or containerized via Docker
- Runtime: Python 3.11+ (FastAPI), or equivalent
- Storage: SQLite (MVP) or PostgreSQL (production)
- External: Google Gemini API access required

**Client (frontend, Kotlin Multiplatform):**
- Desktop: JVM 17+, Windows 10+ / macOS 12+ / Linux (any major distro)
- Web: any Chromium- or Firefox-based browser with Wasm support (last 2 major versions)

**Network:**
- TLS 1.2+ for production deployments
- HTTPS-only in production; HTTP allowed on localhost for development

### 2.5 Design and Implementation Constraints

- **CON-1:** Frontend MUST be built with Kotlin Multiplatform + Compose Multiplatform, targeting Desktop and Web
- **CON-2:** Backend MUST be implemented as a RESTful service per the API Specification
- **CON-3:** AI analysis MUST use Google Gemini (via Google AI Studio) — required by hackathon track 2
- **CON-4:** Uploaded artifacts MUST NOT leave the deployment environment for any reason other than the LLM call, and even there must be scoped to the minimum necessary content
- **CON-5:** The system MUST function entirely on a single machine for the hackathon demo (no external infrastructure required beyond the Gemini API)
- **CON-6:** All long-running operations MUST be asynchronous, returning HTTP 202 with a polling or SSE endpoint
- **CON-7:** Audit trail records MUST be cryptographically chained (SHA-256) and append-only

### 2.6 User Documentation

The following user-facing materials WILL accompany the product:
- README with installation and quickstart
- API reference (`API_SPECIFICATION.md`)
- In-app tooltips and inline help for major actions
- A short demo video for hackathon submission

### 2.7 Assumptions and Dependencies

- The Google Gemini API is available and responds within reasonable latency (<10s per call)
- Users have valid credentials and network access to upload artifacts
- The audited artifacts fit within reasonable size limits (codebase ≤100MB)
- The user's browser supports WebAssembly (for the web client)
- Java Development Kit 17 or higher is installed on desktop clients

---

## 3. External Interface Requirements

### 3.1 User Interfaces

The client application provides a unified experience across desktop and web through Kotlin Multiplatform + Compose Multiplatform.

**Key screens:**
1. **Login / Authentication**
2. **Session list** — overview of all audit sessions with status badges and scores
3. **Session detail** — uploaded artifacts, selected policies, scan controls
4. **Live scan view** — real-time progress bar, current step, findings counter
5. **Findings list** — filterable, sortable table of findings with severity badges
6. **Finding detail** — full evidence, remediation, compliance references
7. **Report generation** — format and content selection, signed PDF download
8. **Audit trail** — chronological event log with integrity verification button
9. **Settings** — API endpoint, theme, user preferences

**UX requirements:**
- Responsive layout adapting to viewport size
- Light and dark themes
- Keyboard navigation for power users
- Empty states with guidance for each list view
- Loading skeletons during async operations

### 3.2 Hardware Interfaces

No specialized hardware required. The system runs on commodity hardware (server: 4 CPU cores, 8GB RAM minimum recommended).

### 3.3 Software Interfaces

| Interface | Purpose | Protocol |
|-----------|---------|----------|
| Google Gemini API | AI analysis of artifacts | HTTPS + JSON |
| File system | Storage of uploads, generated reports, audit logs | Local filesystem |
| SQLite / PostgreSQL | Persistent data storage | SQL |
| Browser File API (Web target) | File selection on web client | JS interop |
| Java File chooser (Desktop target) | File selection on desktop | JVM |

### 3.4 Communications Interfaces

| Channel | Format | Use |
|---------|--------|-----|
| REST API | JSON over HTTP/HTTPS | All client-server interactions |
| Server-Sent Events | text/event-stream | Live scan progress updates |
| Multipart upload | multipart/form-data | File uploads |
| File download | binary stream with Content-Disposition | Report downloads |

All HTTP responses MUST include CORS headers permitting requests from configured client origins.

---

## 4. System Features (Functional Requirements)

### 4.1 Session Management

**REQ-FR-001: Create audit session**
Priority: **Must**
The system SHALL allow an authenticated user to create a new audit session with a name and optional description.
- Input: session name (3–120 chars), description (optional), target environment
- Output: Session object with generated UUID
- Acceptance: Creating a valid session returns HTTP 201 with the persisted object

**REQ-FR-002: List sessions**
Priority: **Must**
The system SHALL return a paginated list of sessions owned by the authenticated user, with filtering by status and sorting by date.

**REQ-FR-003: Delete session**
Priority: **Must**
The system SHALL allow a user to delete a session and its associated uploads, findings, and reports. Audit trail records remain per the retention policy.

---

### 4.2 Multi-Source Ingestion (Connector Layer)

**REQ-FR-010: Upload artifact**
Priority: **Must**
The system SHALL accept artifact uploads via multipart HTTP for the following types: codebase (zip), OpenAPI spec (json/yaml), config files, database schema (sql), env files, Terraform, Kubernetes manifests.
- Constraint: Max 100MB per upload
- Output: Upload record with SHA-256 checksum

**REQ-FR-011: Reject invalid uploads**
Priority: **Must**
The system SHALL reject uploads whose declared `upload_type` does not match the file extension or content signature.

**REQ-FR-012: List connectors**
Priority: **Must**
The system SHALL expose a list of available connectors via the API, including version, supported inputs, and supported languages.

**REQ-FR-013: Pluggable connector architecture**
Priority: **Must**
The system SHALL be architected such that adding a new connector type (e.g., cloud config, container manifest) requires only implementing the connector interface and registering it — no changes to the policy engine or report layer.

---

### 4.3 Policy Management

**REQ-FR-020: List policy packs**
Priority: **Must**
The system SHALL provide pre-built policy packs for at least OWASP API Top 10, HIPAA Technical Safeguards, and SOC2 Common Criteria.

**REQ-FR-021: Inspect policy pack contents**
Priority: **Must**
Users SHALL be able to view the individual rules within any policy pack, including severity, category, and compliance mapping.

**REQ-FR-022: Custom rules**
Priority: **Should**
The system SHOULD allow admin users to define custom rules using pattern matching or AI evaluation.

**REQ-FR-023: Policy pack versioning**
Priority: **Should**
Each policy pack SHALL include a version field, and findings SHALL record which pack version produced them.

---

### 4.4 Scan Execution

**REQ-FR-030: Trigger scan**
Priority: **Must**
The system SHALL allow a user to trigger a scan on a session with selected policy packs. The operation SHALL be asynchronous and return HTTP 202 with a scan ID.

**REQ-FR-031: Live scan progress**
Priority: **Must**
The system SHALL expose a Server-Sent Events stream emitting progress percentage, current step, and findings as they are produced.

**REQ-FR-032: Concurrent scan prevention**
Priority: **Must**
The system SHALL reject new scan requests on a session that already has a running scan, returning HTTP 409.

**REQ-FR-033: Cancel scan**
Priority: **Should**
The system SHOULD allow a user to cancel an in-progress scan, marking it as `cancelled`.

**REQ-FR-034: Scan timeout**
Priority: **Should**
A scan that has not made progress for 5 minutes SHOULD be automatically marked as failed with reason `timeout`.

---

### 4.5 AI-Powered Analysis

**REQ-FR-040: AI evaluation of complex findings**
Priority: **Must**
The system SHALL use Google Gemini to evaluate artifacts where deterministic rules cannot produce a confident verdict — e.g., cross-file vulnerabilities, ambiguous configurations, or context-dependent risks.

**REQ-FR-041: Long-context analysis**
Priority: **Must**
The system SHALL leverage Gemini's long-context window to analyze entire codebases holistically, identifying issues that span multiple files.

**REQ-FR-042: AI-generated remediation guidance**
Priority: **Must**
For every AI-evaluated finding, the system SHALL produce a remediation suggestion including a concrete code example.

**REQ-FR-043: AI cost and rate-limit handling**
Priority: **Should**
The system SHOULD gracefully handle Gemini API rate limits and quota exhaustion, queueing requests and notifying the user if scan completion will be delayed.

---

### 4.6 Findings

**REQ-FR-050: Standardized finding format**
Priority: **Must**
Every finding produced by the system SHALL conform to the `Finding` schema defined in the API Specification, regardless of which connector or rule produced it.

**REQ-FR-051: Evidence capture**
Priority: **Must**
Each finding SHALL include sufficient evidence to allow a developer to locate and verify the issue: source file path, line number (where applicable), and a code snippet showing context.

**REQ-FR-052: Compliance mapping**
Priority: **Must**
Each finding SHALL include zero or more references to specific compliance clauses (e.g., `OWASP-API1:2023`, `HIPAA-164.312(a)(1)`).

**REQ-FR-053: Filter and sort findings**
Priority: **Must**
Users SHALL be able to filter findings by severity, category, rule, or status, and sort by severity or creation time.

**REQ-FR-054: Update finding status**
Priority: **Must**
Users SHALL be able to update a finding's status to `acknowledged`, `false_positive`, or `resolved`, with an optional comment.

**REQ-FR-055: Findings summary**
Priority: **Must**
The system SHALL provide a summary endpoint returning counts by severity, status, and category, along with an overall compliance score (0–100).

---

### 4.7 Reporting

**REQ-FR-060: Generate PDF report**
Priority: **Must**
The system SHALL generate a PDF report for a session containing: executive summary, overall score, findings grouped by severity, remediation guidance, and compliance reference appendix.

**REQ-FR-061: Report signing**
Priority: **Must**
Generated reports SHALL include a SHA-256 signature of their content, embedded as metadata and verifiable via the API.

**REQ-FR-062: Report formats**
Priority: **Should**
The system SHOULD support generating reports in JSON, CSV, and HTML formats in addition to PDF.

**REQ-FR-063: Framework-scoped reports**
Priority: **Should**
Users SHOULD be able to request a report filtered to findings for a specific compliance framework (e.g., HIPAA-only).

**REQ-FR-064: Report download**
Priority: **Must**
The system SHALL provide a download endpoint for generated reports, returning the binary content with appropriate Content-Type and Content-Disposition headers.

---

### 4.8 Audit Trail

**REQ-FR-070: Capture every action**
Priority: **Must**
The system SHALL record every user action (session creation, upload, scan, finding status change, report generation) and every system-initiated event (scan started, scan completed, etc.) in the audit trail.

**REQ-FR-071: Tamper-evident chain**
Priority: **Must**
Each audit record SHALL contain a SHA-256 hash of its content plus the hash of the previous record, forming an append-only chain.

**REQ-FR-072: Chain verification**
Priority: **Must**
The system SHALL expose an endpoint to verify the integrity of a session's audit chain by re-hashing every record and validating the chain.

**REQ-FR-073: Audit trail retrieval**
Priority: **Must**
Users SHALL be able to query the audit trail filtered by event type and time range, returning paginated results.

**REQ-FR-074: Immutability**
Priority: **Must**
Audit trail records, once written, SHALL NOT be editable or deletable through any normal API. Deletion requires direct database access by an admin and is itself logged.

---

### 4.9 Dashboard

**REQ-FR-080: Session overview**
Priority: **Must**
The client SHALL display a session list view with status badges, finding counts, and overall scores.

**REQ-FR-081: Live findings feed**
Priority: **Must**
During an active scan, the dashboard SHALL display findings as they are produced, ordered by severity descending.

**REQ-FR-082: Visual score indicator**
Priority: **Must**
The overall compliance score SHALL be presented as a number out of 100 alongside a color-coded indicator (green ≥80, yellow 50–79, red <50).

**REQ-FR-083: Drill-down navigation**
Priority: **Must**
Users SHALL be able to click any summary card or finding row to navigate to a detailed view.

---

### 4.10 Authentication and Authorization

**REQ-FR-090: User authentication**
Priority: **Must**
The system SHALL authenticate users via email and password, returning a JWT bearer token valid for 24 hours.

**REQ-FR-091: Role-based access**
Priority: **Should**
The system SHOULD support at least three roles: `admin`, `auditor`, `viewer`. Viewers SHALL be read-only; auditors can run scans; admins can manage policies and users.

**REQ-FR-092: Session isolation**
Priority: **Must**
Users SHALL only see sessions they own, unless they have admin role.

---

## 5. Non-Functional Requirements

### 5.1 Performance

**REQ-NFR-001:** A scan of a typical codebase (5,000 lines of code, single OpenAPI spec) SHALL complete in under 60 seconds.
**REQ-NFR-002:** API endpoints (excluding scans and report generation) SHALL respond in under 500ms for the 95th percentile.
**REQ-NFR-003:** The dashboard SHALL load and display the session list in under 2 seconds.
**REQ-NFR-004:** PDF reports SHALL be generated in under 10 seconds for up to 100 findings.

### 5.2 Security

**REQ-NFR-010:** All production deployments SHALL enforce HTTPS / TLS 1.2+.
**REQ-NFR-011:** Passwords SHALL be hashed using bcrypt or Argon2id with appropriate work factors.
**REQ-NFR-012:** Uploaded artifacts SHALL be stored with restricted file system permissions accessible only to the application process.
**REQ-NFR-013:** All API endpoints (except `/health` and `/auth/login`) SHALL require valid JWT authentication.
**REQ-NFR-014:** Generated audit trail hashes SHALL use SHA-256.
**REQ-NFR-015:** The system SHALL NOT log sensitive artifact contents (only metadata and finding identifiers).
**REQ-NFR-016:** All input SHALL be validated server-side; the API SHALL reject requests that violate schema constraints.

### 5.3 Reliability

**REQ-NFR-020:** The system SHALL recover gracefully from Gemini API failures, marking affected scans as `failed` with a clear error message rather than hanging.
**REQ-NFR-021:** Background scans SHALL be resumable if the server restarts mid-scan (state persisted to the database after each major step).
**REQ-NFR-022:** Failed scans SHALL NOT corrupt session data or audit trail records.

### 5.4 Usability

**REQ-NFR-030:** First-time users SHALL be able to complete a full audit (create session → upload → scan → view findings) in under 5 minutes with no documentation.
**REQ-NFR-031:** Error messages SHALL be human-readable and suggest a corrective action where possible.
**REQ-NFR-032:** All long-running operations SHALL display progress feedback (progress bars, spinners, or live counts).
**REQ-NFR-033:** The interface SHALL support both light and dark themes.

### 5.5 Maintainability

**REQ-NFR-040:** The connector layer SHALL be implemented as discrete plugins conforming to a documented interface.
**REQ-NFR-041:** Each component (connector, policy engine, AI layer, report generator) SHALL be independently testable with unit tests.
**REQ-NFR-042:** API changes SHALL follow semantic versioning; breaking changes require an API version bump.

### 5.6 Scalability

**REQ-NFR-050:** The architecture SHALL support horizontal scaling of the API tier by being stateless (all state in DB / object storage).
**REQ-NFR-051:** Scan workers SHALL be deployable as separate processes that pull work from a queue, enabling parallel scan execution in future deployments.
**REQ-NFR-052:** The system SHALL be deployable as a single Docker Compose stack for development and a Kubernetes manifest for production.

### 5.7 Portability

**REQ-NFR-060:** The frontend SHALL run on Windows, macOS, and Linux desktops, and on any modern Chromium- or Firefox-based browser.
**REQ-NFR-061:** The backend SHALL run on any Linux distribution capable of running a container runtime.

---

## 6. Other Requirements

### 6.1 Data Retention

- Audit trail records SHALL be retained for a minimum of 1 year
- Uploaded artifacts MAY be deleted from disk after scan completion if the session is older than 30 days, but the finding records persist
- Generated reports SHALL be retained as long as the parent session exists

### 6.2 Legal and Compliance

- The system SHALL display a privacy notice describing what data is sent to the Gemini API
- The system SHALL provide a means to export and delete a user's data (GDPR right to erasure)
- Use of the system SHALL NOT imply legal compliance certification — outputs are tooling, not legal advice

### 6.3 Internationalization

- All user-facing strings SHALL be externalized for future translation
- English is the only language supported in v1; the architecture SHALL NOT preclude adding others

---

## 7. Appendices

### Appendix A: Glossary

| Term | Definition |
|------|------------|
| **Artifact** | A normalized data structure produced by a connector representing one item of interest in a source system (e.g., a code snippet, a config value) |
| **Audit Trail** | An append-only, cryptographically chained log of all actions in the system |
| **Compliance Mapping** | A reference linking a finding to a specific clause in a compliance framework |
| **Connector** | A pluggable module that ingests a specific system type and produces normalized artifacts |
| **Finding** | A single detected issue with severity, evidence, and remediation guidance |
| **HIPAA** | Health Insurance Portability and Accountability Act — US healthcare data protection law |
| **JWT** | JSON Web Token — standard for stateless authentication tokens |
| **KMP** | Kotlin Multiplatform — JetBrains' framework for sharing code across platforms |
| **OWASP** | Open Web Application Security Project — security industry standards body |
| **Policy Pack** | A versioned bundle of rules corresponding to a compliance framework |
| **Rule** | A single check that produces zero or more findings |
| **SBOM** | Software Bill of Materials — inventory of components in a system |
| **Session** | An audit run scoped to specific uploads and policies |
| **SOC2** | Service Organization Control 2 — security audit framework for service providers |
| **SSE** | Server-Sent Events — HTTP-based streaming protocol |
| **SRS** | Software Requirements Specification — this document |

### Appendix B: Traceability Matrix

A condensed mapping of major requirements to API endpoints. Full mapping is maintained in the API Specification.

| Requirement | Primary API Endpoints |
|-------------|----------------------|
| REQ-FR-001 (Create session) | `POST /sessions` |
| REQ-FR-010 (Upload artifact) | `POST /sessions/{id}/uploads` |
| REQ-FR-020 (List policy packs) | `GET /policies/packs` |
| REQ-FR-030 (Trigger scan) | `POST /sessions/{id}/scans` |
| REQ-FR-031 (Live progress) | `GET /sessions/{id}/scans/{id}/progress` (SSE) |
| REQ-FR-053 (Filter findings) | `GET /sessions/{id}/findings?severity=...` |
| REQ-FR-060 (Generate PDF) | `POST /sessions/{id}/reports` |
| REQ-FR-071 (Chain integrity) | `GET /sessions/{id}/audit-trail/verify` |

### Appendix C: Out-of-Scope Items (Backlog)

The following are explicitly out of scope for v1 but expected in future versions:

- Continuous monitoring and scheduled scans
- GitHub / GitLab / Bitbucket integrations
- Slack and Jira notification integrations
- Multi-tenant SaaS deployment
- Custom policy editor with visual rule builder
- Mobile clients (iOS, Android)
- On-premise air-gapped deployment with local LLM
- Diff reports comparing two scans of the same session
- Integration with CI/CD systems (GitHub Actions, Jenkins, etc.)
- SSO integration (SAML, OIDC)
- Live database connection auditing

### Appendix D: Revision History

| Version | Date | Author | Notes |
|---------|------|--------|-------|
| 0.1 | 2026-05-14 | Initial team | First draft for hackathon scope |
| 1.0 | 2026-05-14 | Initial team | Approved for implementation |

---

*End of Software Requirements Specification v1.0*
