# CSS Motion Hero Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add CSS-only star, energy-line, metadata, and marquee treatments to the logo hero and run the application.

**Architecture:** `index.html` stays a single static home page. New decorative DOM nodes and CSS keyframes are isolated within `.intro-hero`; no API, Java, or problem-page code changes are required.

**Tech Stack:** Spring Boot static resources, vanilla HTML/CSS, Docker Compose, Gradle.

## Global Constraints

- Use only CSS and static HTML; do not add libraries, canvas, or JavaScript timers.
- Keep visual layers decorative and inaccessible to screen readers.
- Preserve the current hero image, home content, and `/problems.html` workflow.
- Disable decorative animations under `prefers-reduced-motion: reduce`.
- Validate desktop and 390px mobile rendering before completion.

---

### Task 1: Add the Decorative Hero Layers

**Files:**
- Modify: `src/main/resources/static/index.html`

**Interfaces:**
- Consumes: existing `.intro-hero` and `/images/woowa-practice-logo-hero.png` background.
- Produces: `aria-hidden` star, energy-line, and marquee elements plus text content that remains above every decorative layer.

- [ ] **Step 1: Add hero markup after the opening intro section tag**

Insert these elements as the first children of `.intro-hero`:

```html
<div class="hero-stars" aria-hidden="true"></div>
<div class="hero-energy" aria-hidden="true"></div>
```

Insert the metadata after the introduction paragraph and before the problem link:

```html
<p class="hero-meta">GITHUB SUBMISSION / SANDBOX GRADING / PASS OR FAIL</p>
```

Insert the marquee as the final child of `.intro-hero`:

```html
<div class="hero-marquee" aria-hidden="true">
  <div>WOOWA PRACTICE // PRECOURSE REVIEW // WOOWA PRACTICE // PRECOURSE REVIEW //</div>
</div>
```

- [ ] **Step 2: Add static layer and text stacking CSS**

Add these rules near `.intro-hero`:

```css
.intro-hero > :not(.hero-stars):not(.hero-energy):not(.hero-marquee) { position: relative; z-index: 2; }
.hero-stars, .hero-energy { position: absolute; z-index: 1; inset: 0; pointer-events: none; }
.hero-stars { opacity: .65; background-image: radial-gradient(circle at 10% 28%, #fff 0 1px, transparent 1.5px), radial-gradient(circle at 73% 17%, #88aaff 0 1px, transparent 1.5px), radial-gradient(circle at 83% 48%, #fff 0 1px, transparent 1.5px); background-size: 180px 180px, 240px 240px, 300px 300px; }
.hero-energy { opacity: .26; background: linear-gradient(165deg, transparent 48%, rgba(136, 170, 255, .65) 49%, transparent 50%), linear-gradient(172deg, transparent 61%, rgba(136, 170, 255, .45) 62%, transparent 63%); background-size: 100% 100%, 100% 100%; }
.hero-meta { margin: 0 0 24px; color: rgba(255, 255, 255, .62); font-family: Consolas, monospace; font-size: 11px; line-height: 1.6; }
```

- [ ] **Step 3: Add marquee and reduced-motion CSS**

```css
.hero-marquee { position: absolute; z-index: 1; right: 0; bottom: 14px; left: 0; overflow: hidden; color: transparent; font-family: Impact, Arial Black, sans-serif; font-size: 40px; line-height: 1; white-space: nowrap; -webkit-text-stroke: 1px rgba(255, 255, 255, .22); }
.hero-marquee > div { width: max-content; animation: hero-marquee 22s linear infinite; }
@keyframes hero-marquee { to { transform: translateX(-25%); } }
@media (prefers-reduced-motion: reduce) { .hero-marquee > div { animation: none; } }
@media (max-width: 540px) { .hero-marquee { display: none; } .hero-meta { max-width: 260px; font-size: 10px; } }
```

Keep `.intro-hero::before` at z-index `0`, decorative layers at `1`, and content at `2`.

- [ ] **Step 4: Verify static constraints**

Run:

```bash
git diff --check
rg -n "setInterval|requestAnimationFrame|three|canvas" src/main/resources/static/index.html
```

Expected: no whitespace error; the search returns no new JavaScript animation or canvas implementation.

- [ ] **Step 5: Commit the CSS motion implementation**

```bash
git add src/main/resources/static/index.html
git commit -m "feat: add css motion hero treatment"
```

### Task 2: Run and Visually Verify the Application

**Files:**
- Verify only: `src/main/resources/static/index.html`

**Interfaces:**
- Consumes: the completed static hero composition.
- Produces: a Docker-served home page with desktop and mobile visual evidence.

- [ ] **Step 1: Run project checks**

```bash
./gradlew test --rerun-tasks && ./gradlew spotlessCheck checkstyleMain checkstyleTest
```

Expected: both Gradle commands report `BUILD SUCCESSFUL`.

- [ ] **Step 2: Rebuild and start the API container**

```bash
SECURITY_ENABLED=false /Applications/Docker.app/Contents/Resources/bin/docker compose up -d --build api
curl --fail --silent http://localhost:8080/ >/dev/null
```

Expected: Docker reports the API as running and curl exits successfully.

- [ ] **Step 3: Check desktop and mobile visuals**

Open `http://localhost:8080/` in the browser, wait for hero image rendering, and capture a desktop screenshot. Set the viewport to 390 by 844, reload, capture a mobile screenshot, then reset the viewport. Confirm no console errors and that mobile hides the marquee.

- [ ] **Step 4: Publish the verified branch**

```bash
git push origin develop
```

Expected: GitHub accepts the current `develop` commits.
