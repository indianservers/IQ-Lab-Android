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

- 154 total game definitions: the original 152 web games plus two infinite puzzles.
- Catalogue level counts: 38 Explorer, 44 Challenger, 34 Thinker, 19 Mastermind and 19 Genius.
- Every game includes ID, name, description, level, category, tags, difficulty, duration, age guidance, icon, accent colour, instructions, scoring model, XP reward, unlock rule, best-score type, status and accessibility notes.
- All 154 catalogue entries are playable.

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

## Infinite puzzle games

Arrow Escape and Connect Colours add two dedicated Compose games alongside the original 152 web catalogue entries. Laser Mirrors and Rotate Rings bring the current total to 156 games. Open a dedicated puzzle catalogue card to choose a mode, resume a saved attempt, or replay an unlocked level.

- Normal has five lives, Challenge three, and Relaxed unlimited lives. No countdown is imposed.
- Arrow Escape v2 uses tap selection, rigid whole-polyline translation, indexed swept-body collisions, and an independent topological solver. Shapes retain every bend during escape. Drag to pan, pinch around the focal point, double-tap to zoom, or use Zoom In/Out, Fit, Reset View and the navigable mini-map.
- Connect Colours uses continuous drag input over an orthogonal routing lattice. Thick rounded strokes, endpoints and the solver share the same occupancy model. Crossings, contact, foreign endpoints and board exits are rejected once per gesture. Incomplete redraws preserve the previous correct connection. Tap a completed line to erase it.
- Hints offer a clue, route preview, or one verified move. Hints are unlimited and do not switch an attempt into practice mode. Colour advice accounts for player routes and can identify connections that must be revised before a verified continuation is possible. Bounded-search failure is never reported as proof of impossibility.
- Arrow levels 1-3 have 4-6 arrows and two bends. Level 4 starts at eight arrows; every later level has at least eight. Levels 4-30 remain a gentle curriculum (up to 33 arrows and five bends, dependency depth at most four). Later paths have 2-12 bends, with 650+ arrows after level 1000. Connect Colours retains its separate seven-tier progression and 12-pair limit.
- SHA-256 seeds include the game, Long level index, generator version and variation. Canonical signatures ignore object ordering and hidden colour solution routes. Duplicate rejection and generation attempts are bounded; every accepted puzzle is independently checked against its witness solution. Arrow v2 constructs variable-length folded orthogonal walks in disjoint routing regions. Directed bands provide a gentle opening curriculum; later boards use an outward mathematical potential field. Every blocker lies later in a verified dependency DAG, so deleting a legal arrow cannot create a deadlock. Canonical v1 boards are archived while unfinished legacy attempts upgrade to v2.
- A separate SharedPreferences namespace follows the existing app's storage convention. Canonical geometry, solution data, mode, current board, lives, hints, timing, camera centre/zoom and best performance survive restarts. Replays and alternate attempts preserve the original level geometry. XP/session rewards can be claimed once per level.
- Shared sound, haptic and reduced-motion preferences are respected. Routing dots, numbered colour symbols and spoken hints have persistent puzzle settings.

### Verification

`InfinitePuzzleTest` samples 10,000 puzzles per game across seeds and all tiers, including dense layouts. It also checks blocked exits, winding heads, route collisions, reversed connections, incomplete routes, hint recovery, canonical signatures and Long overflow. Sampling is regression coverage, not proof of all future puzzles; runtime validation remains mandatory.

`InfinitePuzzleDeviceTest` covers real taps and drag gestures, solver-assisted completion, one-life collision handling, retry, canonical restoration across generator versions, and reward/variant persistence. Test progress is isolated from user progress.

```powershell
.\tools\test-infinite-puzzles.ps1
# Side-by-side package when an installed app uses a different signing key:
.\tools\test-infinite-puzzles.ps1 -Qa -Device
# Optional workaround for Windows JDK "Unable to establish loopback connection":
.\tools\test-infinite-puzzles.ps1 -SocketWorkaround
```

