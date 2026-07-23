# Result-Focused Main Page Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the current blue dashboard main page with a compact coding-practice-inspired GitHub submission workspace that shows only pass/fail grading outcomes.

**Architecture:** Keep `/` as one static HTML document backed by existing REST endpoints. The page retains stage selection, problem detail loading, GitHub submission, and explicit result refresh, but reduces the result model to one semantic status field. No backend contract or admin workflow changes.

**Tech Stack:** Static HTML, CSS, browser JavaScript, Spring Boot static resources, existing REST APIs.

## Global Constraints

- Do not add a frontend framework, build tool, image, SVG, or external asset.
- Do not add a browser code editor, numeric score, test table, feedback controls, or background polling.
- Use manual `결과 새로고침` only.
- Do not copy Programmers branding, wording, assets, source code, or exact layout.
- Use a compact white-surface, thin-divider, green-accent visual system with responsive grid collapse.

---

### Task 1: Replace The User Workspace

**Files:**
- Modify: `src/main/resources/static/index.html`

**Interfaces:**
- Consumes: existing problem list/detail APIs and `SubmissionResponse.Detail.status`.
- Produces: a responsive main page showing problem rows, submission fields, and only `대기 중`, `채점 중`, `통과`, `실패`, or `처리 오류`.

- [ ] **Step 1: Remove excluded result UI from the current document**

Delete `renderTests`, `renderFeedback`, test table markup, feedback buttons, and `handleFeedback`. Keep submission creation and `refreshSubmission`.

- [ ] **Step 2: Render status-only result card**

Replace the current result renderer with:

```js
function renderSubmissionResult() {
  const submission = state.submission;
  if (!submission) return;
  resultContainer.innerHTML = `
    <section class="grading-result">
      ${statusChip(submission.status)}
      <p>${statusMessage(submission.status, submission.errorMessage)}</p>
      <button id="refresh-result" type="button">결과 새로고침</button>
    </section>`;
}
```

`statusMessage` maps `PASSED` to `모든 채점 조건을 통과했습니다.` and `FAILED` to `채점 조건을 통과하지 못했습니다.` without exposing counts, test names, scores, or failure details.

- [ ] **Step 3: Apply the green-accent work-oriented visual system**

Use `--accent: #1f8f5f`, `--accent-soft: #edf8f2`, off-white canvas, white panels, dark text, and thin neutral dividers. Make the desktop layout a 360px problem list and flexible detail pane. Below 860px, stack the panes. Keep stage tabs horizontally scrollable.

- [ ] **Step 4: Verify user-visible behavior in Docker-served browser**

Run `SECURITY_ENABLED=false docker compose up -d --build api`. Confirm `GET /api/problems?stage=ROUND_1` loads the empty state without console errors, no `setInterval` occurs, and mobile width does not overflow.

- [ ] **Step 5: Run server checks and commit**

Run: `./gradlew test --rerun-tasks && ./gradlew spotlessCheck checkstyleMain checkstyleTest`

Expected: both commands return `BUILD SUCCESSFUL`.

```bash
git add src/main/resources/static/index.html docs/superpowers/plans/2026-07-23-result-focused-main-page.md
git commit -m "feat: simplify grading result workspace"
```

## Self-Review

1. Spec coverage: the single task changes the main workspace to status-only results with manual refresh and green coding-practice-inspired visual hierarchy.
2. Placeholder scan: all excluded controls, status mappings, endpoint dependencies, and verification commands are explicit.
3. Type consistency: the page continues to read `SubmissionResponse.Detail.status` and ignores its `tests` field.
