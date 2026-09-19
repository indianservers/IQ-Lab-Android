# Top 30 Games - 20-Level Blueprint Implementation

Source workbook: `C:\Users\saisa\Downloads\IQ_Lab_Best_20_Games_12_Level_Blueprint.xlsx`.

The source workbook defined 20 priority games with 12 internal stages each. The app expands that foundation to 30 priority games with 20 internal stages each, for 600 implementable difficulty stages. The implementation lives in `LevelBlueprint.kt` and maps stable blueprint IDs onto existing playable catalogue game IDs.

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
| DATA_TREND | Data Trend Lab | data-trend-lab |
| ASSUMPTION | Assumption Finder | assumption-finder |
| CONSTRAINT_MAP | Constraint Mapper | constraint-mapper |
| PROB_JUDGE | Probability Judge | probability-judge |
| SCENARIO_OPT | Scenario Optimizer | scenario-optimizer |
| BIAS_DETECT | Bias Detector | bias-detector |
| BAYES_UPDATE | Bayesian Update | bayesian-update |
| COUNTEREXAMPLE | Counterexample Forge | counterexample-forge |
| SYSTEMS | Systems Thinker | systems-thinker |
| RISK_DECISION | Decision Under Risk | decision-under-risk |

## Stage Model

Each mapped game has levels 1-20:

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
13. Synthesis
14. Transfer
15. Ambiguity
16. Optimization
17. Endurance
18. Expert
19. Grandmaster
20. Apex

Each stage includes design intent, exact level design text, session shape, hints, unlock rule, mastery target and score multiplier. Score multipliers extend the workbook pattern from `1.00` to `3.28`.

## App Integration

- Game Details shows a `20-Level Blueprint` card for mapped games.
- Play Session shows the active blueprint level, stage name, exact design and mastery target.
- `QuestionFactory` supports effective tiers 1-20 for priority games.
- `AdaptiveDifficultyEngine` can now move priority games across tiers 1-20 while standard games retain the tier-12 cap.

## Validation

`CatalogAndEngineTest` verifies:

- 30 blueprint games.
- 600 blueprint stages.
- Every blueprint game maps to a playable catalogue game.
- Every mapped game has exactly 20 ordered stages.
- Score multipliers increase monotonically.
- Every mapped game generates valid rounds across all 20 levels.
