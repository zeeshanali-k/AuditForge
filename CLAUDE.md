# CLAUDE.md — AuditForge Frontend Project Plan

> This file is read by Claude Code on every session. It contains the project plan, build commands,
> and constraints for the KMP frontend. The team has its own preferences for architecture and code
> structure — do not impose new patterns; follow existing ones.

---

## Project Identity

**Name:** AuditForge (working name)
**Role:** Frontend client for the AuditForge compliance audit platform
**Type:** Cross-platform Kotlin Multiplatform application — Desktop (JVM) and Web (Wasm)
**Repository scope:** This repository contains ONLY the frontend. The backend lives in a separate
repository.

## What This Application Does

AuditForge audits enterprise systems (codebases, APIs, database configs, cloud configurations)
against compliance frameworks (HIPAA, SOC2, OWASP). Users create audit sessions, upload artifacts,
select policy packs, run AI-powered scans, review findings, and export signed reports.

This frontend is the user's entire window into that workflow. It consumes a REST API exclusively —
there is no direct system access from the client.

## Sources of Truth

Before implementing any feature, read these files:

- **`API_SPECIFICATION.md`** — the API contract. Every endpoint, payload, and error format is
  defined there. Treat it as inviolable; if the spec is wrong, fix the spec first, then implement
  against the corrected version.
- **`SRS.md`** — the functional and non-functional requirements. Every feature here has a
  `REQ-FR-XXX` identifier. When implementing a feature, reference its requirement ID in the commit
  message.
- **`DESIGN_BRIEF.md`** — visual design specifications for screens, components, color tokens, and
  interaction patterns.

## Tech Stack