The QA switch uses application ID `com.indianservers.iqlabs.puzzleqa` so the existing installation and its data remain intact. Normal builds retain the original application ID. The optional challenge countdown and additional game variants are not enabled.

Verified on 22 October 2026: normal debug APK assembled; 24 JVM tests passed (including 10,000 puzzles per game); eight instrumentation tests passed on a connected CPH2717 phone in the separate QA package. Android lint completed with zero errors, 60 warnings and two hints. The captured AndroidRuntime log contained no errors. Memory and cold-launch snapshots are in `app/build/verification`; they are observations, not a full frame-time or memory-stress benchmark. Tablet, emulator and exhaustive rotation coverage remain unverified.


### Arrow Escape v2 verification (22 October 2026)

- 31 JVM tests pass, including the existing 10,000-puzzle sample for each game, 1,000 additional arrow seeds across difficulty tiers, the eight-arrow minimum from level 4, the gentle first-30-level curriculum, 2-12 bend coverage, independent swept-collision comparisons, rigid-animation invariants and camera transforms at 0.25x, 1x and 5x.
- Ten instrumentation tests pass on the CPH2717 QA installation, including real taps, actual two-finger pinch expansion, one-finger pan without arrow removal, view controls, camera persistence across retry, mini-map interaction and rendering/playing a 650-arrow board. The existing app installation remains intact.
- Desktop JVM generation-plus-validation observations for 50/100/250/500/650 arrows: 3/12/35/68/74 ms in the recorded benchmark run. These are measurements from this workstation, not a guarantee of phone frame rate. Detailed logs and device screenshots are under `app/build/verification` and `app/build/puzzle-screenshots`.
- Full tablet/emulator, exhaustive rotation and sustained 60 FPS profiling remain unverified.

## Laser Mirrors and Rotate Rings

Both games are native Compose play surfaces with individually rendered objects, catalogue navigation, level selection, three modes, stars, completion statistics, shared sound/haptic/reduced-motion preferences, and offline progress. Normal has five lives, Challenge three, and Relaxed unlimited lives. Undo and Restart restore real board state. Canonical geometry, seed/version, orientations, released rings, timing, hints and undo history survive process recreation. Reward claims are guarded per level. Hints are unlimited and scoring remains enabled.

Implementation lives in `app/src/main/java/com/indianservers/iqlabs/workshop/`:

- `LaserEngine.kt`: nearest-hit optical tracing, vector-law reflections, absorbing obstacles, fixed reflectors, 50/50 splitters, RGB filters and additive receiver colour, dichroic prisms, shutters, bounded loop detection and an independent orientation solver. Mirror animation traces the actual intermediate surface angle. Ordinary experiments are free; only a failed explicit Challenge beam check spends a life.
- `RingEngine.kt`: separate C-ring tracks, angular occupancy, collar clearance including tube width, snapped rotation and a geometry-based solver. Gold base guides and active neighbour collars constrain release. A successful release lifts the ring out of the plane and retracts its links, so removing a legal ring cannot introduce a new constraint. Dragging rotates freely; an explicitly blocked Release spends one life. Empty-space drag pans, pinch zooms, and Fit restores the view.
- `WorkshopBoards.kt` and `WorkshopScreen.kt`: glossy vector objects, live beams, faceted receiver crystals, shaded rings and collars, selection feedback, finite hint pulses, undo/restart/hint/settings controls, pause, replay, and animated completion.
- `WorkshopModel.kt`, `WorkshopStore.kt` and `WorkshopCatalog.kt`: deterministic SHA-256 seeds, eight difficulty tiers, canonical persistence and catalogue definitions. `MainActivity.kt`, `Catalog.kt`, `IqComponents.kt`, `LevelBlueprint.kt` and `ChallengeSystem.kt` integrate the games while keeping them out of the multiple-choice engine.

