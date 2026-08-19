package com.indianservers.iqlabs.model

import androidx.compose.ui.graphics.Color

enum class LevelId(
    val title: String,
    val audience: String,
    val tagline: String,
    val accent: Color,
    val darkAccent: Color,
    val minXp: Int,
) {
    Explorer("Explorer", "Kids", "Bright visual warmups with roomy targets and gentle timing.", Color(0xFF8DE640), Color(0xFF2F6F1D), 0),
    Challenger("Challenger", "Beginner", "Foundational logic, numbers, words, memory and focus.", Color(0xFFFFB02E), Color(0xFF925B0E), 600),
    Thinker("Thinker", "Intermediate", "Multi-step patterns, sharper timers and stronger working memory.", Color(0xFF14C8FF), Color(0xFF075E8D), 1600),
    Mastermind("Mastermind", "Advanced", "Deduction, complex recall, calculation and planning pressure.", Color(0xFFB35CFF), Color(0xFF60309C), 3200),
    Genius("Genius", "Expert", "Adaptive mixed-domain challenges with minimal hints.", Color(0xFFFF5D64), Color(0xFF9F2530), 5600),
}

enum class Category(val label: String) {
    Math("Math"),
    Memory("Memory"),
    Logic("Logic"),
    Reading("Reading"),
    Focus("Focus"),
    Spatial("Spatial"),
    Speed("Speed"),
}

enum class BestScoreType { HigherIsBetter, LowerIsBetter }
enum class ImplementationStatus { Playable, LaterPhase }

data class GameDefinition(
    val id: String,
    val name: String,
    val description: String,
    val level: LevelId,
    val category: Category,
    val tags: List<String>,
    val difficulty: Int,
    val durationMinutes: Int,
    val minAge: Int,
    val icon: String,
    val accent: Color,
    val instructions: String,
    val scoringModel: String,
    val xpReward: Int,
    val unlockRequirement: String,
    val bestScoreType: BestScoreType,
    val status: ImplementationStatus,
    val accessibilityNotes: String,
)

object IqCatalog {
    val playableIds = setOf("flash-sum", "memory-grid", "sequence-detective", "word-sprint", "pattern-pulse", "quick-match")

    val games: List<GameDefinition> = buildList {
        add(playable("flash-sum", "Flash Sum", "Rapid numbers appear one by one. Keep the running total and submit the final sum.", LevelId.Challenger, Category.Math, listOf("math", "speed", "numbers", "calculation", "timed"), 3, 4, 9, "Σ"))
        add(playable("memory-grid", "Memory Grid", "Watch lit cells, then recreate the pattern from memory.", LevelId.Challenger, Category.Memory, listOf("memory", "visual", "sequence", "focus", "short-session"), 3, 3, 9, "▦"))
        add(playable("sequence-detective", "Sequence Detective", "Find the missing value in arithmetic, geometric and alternating number sequences.", LevelId.Challenger, Category.Logic, listOf("logic", "numbers", "sequence", "patterns", "untimed"), 3, 5, 10, "?"))
        add(playable("word-sprint", "Word Sprint", "Read a brisk micro-passage and answer a comprehension question.", LevelId.Challenger, Category.Reading, listOf("reading", "words", "speed", "focus", "comprehension"), 3, 4, 10, "Aa"))
        add(playable("pattern-pulse", "Pattern Pulse", "Repeat an expanding rhythm of colors and shapes without losing the beat.", LevelId.Challenger, Category.Focus, listOf("focus", "memory", "visual", "sequence", "timed"), 3, 4, 9, "◆"))
        add(playable("quick-match", "Quick Match", "Decide quickly whether the current item matches the previous one under the active rule.", LevelId.Challenger, Category.Speed, listOf("reaction", "speed", "attention", "focus", "adaptive"), 4, 3, 10, "≡"))

        requiredExplorer().forEach { add(it) }
        requiredChallenger().filterNot { candidate -> any { it.id == candidate.id } }.forEach { add(it) }
        requiredThinker().filterNot { candidate -> any { it.id == candidate.id } }.forEach { add(it) }
        requiredMastermind().filterNot { candidate -> any { it.id == candidate.id } }.forEach { add(it) }
        requiredGenius().filterNot { candidate -> any { it.id == candidate.id } }.forEach { add(it) }

        val names = listOf(
            "Neon Number Trail", "Mirror Maze", "Logic Lantern", "Shape Compass", "Orbit Oddity", "Word Forge",
            "Signal Sweep", "Cipher Steps", "Pattern Harbor", "Focus Fuse", "Memory Atlas", "Rapid Roots",
            "Grid Cartographer", "Analogy Arcade", "Peripheral Pop", "Estimate Engine", "Tower Planner",
            "Object Echo", "Reading Relay", "Deduction Dock", "Rotation Relay", "Classification Cloud",
            "Attention Prism", "Missing Link", "Vocabulary Vault", "Reaction Radar", "Visual Sorter",
            "Number Nebula", "Planning Pulse", "Symbol Stream", "Angle Architect", "Word Orbit"
        )
        val categories = Category.entries
        LevelId.entries.forEach { level ->
            var countForLevel = count { it.level == level }
            var index = 0
            while (countForLevel < 30) {
                val category = categories[(index + level.ordinal) % categories.size]
                val base = names[(index + level.ordinal * 7) % names.size]
                val gameName = "$base ${level.title}"
                val id = "${level.name.lowercase()}-${gameName.lowercase().replace(" ", "-")}-${index + 1}"
                if (none { it.id == id }) {
                    add(
                        GameDefinition(
                            id = id,
                            name = gameName,
                            description = descriptionFor(category, level),
                            level = level,
                            category = category,
                            tags = tagsFor(category, level, index),
                            difficulty = (level.ordinal + 1).coerceAtLeast(1) + (index % 2),
                            durationMinutes = 3 + (index % 4),
                            minAge = 6 + level.ordinal * 2,
                            icon = iconFor(category),
                            accent = level.accent,
                            instructions = instructionsFor(category, level),
                            scoringModel = "Correct answers, accuracy, response speed and combo bonuses increase the final score.",
                            xpReward = 40 + level.ordinal * 18 + index % 12,
                            unlockRequirement = if (level.minXp == 0) "Available immediately" else "Unlocks at ${level.minXp} XP",
                            bestScoreType = BestScoreType.HigherIsBetter,
                            status = ImplementationStatus.Playable,
                            accessibilityNotes = "Uses readable text, TalkBack labels and does not rely on color alone.",
                        )
                    )
                    countForLevel++
                }
                index++
            }
        }
    }.sortedWith(compareBy<GameDefinition> { it.level.ordinal }.thenBy { it.name })

