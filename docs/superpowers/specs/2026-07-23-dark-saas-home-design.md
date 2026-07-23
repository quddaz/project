# Dark SaaS Home Design

## Goal

Restyle only the introduction home page as a dark, high-contrast SaaS interface inspired by the supplied BeamQ visual direction while preserving Woowa Practice identity and existing interactions.

## Scope

- Keep `/problems.html` as the current light, work-focused problem workspace.
- Do not use the supplied external video, Tailwind CDN, Google Fonts, or third-party assets because the application must work without external runtime dependencies.
- Reuse the local logo artwork, CSS glow layers, and Three.js particle canvas as the animated hero background.

## Visual System

- Make the full home canvas `#02040a` with near-black section surfaces and thin translucent blue borders.
- Use white for high-priority text, slate blue-gray for supporting copy, cyan `#38bdf8` for metadata, blue `#3b82f6` for primary glow, and restrained purple `#8b5cf6` only in background light effects.
- Update the shared home header to be dark and slightly translucent with backdrop blur. Keep `홈`, `문제`, and GitHub login; use a glass-like outline for login rather than a pill-heavy UI.
- The hero continues to use the animated artwork. Add a dark top-to-middle overlay that protects header and copy contrast while allowing the image and particles to be brighter near the center.
- Change the home headline to two lines: `프리코스 연습을` and a cyan-to-blue-to-purple highlighted `더 깊게.` The existing explanatory sentence and problem command remain beneath it.
- Keep a small cyan uppercase metadata line and retain the outline marquee at the bottom of the hero.

## Content Sections

- Continue the home below the hero as dark full-width bands, not floating page cards.
- Render the practice flow as three compact, translucent dark panels with blue borders and a simple numeric marker.
- Render the status summary as three dense dark panels. Preserve public problem count, browser-local passed count, and latest submission status.
- Use 8px or smaller corner radii and avoid gradients as standalone decorative backgrounds; limited glow overlays within the hero are allowed.

## Responsive and Accessibility

- At 390px, use the existing mobile header behavior, stack flow and summary panels, and keep the art readable behind copy.
- Preserve reduced-motion behavior for artwork, CSS motion, and Three.js.
- Maintain accessible white/slate contrast and use `aria-hidden` for decorative layers.

## Verification

- Run Gradle tests, Spotless, and Checkstyle.
- Rebuild Docker and confirm the home and local Three.js module respond.
- Inspect desktop and mobile screenshots, confirm navigation and problem command stay visible, and check browser console errors.
