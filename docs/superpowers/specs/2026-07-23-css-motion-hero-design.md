# CSS Motion Hero Design

## Goal

Enhance only the introduction home hero with a high-contrast, portfolio-inspired motion treatment. Keep the problem workspace and all grading behavior unchanged.

## Scope

- Continue using the generated logo hero image served at `/images/woowa-practice-logo-hero.png`.
- Do not add React, Three.js, Tailwind, Framer Motion, a canvas, or any new dependency.
- Do not copy the referenced portfolio's brand, copy, assets, or layout.
- Preserve the existing home header, introduction copy, `문제 풀러가기` link, and bright information sections below the hero.

## Hero Composition

- Keep the existing full-width dark image hero with white copy, acid-lime `PRECOURSE PRACTICE` eyebrow, and green problem command.
- Add two non-interactive decorative layers behind the copy: a sparse star field and several faint blue energy lines. Use pseudo-elements and CSS gradients only.
- Add a small monospace metadata line below the introduction copy, describing the practice flow without instructional prose: `GITHUB SUBMISSION / SANDBOX GRADING / PASS OR FAIL`.
- Add a bottom-edge marquee inside the hero with repeated `WOOWA PRACTICE // PRECOURSE REVIEW //` text. The text has a transparent fill and subtle white outline. It moves slowly leftward.
- The logo remains the primary visual object and is never hidden by controls or marquee text.

## Motion and Accessibility

- Use a low-amplitude CSS animation for the energy lines and marquee only. No pointer tracking, JavaScript timers, or automatic API requests are added.
- Respect `prefers-reduced-motion: reduce` by disabling decorative animations.
- Keep decorative layers `aria-hidden` and preserve contrast for the headline and command.

## Responsive Behavior

- Desktop keeps a 520px minimum hero height and the existing lower-middle logo composition.
- At 390px width, retain the current 480px minimum height and right-shifted logo composition to leave the text readable.
- Hide the marquee on mobile if it competes with the logo focal point; metadata may wrap but must not overflow.

## Verification

- Run Gradle tests, Spotless, and Checkstyle.
- Rebuild the Docker API, inspect the home page at desktop and 390px mobile widths, and confirm console error logs are empty.