    val levels: List<LevelId> = LevelId.entries

    fun levelGames(level: LevelId) = games.filter { it.level == level }
    fun game(id: String) = games.first { it.id == id }

    private fun playable(id: String, name: String, description: String, level: LevelId, category: Category, tags: List<String>, difficulty: Int, duration: Int, minAge: Int, icon: String) =
        GameDefinition(
            id = id,
            name = name,
            description = description,
            level = level,
            category = category,
            tags = tags,
            difficulty = difficulty,
            durationMinutes = duration,
            minAge = minAge,
            icon = icon,
            accent = level.accent,
            instructions = "Complete generated rounds, answer carefully, and build combo streaks for stronger scores.",
            scoringModel = "10 points per correct answer, speed and combo bonuses, then XP based on accuracy and level.",
            xpReward = 80 + level.ordinal * 20,
            unlockRequirement = if (level.minXp == 0) "Available immediately" else "Unlocks at ${level.minXp} XP",
            bestScoreType = BestScoreType.HigherIsBetter,
            status = ImplementationStatus.Playable,
            accessibilityNotes = "Large controls, clear feedback, pause support and reduced-motion friendly pacing.",
        )

    private fun requiredExplorer(): List<GameDefinition> = listOf(
        required("number-pop", "Number Pop", "Tap numbers in ascending order.", LevelId.Explorer, Category.Math, listOf("kids", "math", "numbers", "attention", "untimed"), 1, "123"),
        required("shape-match", "Shape Match", "Match identical geometric shapes.", LevelId.Explorer, Category.Spatial, listOf("kids", "visual", "spatial", "logic", "short-session"), 1, "⬡"),
        required("colour-catch", "Colour Catch", "Tap the requested colour while ignoring distractors.", LevelId.Explorer, Category.Focus, listOf("kids", "visual", "attention", "reaction", "timed"), 1, "●"),
        required("memory-grid-junior", "Memory Grid Junior", "Remember highlighted cells on a small grid.", LevelId.Explorer, Category.Memory, listOf("kids", "memory", "visual", "sequence", "untimed"), 1, "▦"),
        required("picture-pairs", "Picture Pairs", "Find matching illustrated cards.", LevelId.Explorer, Category.Memory, listOf("kids", "memory", "visual", "logic", "short-session"), 1, "▣"),
        required("count-the-stars", "Count the Stars", "Quickly count displayed objects.", LevelId.Explorer, Category.Math, listOf("kids", "math", "numbers", "visual", "timed"), 1, "*"),
        required("bigger-or-smaller", "Bigger or Smaller", "Select the larger or smaller number.", LevelId.Explorer, Category.Math, listOf("kids", "math", "numbers", "logic", "untimed"), 1, "<"),
        required("missing-number-explorer", "Missing Number", "Complete a simple number sequence.", LevelId.Explorer, Category.Logic, listOf("kids", "numbers", "sequence", "logic", "untimed"), 1, "?"),
        required("pattern-train", "Pattern Train", "Complete a repeating visual pattern.", LevelId.Explorer, Category.Logic, listOf("kids", "visual", "sequence", "logic", "short-session"), 1, "◇"),
        required("odd-one-out-junior", "Odd One Out Junior", "Find the object that differs from the group.", LevelId.Explorer, Category.Focus, listOf("kids", "visual", "attention", "logic", "untimed"), 1, "◌"),
        required("shadow-match", "Shadow Match", "Match an object with its silhouette.", LevelId.Explorer, Category.Spatial, listOf("kids", "visual", "spatial", "logic", "short-session"), 2, "◐"),
        required("direction-dash", "Direction Dash", "Follow left, right, up and down instructions.", LevelId.Explorer, Category.Focus, listOf("kids", "attention", "reaction", "spatial", "timed"), 2, "↗"),
        required("quick-tap", "Quick Tap", "React when the target appears.", LevelId.Explorer, Category.Speed, listOf("kids", "reaction", "attention", "timed", "short-session"), 2, "↯"),
        required("simon-colours", "Simon Colours", "Repeat an increasing colour sequence.", LevelId.Explorer, Category.Memory, listOf("kids", "memory", "visual", "sequence", "timed"), 2, "◆"),
        required("animal-order", "Animal Order", "Remember and reproduce an object sequence.", LevelId.Explorer, Category.Memory, listOf("kids", "memory", "visual", "sequence", "untimed"), 2, "ABC"),
        required("maze-scout", "Maze Scout", "Navigate a small maze efficiently.", LevelId.Explorer, Category.Spatial, listOf("kids", "spatial", "planning", "logic", "short-session"), 2, "▧"),
        required("word-and-picture", "Word and Picture", "Match a simple word to the correct image.", LevelId.Explorer, Category.Reading, listOf("kids", "reading", "words", "visual", "untimed"), 1, "Aa"),
        required("letter-hunt", "Letter Hunt", "Find a target letter among distractors.", LevelId.Explorer, Category.Reading, listOf("kids", "reading", "words", "attention", "timed"), 1, "B"),
        required("rhyme-time", "Rhyme Time", "Select the word that rhymes.", LevelId.Explorer, Category.Reading, listOf("kids", "reading", "words", "logic", "untimed"), 2, "R"),
        required("first-sound", "First Sound", "Match words by their starting sound.", LevelId.Explorer, Category.Reading, listOf("kids", "reading", "words", "attention", "short-session"), 2, "F"),
        required("tiny-word-sprint", "Tiny Word Sprint", "Read short words under comfortable timing.", LevelId.Explorer, Category.Reading, listOf("kids", "reading", "words", "speed", "timed"), 2, "go"),
        required("story-snapshot", "Story Snapshot", "Read a short sentence and answer one question.", LevelId.Explorer, Category.Reading, listOf("kids", "reading", "words", "memory", "untimed"), 2, "¶"),
        required("flash-count", "Flash Count", "Estimate or remember briefly displayed objects.", LevelId.Explorer, Category.Memory, listOf("kids", "memory", "numbers", "visual", "timed"), 2, "••"),
        required("simple-sum", "Simple Sum", "Solve child-friendly addition questions.", LevelId.Explorer, Category.Math, listOf("kids", "math", "numbers", "untimed", "short-session"), 1, "+"),
        required("take-away", "Take Away", "Solve visual subtraction questions.", LevelId.Explorer, Category.Math, listOf("kids", "math", "numbers", "visual", "untimed"), 1, "-"),
        required("number-balance", "Number Balance", "Select the value that balances a simple equation.", LevelId.Explorer, Category.Math, listOf("kids", "math", "numbers", "logic", "untimed"), 2, "="),
        required("shape-builder", "Shape Builder", "Combine basic shapes to match a target.", LevelId.Explorer, Category.Spatial, listOf("kids", "visual", "spatial", "logic", "short-session"), 2, "▱"),
        required("size-sort", "Size Sort", "Arrange objects from smallest to largest.", LevelId.Explorer, Category.Logic, listOf("kids", "visual", "logic", "sequence", "untimed"), 2, "▲"),
        required("safe-path", "Safe Path", "Remember and follow a briefly shown path.", LevelId.Explorer, Category.Memory, listOf("kids", "memory", "spatial", "sequence", "short-session"), 2, "⌁"),
        required("focus-friend", "Focus Friend", "Follow a changing target rule while ignoring distractions.", LevelId.Explorer, Category.Focus, listOf("kids", "attention", "focus", "reaction", "timed"), 2, "◎"),
    )

