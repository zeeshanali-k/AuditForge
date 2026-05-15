# Design Brief — AuditForge

> **How to use this document:** Copy this entire file and paste it into Claude (or your preferred design AI) as a single prompt. The output will be visual design specifications detailed enough for Claude Code to implement directly in Compose Multiplatform.

---

You are designing **AuditForge**, an AI-powered compliance audit platform for enterprise security teams. Generate a complete visual design specification following the brief below. Be concrete and implementable — assume your output will be handed directly to engineers using Kotlin Multiplatform + Compose Multiplatform with Material 3 as the component foundation.

## Product Context

AuditForge audits enterprise systems — codebases, APIs, database configurations, cloud configurations — against compliance frameworks (HIPAA, SOC2, OWASP API Top 10). Users upload artifacts, the system runs scans powered by Google Gemini, and produces:

- Findings with severity, evidence, and remediation guidance
- Compliance scores mapped to specific regulatory clauses
- Signed PDF reports suitable for auditors
- A tamper-evident audit trail with cryptographic chain verification

The application is desktop and web only. There is no mobile target. Users will spend hours in this tool — density and clarity matter more than visual flair.

## Target Users

1. **Security Engineers** (primary) — power users who triage findings and run scans daily. Comfortable with technical UIs and keyboard shortcuts.
2. **Compliance Officers** — review reports, sign off on releases. Less technical, but need clear, defensible information.
3. **Developers** — view findings for code they wrote, apply remediations. Want code-focused detail views.
4. **Auditors (read-only)** — verify reports and audit trail integrity. Need credibility cues and easy export.

## Aesthetic Direction

**Tone:** Professional, trustworthy, enterprise-grade. Reference points: Linear, Stripe Dashboard, Datadog, Vercel, Sentry. Not consumer-app, not playful, not gamified.

**Density:** Higher than average. These are power users. Do not waste screen space on excessive padding or decoration, but maintain readability.

**Visual style:**
- Flat, no gradients
- Subtle borders OR subtle shadows, never both
- Rounded corners: 8px for cards, 6px for buttons, 4px for badges
- Generous whitespace inside content blocks, tight whitespace between layout regions
- Information first, decoration absent

**Themes:** Both light and dark must be designed in parallel. Do not produce a dark theme as inverted light.

## Color System

Provide named tokens, not raw hex values in the UI specs. Define the tokens once and reference them everywhere.

**Token categories required:**
- `surface.*` — backgrounds (primary, secondary, elevated, sunken)
- `text.*` — text colors (primary, secondary, tertiary, on-accent)
- `border.*` — border colors (default, strong, focus)
- `accent.*` — primary action color (default, hover, pressed)
- `severity.*` — critical, high, medium, low
- `status.*` — success, warning, info, neutral

**Suggested base palette (adjust as needed):**
- Neutral: cool grays
- Accent: a calm blue or teal (avoid yellow/orange/red as accents — those are severity colors)
- Severity:
  - Critical: red
  - High: orange
  - Medium: amber
  - Low: blue-gray
  - Resolved: green

Specify hex values for every token in both light and dark mode. Verify WCAG AA contrast (4.5:1 for body text, 3:1 for large text).

## Typography

Use Inter or system UI fonts. Define a scale:

- Display (24px / weight 500) — page titles
- H1 (20px / 500) — section headings
- H2 (16px / 500) — subsection headings
- Body (14px / 400) — default
- Caption (12px / 400) — metadata, hints
- Code (13px / 400 / monospace) — code snippets

Line heights: 1.5 for body, 1.3 for headings.

## Spacing Scale

Base unit: 4px. Allowed multiples: 4, 8, 12, 16, 24, 32, 48, 64. Do not introduce values outside this scale.

## Iconography

Choose one icon library and stick to it. Recommended: Lucide or Tabler (both have outline-only variants that match the aesthetic). Specify the icon used for each major action:

- Upload, download, delete, edit, search, filter, sort
- Severity indicators (one per level)
- Status indicators (success, warning, error, info)
- Navigation (sessions, policies, audit trail, settings)
- File types (code, spec, config, db)

---

## Screens to Design

For each screen below, produce:

1. **Layout sketch** — ASCII diagram or text description of regions, panels, and proportional widths
2. **Component breakdown** — list of components used (cards, tables, badges, modals, etc.)
3. **States** — empty, loading, populated, error
4. **Color and typography mapping** — which tokens apply where
5. **Interaction notes** — hover effects, click flows, keyboard shortcuts where relevant
6. **Responsive behavior** — how the layout adapts from 1024px to 2560px wide viewports

### 1. Login Screen

Centered card on a neutral background. Email and password fields, an optional "Remember me" checkbox, a single primary "Sign in" button. Subtle product name above the card. No marketing content, no illustrations.

### 2. Session List (Home Dashboard)

The default landing view after login.

