# Grading Dashboard Frontend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the static user and administrator forms with responsive operational dashboards for grading and problem maintenance.

**Architecture:** The Spring Boot server continues to serve two static HTML documents. Each page owns its small JavaScript state and calls existing REST endpoints with `fetch`; no framework, bundle step, or backend API change is added. The user page has stage selection, a selected-problem work panel, manual result refresh, and test-result feedback. The admin page combines a problem list with create and version-publish forms.

**Tech Stack:** Static HTML, CSS, browser JavaScript, Spring Boot static resources, existing REST APIs.

## Global Constraints

- Do not add an AI provider or any AI-generated feedback behavior.
- Do not add a frontend framework, npm package, or build pipeline.
- Do not poll submission status in the background; use an explicit refresh command only.
- Keep all existing REST API contracts unchanged.
- Use dense operational layouts with 6-8px control corners, visible keyboard focus, and responsive grid collapse.
- No external icons, SVG assets, or images are required for these work-focused interfaces.

---

### Task 1: Build The User Grading Dashboard

**Files:**
- Modify: `src/main/resources/static/index.html`

**Interfaces:**
- Consumes: `GET /api/problems?stage={ROUND_1|ROUND_2|ROUND_3|ROUND_4|ROUND_5|FINAL}` returning `{problems:[{slug,title,stage,displayOrder}]}`.
- Consumes: `GET /api/problems/{slug}` returning `ProblemResponse.Detail`.
- Consumes: `POST /api/problems/{slug}/submissions` and `GET /api/submissions/{id}` returning `SubmissionResponse.Detail`.
- Consumes: `POST` and `GET /api/submissions/{id}/feedback`.
- Produces: A browser-only dashboard with stage tabs, selected problem detail, submission creation, explicit result refresh, and feedback display.

- [ ] **Step 1: Replace the document shell with operational layout landmarks**

Create a header, a stage tab row, a `main` grid with list and work-panel sections, and an `aria-live` status region. Use stable IDs:

```html
<section class="problem-pane" aria-label="문제 목록">
  <div id="problem-list" class="problem-list"></div>
</section>
<aside id="work-panel" class="work-panel" aria-live="polite"></aside>
```

- [ ] **Step 2: Add page-local style rules**

Define a two-column grid at desktop and one-column layout below 860px. Use CSS custom properties for `--ink`, `--surface`, `--line`, `--primary`, `--success`, `--warning`, and `--danger`; create semantic `.status-chip` variants and focus-visible outlines.

- [ ] **Step 3: Implement problem loading and selected-detail rendering**

Use this state and endpoint helper:

```js
const state = { stage: 'ROUND_1', problems: [], selectedSlug: null, lastSubmissionId: null };
async function request(url, options = {}) {
  const response = await fetch(url, options);
  const body = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(body.message || '요청을 처리하지 못했습니다.');
  return body;
}
```

Render an empty list message when `problems.length === 0`. When a row is selected, request `/api/problems/${slug}` and render title, description, Java version, starter-repository anchor, and the submission form.

- [ ] **Step 4: Implement submission, explicit refresh, and feedback controls**

On form submit, call:

```js
await request(`/api/problems/${slug}/submissions`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ repositoryUrl, ...(commitSha ? { commitSha } : {}) })
});
```

Store `lastSubmissionId`. Render a `결과 새로고침` button that calls `GET /api/submissions/${id}` only when clicked. Render returned `testResults` in a table and expose a separate `피드백 생성` command that calls the existing feedback `POST` endpoint after a terminal status.

- [ ] **Step 5: Manually verify the dashboard with the running local stack**

Open `http://localhost:8080/` at desktop and mobile widths. Verify stage changes request a list, an empty stage shows an empty state, and a selected problem shows a submission form. Confirm no repeating network request is issued while the page is idle.

- [ ] **Step 6: Commit**

```bash
git add src/main/resources/static/index.html
git commit -m "feat: build grading dashboard"
```

### Task 2: Build The Administrator Workspace

**Files:**
- Modify: `src/main/resources/static/admin/problems.html`