    private fun requiredChallenger(): List<GameDefinition> = listOf(
        required("mental-math-rush", "Mental Math Rush", "Solve mixed arithmetic under time pressure.", LevelId.Challenger, Category.Math, listOf("math", "speed", "numbers", "calculation", "timed"), 3, "+"),
        required("equation-builder", "Equation Builder", "Arrange values and operators to create a valid equation.", LevelId.Challenger, Category.Math, listOf("math", "logic", "numbers", "planning", "untimed"), 3, "="),
        required("number-matrix", "Number Matrix", "Find the missing value in a number grid.", LevelId.Challenger, Category.Logic, listOf("logic", "numbers", "patterns", "sequence", "untimed"), 3, "▦"),
        required("target-total", "Target Total", "Select numbers that combine to reach a target.", LevelId.Challenger, Category.Math, listOf("math", "numbers", "planning", "calculation", "timed"), 3, "◎"),
        required("estimate-it", "Estimate It", "Choose the closest estimate without exact calculation.", LevelId.Challenger, Category.Math, listOf("math", "numbers", "attention", "short-session", "timed"), 3, "~"),
        required("fraction-match", "Fraction Match", "Match basic fractions with equivalent visual values.", LevelId.Challenger, Category.Math, listOf("math", "visual", "numbers", "logic", "untimed"), 3, "½"),
        required("logic-gates", "Logic Gates", "Determine results from simple AND, OR and NOT rules.", LevelId.Challenger, Category.Logic, listOf("logic", "planning", "attention", "advanced", "untimed"), 4, "∧"),
        required("deduction-cards", "Deduction Cards", "Use clues to identify the correct card or object.", LevelId.Challenger, Category.Logic, listOf("logic", "memory", "planning", "focus", "untimed"), 4, "▤"),
        required("rule-switch", "Rule Switch", "Switch between two sorting rules.", LevelId.Challenger, Category.Focus, listOf("focus", "attention", "logic", "reaction", "adaptive"), 4, "⇄"),
        required("symbol-sequence", "Symbol Sequence", "Continue a non-verbal symbol pattern.", LevelId.Challenger, Category.Logic, listOf("logic", "visual", "sequence", "patterns", "untimed"), 3, "◇"),
        required("rotation-match", "Rotation Match", "Identify a rotated version of a shape.", LevelId.Challenger, Category.Spatial, listOf("spatial", "visual", "rotation", "logic", "timed"), 3, "↻"),
        required("mirror-mind", "Mirror Mind", "Choose the correct mirrored image.", LevelId.Challenger, Category.Spatial, listOf("spatial", "visual", "logic", "attention", "timed"), 3, "⇋"),
        required("cube-counter", "Cube Counter", "Count cubes in simple stacked arrangements.", LevelId.Challenger, Category.Spatial, listOf("spatial", "visual", "numbers", "attention", "untimed"), 3, "▣"),
        required("route-planner", "Route Planner", "Find the shortest valid route through a board.", LevelId.Challenger, Category.Spatial, listOf("spatial", "planning", "logic", "sequence", "untimed"), 4, "⌁"),
        required("n-back-starter", "N-Back Starter", "Identify whether the current item matches an earlier item.", LevelId.Challenger, Category.Memory, listOf("memory", "attention", "sequence", "focus", "timed"), 4, "N"),
        required("dual-focus", "Dual Focus", "Track a visual target while answering simple questions.", LevelId.Challenger, Category.Focus, listOf("focus", "attention", "math", "reaction", "timed"), 4, "◎"),
        required("distraction-filter", "Distraction Filter", "Find targets among visually similar distractors.", LevelId.Challenger, Category.Focus, listOf("focus", "visual", "attention", "reaction", "timed"), 3, "◉"),
        required("reaction-choice", "Reaction Choice", "React differently according to colour or shape rules.", LevelId.Challenger, Category.Speed, listOf("reaction", "speed", "attention", "visual", "timed"), 3, "↯"),
        required("word-link", "Word Link", "Identify the relationship connecting two words.", LevelId.Challenger, Category.Reading, listOf("reading", "words", "logic", "untimed", "short-session"), 3, "Aa"),
        required("analogy-starter", "Analogy Starter", "Complete basic verbal analogies.", LevelId.Challenger, Category.Reading, listOf("reading", "words", "logic", "planning", "untimed"), 3, ":"),
        required("category-sort", "Category Sort", "Group words or objects by a common property.", LevelId.Challenger, Category.Logic, listOf("logic", "words", "attention", "planning", "untimed"), 3, "▥"),
        required("scrambled-words", "Scrambled Words", "Reconstruct familiar words from shuffled letters.", LevelId.Challenger, Category.Reading, listOf("reading", "words", "logic", "focus", "untimed"), 3, "abc"),
        required("reading-scan", "Reading Scan", "Find specific information inside a short passage.", LevelId.Challenger, Category.Reading, listOf("reading", "words", "speed", "attention", "timed"), 3, "¶"),
        required("recall-chain", "Recall Chain", "Remember and reproduce mixed word, number and symbol sequences.", LevelId.Challenger, Category.Memory, listOf("memory", "sequence", "words", "numbers", "timed"), 4, "∞"),
    )

