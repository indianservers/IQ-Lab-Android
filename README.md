# IQ Lab - Brain Games & IQ Training

Phase 4 foundation for a premium Compose brain-training app. The current build focuses on a polished local-first experience, a real data-driven catalogue, all five playable level libraries, adaptive difficulty controls, local analytics foundations, and deterministic local challenge/tournament primitives.

## Build

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:assembleDebug
```

## Implemented Screens

- First-run onboarding with skip and replay support.
- Home with XP, streak, continue training, daily challenge, level cards and recommendations.
- Catalogue with search, level/category filters, favourites and future-phase treatment.
- Level details with unlock/progress status and all games for that level.
- Game details with instructions, tags, metadata, best score and play/future status.
- Gameplay and results screens for all Explorer, Challenger and Thinker playable games.
- Advanced progress dashboard with rank, XP, level progress, skill confidence, recent session history and 30 achievement definitions.
- Settings with sound, haptics, reduced motion, text-size control, colour-friendly toggle, reset, onboarding replay and about note.
- Adaptive, Fixed and Relaxed difficulty mode controls.
- Home Challenge Centre panel with deterministic Daily Arena and Weekly Cup entry points plus share-code preview.

## Catalogue

The single source of truth is `IqCatalog` in `app/src/main/java/com/indianservers/iqlabs/model/Catalog.kt`.

- 150 total game definitions.
- 30 games per level: Explorer, Challenger, Thinker, Mastermind and Genius.
- Every game includes ID, name, description, level, category, tags, difficulty, duration, age guidance, icon, accent colour, instructions, scoring model, XP reward, unlock rule, best-score type, status and accessibility notes.
- 150 games are marked playable across Explorer, Challenger, Thinker, Mastermind and Genius.

## Playable Games

Explorer contains: Number Pop, Shape Match, Colour Catch, Memory Grid Junior, Picture Pairs, Count the Stars, Bigger or Smaller, Missing Number, Pattern Train, Odd One Out Junior, Shadow Match, Direction Dash, Quick Tap, Simon Colours, Animal Order, Maze Scout, Word and Picture, Letter Hunt, Rhyme Time, First Sound, Tiny Word Sprint, Story Snapshot, Flash Count, Simple Sum, Take Away, Number Balance, Shape Builder, Size Sort, Safe Path and Focus Friend.

Challenger contains: Flash Sum, Memory Grid, Sequence Detective, Word Sprint, Pattern Pulse, Quick Match, Mental Math Rush, Equation Builder, Number Matrix, Target Total, Estimate It, Fraction Match, Logic Gates, Deduction Cards, Rule Switch, Symbol Sequence, Rotation Match, Mirror Mind, Cube Counter, Route Planner, N-Back Starter, Dual Focus, Distraction Filter, Reaction Choice, Word Link, Analogy Starter, Category Sort, Scrambled Words, Reading Scan and Recall Chain.

Thinker contains: Rapid Equations, Number Pyramid, Target Operations, Percentage Pulse, Fraction Forge, Algebra Balance, Prime Patrol, Sequence Fusion, Number Matrix, Mental Chain, Logic Grid, Syllogism Lab, Code Breaker, Conditional Logic, Truth Detectives, Rule Discovery, Rotation Rush, Cube Net, Spatial Fold, Hidden Shape, Mirror Maze, Visual Matrix, Working Memory Mix, Reverse Recall, Memory Interference, Dual N-Back, Target Tracker, Stroop Shift, Reading Burst and Verbal Connections.

Mastermind contains 30 advanced games including Equation Cascade, Number Web, Modular Mind, Logic Grid Pro, 3D Rotation, Dual N-Back Pro, Critical Reading, Argument Analyser and Word Matrix.

Genius contains 30 expert games including Operator Cipher, Alphametic Vault, Number Theory Lab, Logic Grid Elite, Cube Slice, Triple N-Back, Rapid Inference and Grand Gauntlet.

Generated rounds, shared scoring, XP calculation and personal-best checks live in `app/src/main/java/com/indianservers/iqlabs/game/GameEngine.kt`.

## Architecture

- Kotlin + Jetpack Compose + Material 3.
- Dependency-light stateful app shell with manual screen state instead of Navigation Compose, because the starter project did not include navigation dependencies.
- SharedPreferences stores onboarding, XP, streak, sessions, favourites, best scores and settings.
- Catalogue metadata, question generation and scoring are separated from Compose UI.
- Programmatic Compose visuals are used for the current asset foundation: logo/orb, level emblems, cards, background grid and achievement styling.
- `ProgressSystems.kt` contains mastery-star rules, unlock rules, daily plan generation and achievement definitions.
- `AdaptiveAnalytics.kt` contains adaptive difficulty, skill observations, confidence states, session metrics and explainable recommendations.
- `ChallengeSystem.kt` contains deterministic Daily Arena, Weekly Cup, Category Sprint, Grand Gauntlet, share-code import/export, local standings, local leagues and challenge scoring.
- `LevelBlueprint.kt` expands the attached workbook into 30 mapped priority games with 20 levels each, for 600 internal stages.

## Scoring And XP

Score combines correct answers, difficulty, remaining time and best combo, with incorrect-answer penalty and a floor of zero. XP is always non-negative and scales with accuracy, level difficulty and game reward.

## Stars, Unlocks And Daily Plan

Each completed session earns one to three stars from game-specific difficulty thresholds. Explorer is always available; Challenger unlocks through early XP or Explorer completion. The daily plan is generated locally from the current date, mixes playable categories and targets an 8-12 minute session.

## Adaptive Difficulty And Analytics

Difficulty modes are Adaptive, Fixed and Relaxed. Adaptive mode waits for a rolling window of completed rounds, then adjusts one tier at a time using accuracy, median response time and completion quality. Cooldown prevents rapid oscillation; priority games can advance through tier 20 while standard games remain capped at tier 12, and the UI explains the current tier and reason.

The skill model tracks training indicators only, never clinical IQ. It covers mathematical reasoning, memory, working memory, logic, verbal reasoning, reading, processing speed, attention, spatial reasoning, cognitive flexibility, planning and reaction speed. Skill confidence is based on observation count and starts with clear insufficient-data states.

## Challenges And Tournaments

Challenge definitions are versioned, deterministic and backend-free. Share codes contain no private user data and include a lightweight checksum for accidental tamper detection. Daily Arena and Weekly Cup are stable for the same local date or ISO week. Standings are local only; the app does not show fake players, global rankings or population percentiles.

## Top 30 Blueprint

The original workbook blueprint is expanded into a 20-level progression layer for 30 priority games. Game Details and Play Session display the active stage design, and generators/adaptive difficulty support tiers 1-20 for those games.

## Accessibility Notes

The UI uses large touch targets, text-based instructions, TalkBack content descriptions for game icons, non-colour feedback, no account collection, no public leaderboard and no diagnostic language.

## Roadmap

- Phase 2: expand fully playable Kids and Beginner libraries.
- Phase 5: polish, localisation, optional cloud features and release readiness.

## Adding A New Game

1. Add or promote a `GameDefinition` in `IqCatalog`.
2. Add a generator branch in `QuestionFactory.questionsFor`.
3. Keep answers inside `Question.choices`.
4. Add focused unit coverage in `CatalogAndEngineTest`.
5. Use the existing detail, play and result screens unless the game needs a custom interaction surface.

## Current Limits

This is a compact Phase 4 foundation, not the entire long-form brief implemented at enterprise depth. Room, DataStore, Hilt, generated raster art, full Compose UI tests and emulator visual QA are not yet added. Persistence is local SharedPreferences, and the 150 playable games use shared generated-question/session mechanics rather than bespoke interaction engines for every game family. Challenge and tournament support is modeled and reachable from Home, but not yet expanded into all 17 requested standalone screens.