- **Language:** Kotlin 2.0+
- **UI Framework:** Compose Multiplatform 1.6+
- **Targets:** Desktop (JVM 17+), Web (Wasm, JS)
- **HTTP:** Ktor Client 2.3+
- **Navigation:** Compose Navigation
- **DI:** Koin, Koin annotations using koin compiler plugin instead of
  ksp (https://insert-koin.io/docs/intro/koin-compiler-plugin)
- **Serialization:** kotlinx.serialization (JSON)
- **Async:** kotlinx.coroutines
- **Build:** Gradle (use the wrapper, never the global `gradle`)
- **JDK required:** 17 or 21 (LTS)
- **Architecture:** Follow MVI architecture and keep a single state as single source of truth for
  ui, if you have created multiple states, then merge them using combine operator in a single
  uiState field

## Build & Run Commands

```bash
# Desktop development
./gradlew :composeApp:run

# Web development (auto-reload at http://localhost:8080)
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# Run all tests
./gradlew :composeApp:allTests

# Format check
./gradlew ktlintCheck

# Build distributables
./gradlew :composeApp:packageDistributionForCurrentOS    # desktop installer
./gradlew :composeApp:wasmJsBrowserDistribution          # web static bundle

# Clean build (use when targets misbehave)
./gradlew clean
```

## Project Layout

This is the standard Compose Multiplatform layout. The internal architecture inside each source set
is the team's call — do not refactor it.

```
composeApp/
├── src/
│   ├── commonMain/      ← shared code: UI, view models, API client, models
│   ├── jvmMain/         ← desktop platform code (file chooser, OS integration)
│   ├── wasmJsMain/      ← web platform code (browser File API, JS interop)
│   └── commonTest/      ← shared tests
├── build.gradle.kts     ← module configuration
build.gradle.kts         ← root configuration
gradle.properties        ← Kotlin and Compose versions
```

---

## Implementation Plan

The work is broken into phases. Each phase has clear deliverables tied to SRS requirement IDs.
Complete a phase fully — including tests and error states — before moving to the next.

### Phase 1 — Foundation (REQ-FR-090..092, REQ-NFR-030, REQ-NFR-033)

**Goal:** Authenticated user can launch the app on desktop or web and see an empty session list.

Deliverables:

- KMP project skeleton with both targets compiling and running
- Theming system supporting light and dark modes
- Ktor client configured with base URL from build config
- Token storage abstraction (`expect`/`actual` for JVM and Wasm)
- Login screen with form validation
- JWT bearer token attached to all subsequent requests
- 401 handling that redirects to login
- Empty-state shell of the session list screen

### Phase 2 — Session Management (REQ-FR-001..003)

**Goal:** Users can create, view, and delete sessions.

Deliverables:

- Session list view with sort, filter, pagination
- Create session modal with validation
- Session detail screen scaffold (tabs: Uploads, Policies, Scans, Findings, Reports, Audit Trail)
- Delete session with confirmation
- Loading skeletons and error toasts

### Phase 3 — Upload & Policy Selection (REQ-FR-010..013, REQ-FR-020..021)

**Goal:** Users can upload artifacts and select policy packs.

Deliverables:

- Platform file picker abstraction (`expect class FilePicker`)
    - JVM implementation using `JFileChooser`
    - Wasm implementation using browser File API
- Multipart upload to `POST /sessions/{id}/uploads`
- Upload progress indication
- Upload list with delete action
- Policy pack browser with framework grouping
- Pack detail view with rule list
- Pack selection (multi-select) with visual indication

### Phase 4 — Scan Execution (REQ-FR-030..034, REQ-FR-031)

**Goal:** Users can trigger scans and watch live progress.

Deliverables:

- "Run scan" action wired to `POST /sessions/{id}/scans`
- SSE stream consumption via Ktor Client SSE plugin
- Live progress bar with percentage and current step
- Live findings feed (newest first, severity color-coded)
- Cancel scan action
- Error handling: scan failure, timeout, network drop
- Toast/notification on scan completion

### Phase 5 — Findings (REQ-FR-050..055)

**Goal:** Users can browse, filter, and triage findings.

Deliverables:

- Findings list with: severity badge, title, category, location, status, age
- Filter bar: severity, category, status, search
- Sort options (severity desc, age asc)
- Pagination
- Finding detail screen: evidence with syntax-highlighted snippet, remediation block, compliance
  refs
- Status updates (acknowledge, mark false positive, mark resolved) with optional comment
- Summary widget (score + breakdown by severity)

### Phase 6 — Reports (REQ-FR-060..064)

**Goal:** Users can generate and download signed reports.

Deliverables:

- "Generate report" modal: format selector, options (include resolved, framework filter, sign)
- POST to `/sessions/{id}/reports`, poll until ready
- Download handling
    - JVM: save dialog → file system
    - Wasm: trigger browser download via Blob URL
- List of past reports with re-download

### Phase 7 — Audit Trail (REQ-FR-070..074)

**Goal:** Users can inspect the audit trail and verify its integrity.

Deliverables:

- Audit trail viewer with chronological events
- Filter by event type and time range
- Expandable event details (hashes, actor, payload)
- "Verify chain integrity" button → modal with result

### Phase 8 — Polish (REQ-NFR-030..033)

**Goal:** Production-feel quality.

Deliverables:

- Keyboard shortcuts (`/` search, `n` new session, `Esc` close modals)
- Loading skeletons everywhere lists/data appear
- Empty states for every list view
- Error messages with corrective suggestions
- Tooltips on icon-only buttons
- Theme persistence across sessions
- Smoke tests for major flows

---

## Platform-Specific Concerns

### File Uploads (CRITICAL)

Web cannot access local file paths. Build a shared abstraction:

```kotlin
// commonMain
expect class FilePicker() {
    suspend fun pickFile(allowedExtensions: List<String>): PickedFile?
}

data class PickedFile(
    val name: String,
    val bytes: ByteArray,
    val mimeType: String
)
```

- JVM `actual`: wrap `JFileChooser`, read the chosen file into bytes
- Wasm `actual`: wrap browser `<input type="file">` interop, read via File API

### Token Storage

JWT tokens must not leak. Implement a platform-specific secure storage abstraction:

- JVM: encrypted file in user config directory
- Wasm: `sessionStorage` (NOT `localStorage` — tokens should not persist across browser sessions in
  v1)

### SSE Streams

Use Ktor's SSE plugin in `commonMain`. Confirm both engine targets:

- JVM: `ktor-client-okhttp` or `ktor-client-cio`
- Wasm: `ktor-client-js`

### Downloads

- JVM: `JFileChooser` save dialog, write bytes
- Wasm: create Blob, generate object URL, programmatically click an `<a>` tag

---

## Common Tasks for Claude Code

### Adding a new screen

1. Read the relevant SRS requirement(s) and matching API endpoint(s)
2. Read the corresponding section in `DESIGN_BRIEF.md`
3. Add data models in the team's existing models directory
4. Add API service function returning `ApiResult<T>` (or whatever the team's result type is)
5. Implement view model and composable, matching existing patterns
6. Add to navigation graph
7. Cover with at least one smoke test

### Implementing a new API call

1. Read the endpoint section of `API_SPECIFICATION.md` thoroughly
2. Match field names exactly — the spec uses snake_case
3. Use kotlinx.serialization with `@SerialName` annotations for snake_case ↔ camelCase mapping
4. Handle all documented error codes for that endpoint
5. Add a unit test with mocked responses

### Adding a platform-specific capability

1. Define an `expect` declaration in `commonMain`
2. Implement `actual` in `jvmMain` and `wasmJsMain` — both must compile
3. Do not skip the Wasm implementation; partial-platform features break the build

---

## Conventions

- **All UI text in sentence case.** "Create session", not "Create Session" or "CREATE SESSION"
- **Theme tokens only.** Never hardcode colors, spacing, or typography — use the theme
- **Material 3 components** as base; customize where the design brief specifies
- **No emojis** in UI text or code
- **Compose Multiplatform compatibility:** no JVM-only APIs in `commonMain`
- **Error handling:** every API call must handle the documented errors gracefully; never let an
  exception bubble to the UI
- **Loading states required:** every async operation visible to the user must show a progress
  indicator
- **Empty states required:** every list view must have a meaningful empty state

## Constraints

1. **Do not modify `API_SPECIFICATION.md` without discussion** — it is the contract with the backend
   team
2. **Do not introduce new architectural patterns** unless asked — the team has its preferences
3. **Both targets must build at all times** — if a change breaks Wasm, fix it before committing
4. **No external CDN dependencies** in the Wasm bundle for production
5. **Theme parity** — light and dark must look equally polished; do not ship a dark theme that's
   just inverted colors
6. **Sentence case is non-negotiable**

## Reference

- Compose Multiplatform: https://www.jetbrains.com/compose-multiplatform/
- Ktor Client: https://ktor.io/docs/client.html
- Kotlin Multiplatform: https://kotlinlang.org/docs/multiplatform.html
- Material 3 for Compose: https://developer.android.com/jetpack/compose/designsystems/material3

---

*End of CLAUDE.md*
