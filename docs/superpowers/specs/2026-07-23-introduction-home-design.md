# Introduction Home Design

## Goal

Make `/` an introduction-focused landing page for Woowa Practice. Keep problem discovery and GitHub repository submission on a separate problem page.

## Navigation

- The shared header shows the Woowa Practice brand, `홈`, `문제`, and the existing GitHub login link.
- `홈` points to `/` and is active on the new landing page.
- `문제` points to `/problems.html` and is active on the existing problem workspace.
- The admin page is unchanged.

## Home Page

The page uses a restrained, solved.ac-inspired visual language: a white surface, thin dividers, soft gray page background, and green as the only strong accent. It does not copy solved.ac branding, assets, or layout.

The page contains these sections in order:

1. An introduction hero with the service name, the message `GitHub 저장소를 제출해 프리코스 과제를 검증하세요.`, and a `문제 풀러가기` link to `/problems.html`.
2. A compact three-step explanation: GitHub repository submission, isolated grading, and pass/fail result confirmation.
3. A small status summary showing the number of public problems, passed problems, and the latest submission status. Before there is a submission, the last item explains that there is no submission history.

The hero is a full-width, lightly tinted content band rather than a floating card. The summary may use small, individually framed items. The layout must work at desktop and 390px mobile widths without text overlap or horizontal overflow.

## Problem Page

Move the current `index.html` problem workspace to `problems.html` with its existing behavior intact:

- Stage tabs and table-like problem list remain.
- The GitHub repository submission form, optional commit SHA, and manual result refresh remain.
- User-facing results remain status-only: pending, grading, passed, failed, or error.
- No score, test count, per-test detail, feedback, or automatic polling is introduced.

## Data Flow

- Home reads `GET /api/problems?stage=<stage>` as needed to calculate the total public problem count across stages.
- Home stores the latest successful submission made during the current browser session in local storage and displays only its status. It does not introduce a new backend API.
- The problem page continues using the existing problem and submission APIs.
- API failures render localized, plain-language empty states without blocking navigation.

## Testing

- Keep existing server tests and formatting/checkstyle validation green.
- Confirm `/` and `/problems.html` render from the Docker-served application.
- Verify the desktop and 390px mobile layouts in the browser, including the navigation links and no browser console errors.
