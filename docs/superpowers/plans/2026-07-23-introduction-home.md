# Introduction Home Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an introduction-focused landing page at `/` and move the current grading workspace to `/problems.html`.

**Architecture:** Static pages served by Spring Boot remain responsible for the user interface. The problem page retains its current API-driven submission behavior, while the new landing page independently reads the existing problem API for a summary and reads browser-local per-problem submission statuses without adding a server endpoint.

**Tech Stack:** Java 21, Spring Boot static resources, vanilla HTML/CSS/JavaScript, Docker Compose, Gradle, JUnit 5.

## Global Constraints

- Use the existing REST APIs only; do not add an endpoint for home-page metrics.
- Keep the result surface status-only: queued, running, passed, failed, or error.
- Do not add score, per-test result, feedback, or automatic polling.
- Preserve the existing GitHub submission workflow on `/problems.html`.
- Use white surfaces, thin dividers, soft gray backgrounds, and green accents; do not copy third-party branding, assets, or source.
- Verify desktop and 390px mobile layouts without text overlap or unintended horizontal overflow.

---

### Task 1: Relocate the Existing Problem Workspace

**Files:**
- Create: `src/main/resources/static/problems.html`
- Modify: `src/main/resources/static/index.html`

**Interfaces:**
- Consumes: existing `GET /api/problems?stage=<ROUND_1|ROUND_2|ROUND_3|ROUND_4|ROUND_5|FINAL>`, `GET /api/problems/{slug}`, `POST /api/problems/{slug}/submissions`, and `GET /api/submissions/{id}` endpoints.
- Produces: `/problems.html` as the unchanged interactive grading workspace and `/` as a separate page location available for the landing page.

- [ ] **Step 1: Copy the current workspace into the problem route**

Run:

```bash
cp src/main/resources/static/index.html src/main/resources/static/problems.html
```

Expected: `problems.html` contains the current stage tabs, problem list, submission form, and manual refresh behavior.

- [ ] **Step 2: Update the shared problem-page navigation**

In `problems.html`, replace the current single active navigation link with these exact links:

```html
<nav class="site-nav" aria-label="주요 메뉴">
  <a class="nav-link" href="/">홈</a>
  <a class="nav-link" href="/problems.html" aria-current="page">문제</a>
</nav>
```

Keep the brand link as `href="/"` and the GitHub login link as `href="/oauth2/authorization/github"`.

- [ ] **Step 3: Persist the latest submission status for the home summary**

Add these helpers directly before the existing `submit` function in `problems.html`:

```javascript
const SUBMISSION_STATUSES_KEY = 'woowa-practice.submission-statuses';

function saveSubmissionStatus(slug, submission) {
  const statuses = JSON.parse(localStorage.getItem(SUBMISSION_STATUSES_KEY) || '{}');
  statuses[slug] = {
    id: submission.id,
    status: submission.status,
    submittedAt: submission.submittedAt
  };
  localStorage.setItem(SUBMISSION_STATUSES_KEY, JSON.stringify(statuses));
}
```

Immediately after the existing submit response assigns `state.submission`, call:

```javascript
saveSubmissionStatus(state.selectedSlug, state.submission);
```

The stored object is intentionally status-only and contains no repository URL, commit SHA, test details, or feedback.

- [ ] **Step 4: Verify the existing problem workflow manually**

Run:

```bash
./gradlew test --rerun-tasks
```

Expected: `BUILD SUCCESSFUL`.

Open `http://localhost:8080/problems.html` and confirm the `문제` navigation is active, stage tabs render, the list API request is made, and the page still has no `setInterval` call.

- [ ] **Step 5: Commit the route split**

```bash
git add src/main/resources/static/problems.html
git commit -m "feat: move grading workspace to problem page"
```

### Task 2: Build the Introduction Landing Page

**Files:**
- Modify: `src/main/resources/static/index.html`

**Interfaces:**
- Consumes: `GET /api/problems?stage=<stage>` for all six known stages and local storage key `woowa-practice.submission-statuses` containing a slug-keyed map of `{ id, status, submittedAt }` values.
- Produces: a static landing page at `/` with an introduction hero, a problem-count summary, and a status-only latest submission summary.

- [ ] **Step 1: Replace the old problem-page markup with the landing-page structure**

Replace the body content with a shared header and the following semantic sections:

```html
<header class="site-header">
  <div class="header-inner">
    <a class="brand" href="/">Woowa Practice <small>코드 연습</small></a>
    <nav class="site-nav" aria-label="주요 메뉴">
      <a class="nav-link" href="/" aria-current="page">홈</a>
      <a class="nav-link" href="/problems.html">문제</a>
    </nav>
    <a class="login-link" href="/oauth2/authorization/github">GitHub 로그인</a>
  </div>
</header>
<main>
  <section class="intro-hero" aria-labelledby="intro-title">
    <p class="eyebrow">PRECOURSE PRACTICE</p>
    <h1 id="intro-title">Woowa Practice</h1>
    <p>GitHub 저장소를 제출해 프리코스 과제를 검증하세요.</p>
    <a class="primary-link" href="/problems.html">문제 풀러가기</a>
  </section>
  <section class="how-it-works" aria-labelledby="how-title">
    <h2 id="how-title">연습 흐름</h2>
    <ol class="step-list">
      <li><strong>1. 저장소 제출</strong><span>공개 GitHub 저장소 주소를 등록합니다.</span></li>
      <li><strong>2. 격리 채점</strong><span>문제의 공식 테스트를 주입해 실행합니다.</span></li>
      <li><strong>3. 결과 확인</strong><span>통과 또는 실패 결과를 확인합니다.</span></li>
    </ol>
  </section>
  <section class="home-summary" aria-labelledby="summary-title">
    <h2 id="summary-title">현황</h2>
    <div class="summary-grid">
      <article><span>공개 문제</span><strong id="public-problem-count">-</strong></article>
      <article><span>통과한 문제</span><strong id="passed-problem-count">0</strong></article>
      <article><span>최근 제출</span><strong id="latest-submission-status">제출 이력 없음</strong></article>
    </div>
  </section>
</main>
<div id="page-status" role="status" aria-live="polite"></div>
```

