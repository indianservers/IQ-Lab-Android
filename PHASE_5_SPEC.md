# IQ Lab Phase 5 Spec

Phase 5 prepares IQ Lab for release-readiness while preserving the compact architecture currently in the repository.

## Release Goals

- Keep all 150 catalogue games playable.
- Harden challenge-code and backup parsing.
- Add release build configuration with R8/resource shrinking.
- Document privacy, localisation, QA, release gates and store listing copy.
- Preserve offline-first behaviour and avoid unnecessary permissions.

## Non-Goals

- No Play Store submission.
- No committed signing credentials or keystores.
- No cloud sync, analytics SDK, ads, purchases, social features or medical claims.
- No fake screenshots, fake rankings, fake ratings or fake online users.

## Current Architecture Constraint

The app uses a compact Compose plus SharedPreferences architecture. Phase 5 does not introduce Room/DataStore/Hilt because doing so late would risk user-progress compatibility without enough migration history.
