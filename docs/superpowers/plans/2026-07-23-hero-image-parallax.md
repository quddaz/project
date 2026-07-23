# Hero Image Parallax Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Animate the logo artwork with gentle float and desktop pointer parallax.

**Architecture:** Replace the hero background image with an `aria-hidden` artwork layer controlled by CSS custom properties. A short script updates those properties from desktop pointer position; CSS handles animation and fallback rules.

**Tech Stack:** Static HTML, CSS, vanilla JavaScript, Spring Boot static resources, Docker Compose, Gradle.

### Task 1: Add Artwork Layer and Motion

**Files:**
- Modify: `src/main/resources/static/index.html`

- [ ] Add `<div class="hero-artwork" aria-hidden="true"></div>` as the first visual child after the particle canvas.
- [ ] Remove the image URL from `.intro-hero` and style `.hero-artwork` with the existing image URL, `transform: translate(var(--art-x, 0px), var(--art-y, 0px))`, and a 9-second `hero-float` animation bounded to 10px vertical movement.
- [ ] Keep artwork below the dark overlay, decorative layers, and content. Set its initial background position to the existing desktop and mobile positions.
- [ ] Add a desktop-only pointer listener that writes `--art-x` in a -12px to 12px range and `--art-y` in a -8px to 8px range. Add `pointerleave` reset and skip all listeners for mobile or reduced motion.
- [ ] Disable artwork animation under `prefers-reduced-motion: reduce` and preserve static art on mobile.

### Task 2: Verify and Publish

**Files:**
- Verify only: `src/main/resources/static/index.html`

- [ ] Run `git diff --check`, `./gradlew test --rerun-tasks`, and `./gradlew spotlessCheck checkstyleMain checkstyleTest`.
- [ ] Rebuild with `SECURITY_ENABLED=false /Applications/Docker.app/Contents/Resources/bin/docker compose up -d --build api` and confirm `http://localhost:8080/` returns successfully.
- [ ] Inspect desktop and 390px mobile rendering in the browser, verify no console errors, then commit and push `develop`.
