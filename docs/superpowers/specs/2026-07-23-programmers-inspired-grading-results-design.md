# Programmers-Inspired Grading Results Design

## Goal

Reframe the user dashboard as a compact problem-solving workspace inspired by the information density and navigation rhythm of coding-practice sites, while keeping GitHub submission as the only code-delivery method.

## Reference Boundary

Use the general interaction pattern of a coding-practice service: compact top navigation, stage filters, table-like problem rows, restrained white surfaces, thin separators, and green status accents. Do not copy the Programmers logo, wording, proprietary assets, page structure, or source code.

## User Workspace

- The left pane is a problem table with stage, title, order, and the latest known submission state when available.
- The right pane presents the selected problem's description, starter repository link, GitHub repository URL input, optional commit SHA, and one submission command.
- No browser-based code editor, source preview, score, numeric progress, test-result table, or feedback controls appear on the user page.
- After submission, the result area shows only a large semantic state: `대기 중`, `채점 중`, `통과`, `실패`, or `처리 오류`.
- Result retrieval is explicit through a `결과 새로고침` button. There is no background polling.
- `PASSED` maps to `통과`; `FAILED` maps to `실패`; `QUEUED`, `RUNNING`, and `ERROR` retain distinct non-score states.

## Administrator Workspace

- Keep the existing problem-list, creation, and version-publication workflows.
- Apply the same compact white-surface, line-separated, green-accent visual vocabulary.
- Administrator-only technical fields, including official test source, remain visible because they are required for problem management.

## Data And Failure Behavior

- Continue using existing APIs; no API or database schema change is needed.
- The user page reads submission `status` only for the result region and ignores `tests` and feedback payloads.
- Authentication, validation, repository, and network failures show the server-provided message next to the action that failed.
- A manual result refresh preserves submitted repository fields and does not create another submission.

## Verification

- Confirm no page code contains automatic polling APIs such as `setInterval`.
- Confirm a `PASSED` response renders `통과` without score or test details; confirm `FAILED` renders `실패`.
- Verify desktop and mobile layouts in the local Docker-served application.
- Run the existing Gradle test and style checks after static asset updates.
