# Logo Hero Design

## Goal

Use the generated logo-centered image as the visual hero on the introduction home page without changing the existing problem workspace or grading behavior.

## Visual Design

- Add the generated PNG under `src/main/resources/static/images/` so Spring Boot serves it with the application.
- Replace the light green home hero with a full-width dark visual hero using the image as the background.
- Keep the central logo visible in the lower-middle part of the frame and preserve the dark negative space above it.
- Overlay the existing `PRECOURSE PRACTICE` label, `Woowa Practice` heading, introduction sentence, and `문제 풀러가기` command. The copy is not placed inside a card.
- Use high-contrast white text and the existing green command button. Add a dark image overlay only as needed for accessible text contrast.
- Preserve the bright, structured `연습 흐름` and `현황` sections beneath the hero.

## Responsive Behavior

- Use a fixed hero composition with `background-size: cover` and a desktop minimum height around 520px.
- At 390px width, use a minimum height around 480px and a background position that keeps the logo visible below the copy.
- Avoid horizontal overflow, text overlap, and visual controls obscuring the logo.

## Verification

- Run existing Gradle tests, Spotless, and Checkstyle.
- Rebuild the Docker API and confirm the image returns HTTP 200.
- Inspect desktop and 390px mobile home screenshots in the browser; confirm the image loads, text remains legible, and browser console errors are empty.