    private fun required(id: String, name: String, description: String, level: LevelId, category: Category, tags: List<String>, difficulty: Int, icon: String) =
        playable(id, name, description, level, category, tags, difficulty, 2 + difficulty, 6 + level.ordinal * 2, icon)

    private fun requiredThinker(): List<GameDefinition> = listOf(
        required("rapid-equations", "Rapid Equations", "Solve mixed-operation equations with order-of-operations rules.", LevelId.Thinker, Category.Math, listOf("math", "calculation", "numbers", "timed", "adaptive"), 4, "Σ"),
        required("number-pyramid", "Number Pyramid", "Determine missing values in mathematical pyramids.", LevelId.Thinker, Category.Math, listOf("math", "logic", "numbers", "patterns", "untimed"), 4, "△"),
        required("target-operations", "Target Operations", "Combine numbers and operators to reach a target value.", LevelId.Thinker, Category.Math, listOf("math", "planning", "numbers", "calculation", "adaptive"), 4, "◎"),
        required("percentage-pulse", "Percentage Pulse", "Solve percentage increases, decreases, comparisons and reverse percentages.", LevelId.Thinker, Category.Math, listOf("math", "numbers", "calculation", "timed", "adaptive"), 4, "%"),
        required("fraction-forge", "Fraction Forge", "Compare, simplify and calculate with fractions.", LevelId.Thinker, Category.Math, listOf("math", "numbers", "logic", "untimed", "adaptive"), 4, "½"),
        required("algebra-balance", "Algebra Balance", "Solve visual and symbolic equations containing unknown values.", LevelId.Thinker, Category.Math, listOf("math", "logic", "numbers", "planning", "untimed"), 4, "x"),
        required("prime-patrol", "Prime Patrol", "Rapidly identify primes, factors and multiples.", LevelId.Thinker, Category.Math, listOf("math", "numbers", "speed", "attention", "timed"), 4, "P"),
        required("sequence-fusion", "Sequence Fusion", "Complete sequences that alternate or combine two mathematical rules.", LevelId.Thinker, Category.Logic, listOf("logic", "numbers", "sequence", "patterns", "adaptive"), 4, "?"),
        required("number-matrix-thinker", "Number Matrix", "Solve missing-number problems across rows, columns and diagonals.", LevelId.Thinker, Category.Logic, listOf("logic", "numbers", "patterns", "planning", "untimed"), 4, "▦"),
        required("mental-chain", "Mental Chain", "Maintain and update a running value through a sequence of operations.", LevelId.Thinker, Category.Memory, listOf("memory", "math", "numbers", "working-memory", "timed"), 4, "∞"),
        required("logic-grid", "Logic Grid", "Use several clues to match objects, positions and attributes.", LevelId.Thinker, Category.Logic, listOf("logic", "deduction", "planning", "attention", "untimed"), 4, "▥"),
        required("syllogism-lab", "Syllogism Lab", "Determine whether conclusions follow from supplied statements.", LevelId.Thinker, Category.Logic, listOf("logic", "verbal", "reading", "deduction", "untimed"), 4, "∴"),
        required("code-breaker", "Code Breaker", "Deduce a hidden code from positional and correctness clues.", LevelId.Thinker, Category.Logic, listOf("logic", "deduction", "memory", "planning", "adaptive"), 5, "#"),
        required("conditional-logic", "Conditional Logic", "Apply if, only-if, AND, OR and NOT rules to reach a conclusion.", LevelId.Thinker, Category.Logic, listOf("logic", "planning", "attention", "deduction", "untimed"), 5, "⇒"),
        required("truth-detectives", "Truth Detectives", "Determine which statements are truthful from constraints.", LevelId.Thinker, Category.Logic, listOf("logic", "deduction", "reading", "planning", "untimed"), 5, "T"),
        required("rule-discovery", "Rule Discovery", "Infer a hidden classification rule from accepted and rejected examples.", LevelId.Thinker, Category.Logic, listOf("logic", "patterns", "cognitive-flexibility", "adaptive", "untimed"), 5, "◇"),
        required("rotation-rush", "Rotation Rush", "Identify complex rotated shapes under controlled time pressure.", LevelId.Thinker, Category.Spatial, listOf("spatial", "visual", "rotation", "speed", "timed"), 4, "↻"),
        required("cube-net", "Cube Net", "Determine which nets fold into valid cubes and opposite faces.", LevelId.Thinker, Category.Spatial, listOf("spatial", "visual", "logic", "planning", "untimed"), 4, "□"),
        required("spatial-fold", "Spatial Fold", "Predict the result of folding or punching a simple paper shape.", LevelId.Thinker, Category.Spatial, listOf("spatial", "visual", "logic", "planning", "adaptive"), 4, "⌁"),
        required("hidden-shape", "Hidden Shape", "Locate a target geometric structure inside a complex design.", LevelId.Thinker, Category.Spatial, listOf("spatial", "visual", "attention", "focus", "timed"), 4, "⬡"),
        required("mirror-maze-thinker", "Mirror Maze", "Predict a path affected by mirrored controls.", LevelId.Thinker, Category.Spatial, listOf("spatial", "planning", "visual", "logic", "adaptive"), 5, "⇋"),
        required("visual-matrix", "Visual Matrix", "Complete a non-verbal matrix using shape, rotation and position rules.", LevelId.Thinker, Category.Spatial, listOf("spatial", "visual", "patterns", "logic", "untimed"), 5, "▧"),
        required("working-memory-mix", "Working Memory Mix", "Remember mixed sequences of numbers, words, colours and positions.", LevelId.Thinker, Category.Memory, listOf("memory", "working-memory", "sequence", "attention", "adaptive"), 4, "▦"),
        required("reverse-recall", "Reverse Recall", "Reproduce sequences in reverse or transformed order.", LevelId.Thinker, Category.Memory, listOf("memory", "working-memory", "sequence", "focus", "untimed"), 4, "↩"),
        required("memory-interference", "Memory Interference", "Remember target information while completing a short distracting task.", LevelId.Thinker, Category.Memory, listOf("memory", "working-memory", "attention", "focus", "adaptive"), 5, "◌"),
        required("dual-n-back", "Dual N-Back", "Track visual position and symbol streams at an accessible intermediate level.", LevelId.Thinker, Category.Memory, listOf("memory", "attention", "working-memory", "sequence", "timed"), 5, "N"),
        required("target-tracker", "Target Tracker", "Track selected changing targets among distractors.", LevelId.Thinker, Category.Focus, listOf("focus", "attention", "visual", "reaction", "timed"), 4, "◎"),
        required("stroop-shift", "Stroop Shift", "Respond according to changing colour, word or symbol rules.", LevelId.Thinker, Category.Focus, listOf("focus", "attention", "cognitive-flexibility", "reaction", "adaptive"), 5, "⇄"),
        required("reading-burst", "Reading Burst", "Read a short passage at adjustable speed and answer inference questions.", LevelId.Thinker, Category.Reading, listOf("reading", "verbal", "words", "speed", "adaptive"), 4, "¶"),
        required("verbal-connections", "Verbal Connections", "Solve analogies, word relationships and multi-step word links.", LevelId.Thinker, Category.Reading, listOf("reading", "verbal", "words", "logic", "untimed"), 4, "Aa"),
    )

