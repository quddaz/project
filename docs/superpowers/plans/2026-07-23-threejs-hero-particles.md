# Three.js Hero Particles Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a low-density Three.js particle canvas behind the home hero content and run the application.

**Architecture:** A pinned Three.js ES module is served locally from `/vendor/three.module.js` and initializes only after the DOM is ready in eligible desktop environments. The transparent canvas is contained by `.intro-hero`; existing CSS layers remain the visual fallback.

**Tech Stack:** Spring Boot static resources, vanilla HTML/CSS/JavaScript, Three.js ES module CDN, Docker Compose, Gradle.

## Global Constraints

- Use a pinned locally served Three.js module and no npm dependency.
- Restrict the scene to 1,200 points and a 1.5 device-pixel-ratio cap.
- Keep pointer events, navigation, and accessibility behavior unchanged.
- Skip initialization for mobile, reduced-motion, or unavailable WebGL.
- Dispose rendering resources and listeners on page unload.

---

### Task 1: Add the Canvas Layer and Three.js Module

**Files:**
- Modify: `src/main/resources/static/index.html`

**Interfaces:**
- Consumes: `.intro-hero` and its existing CSS visual layers.
- Produces: `#hero-particles` canvas with a transparent Three.js scene, or no canvas drawing on unsupported environments.

- [ ] **Step 1: Add the canvas before other hero children**

```html
<canvas id="hero-particles" aria-hidden="true"></canvas>
```

Add the canvas CSS:

```css
#hero-particles { position: absolute; z-index: 1; inset: 0; width: 100%; height: 100%; pointer-events: none; }
```

Set `.hero-stars` and `.hero-energy` to z-index `2`, hero content to z-index `3`, and the marquee to z-index `2` so the canvas remains behind interactive content.

- [ ] **Step 2: Add the ES-module scene setup**

Add a `type="module"` script at the end of body that imports exactly:

```javascript
const THREE = await import('/vendor/three.module.js');
```

Before initialization, return when either condition is true:

```javascript
window.matchMedia('(prefers-reduced-motion: reduce)').matches || window.innerWidth <= 540
```

Create `WebGLRenderer({ canvas, alpha: true, antialias: false, powerPreference: 'low-power' })`, cap pixel ratio with `Math.min(window.devicePixelRatio, 1.5)`, create a perspective camera at z `90`, and create 1,200 points via `THREE.BufferGeometry`, `THREE.Float32BufferAttribute`, `THREE.PointsMaterial`, and `THREE.Points`.

- [ ] **Step 3: Add bounded animation, pointer response, resize, and cleanup**

On each frame, update only `points.rotation.y` and `points.rotation.x` using a small interpolation toward pointer-derived targets, then render the scene. Add a resize listener that uses `hero.clientWidth` and `hero.clientHeight` for aspect ratio and renderer dimensions. On `pagehide`, cancel the animation frame, remove both listeners, dispose geometry/material/renderer, and call `renderer.forceContextLoss()`.

Wrap renderer initialization in `try/catch`; in a catch block, do not alter the CSS fallback or write to the console.

- [ ] **Step 4: Verify source constraints**

```bash
git diff --check
rg -n "1200|prefers-reduced-motion|forceContextLoss|pagehide|/vendor/three.module.js" src/main/resources/static/index.html
```

Expected: no whitespace errors and all five required safeguards are present.

- [ ] **Step 5: Commit the Three.js implementation**

```bash
git add src/main/resources/static/index.html
git commit -m "feat: add threejs hero particles"
```

### Task 2: Run and Verify WebGL Fallbacks

**Files:**
- Verify only: `src/main/resources/static/index.html`

**Interfaces:**
- Consumes: the completed Three.js hero module.
- Produces: verified desktop WebGL particles and mobile CSS fallback.

- [ ] **Step 1: Run project checks**

```bash
./gradlew test --rerun-tasks && ./gradlew spotlessCheck checkstyleMain checkstyleTest
```

Expected: both Gradle commands report `BUILD SUCCESSFUL`.

- [ ] **Step 2: Rebuild and start Docker**

```bash
SECURITY_ENABLED=false /Applications/Docker.app/Contents/Resources/bin/docker compose up -d --build api
curl --fail --silent http://localhost:8080/ >/dev/null
```

Expected: the API runs and the home page returns HTTP 200.

- [ ] **Step 3: Verify the rendered canvas**

Open `http://localhost:8080/` in the browser. Confirm `#hero-particles` has a nonblank canvas pixel sample and an animation frame changes the canvas sample. Move the pointer inside the hero and confirm the particle layer responds without blocking `문제 풀러가기`. Capture a desktop screenshot.

- [ ] **Step 4: Verify mobile fallback and console**

Set the viewport to 390 by 844, reload, and confirm the canvas is absent or blank while the CSS hero stays visible. Confirm browser console error logs are empty, reset the viewport, and keep the home page open.

- [ ] **Step 5: Publish the verified implementation**

```bash
git push origin develop
```

Expected: GitHub accepts the latest `develop` commits.
