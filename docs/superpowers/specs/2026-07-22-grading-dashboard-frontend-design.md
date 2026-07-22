# Grading Dashboard Frontend Design

## Goal

Convert the static user and administrator pages into focused operational interfaces for exploring precourse problems, submitting a GitHub repository, tracking grading, reading test outcomes, and maintaining DB-managed problem definitions.

## Scope

This work updates the existing static Spring Boot assets only. It adds no frontend framework, backend endpoint, AI provider, or source-code storage behavior.

## User Dashboard

The user page at `/` is a desktop-first grading dashboard with a responsive single-column mobile layout.

- A compact header identifies Woowa Practice and provides GitHub login.
- A fixed set of stage tabs selects `ROUND_1` through `FINAL` and requests `GET /api/problems?stage={stage}`.
- The main area has a problem list and one selected-problem work panel. The list shows title, stage, and ordering without decorative cards.
- The work panel shows a selected problem's title, description, Java version, and starter repository link. It accepts a GitHub repository URL and optional commit SHA.
- Submission calls `POST /api/problems/{slug}/submissions`. The panel changes immediately to a queued state and stores the returned submission ID in page state.
- Submission status is refreshed only when the user explicitly reloads the page or presses a refresh control. The page never polls in the background.
- The result view shows submission status and every returned test result. A refresh button requests the latest result for the last submission ID. A feedback request is available once grading is terminal and reads the existing feedback endpoint when present.
- Empty stages, unauthenticated submissions, network failures, invalid responses, and request errors are rendered as concise inline states. Existing API error messages remain the user-facing source of truth.

## Administrator Workspace

The administrator page at `/admin/problems.html` serves repeated problem-maintenance work.

- The left area lists `GET /api/admin/problems` results with stage, version, and selected state.
- The right area keeps the existing creation form, grouped into metadata, problem content, and official test-source sections.
- After a successful `POST /api/admin/problems`, the list refreshes and the form resets to usable defaults.
- A selected problem exposes a version-publishing form that calls `POST /api/admin/problems/{slug}/versions`.
- Validation, permission, and API failures are shown next to the action that failed. The page does not invent client-side policy validation beyond required field affordances.

## Visual System

- Use a restrained work-oriented palette: near-white surfaces, ink text, blue primary actions, green pass states, red failure states, amber queued/running states.
- Use dense but readable rows, 6-8px control corners, clear focus rings, and semantic status chips.
- Use inline SVG-free Unicode-independent text and CSS only; no external icon or asset dependency is needed for this operations dashboard.
- Keep all text at fixed readable sizes. Layout changes through grid collapse rather than viewport-scaled typography.
- Buttons represent commands; tabs represent stages; status is not encoded only by color.

## Data And Failure Handling

Page state is kept in JavaScript memory: active stage, problems, selected slug, and the last submission ID. Fetch helpers parse the existing API error shape and return an actionable message even when the server response is non-JSON.

The dashboard never assumes a problem exists or a user is logged in. A `401` or `403` submission response points the user to GitHub login without clearing entered repository data. A manual refresh retrieves the latest submission result.

## Verification

- Existing API acceptance tests remain green.
- Add static-page tests only where the existing test infrastructure can exercise a user-visible contract; do not add a browser framework solely for markup assertions.
- Run the application in Docker with `SECURITY_ENABLED=false` and verify stage loading, empty state, and invalid-admin-request messaging through the browser.
- Verify desktop and mobile viewport layouts manually after the application assets are rebuilt.

## Non-Goals

- AI feedback generation or provider integration.
- Comparing submissions.
- Storing submission source snapshots.
- A separate SPA build pipeline or frontend framework.