    private fun requiredMastermind(): List<GameDefinition> = listOf(
        advanced("equation-cascade", "Equation Cascade", "Maintain and solve a chain of equations with changing dependencies.", Category.Math, listOf("math", "calculation", "planning", "adaptive", "timed"), "Σ"),
        advanced("number-web", "Number Web", "Determine missing values in interconnected numerical nodes.", Category.Math, listOf("math", "numbers", "logic", "patterns", "untimed"), "◎"),
        advanced("modular-mind", "Modular Mind", "Solve clock arithmetic, remainder and cyclical-number problems.", Category.Math, listOf("math", "numbers", "logic", "calculation", "adaptive"), "↻"),
        advanced("ratio-relay", "Ratio Relay", "Solve chained ratios, proportions, rates and scaling relationships.", Category.Math, listOf("math", "numbers", "calculation", "planning", "untimed"), ":"),
        advanced("sequence-architect", "Sequence Architect", "Infer multi-layer sequences combining position and transformation rules.", Category.Logic, listOf("logic", "numbers", "sequence", "patterns", "adaptive"), "?"),
        advanced("magic-matrix", "Magic Matrix", "Complete numerical matrices governed by row, column and diagonal relationships.", Category.Logic, listOf("logic", "numbers", "patterns", "planning", "untimed"), "▦"),
        advanced("countdown-solver", "Countdown Solver", "Reach a target using supplied values and limited operators.", Category.Math, listOf("math", "planning", "numbers", "calculation", "timed"), "⏱"),
        advanced("quantitative-comparison", "Quantitative Comparison", "Compare two complex expressions without unnecessary calculation.", Category.Math, listOf("math", "numbers", "logic", "speed", "untimed"), "<"),
        advanced("arithmetic-logic-grid", "Arithmetic Logic Grid", "Use numerical clues and placement constraints to complete a grid.", Category.Logic, listOf("logic", "math", "numbers", "planning", "untimed"), "▥"),
        advanced("equation-switch", "Equation Switch", "Solve expressions while operator meaning changes between rounds.", Category.Focus, listOf("math", "focus", "cognitive-flexibility", "adaptive", "timed"), "⇄"),
        advanced("deduction-vault", "Deduction Vault", "Unlock a vault using ordered clues and positional constraints.", Category.Logic, listOf("logic", "deduction", "planning", "memory", "untimed"), "#"),
        advanced("constraint-circuit", "Constraint Circuit", "Place components while satisfying a network of logical rules.", Category.Logic, listOf("logic", "planning", "deduction", "spatial", "untimed"), "∧"),
        advanced("knights-and-liars", "Knights and Liars", "Determine roles from interacting truthful and false statements.", Category.Logic, listOf("logic", "deduction", "reading", "planning", "untimed"), "K"),
        advanced("boolean-builder", "Boolean Builder", "Select expressions that satisfy complex logical outcomes.", Category.Logic, listOf("logic", "planning", "attention", "adaptive", "untimed"), "∨"),
        advanced("set-logic", "Set Logic", "Solve union, intersection, exclusion and overlapping-group problems.", Category.Logic, listOf("logic", "classification", "planning", "reading", "untimed"), "∩"),
        advanced("logic-grid-pro", "Logic Grid Pro", "Complete a larger multi-category deduction grid from indirect clues.", Category.Logic, listOf("logic", "deduction", "planning", "memory", "untimed"), "▤"),
        advanced("rule-network", "Rule Network", "Trace consequences through interconnected conditional rules.", Category.Logic, listOf("logic", "planning", "deduction", "adaptive", "untimed"), "⇒"),
        advanced("schedule-solver", "Schedule Solver", "Arrange events while satisfying time, order and dependencies.", Category.Logic, listOf("logic", "planning", "sequence", "reading", "untimed"), "◷"),
        advanced("three-d-rotation", "3D Rotation", "Match complex objects after rotation around multiple axes.", Category.Spatial, listOf("spatial", "visual", "rotation", "logic", "timed"), "↻"),
        advanced("cube-assembly", "Cube Assembly", "Determine whether component pieces can form a target cube.", Category.Spatial, listOf("spatial", "visual", "planning", "logic", "untimed"), "□"),
        advanced("paper-fold-pro", "Paper Fold Pro", "Predict complex fold, cut, hole and unfold results.", Category.Spatial, listOf("spatial", "visual", "planning", "adaptive", "untimed"), "⌁"),
        advanced("spatial-sequence", "Spatial Sequence", "Continue sequences combining rotation, reflection and movement.", Category.Spatial, listOf("spatial", "visual", "sequence", "patterns", "adaptive"), "◇"),
        advanced("perspective-match", "Perspective Match", "Identify a 3D object from a different viewpoint.", Category.Spatial, listOf("spatial", "visual", "rotation", "attention", "timed"), "⬡"),
        advanced("tangram-logic", "Tangram Logic", "Identify valid geometric arrangements under constraints.", Category.Spatial, listOf("spatial", "visual", "logic", "planning", "untimed"), "▱"),
        advanced("dual-n-back-pro", "Dual N-Back Pro", "Track two simultaneous streams with increased sequence depth.", Category.Memory, listOf("memory", "working-memory", "attention", "sequence", "timed"), "N"),
        advanced("interference-shift", "Interference Shift", "Retain information while switching between distracting rules.", Category.Memory, listOf("memory", "working-memory", "focus", "adaptive", "timed"), "⇄"),
        advanced("multi-target-tracker", "Multi-Target Tracker", "Track multiple targets through controlled transformation.", Category.Focus, listOf("focus", "attention", "visual", "reaction", "timed"), "◎"),
        advanced("critical-reading", "Critical Reading", "Read dense passages and answer inference, structure and evidence questions.", Category.Reading, listOf("reading", "verbal", "logic", "attention", "untimed"), "¶"),
        advanced("argument-analyser", "Argument Analyser", "Identify premises, conclusions, assumptions and weaknesses.", Category.Reading, listOf("reading", "verbal", "logic", "deduction", "untimed"), "∴"),
        advanced("word-matrix", "Word Matrix", "Complete matrices based on semantic and categorical relationships.", Category.Reading, listOf("reading", "verbal", "words", "logic", "adaptive"), "Aa"),
    )

