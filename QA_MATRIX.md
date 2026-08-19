# QA Matrix

The executable QA source is `CatalogAndEngineTest`, which verifies all five levels have at least 30 playable games, all 150 playable games generate valid deterministic rounds, IDs are unique, metadata is present, options are unique, challenges round-trip and backup parsing rejects malformed data.

It also verifies the imported Best 20 Games workbook blueprint: 20 mapped games, 12 levels each, 240 stages total, playable catalogue mappings, increasing score multipliers and valid question generation across all 12 tiers.

## Common Status For All 150 Games

- Launch status: routed through shared Game Details and Play screens.
- Tutorial status: shared Session Brief screen.
- Generated content: deterministic by game ID, seed and difficulty tier.
- Difficulty: catalogue tier with Adaptive, Fixed and Relaxed mode support.
- Pause/resume: shared pause state.
- Results: shared score, accuracy, XP, stars and personal best.
- Rewards: completed-session path persists once per completion event.
- Accessibility: shared large controls and text instructions; deeper TalkBack QA remains manual.
- Theme: shared Material 3 light/dark theme.
- Localisation: resource scaffolding exists; hardcoded Compose strings remain a release limitation.

## Representative Rows

| Level | Count | Engine family | Automated coverage |
| --- | ---: | --- | --- |
| Explorer | 30 | Shared math, memory, logic, reading, focus, spatial, speed generators | Unit matrix |
| Challenger | 30 | Shared generated-question engines plus Phase 1 explicit games | Unit matrix |
| Thinker | 30 | Intermediate generated-question engines with adaptive tier input | Unit matrix |
| Mastermind | 30 | Advanced generated-question engines with challenge compatibility | Unit matrix |
| Genius | 30 | Expert generated-question engines with challenge compatibility | Unit matrix |

## Manual QA Still Required

- Full 150-game tap-through on device.
- Process-death restoration.
- Large-font review.
- Arabic RTL review.
- Tablet/foldable screenshots.
- Reduced-motion review.
- Real store screenshots and feature graphic from device renders.