- **Top bar:** product name (left), global search (center), user menu (right)
- **Left sidebar:** vertical nav with: Sessions, Policies, Audit trail, Settings. Collapsed by default on narrow viewports.
- **Main area:**
  - Top: page title + "Create session" primary button (right-aligned)
  - List or card grid of sessions. Each session row/card shows:
    - Session name (truncated if long)
    - Status badge (Created / Scanning / Completed / Failed)
    - Severity count chips (e.g., 3 critical · 7 high · 9 medium · 4 low) using severity colors as compact pills
    - Overall score (large number, color-coded: green ≥80, amber 50–79, red <50)
    - Last activity timestamp ("3 hours ago")
  - Filter bar above the list (status, date range, search)
  - Pagination at the bottom
- **Empty state:** illustration-free, just a centered message and a "Create your first session" call to action

### 3. Create Session Modal

Modal overlay or right-side slide-over (choose one and use consistently throughout the app for forms).

Fields:
- Session name (required, 3–120 chars)
- Description (optional, multi-line)
- Target environment (radio: Development / Staging / Production)

Buttons: Cancel (ghost), Create session (primary).

### 4. Session Detail

The hub view for a single audit session.

- **Header:** Session name (large, editable inline), status badge, score badge, action menu (⋯ with Delete, Export settings)
- **Tabs:** Uploads · Policies · Scans · Findings · Reports · Audit trail
- Each tab content area renders below the tab strip

### 5. Uploads Tab

- Drop zone at the top spanning the content width: dashed border, hover state, "Drop files here or click to browse"
- File type hints below the drop zone (small caption)
- Below: list of uploads (table or card list)
  - Columns: filename, type icon + label, size, uploaded date, delete action
  - Sort by uploaded date desc by default
- Upload progress: when uploading, show a horizontal progress bar in place of the drop zone with cancel option
- **Empty state:** drop zone only, no upload list

### 6. Policies Tab

Two-column layout:

- **Left (40%):** browse available policy packs, grouped by framework (OWASP, HIPAA, SOC2). Each pack shows name, rule count, and a "Select" checkbox.
- **Right (60%):** selected packs summary. Each selected pack displays as a card with: name, framework, rule count, "View rules" expander, "Remove" action.
- Bottom right: "Save selection" button (only enabled when changes pending)

When "View rules" is expanded, show a virtualized list of rules with severity dot, name, category.

### 7. Live Scan View

The most visually dynamic screen. Triggered by clicking "Run scan" on the session detail.

- **Top:** scan status header — "Scanning Q2 2026 Compliance Audit" with a subtle pulse animation
- **Progress bar:** large, horizontal, with percentage label and current step text below ("Analyzing patient-portal.yaml...")
- **Severity counters:** four prominent number tiles for Critical/High/Medium/Low — numbers tick up live as findings arrive
- **Live findings feed:** scrolling list on the right (or below on narrower viewports). Each entry: severity badge, finding title, source location, "just now" timestamp. New entries slide in from the top.
- **Cancel scan** button (destructive style) at the top right
- **Completion state:** progress bar fills, banner appears: "Scan complete · 23 findings · Score 78/100" with primary "Review findings" button

### 8. Findings List

- **Top summary widget:** large overall score (3-digit display), severity breakdown bar chart, policy coverage indicator
- **Filter bar:** severity chips (multi-select), category dropdown, status filter, search input. Active filters show as removable chips below.
- **Findings table:**
  - Columns: severity (colored dot + label), title, category, location (file:line, truncated), status badge, age
  - Row hover highlights the row
  - Row click opens the finding detail
  - Bulk-select checkboxes on the left for batch actions (Acknowledge, Mark as false positive)
- **Empty state when filtered:** "No findings match the current filters" with a "Clear filters" link

### 9. Finding Detail

Full-screen or large modal layout.