Levels progress through tutorial (1-10), beginner (11-30), easy (31-75), medium (76-150), hard (151-300), expert (301-600), master (601-1000) and genius (1001+). Bounded object counts keep later levels playable; composition, topology, colours and rotations vary by seed. Every generated board is independently validated before play. Laser hints search the current orientation state; ring hints recompute legal release geometry from the remaining rings.

`WorkshopEngineTest` adds eight JVM tests, including 10,000 samples per new game across multiple seeds and all tiers, reflection/intersection laws, splitter power, colour routing/mixing, occlusion, loop termination, collar geometry and hints. `WorkshopDeviceTest` adds six phone tests covering actual mirror taps and ring drags, completion, failed actions/lives, undo/restart, advanced optical hints, canonical restoration and once-only rewards. Device tests use isolated progress namespaces and a side-by-side QA package.

Verified on 22 October 2026: all 39 JVM tests passed, including the 20,000 new workshop samples; all 16 instrumentation tests passed on the CPH2717 QA installation (46.212 seconds). Normal-package debug assembly and Android lint succeeded with zero errors, 63 warnings and two hints. Three workshop lint warnings concern screen-height API preference and optional SharedPreferences KTX syntax. Final build/device logs are `app/build/verification/workshop-final-build.log` and `workshop-device-final.log`; captured screens are in `app/build/workshop-screenshots/`. The existing installed app and its progress were preserved.

The prism is an explicitly defined dichroic RGB routing model, not a wave-optics or material-dispersion simulation. Rings use fixed collars and planar tracks with out-of-plane release, not fully interlocked 3D rigid-body physics. Visuals follow the supplied warm ivory/glossy references using native drawing rather than flattened mockups. One physical phone has been tested; tablet/emulator layouts, exhaustive rotation and sustained frame-time profiling remain unverified.


## Compact play assistance

All shared catalogue game sessions now start with five lives. Incorrect submissions and invalid tower/trail/matching actions spend one life; reaching zero offers a full session retry without awarding a completion reward. Lives and the current session counters survive activity recreation. The four dedicated puzzle games retain five regular lives, three Challenge lives and unlimited Relaxed lives.

Every play screen has compact top-right heart/count and next-move hint controls with 48dp touch targets. Dedicated puzzle hints use the existing current-state solvers. Shared boards give answer-specific guidance, with current-state advice for sequence, memory grids, matching pairs, Latin grids, towers, trails, circuits and multi-selection. Tower advice searches legal continuations; grid advice can correct a conflicting entry. Hints do not spend lives. Hints are unlimited across the dedicated puzzle games and scoring remains enabled.

Verified on 22 October 2026: 41 JVM tests and 17 phone instrumentation tests passed, including the new hint/lives/exhaustion/retry test. Debug assembly and lint succeeded (zero errors, 63 warnings, two hints). Logs: `app/build/verification/assist-final-build.log` and `assist-device.log`.


## Encouragement during play

All hint-credit labels and counters have been removed. Highlight, preview and one-move assistance remain available without a credit limit; hint use is still recorded for performance scoring. Existing saved attempts resume with scoring enabled.

Fourteen original appreciation messages rotate after a correct round or productive move completed within eight seconds. They can appear mid-round when a pair is matched, a tower disc is legally placed, a trail advances, an arrow escapes, a colour connection is completed, an optical network improves, or a ring releases. Hints and automated moves suppress quick-solver praise for that step. Messages last 3.2 seconds, have a 12-second cooldown, use a polite accessibility announcement, and occupy a reserved line so the canvas stays still. Slower play is never penalised or criticised.

Verified on 22 October 2026: 43 JVM tests and 18 phone tests passed, including mid-round appreciation before completion and unlimited hints with scoring preserved. Debug assembly and lint succeeded. Logs: `app/build/verification/praise-final-build.log` and `praise-device.log`.
