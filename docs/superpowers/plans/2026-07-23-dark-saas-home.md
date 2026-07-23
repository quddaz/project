# Dark SaaS Home Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restyle `/` as a dark, high-contrast SaaS home while retaining the existing local animated artwork and data behavior.

**Architecture:** Change only `index.html` CSS and home copy. Existing static APIs, local artwork, CSS layers, and Three.js module remain intact.

### Task 1: Apply the Dark Home System

**Files:**
- Modify: `src/main/resources/static/index.html`

- [ ] Set the page, header, sections, flow panels, and summary panels to the documented near-black/cyan/blue palette.
- [ ] Change the home hero heading to two lines with cyan-blue-purple highlight, retaining accessible copy and problem command.
- [ ] Keep artwork/particles under a high-contrast overlay; keep the problem page untouched.
- [ ] Stack dark panels correctly at 390px and keep all reduced-motion behavior.

### Task 2: Verify and Run

- [ ] Run `git diff --check`, `./gradlew test --rerun-tasks`, and `./gradlew spotlessCheck checkstyleMain checkstyleTest`.
- [ ] Rebuild the Docker API, verify `/` and `/vendor/three.module.js`, then inspect desktop/mobile where browser access permits.
- [ ] Commit, push `develop`, sync the desktop copy, and leave `http://localhost:8080/` running.