- [ ] **Step 2: Add the landing-page styling**

Keep the existing CSS variable names for green, ink, muted text, surface, and divider colors. Add styles that satisfy these concrete layout rules:

```css
.intro-hero { max-width: 1192px; margin: 28px auto 0; padding: 72px 40px; border-block: 1px solid var(--line); background: #edf7f1; }
.how-it-works, .home-summary { max-width: 1192px; margin: 0 auto; padding: 40px; border-bottom: 1px solid var(--line); }
.step-list, .summary-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; }
@media (max-width: 540px) { .intro-hero, .how-it-works, .home-summary { margin: 0; padding: 32px 20px; } .step-list, .summary-grid { grid-template-columns: 1fr; } }
```

Style `primary-link` as the single green command button. Style each `summary-grid article` as an individually framed item with a maximum `8px` border radius. Do not use gradients, decorative image assets, or text inside non-command pill controls.

- [ ] **Step 3: Implement the problem count and local latest-status summary**

Add this script after the page-status element:

```javascript
const STAGES = ['ROUND_1', 'ROUND_2', 'ROUND_3', 'ROUND_4', 'ROUND_5', 'FINAL'];
const SUBMISSION_STATUSES_KEY = 'woowa-practice.submission-statuses';

function statusLabel(status) {
  return ({ QUEUED: '대기 중', RUNNING: '채점 중', PASSED: '통과', FAILED: '실패', ERROR: '처리 오류' })[status] || '제출 이력 없음';
}

async function loadPublicProblemCount() {
  const results = await Promise.all(STAGES.map(async stage => {
    const response = await fetch(`/api/problems?stage=${stage}`);
    if (!response.ok) throw new Error('문제를 불러오지 못했습니다.');
    const body = await response.json();
    return Array.isArray(body.problems) ? body.problems.length : 0;
  }));
  document.querySelector('#public-problem-count').textContent = results.reduce((total, count) => total + count, 0);
}

function loadSubmissionSummary() {
  const raw = localStorage.getItem(SUBMISSION_STATUSES_KEY);
  if (!raw) return;
  try {
    const statuses = Object.values(JSON.parse(raw));
    const latest = statuses.sort((left, right) => Date.parse(right.submittedAt) - Date.parse(left.submittedAt))[0];
    if (!latest) return;
    document.querySelector('#latest-submission-status').textContent = statusLabel(latest.status);
    document.querySelector('#passed-problem-count').textContent = statuses.filter(item => item.status === 'PASSED').length;
  } catch (error) {
    localStorage.removeItem(SUBMISSION_STATUSES_KEY);
  }
}

loadSubmissionSummary();
loadPublicProblemCount().catch(() => {
  document.querySelector('#public-problem-count').textContent = '확인 필요';
});
```

This page must perform no repeating fetch, timeout loop, or polling.

- [ ] **Step 4: Check browser-visible behavior**

Run:

```bash
SECURITY_ENABLED=false /Applications/Docker.app/Contents/Resources/bin/docker compose up -d --build api
curl --fail --silent http://localhost:8080/
curl --fail --silent http://localhost:8080/problems.html
```

Expected: both curl commands return HTML without errors. In the browser, inspect the desktop and 390px versions of `/`, then follow `문제 풀러가기` and confirm `/problems.html` opens. Confirm browser console error logs are empty.

- [ ] **Step 5: Commit the landing page**

```bash
git add src/main/resources/static/index.html src/main/resources/static/problems.html
git commit -m "feat: add introduction home page"
```

### Task 3: Run Final Project Verification

**Files:**
- Verify only: `src/main/resources/static/index.html`
- Verify only: `src/main/resources/static/problems.html`

**Interfaces:**
- Consumes: the static route split from Tasks 1 and 2.
- Produces: verified local Docker runtime, formatted static markup, and a clean working tree ready to publish.

- [ ] **Step 1: Run the full verification suite**

```bash
./gradlew test --rerun-tasks && ./gradlew spotlessCheck checkstyleMain checkstyleTest
```

Expected: both Gradle invocations report `BUILD SUCCESSFUL`.

- [ ] **Step 2: Inspect scope and status-only constraints**

```bash
git diff HEAD~2..HEAD --check
rg -n "setInterval|renderTests|feedback|score" src/main/resources/static/index.html src/main/resources/static/problems.html
git status --short
```

Expected: no whitespace errors, no automatic polling or newly displayed score/test/feedback UI, and an empty working tree.

- [ ] **Step 3: Publish the verified branch**

```bash
git push origin develop
```

Expected: GitHub accepts the latest `develop` commits.
