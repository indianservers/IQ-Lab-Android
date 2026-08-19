# IQ Lab Phase 4 Spec

Phase 4 expands IQ Lab from 90 playable games to the full 150-game catalogue by making Mastermind and Genius playable, then adds local challenge and tournament foundations.

## Scope

- Preserve existing IDs and local SharedPreferences progress.
- Add 30 playable Mastermind games and 30 playable Genius games.
- Keep Explorer, Challenger and Thinker playable.
- Add deterministic local challenge definitions, share-code export/import, daily arena, weekly cup, pass-and-play data models and versioned scoring metadata.
- Expand achievements to at least 75 functional definitions.

## Out Of Scope

- Cloud accounts, online multiplayer, public leaderboards, fake opponents, purchases, ads, medical or clinical IQ claims, full localisation and Play Store release work.

## Persistence

The current app does not use Room. Phase 4 preserves the existing SharedPreferences store and adds compact local model foundations rather than introducing a database migration midstream.
