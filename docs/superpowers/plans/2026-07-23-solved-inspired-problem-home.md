# Solved-Inspired Problem Home Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the main page with a solved-inspired problem home and Programmers-style problem list while retaining GitHub submission and status-only grading results.

**Architecture:** Keep the current static page and existing REST requests. Add header navigation and a bounded problem banner, then reshape the list into a table-like section and retain the existing responsive selected-problem workspace.

**Tech Stack:** Static HTML, CSS, browser JavaScript, Spring Boot static resources.

## Global Constraints

- Do not copy reference-site branding, source, assets, exact copy, tiers, or statistics.
- Do not add a score, code editor, test details, feedback controls, or automatic polling.
- Keep manual result refresh and existing API contracts.
- Use green as the single active/success accent with white/soft-gray surfaces and thin dividers.

---

### Task 1: Build The Banner And Problem List Home

**Files:**
- Modify: `src/main/resources/static/index.html`

**Interfaces:**
- Consumes: existing problem list/detail and submission APIs.
- Produces: header navigation, problem banner, table-like problem rows, responsive selection workspace, and status-only result display.

- [ ] **Step 1: Add header navigation and banner landmarks**

Add a header with `Woowa Practice`, an active `문제` link, and GitHub login. Add a banner before stage filters:

```html
<section class="problem-banner" aria-labelledby="problem-banner-title">
  <p class="banner-eyebrow">PRECOURSE PRACTICE</p>
  <h1 id="problem-banner-title">문제</h1>
  <p>GitHub 저장소를 제출해 프리코스 과제를 검증하세요.</p>
</section>
```

- [ ] **Step 2: Render a table-like problem list**

Use list headings `번호`, `문제`, `차수`, `결과`. Each row shows `displayOrder`, title, stage, and a locally stored status or `미제출` without numeric score.

- [ ] **Step 3: Preserve status-only workspace behavior**

Keep problem detail, repository URL, commit SHA, submit command, and `결과 새로고침`. Ensure result rendering exposes only the five Korean states and short messages, never tests or feedback.

- [ ] **Step 4: Verify and run locally**

Run `./gradlew test --rerun-tasks && ./gradlew spotlessCheck checkstyleMain checkstyleTest`, then `SECURITY_ENABLED=false docker compose up -d --build api`. Verify header, banner, list headings, empty state, and mobile 390px layout with no console errors.

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/static/index.html docs/superpowers/plans/2026-07-23-solved-inspired-problem-home.md
git commit -m "feat: build problem home workspace"
```

## Self-Review

1. The plan covers header, banner, table-like problem list, status-only submission workspace, desktop/mobile verification, and local Docker execution.
2. All visual references are bounded to general patterns; forbidden copied branding and excluded result features are explicit.
3. Existing API contracts remain unchanged.