    private fun requiredGenius(): List<GameDefinition> = listOf(
        expert("operator-cipher", "Operator Cipher", "Deduce unknown operator meanings and solve the resulting expressions.", Category.Math, listOf("math", "logic", "calculation", "adaptive", "expert"), "⊗"),
        expert("alphametic-vault", "Alphametic Vault", "Solve letter-to-digit arithmetic puzzles with valid unique solutions.", Category.Math, listOf("math", "logic", "numbers", "planning", "expert"), "A1"),
        expert("number-theory-lab", "Number Theory Lab", "Solve factor, divisibility, prime and remainder relationships.", Category.Math, listOf("math", "numbers", "logic", "calculation", "expert"), "P"),
        expert("recursive-sequence", "Recursive Sequence", "Infer sequences where each term depends on several earlier terms.", Category.Logic, listOf("logic", "numbers", "sequence", "memory", "expert"), "∞"),
        expert("equation-maze", "Equation Maze", "Navigate a route where every step preserves a mathematical condition.", Category.Math, listOf("math", "planning", "logic", "spatial", "expert"), "▧"),
        expert("combinatorial-count", "Combinatorial Count", "Count valid arrangements under clear restrictions.", Category.Math, listOf("math", "planning", "logic", "numbers", "expert"), "C"),
        expert("probability-instinct", "Probability Instinct", "Compare probabilities and conditional outcomes using transparent scenarios.", Category.Math, listOf("math", "logic", "numbers", "reading", "expert"), "%"),
        expert("balance-scale", "Balance Scale", "Deduce object weights from several balance relationships.", Category.Logic, listOf("logic", "math", "deduction", "planning", "expert"), "="),
        expert("optimisation-route", "Optimisation Route", "Find the best valid path under cost, time or resource constraints.", Category.Spatial, listOf("spatial", "planning", "logic", "math", "expert"), "⌁"),
        expert("matrix-elite", "Matrix Elite", "Solve multi-rule numerical matrices with layered transformations.", Category.Logic, listOf("logic", "numbers", "patterns", "planning", "expert"), "▦"),
        expert("logic-grid-elite", "Logic Grid Elite", "Solve large deduction grids with indirect and conditional clues.", Category.Logic, listOf("logic", "deduction", "planning", "memory", "expert"), "▥"),
        expert("cipher-master", "Cipher Master", "Deduce a hidden code from multi-stage positional feedback.", Category.Logic, listOf("logic", "deduction", "memory", "planning", "expert"), "#"),
        expert("paradox-lab", "Paradox Lab", "Resolve self-reference and consistency puzzles with unambiguous answers.", Category.Logic, listOf("logic", "deduction", "reading", "planning", "expert"), "∴"),
        expert("conditional-chain", "Conditional Chain", "Trace long implication chains and identify necessary conclusions.", Category.Logic, listOf("logic", "deduction", "planning", "attention", "expert"), "⇒"),
        expert("truth-network", "Truth Network", "Determine truth states across a connected network of statements.", Category.Logic, listOf("logic", "deduction", "reading", "memory", "expert"), "T"),
        expert("set-intersection-elite", "Set Intersection Elite", "Solve multi-set classification and constraint problems.", Category.Logic, listOf("logic", "classification", "planning", "reading", "expert"), "∩"),
        expert("resource-planner", "Resource Planner", "Allocate limited resources while satisfying competing objectives.", Category.Logic, listOf("logic", "planning", "math", "attention", "expert"), "▤"),
        expert("grand-deduction", "Grand Deduction", "Solve mixed deduction combining order, category and quantity clues.", Category.Logic, listOf("logic", "deduction", "planning", "memory", "expert"), "◇"),
        expert("cube-slice", "Cube Slice", "Predict the cross-section or visible result of slicing a 3D object.", Category.Spatial, listOf("spatial", "visual", "rotation", "logic", "expert"), "□"),
        expert("block-projection", "Block Projection", "Reconstruct a 3D arrangement from multiple 2D views.", Category.Spatial, listOf("spatial", "visual", "planning", "logic", "expert"), "▣"),
        expert("folding-elite", "Folding Elite", "Solve complex folding, cutting and face-orientation problems.", Category.Spatial, listOf("spatial", "visual", "planning", "adaptive", "expert"), "⌁"),
        expert("rotation-matrix", "Rotation Matrix", "Complete matrices containing 3D rotation and feature changes.", Category.Spatial, listOf("spatial", "visual", "rotation", "patterns", "expert"), "↻"),
        expert("visual-analogy-elite", "Visual Analogy Elite", "Infer layered visual relationships and apply them to a new object.", Category.Spatial, listOf("spatial", "visual", "logic", "patterns", "expert"), "⬡"),
        expert("topology-paths", "Topology Paths", "Determine valid paths through interconnected surfaces and structures.", Category.Spatial, listOf("spatial", "planning", "logic", "attention", "expert"), "◎"),
        expert("triple-n-back", "Triple N-Back", "Track three controlled information streams with accessible starting tiers.", Category.Memory, listOf("memory", "working-memory", "attention", "sequence", "expert"), "N3"),
        expert("working-memory-update", "Working Memory Update", "Continuously replace, reorder and recall active items.", Category.Memory, listOf("memory", "working-memory", "sequence", "focus", "expert"), "↩"),
        expert("memory-under-interference", "Memory Under Interference", "Retain complex information across distracting tasks.", Category.Memory, listOf("memory", "working-memory", "attention", "adaptive", "expert"), "◌"),
        expert("divided-attention-elite", "Divided Attention Elite", "Monitor two simultaneous tasks without unsafe flashing.", Category.Focus, listOf("focus", "attention", "reaction", "visual", "expert"), "◎"),
        expert("rapid-inference", "Rapid Inference", "Read compact passages quickly and identify supported conclusions.", Category.Reading, listOf("reading", "verbal", "logic", "speed", "expert"), "¶"),
        expert("grand-gauntlet", "Grand Gauntlet", "A mixed-domain challenge combining math, logic, memory, spatial and verbal rounds.", Category.Logic, listOf("logic", "math", "memory", "spatial", "expert"), "◆"),
    )