- **Top:** breadcrumb (Session > Findings > Finding #X), severity badge, status dropdown
- **Two-column body:**
  - **Left column (40%):** metadata
    - Rule that produced this finding
    - Severity, category
    - Compliance references (as chip list, clickable)
    - Source path
    - Created at, updated at
    - Comments section
  - **Right column (60%):**
    - **Evidence block:** syntax-highlighted code snippet with the line number, the offending line highlighted, surrounding context shown
    - **Description block:** paragraph explaining what was detected
    - **Remediation block:** explanation followed by a syntax-highlighted "fixed" code example, with a copy button
    - **References:** external links (OWASP, etc.)
- **Action bar at the bottom:** Status dropdown, comment field, "Previous finding" / "Next finding" navigation buttons

### 10. Reports Tab

- **Top:** "Generate report" primary button
- **List of generated reports:** each row shows format icon, generated date, finding count, score, signed indicator (small lock icon if signed), download button
- **Empty state:** "No reports yet. Generate your first report to share with auditors."

### 11. Generate Report Modal

Form fields:
- Format (segmented control: PDF / JSON / CSV / HTML — PDF default)
- Compliance framework filter (dropdown: All / HIPAA / SOC2 / OWASP)
- Toggles: "Include resolved findings", "Include remediation guidance", "Sign report (recommended)"
- Buttons: Cancel, Generate

When generating: button transforms to a progress indicator. When complete: transforms to "Download" button.

### 12. Audit Trail

Chronological event list, newest first.

- **Filter bar:** event type dropdown, date range picker
- **Event list:** each row shows
  - Timestamp (left, fixed width)
  - Event type icon
  - Actor name ("Jane Doe" or "System")
  - One-line summary
  - Expandable chevron on the right
- **Expanded row:** shows full event details — actor ID, payload JSON, previous hash, current hash (truncated with copy buttons)
- **Top right:** "Verify chain integrity" button → opens modal showing verification result with checkmark or red X, total events verified, link to break point if any

### 13. Settings

Sections (stacked vertically with section headings):

- **Profile:** name, email (read-only), change password
- **API configuration:** base URL field, "Test connection" button
- **Appearance:** theme selector (System / Light / Dark) — show theme preview swatches
- **About:** version, license, links

---

## Component Specifications

For each of these primitive components, provide visual specs (size, padding, colors, states) for both light and dark themes:

### Buttons
- **Primary:** filled with accent color
- **Secondary:** outlined with accent border
- **Ghost:** no border, text only with accent color
- **Destructive:** filled with critical/red color
- All buttons: default, hover, active, disabled, loading states. Loading shows a spinner inline.

### Form Fields
- Text input (default, focus, error, disabled)
- Multi-line text area
- Dropdown / select (closed and open states)
- Checkbox, radio (off, on, indeterminate)
- Segmented control
- Toggle switch
- Date picker

### Badges & Chips
- Severity badges (critical/high/medium/low) — colored dot + label
- Status badges (created/scanning/completed/failed/open/acknowledged/false_positive/resolved)
- Removable filter chips
- Compliance reference chips (small, monospace text, clickable)

### Tables
- Header row (sticky, sortable indicators)
- Body rows (default, hover, selected)
- Bulk-select checkbox column
- Pagination footer

### Cards
- Session card (with score, severity chips, metadata)
- Generic content card

### Modals & Slide-overs
- Modal (centered, dimmed backdrop)
- Right-side slide-over (for forms)
- Confirmation modal (for destructive actions)

### Toasts & Banners
- Success, warning, error, info variants
- Auto-dismiss timing
- Stacking behavior

### Navigation
- Top bar
- Left sidebar (collapsed and expanded states)
- Tab strip (horizontal, with active indicator)
- Breadcrumb

### Code Display
- Inline code (background tint, monospace)
- Code block (syntax highlighting, copy button, line numbers)
- Diff block (added/removed line markers)

### Charts (minimal)
- Horizontal stacked bar for severity breakdown
- Donut for category distribution (used sparingly)

## Interaction Patterns

- **Optimistic UI** where safe (status updates on findings should reflect immediately)
- **Skeleton loaders** for any data fetch — never blank screens
- **Toasts** for non-blocking confirmations and errors
- **Inline errors** beside form fields
- **Confirmation modals** required for: delete session, delete upload, mark as false positive on critical findings
- **Keyboard shortcuts:**
  - `/` — focus global search
  - `n` — new session
  - `Esc` — close modal / clear filter
  - `j` / `k` — next / previous in lists
  - `?` — show keyboard shortcut help

## Accessibility

- WCAG AA contrast minimum throughout
- All interactive elements reachable by keyboard
- Focus states clearly visible (2px outline using accent color)
- Status changes announced via ARIA live regions
- No reliance on color alone — always pair with text or icons
- Minimum click target 32×32px

## Technical Constraints

- Implementable in Compose Multiplatform with Material 3 — avoid custom shaders, WebGL effects, or anything that does not translate to JVM and Wasm
- No JavaScript dependencies for visual rendering (Wasm target)
- All animations are CSS-style equivalent (translate, opacity, color transitions)
- No reliance on hover for critical functionality (some Wasm input methods do not support hover)
- Support viewports from 1024×768 to 2560×1440
- All UI strings in **sentence case** (not Title Case, not ALL CAPS)

## Output Format

Structure your response as:

### Part 1 — Design Philosophy
A brief (3–5 paragraph) statement describing the design language and the principles guiding decisions.

### Part 2 — Design System
- Color tokens (table with hex values, light + dark)
- Typography scale (table)
- Spacing scale (list)
- Iconography (library choice + per-screen icon list)

### Part 3 — Component Specifications
For each component category, provide visual specs and state variations.

### Part 4 — Screen Designs
For each screen (1–13), provide the six items requested above. Include ASCII layout sketches.

### Part 5 — Interaction & Motion
- Keyboard shortcut map
- Animation timing and easing curves
- Accessibility checklist per screen

### Part 6 — Implementation Notes
- Specific Compose Material 3 components to use as base for each component category
- Where to deviate from Material defaults and why

## Do Not

- Do not propose mobile-first layouts — desktop and web only
- Do not use stock illustrations, 3D renders, or photographic imagery
- Do not include consumer patterns (gamification, badges-for-achievement, emoji-heavy UI)
- Do not invent compliance frameworks, finding categories, or product features not described in this brief
- Do not specify pixel-perfect absolute coordinates — provide proportional layouts ("left 30% / right 70%")
- Do not produce a dark theme that is just an inversion of the light theme
- Do not use heavy gradients, glow effects, or neon accents
- Do not propose features outside the scope listed in this brief

Begin with Part 1.
