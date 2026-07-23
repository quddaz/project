# Hero Image Parallax Design

## Goal

Make the logo hero artwork feel alive with a gentle floating motion and desktop pointer parallax while preserving readable foreground content.

## Visual Behavior

- Move the logo artwork from the `.intro-hero` background declaration into a dedicated `.hero-artwork` element below all foreground text and controls.
- The artwork slowly floats vertically over a 9-second CSS animation with a movement no greater than 10px.
- On desktop, pointer movement within the hero updates CSS custom properties that shift the artwork no more than 12px horizontally and 8px vertically.
- Existing Three.js particles continue to respond to the same pointer movement, but grading and navigation behavior remain unchanged.

## Layering

- `.hero-artwork` sits above the dark base background and below the existing contrast overlay.
- The contrast overlay remains above artwork and below particles, stars, energy lines, marquee, and interactive content.
- Hero copy and the problem command remain the highest visible layer and are never occluded by the artwork.

## Accessibility and Responsive Behavior

- The artwork is decorative and uses `aria-hidden="true"`.
- At 540px and below, disable pointer parallax and retain a static artwork position.
- Under `prefers-reduced-motion: reduce`, disable floating and pointer parallax while keeping the static image visible.

## Verification

- Run Gradle tests, Spotless, and Checkstyle.
- Rebuild Docker and inspect the desktop hero for movement and text readability.
- Inspect 390px mobile for a stable static image, no overlap, and no console errors.