    private fun advanced(id: String, name: String, description: String, category: Category, tags: List<String>, icon: String) =
        required(id, name, description, LevelId.Mastermind, category, tags, 5, icon)

    private fun expert(id: String, name: String, description: String, category: Category, tags: List<String>, icon: String) =
        required(id, name, description, LevelId.Genius, category, tags, 6, icon)

    private fun descriptionFor(category: Category, level: LevelId) = when (category) {
        Category.Math -> "Practice mental calculation and number reasoning tuned for ${level.audience.lowercase()} players."
        Category.Memory -> "Strengthen recall, working memory and visual encoding with escalating patterns."
        Category.Logic -> "Solve classification, deduction and missing-rule puzzles with clear feedback."
        Category.Reading -> "Train fast recognition, comprehension and vocabulary through short sessions."
        Category.Focus -> "Filter distractions and sustain attention while the rules shift."
        Category.Spatial -> "Rotate, compare and map geometric forms under increasing complexity."
        Category.Speed -> "Build decision speed with accuracy-first reaction challenges."
    }

    private fun instructionsFor(category: Category, level: LevelId) =
        "Review the rule, complete each round, and aim for accurate streaks. ${level.title} sessions use difficulty ${level.ordinal + 1} pacing."

    private fun tagsFor(category: Category, level: LevelId, index: Int): List<String> {
        val base = when (category) {
            Category.Math -> listOf("math", "numbers", "calculation")
            Category.Memory -> listOf("memory", "visual", "sequence")
            Category.Logic -> listOf("logic", "patterns", "planning")
            Category.Reading -> listOf("reading", "words", "comprehension")
            Category.Focus -> listOf("focus", "attention", "timed")
            Category.Spatial -> listOf("spatial", "visual", "rotation")
            Category.Speed -> listOf("speed", "reaction", "focus")
        }
        val levelTag = when (level) {
            LevelId.Explorer -> "kids"
            LevelId.Challenger -> "beginner"
            LevelId.Thinker -> "sequence"
            LevelId.Mastermind -> "advanced"
            LevelId.Genius -> "expert"
        }
        val mode = if (index % 3 == 0) "short-session" else if (index % 3 == 1) "adaptive" else "untimed"
        return (base + levelTag + mode).distinct().take(6)
    }

    private fun iconFor(category: Category) = when (category) {
        Category.Math -> "+"
        Category.Memory -> "▦"
        Category.Logic -> "◇"
        Category.Reading -> "Aa"
        Category.Focus -> "◎"
        Category.Spatial -> "⬡"
        Category.Speed -> "↯"
    }
}
