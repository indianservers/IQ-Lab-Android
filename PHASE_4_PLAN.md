# IQ Lab Phase 4 Plan

1. Existing-project audit and regression repair.
2. Add durable Phase 4 docs.
3. Implement required Mastermind catalogue entries.
4. Implement required Genius catalogue entries.
5. Extend shared generators so all 150 playable games produce valid rounds.
6. Add challenge and tournament model foundations.
7. Add advanced mastery and 75 achievement definitions.
8. Add UI surfaces for challenges and full-level completion signals inside the existing navigation.
9. Add tests for catalogue, challenge codes, tournaments, scoring and achievements.
10. Run unit tests, lint and debug build.

## Validation Gates

- Unit tests must prove all five levels have at least 30 playable games.
- Challenge-code round trip and invalid-code rejection must pass.
- Daily and weekly seeds must be deterministic.
- Lint and debug assembly must pass.
