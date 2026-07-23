# Solved-Inspired Problem Home Design

## Goal

Make the Woowa Practice main page feel like a focused problem-practice home: a compact navigation and large problem banner inspired by solved.ac's learning-home rhythm, followed by a programming-practice-style problem list and GitHub-based grading flow.

## Reference Boundary

Use only general interaction ideas from solved.ac and Programmers: prominent problem discovery, compact navigation, instructional banner, stage filtering, and scannable problem rows. Do not reproduce their brands, logos, labels, tier systems, statistics, assets, source code, or exact layouts.

## Main Page

- The header contains `Woowa Practice`, a current `문제` navigation item, and GitHub login.
- A full-width but bounded banner introduces `문제` and explains that users submit a GitHub repository to validate a precourse assignment.
- The problem section begins directly below the banner with stage tabs and a table-like list.
- Each problem row displays order, title, stage, and its local latest-result state when the user has submitted in the current page session. It never shows a numeric score.
- Selecting a row opens the problem workspace beside the list on desktop and beneath it on mobile.
- The workspace contains problem description, starter-repository link, repository URL field, optional commit SHA, submit command, and one explicit result-refresh command.
- Result state is limited to `대기 중`, `채점 중`, `통과`, `실패`, and `처리 오류`. Test details, feedback, and background polling remain excluded.

## Visual System

- Use a neutral white and soft-gray canvas, charcoal text, and one restrained green accent for active navigation, selected rows, focus states, and successful grading.
- Use banner typography only inside the banner; lists and workspace use compact operational sizing.
- Keep panels at 6-8px radius, use thin separators, and avoid floating-card stacks or decorative gradients.
- On screens narrower than 860px, stack the selected workspace under the problem list while preserving touch-friendly row and input heights.

## Verification

- Confirm the local page renders a header, banner, stage tabs, and empty problem state with no console errors.
- Confirm no automatic timer API is present.
- Confirm manual refresh is the only submission-result retrieval trigger.
- Verify desktop and 390px mobile layouts in the Docker-served local application.
- Run Gradle tests and style checks after updating static assets.
