# Best 20 Games - 12-Level Blueprint Implementation

Source workbook: `C:\Users\saisa\Downloads\IQ_Lab_Best_20_Games_12_Level_Blueprint.xlsx`.

The workbook defines 20 priority games with 12 internal stages each, for 240 implementable difficulty stages. The implementation lives in `LevelBlueprint.kt` and maps workbook IDs onto stable existing catalogue game IDs.

## Implemented Mapping

| Workbook ID | Workbook Game | Catalogue ID |
| --- | --- | --- |
| MEM_GRID | Memory Grid | memory-grid |
| FLASH_SUM | Flash Sum | flash-sum |
| SEQ_DETECT | Sequence Detective | sequence-detective |
| WORD_SPRINT | Word Sprint | word-sprint |
| PATTERN_PULSE | Pattern Pulse | pattern-pulse |
| QUICK_MATCH | Quick Match | quick-match |
| MATH_RUSH | Mental Math Rush | mental-math-rush |
| LOGIC_GRID | Logic Grid | logic-grid |
| ROTATION_RUSH | Rotation Rush | rotation-rush |
| NBACK | N-Back Lab | n-back-starter |
| STROOP_SHIFT | Stroop Shift | stroop-shift |
| TARGET_TRACK | Target Tracker | target-tracker |
| CODE_BREAKER | Code Breaker | code-breaker |
| VIS_MATRIX | Visual Matrix | visual-matrix |
| EQUATION_BUILD | Equation Builder | equation-builder |
| CUBE_NET | Cube Net | cube-net |
| ROUTE_PLAN | Route Planner | route-planner |
| RECALL_CHAIN | Recall Chain | recall-chain |
| ARG_ANALYSER | Argument Analyser | argument-analyser |
| GRAND_GAUNTLET | Grand Gauntlet | grand-gauntlet |

## Stage Model

Each mapped game has levels 1-12:

1. Orientation
2. Foundation
3. Control
4. Momentum
5. Expansion
6. Interference
7. Dual Rule
8. Pressure
9. Advanced
10. Mastery
11. Elite
12. Limit Test

Each stage includes design intent, exact level design text, session shape, hints, unlock rule, mastery target and score multiplier. Score multipliers follow the workbook pattern from `1.00` to `2.32`.

## App Integration

- Game Details shows a `12-Level Blueprint` card for mapped games.
- Play Session shows the active blueprint level, stage name, exact design and mastery target.
- `QuestionFactory` supports effective tiers 1-12.
- `AdaptiveDifficultyEngine` can now move across tiers 1-12.

## Validation

`CatalogAndEngineTest` verifies:

- 20 blueprint games.
- 240 blueprint stages.
- Every blueprint game maps to a playable catalogue game.
- Every mapped game has exactly 12 ordered stages.
- Score multipliers increase monotonically.
- Every mapped game generates valid rounds across all 12 levels.