**Interfaces:**
- Consumes: `GET /api/admin/problems` returning `ProblemResponse.Detail[]`.
- Consumes: `POST /api/admin/problems` with `ProblemAdminRequest`.
- Consumes: `POST /api/admin/problems/{slug}/versions` with `{version,javaVersion,applicationTestSource}`.
- Produces: A responsive administrator workspace with list selection, creation, and version publication.

- [ ] **Step 1: Replace the single-form shell with list and editor regions**

Use:

```html
<section class="admin-list" aria-label="등록된 문제"><div id="problem-list"></div></section>
<section class="editor"><form id="problem-form"></form><form id="version-form" hidden></form></section>
```

Keep the existing request field names so `FormData` can create the API payload unchanged.

- [ ] **Step 2: Add responsive workspace styles and local feedback states**

Use an 300px left list column and an editor column above 960px, collapsing below it. Make the test-source field monospace, preserve visible invalid/error status, and keep buttons at fixed command height.

- [ ] **Step 3: Implement list load, selection, and version form visibility**

On load and after successful mutation, call `GET /api/admin/problems`. A selected row sets `selectedProblem`, displays its metadata, and unhides `#version-form` with the selected slug in its heading.

- [ ] **Step 4: Implement create and version-publish actions**

For create, convert `displayOrder`, `javaVersion`, and `version` to numbers, call `POST /api/admin/problems`, reset usable defaults, and refresh the list. For version publish, call:

```js
await request(`/api/admin/problems/${selectedProblem.slug}/versions`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ version: Number(version), javaVersion: Number(javaVersion), applicationTestSource })
});
```

Show all request errors beside the form that initiated the action.

- [ ] **Step 5: Manually verify with the running local stack**

Open `http://localhost:8080/admin/problems.html`. Confirm the page renders without JavaScript errors, empty list state is usable, and an invalid empty create request displays `요청 값이 올바르지 않습니다.` rather than leaving the form disabled.

- [ ] **Step 6: Commit**

```bash
git add src/main/resources/static/admin/problems.html
git commit -m "feat: build problem administration workspace"
```

### Task 3: Verify And Publish The Static Frontend

**Files:**
- Modify: `src/main/resources/static/index.html`
- Modify: `src/main/resources/static/admin/problems.html`

**Interfaces:**
- Consumes: The Docker API image defined by `Dockerfile` and `docker-compose.yml`.
- Produces: Rebuilt static pages served at `http://localhost:8080/` and `http://localhost:8080/admin/problems.html`.

- [ ] **Step 1: Run server test suite and formatting checks**

Run: `./gradlew test --rerun-tasks && ./gradlew spotlessCheck checkstyleMain checkstyleTest`

Expected: both commands report `BUILD SUCCESSFUL`.

- [ ] **Step 2: Rebuild and restart the API container**

Run: `SECURITY_ENABLED=false docker compose up -d --build api`

Expected: the API container is `Up` and `curl --fail 'http://localhost:8080/api/problems?stage=ROUND_1'` returns a JSON response.

- [ ] **Step 3: Inspect desktop and mobile browser layouts**

Open the two routes at 1440px and 390px widths. Confirm controls fit their containers, the user work panel moves below the list on mobile, and the admin editor moves below the problem list.

- [ ] **Step 4: Commit verification-only corrections when required**

```bash
git add src/main/resources/static/index.html src/main/resources/static/admin/problems.html
git commit -m "fix: refine responsive dashboard layout"
```

Only create this commit when the verification step changed one of the listed files.

## Self-Review

1. Spec coverage: Task 1 implements the user dashboard and explicit refresh behavior; Task 2 implements the administrator workspace; Task 3 verifies both against the local Docker stack.
2. Placeholder scan: the plan uses exact endpoints, IDs, commands, and status behavior. The optional verification commit is explicitly conditional on a real correction.
3. Type consistency: API field names match `ProblemResponse`, `SubmissionResponse`, `ProblemAdminRequest`, and `ProblemAdminVersionRequest` contracts.
