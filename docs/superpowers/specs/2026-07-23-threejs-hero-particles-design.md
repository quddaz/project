# Three.js Hero Particles Design

## Goal

Add a lightweight Three.js particle layer to the home hero while retaining the existing logo image, CSS motion treatment, and all product behavior.

## Scope

- Serve the official Three.js ES module from a pinned static resource at `/vendor/three.module.js`; do not add an npm build pipeline or backend dependency.
- Render only inside a transparent canvas positioned within `.intro-hero`, behind the readable content and above the background image.
- Keep the current CSS star field, energy lines, metadata, and marquee as the no-WebGL visual baseline.
- Do not change `/problems.html`, any API, grading, feedback, or navigation behavior.

## Scene

- Create one `THREE.Scene`, one `THREE.PerspectiveCamera`, and one `THREE.WebGLRenderer` with `{ alpha: true, antialias: false, powerPreference: 'low-power' }`.
- Generate 1,200 `THREE.Points` vertices inside a shallow rectangular volume using `THREE.BufferGeometry` and `THREE.PointsMaterial`.
- Use a small blue-white point color, additive blending, transparent material, and low opacity.
- Move the point cloud slowly on each animation frame. Pointer movement shifts the cloud's rotation slightly; it does not affect links, buttons, or page scrolling.
- Cap device pixel ratio at `1.5`, update renderer and camera on resize, and dispose geometry, material, renderer, listeners, and the animation frame during page unload.

## Accessibility and Fallback

- Mark the canvas `aria-hidden="true"` and set `pointer-events: none`.
- Skip Three.js initialization when `prefers-reduced-motion: reduce` is active, WebGL is unavailable, or the viewport is narrower than 541px.
- A failed module load or renderer setup must leave the existing CSS hero visible without displaying an error to the user.

## Verification

- Run Gradle tests, Spotless, and Checkstyle.
- Rebuild Docker and confirm the home page is served.
- Inspect desktop screenshots to confirm a nonblank particle layer behind the content and logo.
- Inspect 390px mobile screenshots to confirm the canvas is absent and the CSS fallback remains usable.
- Confirm console error logs are empty in both layouts.
