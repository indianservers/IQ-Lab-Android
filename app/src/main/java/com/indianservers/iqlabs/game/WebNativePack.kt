package com.indianservers.iqlabs.game

import com.indianservers.iqlabs.model.GameDefinition
import kotlin.random.Random

internal object WebNativePack {
    fun create(game: GameDefinition, random: Random, round: Int, tier: Int): Question? {
        val family = familyFor(game.id) ?: return null
        return when (family) {
            Family.MemoryCards -> memoryCards(game, random, tier)
            Family.NumberSpan -> numberSpan(game, random, tier)
            Family.PatternGrid -> patternGrid(game, random, tier)
            Family.Stroop -> stroopInk(game, random)
            Family.NBack -> nBackLive(game, random, tier)
            Family.MentalMath -> mentalMath(game, random, tier)
            Family.MissingMatrix -> missingMatrix(game, random)
            Family.Rotation -> rotationPick(game, random)
            Family.OddOne -> oddOneLive(game, random)
            Family.Sequence -> sequenceNext(game, random, tier)
            Family.FlashSum -> flashSumLive(game, random, tier)
            Family.Peripheral -> peripheral(game, random)
            Family.VisualSearch -> visualSearch(game, random, tier)
            Family.Simon -> simon(game, random, tier)
            Family.Reaction -> reaction(game, random)
            Family.MultiTask -> multiTask(game, random)
            Family.WordCat -> wordCat(game, random)
            Family.Story -> story(game, random)
            Family.SymbolDigit -> symbolDigit(game, random)
            Family.Hidden -> hiddenObject(game, random)
            Family.Direction -> directionSeq(game, random, tier)
            Family.Sudoku -> sudoku(game, random)
            Family.Tower -> tower(game, random, tier)
            Family.Trail -> trail(game, random, tier)
            Family.Faces -> faces(game, random)
            Family.FocusDot -> focusDot(game, random)
            Family.SpeedCompare -> speedCompare(game, random, tier)
            Family.EquationRelay -> equationRelay(game, random, tier)
            Family.TargetForge -> targetForge(game, random, tier)
            Family.Inference -> inference(game, random)
            Family.Keyword -> keyword(game, random)
            Family.LogicGrid -> logicAssign(game, random)
            Family.LogicBridge -> logicBridge(game, random)
            Family.SwitchCircuit -> switchCircuit(game, random)
            Family.Truth -> truthLive(game, random)
            Family.GridLock -> gridLock(game, random)
            Family.MemoryWeave -> memoryWeave(game, random, tier)
            Family.RuleMatrix -> ruleMatrix(game, random)
            Family.AttentionSplit -> attentionSplit(game, random)
            Family.RapidProof -> rapidProof(game, random)
            Family.NumberCipher -> numberCipher(game, random, tier)
            Family.ConstraintMaze -> constraintMaze(game, random)
            Family.OpSwitch -> opSwitch(game, random, tier)
            Family.SpatialStack -> spatialStack(game, random)
            Family.Analogies -> analogies(game, random)
            Family.ShapeDetective -> shapeDetective(game, random)
            Family.OddToy -> oddToy(game, random)
            Family.AnimalLine -> animalLine(game, random)
            Family.PuzzlePath -> puzzlePath(game, random)
            Family.WhatNext -> whatNext(game, random)
            Family.ColorDiff -> colorDiff(game, random, tier)
            Family.Rsvp -> rsvp(game, random)
            Family.CodeBreaker -> codeBreaker(game, random)
        }
    }

    fun enhancementsFor(game: GameDefinition): List<String> = when (familyFor(game.id)) {
        Family.MemoryCards -> listOf("Flip animated cards", "Track moves and mistakes", "Clear adaptive board sizes")
        Family.NumberSpan -> listOf("Watch paced digits", "Recall forward or backward", "Grow your memory span")
        Family.PatternGrid -> listOf("Watch sequential tile glow", "Rebuild an adaptive grid", "Track selected-cell accuracy")
        Family.Stroop -> listOf("Use large colour controls", "Mix congruent and conflict trials", "Measure inhibition speed")
        Family.NBack -> listOf("See the active N level", "Compare the live stream", "Track hits and false alarms")
        Family.MentalMath -> listOf("Use a native keypad", "Mix arithmetic operations", "Balance speed and accuracy")
        Family.MissingMatrix -> listOf("Read row and column rules", "Inspect native shape symbols", "Reveal the transformation")
        Family.Rotation -> listOf("Compare turned shapes", "Avoid mirror traps", "Progress through harder angles")
        Family.OddOne -> listOf("Inspect meaningful groups", "Find subtle distractors", "Review the group rule")
        Family.Sequence -> listOf("Read layered sequences", "Predict the next item", "Review the discovered rule")
        Family.FlashSum -> listOf("Watch one number at a time", "Hold a running total", "Increase chain length")
        Family.Peripheral -> listOf("Hold the centre marker", "Use true edge targets", "Review peripheral accuracy")
        Family.VisualSearch -> listOf("Keep the target pinned", "Search responsive grids", "Measure search speed")
        Family.Simon -> listOf("Watch paced colour cues", "Replay with large pads", "Build a longest chain")
        Family.Reaction -> listOf("Wait through random delays", "Avoid false starts", "Record millisecond responses")
        Family.MultiTask -> listOf("Track two information streams", "Answer the requested stream", "Adapt overload by level")
        Family.WordCat -> listOf("Classify clear word sets", "Use fast category controls", "Review category accuracy")
        Family.Story -> listOf("Read paced story cards", "Answer delayed questions", "Review remembered evidence")
        Family.SymbolDigit -> listOf("Keep the symbol key visible", "Decode with a native keypad", "Track coding speed")
        Family.Hidden -> listOf("Search a large native scene", "Use generous tap targets", "Take penalized hints after errors")
        Family.Direction -> listOf("Watch an animated route", "Replay with directional pads", "Grow path complexity")
        Family.Sudoku -> listOf("Edit a native logic grid", "Highlight row and column conflicts", "Undo before checking")
        Family.Tower -> listOf("Move discs between native pegs", "Prevent illegal placement", "Compare moves with optimal")
        Family.Trail -> listOf("Connect spatial targets", "Switch number-letter trails", "Track path errors")
        Family.Faces -> listOf("Study distinct illustrated faces", "Separate study and recognition", "Review recognition accuracy")
        Family.FocusDot -> listOf("Follow the live focus position", "Use large direction pads", "Track sustained accuracy")
        Family.SpeedCompare -> listOf("Compare large values", "Respond with two fixed controls", "Build a speed streak")
        Family.EquationRelay -> listOf("Follow operation cards", "Hold intermediate values", "Extend arithmetic chains")
        Family.TargetForge -> listOf("Compare valid expressions", "Respect operator precedence", "Reward efficient solutions")
        Family.Inference -> listOf("Read compact evidence", "Choose the supported inference", "Review decisive evidence")
        Family.Keyword -> listOf("Compare paired reports", "Find the decisive detail", "Review evidence accuracy")
        Family.LogicGrid -> listOf("Read assignment clues", "Eliminate impossible choices", "Review the deduction")
        Family.LogicBridge -> listOf("Select every valid tile", "Keep the bridge rule visible", "Validate the full set")
        Family.SwitchCircuit -> listOf("Toggle native switches", "Watch live gate output", "Match the target signal")
        Family.Truth -> listOf("Read statement cards", "Test contradictions", "Review the logical ruling")
        Family.GridLock -> listOf("Inspect placement constraints", "Choose a legal grid colour", "Review forbidden contacts")
        Family.MemoryWeave -> listOf("Bind shape, colour and order", "Replay paced sequences", "Scale memory interference")
        Family.RuleMatrix -> listOf("Inspect crisp matrix symbols", "Apply row and column rules", "Review transformations")
        Family.AttentionSplit -> listOf("Keep the live rule visible", "Switch colour and parity streams", "Measure switch accuracy")
        Family.RapidProof -> listOf("Separate premises and conclusion", "Use fixed validity controls", "Review the fallacy")
        Family.NumberCipher -> listOf("Keep examples pinned", "Enter answers in a scratchpad", "Reveal the cipher rule")
        Family.ConstraintMaze -> listOf("Read walls, keys and goals", "Choose the next safe move", "Reward shorter paths")
        Family.OpSwitch -> listOf("Keep the current operator rule visible", "Switch arithmetic meanings", "Measure switch cost")
        Family.SpatialStack -> listOf("Follow ordered rotations", "Track final orientation", "Increase turn depth")
        Family.Analogies -> listOf("Identify relationship types", "Compare strong distractors", "Review the verbal link")
        Family.ShapeDetective -> listOf("Inspect shape-colour clues", "Combine multiple attributes", "Use a detective hint after errors")
        Family.OddToy -> listOf("Inspect illustrated toy groups", "Find the rule breaker", "Review the category rule")
        Family.AnimalLine -> listOf("Inspect the visible animals", "Build the requested order", "Receive position-aware feedback")
        Family.PuzzlePath -> listOf("Read the robot command queue", "Choose the final movement", "Review the completed route")
        Family.WhatNext -> listOf("Inspect picture sequences", "Build a correct-answer combo", "Reveal the pattern rule")
        Family.ColorDiff -> listOf("Compare calibrated shade steps", "Use border cues with colour", "Adapt visual difficulty")
        Family.Rsvp -> listOf("Read at a level-adjusted pace", "See live words-per-minute", "Calibrate speed by comprehension")
        Family.CodeBreaker -> listOf("Read exact and misplaced clues", "Compare four possible codes", "Deduce one consistent answer")
        null -> listOf("Use native controls", "Receive immediate feedback", "Progress through adaptive levels")
    }

    private enum class Family {
        MemoryCards, NumberSpan, PatternGrid, Stroop, NBack, MentalMath, MissingMatrix, Rotation,
        OddOne, Sequence, FlashSum, Peripheral, VisualSearch, Simon, Reaction, MultiTask, WordCat,
        Story, SymbolDigit, Hidden, Direction, Sudoku, Tower, Trail, Faces, FocusDot, SpeedCompare,
        EquationRelay, TargetForge, Inference, Keyword, LogicGrid, LogicBridge, SwitchCircuit, Truth,
        GridLock, MemoryWeave, RuleMatrix, AttentionSplit, RapidProof, NumberCipher, ConstraintMaze,
        OpSwitch, SpatialStack, Analogies, ShapeDetective, OddToy, AnimalLine, PuzzlePath, WhatNext,
        ColorDiff, Rsvp, CodeBreaker,
    }

    private fun familyFor(id: String): Family? = families[id]

    private val families: Map<String, Family> = buildMap {
        fun putAll(family: Family, vararg ids: String) = ids.forEach { put(it, family) }
        putAll(Family.MemoryCards, "kids-picture-pairs", "memory-cards", "seniors-card-recall")
        putAll(Family.NumberSpan, "kids-number-train", "number-memory", "seniors-digit-span")
        putAll(Family.PatternGrid, "kids-light-patterns", "pattern-memory", "seniors-spatial-span")
        putAll(Family.Stroop, "stroop-test", "kids-color-focus", "seniors-inhibition-control")
        putAll(Family.NBack, "dual-nback", "kids-back-match", "seniors-dual-nback", "expert-dual-nback")
        putAll(Family.MentalMath, "mental-math", "kids-counting-sprint", "seniors-math-fluency", "expert-math-sprint")
        putAll(Family.MissingMatrix, "missing-shape", "kids-missing-shape", "seniors-abstract-completion")
        putAll(Family.Rotation, "rotation-puzzle", "kids-turn-the-shape", "seniors-mental-rotation", "master-mental-rotation")
        putAll(Family.OddOne, "odd-one-out", "kids-animal-odd-one", "seniors-visual-discrimination")
        putAll(Family.Sequence, "sequence-prediction", "kids-next-in-line", "seniors-complex-sequences", "master-sequence-control")
        putAll(Family.FlashSum, "kids-flash-count", "flash-calculation", "seniors-running-sum")
        putAll(Family.Peripheral, "peripheral-vision", "kids-edge-watch", "seniors-peripheral-control", "master-peripheral-control")
        putAll(Family.VisualSearch, "visual-search", "kids-find-the-star", "seniors-symbol-search")
        putAll(Family.Simon, "simon-game", "kids-color-repeat", "seniors-sequence-control")
        putAll(Family.Reaction, "reaction-time", "kids-fast-tap", "seniors-reaction-benchmark")
        putAll(Family.MultiTask, "multi-task", "kids-two-things", "seniors-divided-attention", "master-divided-attention")
        putAll(Family.WordCat, "word-association", "kids-word-sort", "rapid-categ", "kids-quick-sort", "seniors-semantic-speed", "seniors-category-fluency")
        putAll(Family.Story, "story-recall", "kids-story-pictures", "seniors-detail-recall")
        putAll(Family.SymbolDigit, "symbol-digit", "kids-symbol-code", "seniors-coding-speed", "master-coding-speed")
        putAll(Family.Hidden, "hidden-object", "kids-hidden-picture")
        putAll(Family.Direction, "direction-memory", "kids-arrow-trail", "seniors-route-memory")
        putAll(Family.Sudoku, "sudoku-mini", "kids-mini-grid", "seniors-logic-grid")
        putAll(Family.Tower, "tower-logic", "kids-tower-builder", "seniors-planning-tower")
        putAll(Family.Trail, "trail-making", "kids-dot-path", "seniors-trail-switching", "seniors-visual-scan")
        putAll(Family.Faces, "face-memory", "kids-friendly-faces", "seniors-face-recognition")
        putAll(Family.FocusDot, "focus-timer", "kids-focus-dot", "seniors-sustained-focus")
        putAll(Family.SpeedCompare, "speed-comparison", "kids-bigger-number", "seniors-number-comparison")
        putAll(Family.EquationRelay, "equation-relay", "seniors-equation-relay", "expert-equation-relay", "master-equation-relay")
        putAll(Family.TargetForge, "target-number-forge", "seniors-target-number-forge", "expert-target-forge", "master-target-forge")
        putAll(Family.Inference, "inference-rush", "seniors-inference-rush", "expert-inference-rush", "master-inference-rush")
        putAll(Family.Keyword, "keyword-crossfire", "seniors-keyword-crossfire", "expert-keyword-crossfire", "master-keyword-crossfire")
        putAll(Family.LogicGrid, "advanced-logic-grid", "expert-logic-grid", "master-logic-grid")
        putAll(Family.LogicBridge, "logic-bridge")
        putAll(Family.SwitchCircuit, "switch-circuit", "expert-switch-circuit", "master-switch-circuit")
        putAll(Family.Truth, "truth-detective", "master-truth-detective")
        putAll(Family.GridLock, "grid-lock", "expert-grid-lock", "master-grid-lock")
        putAll(Family.MemoryWeave, "expert-memory-weave", "master-memory-orbit")
        putAll(Family.RuleMatrix, "expert-rule-matrix", "master-logic-tangle", "kids-picture-matrix", "matrix-reasoning", "seniors-advanced-matrix", "expert-matrix-reasoning")
        putAll(Family.AttentionSplit, "expert-attention-split", "master-attention-collapse")
        putAll(Family.RapidProof, "expert-rapid-proof", "master-proof-storm")
        putAll(Family.NumberCipher, "expert-number-cipher", "master-cipher-ladder")
        putAll(Family.ConstraintMaze, "constraint-maze", "expert-constraint-maze")
        putAll(Family.OpSwitch, "operation-switch", "expert-operation-switch")
        putAll(Family.SpatialStack, "spatial-stack", "expert-spatial-stack")
        putAll(Family.Analogies, "analogies-pro")
        putAll(Family.ShapeDetective, "kids-shape-detective")
        putAll(Family.OddToy, "kids-odd-toy-out")
        putAll(Family.AnimalLine, "kids-animal-line-up")
        putAll(Family.PuzzlePath, "kids-puzzle-path")
        putAll(Family.WhatNext, "kids-what-comes-next")
        putAll(Family.ColorDiff, "kids-color-difference")
        putAll(Family.Rsvp, "fast-reading", "kids-word-pop", "seniors-rapid-reading")
        putAll(Family.CodeBreaker, "code-breaker", "expert-code-breaker", "master-code-breaker")
    }

    private fun q(
        game: GameDefinition,
        cue: String,
        choices: List<String>,
        answer: String,
        detail: String,
        play: PlayStyle,
        field: List<String> = emptyList(),
    ): Question {
        val unique = choices.filter { it.isNotBlank() }.distinct().toMutableList()
        if (answer !in unique) unique.add(0, answer)
        var n = 1
        while (unique.size < 2) {
            unique.add("$answer-$n")
            n++
        }
        return Question("${game.name}: $cue", unique, answer, detail, play = play, field = field)
    }

    private fun picks(answer: String, random: Random, noise: List<String>): List<String> =
        (listOf(answer) + noise.filter { it != answer }.distinct().shuffled(random)).distinct().take(4).let { list ->
            if (list.size >= 2 && answer in list) list else (list + answer + "skip").distinct().take(4)
        }

    private fun memoryCards(game: GameDefinition, random: Random, tier: Int): Question {
        val pool = listOf("🦊", "🦉", "🐱", "🐝", "⭐", "🌙", "🍎", "🪁").shuffled(random).take((3 + tier / 6).coerceIn(3, 6))
        val tiles = (pool + pool).shuffled(random)
        return q(game, "Flip tiles until every pair is open.", listOf("matched", "stuck"), "matched", "Clear every twin.", PlayStyle.Match, tiles)
    }

    private fun numberSpan(game: GameDefinition, random: Random, tier: Int): Question {
        val n = (4 + tier / 4).coerceIn(4, 9)
        val digits = List(n) { random.nextInt(0, 10) }
        val reverse = tier >= 10 || game.id.contains("advanced")
        val answer = (if (reverse) digits.reversed() else digits).joinToString("")
        val noise = List(3) { List(n) { random.nextInt(0, 10) }.joinToString("") }.filter { it != answer }
        return q(game, "Watch the digits, then enter them ${if (reverse) "in reverse" else "in order"}.", picks(answer, random, noise), answer, "Correct span: $answer.", PlayStyle.Reveal, digits.map { it.toString() })
    }

    private fun patternGrid(game: GameDefinition, random: Random, tier: Int): Question {
        val size = if (game.id.contains("junior") || game.id.startsWith("kids-")) 9 else 16
        val lit = (1..size).shuffled(random).take((2 + tier / 3).coerceIn(2, size / 2)).sorted()
        val answer = lit.joinToString("-")
        val noise = List(3) { (1..size).shuffled(random).take(lit.size).sorted().joinToString("-") }
        return q(game, "Watch the glow, then rebuild the grid.", picks(answer, random, noise), answer, "Lit cells $answer.", PlayStyle.Grid)
    }

    private fun stroopInk(game: GameDefinition, random: Random): Question {
        val inks = listOf("Red", "Blue", "Green", "Yellow", "Purple")
        val ink = inks[random.nextInt(inks.size)]
        val word = inks[random.nextInt(inks.size)]
        return q(game, "Name the INK colour, not the written word.", inks, ink, "Ink is $ink.", PlayStyle.Targets, listOf("stroop", word, ink))
    }

    private fun nBackLive(game: GameDefinition, random: Random, tier: Int): Question {
        val n = when {
            game.id.contains("triple") -> 3
            game.id.contains("dual") || game.id.contains("nback") -> 2
            else -> 1
        }.coerceAtMost(1 + tier / 6)
        val stream = MutableList(5 + n) { random.nextInt(0, 9) }
        val match = random.nextBoolean()
        val comparison = stream.lastIndex - n
        stream[stream.lastIndex] = if (match) stream[comparison] else (stream[comparison] + random.nextInt(1, 9)) % 9
        return q(game, "Watch the 3×3 grid. Did the final position match $n steps back?", listOf("Yes", "No"), if (match) "Yes" else "No", "Compare the final flash with $n back.", PlayStyle.NBack, stream.map { it.toString() })
    }

    private fun mentalMath(game: GameDefinition, random: Random, tier: Int): Question {
        val a = random.nextInt(2, 9 + tier)
        val b = random.nextInt(2, 8 + tier)
        val op = listOf("+", "−", "×")[random.nextInt(3)]
        val answer = when (op) {
            "−" -> a + 3 - b
            "×" -> a * b
            else -> a + b
        }
        val shown = when (op) {
            "−" -> "$a − $b + 3"
            "×" -> "$a × $b"
            else -> "$a + $b"
        }
        return q(game, "Lock $shown.", listOf(answer.toString(), (answer + 1).toString(), (answer - 1).toString(), (answer + 3).toString()).distinct(), answer.toString(), "$shown = $answer.", PlayStyle.Keypad, listOf(shown))
    }

    private fun missingMatrix(game: GameDefinition, random: Random): Question {
        val shapes = listOf("●", "■", "▲").shuffled(random)
        val colors = listOf("R", "B", "G").shuffled(random)
        val grid = List(3) { r -> List(3) { c -> "${shapes[r]}${colors[c]}" } }
        val answer = grid[2][2]
        val visible = grid.mapIndexed { r, row -> row.mapIndexed { c, cell -> if (r == 2 && c == 2) "?" else cell } }.flatten()
        val noise = listOf("${shapes[0]}${colors[2]}", "${shapes[2]}${colors[0]}", "${shapes[1]}${colors[1]}")
        return q(game, "Rows share shape, columns share colour. Select the missing cell.", picks(answer, random, noise), answer, "Missing cell is $answer.", PlayStyle.Matrix, visible)
    }

    private fun rotationPick(game: GameDefinition, random: Random): Question {
        val turns = listOf(90 to "→", 180 to "↓", 270 to "←")
        val turn = turns[random.nextInt(turns.size)]
        val choices = listOf("↑", "→", "↓", "←")
        return q(game, "Rotate ↑ clockwise by ${turn.first}°. Select the final direction.", choices, turn.second, "A ${turn.first}° clockwise turn points ${turn.second}.", PlayStyle.Targets, listOf("↑", "↻ ${turn.first}°"))
    }

    private fun oddOneLive(game: GameDefinition, random: Random): Question {
        val sets = listOf(
            listOf("🐶", "🐱", "🐰", "🐼") to "🚂",
            listOf("🍎", "🍌", "🍇", "🍊") to "🪁",
            listOf("🚗", "🚌", "🚲", "🛵") to "⭐",
        )
        val (good, odd) = sets[random.nextInt(sets.size)]
        val tiles = (good.take(3) + odd).shuffled(random)
        return q(game, "Smash the picture that does not belong.", tiles, odd, "$odd is the odd one.", PlayStyle.Targets, tiles)
    }

    private fun sequenceNext(game: GameDefinition, random: Random, tier: Int): Question {
        val start = random.nextInt(2, 9)
        val step = random.nextInt(2, 4 + tier / 5)
        val seq = List(4) { start + it * step }
        val answer = (seq.last() + step).toString()
        val noise = listOf((seq.last() + step + 1).toString(), (seq.last() * 2).toString(), start.toString())
        return q(game, "Train ${seq.joinToString("  ")}  ?. Smash the next car.", picks(answer, random, noise), answer, "Add $step.", PlayStyle.Targets, picks(answer, random, noise))
    }

    private fun flashSumLive(game: GameDefinition, random: Random, tier: Int): Question {
        val nums = List(3 + tier / 4) { random.nextInt(1, 9 + tier) }
        val answer = nums.sum().toString()
        return q(game, "Numbers flash. Lock the running total.", listOf(answer, (nums.sum() + 1).toString(), (nums.sum() - 1).toString(), nums.first().toString()).distinct(), answer, "Add ${nums.joinToString("+")}.", PlayStyle.Reveal, nums.map { it.toString() })
    }

    private fun peripheral(game: GameDefinition, random: Random): Question {
        val glyphs = listOf("▲", "●", "■", "◆", "★")
        val target = glyphs[random.nextInt(glyphs.size)]
        val ring = (listOf(target) + glyphs.filter { it != target }.shuffled(random).take(3)).shuffled(random)
        return q(game, "Hold the centre. Smash the edge $target.", ring, target, "The live edge is $target.", PlayStyle.Peripheral, listOf("·") + ring)
    }

    private fun visualSearch(game: GameDefinition, random: Random, tier: Int): Question {
        val letters = ('A'..'Z').toList()
        val target = listOf("★", "A", "T", "X")[random.nextInt(4)]
        val clutter = List(12 + tier) { if (random.nextInt(8) == 0) target else letters[random.nextInt(26)].toString() }.toMutableList()
        if (target !in clutter) clutter[random.nextInt(clutter.size)] = target
        return q(game, "Find and tap one $target in the field.", clutter.distinct().take(8).let { if (target in it) it else it.dropLast(1) + target }, target, "Tap $target in the field.", PlayStyle.Search, clutter)
    }

    private fun simon(game: GameDefinition, random: Random, tier: Int): Question {
        val pads = listOf("Red", "Blue", "Green", "Yellow")
        val seq = List(3 + tier / 5) { pads[random.nextInt(4)] }
        val answer = seq.joinToString(" → ")
        val noise = List(3) { seq.shuffled(random).joinToString(" → ") }
        return q(game, "Replay the glow.", picks(answer, random, noise), answer, "Order is $answer.", PlayStyle.Sequence, seq)
    }

    private fun reaction(game: GameDefinition, random: Random): Question {
        val wait = 700 + random.nextInt(900)
        return q(game, "Wait for GO, then smash Go — not early.", listOf("Go", "Wait"), "Go", "Tap only after GO.", PlayStyle.Wait, listOf(wait.toString()))
    }

    private fun multiTask(game: GameDefinition, random: Random): Question {
        val word = listOf("BLUE", "RED", "THREE", "CIRCLE")[random.nextInt(4)]
        val shapes = random.nextInt(2, 6)
        val askWord = random.nextBoolean()
        val answer = if (askWord) word else shapes.toString()
        val cue = if (askWord) "Remember both streams. Which word appeared?" else "Remember both streams. How many squares appeared?"
        val choices = if (askWord) listOf(word, "RED", "SQUARE", "NONE").distinct() else listOf(answer, (shapes + 1).toString(), (shapes - 1).coerceAtLeast(1).toString(), "0")
        return q(game, cue, choices, answer, "Track both streams.", PlayStyle.Reading, listOf(word, "■".repeat(shapes)))
    }

    private fun wordCat(game: GameDefinition, random: Random): Question {
        val items = listOf("apple" to "fruit", "dog" to "animal", "car" to "vehicle", "oak" to "tree", "flute" to "music")
        val item = items[random.nextInt(items.size)]
        val cats = items.map { it.second }.distinct()
        return q(game, "Sort \"${item.first}\" into its group.", cats, item.second, "${item.first} is ${item.second}.", PlayStyle.Targets, cats)
    }

    private fun story(game: GameDefinition, random: Random): Question {
        val names = listOf("Mira", "Ravi", "Nia", "Omar")
        val things = listOf("lantern", "ticket", "map", "badge")
        val name = names[random.nextInt(names.size)]
        val thing = things[random.nextInt(things.size)]
        val n = random.nextInt(2, 6)
        val passage = "$name hid $n $thing tokens by the river."
        return q(game, "Watch the story, then answer: who hid them?", picks(name, random, names), name, "The actor is $name.", PlayStyle.Reading, passage.split(" "))
    }

    private fun symbolDigit(game: GameDefinition, random: Random): Question {
        val key = listOf("★" to 1, "▲" to 2, "●" to 3, "■" to 4).shuffled(random)
        val pick = key[random.nextInt(key.size)]
        val legend = key.joinToString("  ") { "${it.first}=${it.second}" }
        return q(game, "Key $legend. Lock the code for ${pick.first}.", key.map { it.second.toString() }, pick.second.toString(), "${pick.first} maps to ${pick.second}.", PlayStyle.SymbolKey, listOf(legend, pick.first))
    }

    private fun hiddenObject(game: GameDefinition, random: Random): Question {
        val scene = listOf("🌳", "🏠", "🚗", "🌸", "🐦", "⭐", "🎈", "🐟").shuffled(random)
        val target = scene[random.nextInt(scene.size)]
        return q(game, "Find $target hiding in the scene.", scene, target, "$target was in the scene.", PlayStyle.Search, scene)
    }

    private fun directionSeq(game: GameDefinition, random: Random, tier: Int): Question {
        val dirs = listOf("Up", "Right", "Down", "Left")
        val seq = List(2 + tier / 5) { dirs[random.nextInt(4)] }
        val answer = seq.joinToString(" → ")
        return q(game, "Replay the arrow trail.", listOf(answer) + List(3) { List(seq.size) { dirs[random.nextInt(4)] }.joinToString(" → ") }.filter { it != answer }.distinct().take(3), answer, "Trail is $answer.", PlayStyle.Sequence, seq)
    }

    private fun sudoku(game: GameDefinition, random: Random): Question {
        val solved = listOf(1, 2, 3, 4, 3, 4, 1, 2, 2, 1, 4, 3, 4, 3, 2, 1)
        val holes = (0..15).shuffled(random).take((2 + game.difficulty / 2).coerceIn(3, 7)).toSet()
        val shown = solved.mapIndexed { i, v -> if (i in holes) 0 else v }
        val answer = solved.joinToString("")
        val noise = listOf(solved.toMutableList().also { it[holes.first()] = (it[holes.first()] % 4) + 1 }.joinToString(""))
        return q(game, "Fill the 4×4 so every row and column uses 1–4.", picks(answer, random, noise + "12344321"), answer, "A valid latin fill.", PlayStyle.Sudoku, shown.map { it.toString() })
    }

    private fun tower(game: GameDefinition, random: Random, tier: Int): Question {
        val discs = (3 + tier / 8).coerceIn(3, 4)
        return q(game, "Move $discs discs to peg C. Never set a big disc on a small one.", listOf("done", "stuck"), "done", "Stack on C.", PlayStyle.Tower, listOf(discs.toString()))
    }

    private fun trail(game: GameDefinition, random: Random, tier: Int): Question {
        val n = (5 + tier / 4).coerceIn(5, 9)
        val switching = game.id.contains("switching")
        val order = if (switching) {
            (1..((n + 1) / 2)).flatMap { listOf(it.toString(), ('A'.code + it - 1).toChar().toString()) }.take(n)
        } else (1..n).map { it.toString() }
        return q(game, "Tap ${if (switching) "1-A-2-B" else "1 through $n"} in order.", listOf("clear", "break"), "clear", "Path: ${order.joinToString(" → ")}.", PlayStyle.Trail, order.shuffled(random))
    }

    private fun faces(game: GameDefinition, random: Random): Question {
        val studied = listOf("🙂", "😊", "😎", "🤓", "😇", "🤠").shuffled(random).take(3)
        val lure = listOf("😴", "😡", "🥶", "🥳").shuffled(random).take(3)
        val probe = (studied.take(1) + lure).shuffled(random)
        val answer = studied.first()
        return q(game, "Study the faces, then identify one you saw.", probe, answer, "$answer was in the study set.", PlayStyle.StudyChoice, studied + "|" + probe)
    }

    private fun focusDot(game: GameDefinition, random: Random): Question {
        val pads = listOf("N", "E", "S", "W")
        val path = List(4 + game.difficulty / 2) { pads[random.nextInt(4)] }
        return q(game, "Tap the moving dot at each stop. Keep your attention on the arena.", listOf("tracked", "missed"), "tracked", "You stayed with the moving target.", PlayStyle.FocusTrack, path)
    }

    private fun speedCompare(game: GameDefinition, random: Random, tier: Int): Question {
        val digits = if (game.id.startsWith("kids-")) (1 + tier / 8).coerceIn(1, 3) else (3 + tier / 7).coerceIn(3, 6)
        val lower = when (digits) { 1 -> 1; 2 -> 10; 3 -> 100; 4 -> 1_000; 5 -> 10_000; else -> 100_000 }
        val upper = when (digits) { 1 -> 10; 2 -> 100; 3 -> 1_000; 4 -> 10_000; 5 -> 100_000; else -> 1_000_000 }
        val a = random.nextInt(lower, upper)
        val b = random.nextInt(lower, upper).let { if (it == a) if (it + 1 < upper) it + 1 else it - 1 else it }
        val answer = maxOf(a, b).toString()
        return q(game, "Smash the larger number.", listOf(a.toString(), b.toString()), answer, "$answer is larger.", PlayStyle.Targets, listOf(a.toString(), b.toString()))
    }

    private fun equationRelay(game: GameDefinition, random: Random, tier: Int): Question {
        val start = random.nextInt(3, 12)
        val add = random.nextInt(1, 4 + tier / 5)
        val answer = (start + add) * 2
        return q(game, "Hold $start, then +$add, then ×2. Lock the carry.", listOf(answer.toString(), (start * 2).toString(), (start + add).toString(), (answer + 1).toString()).distinct(), answer.toString(), "Carry becomes $answer.", PlayStyle.Keypad, listOf(start.toString(), "+$add", "×2"))
    }

    private fun targetForge(game: GameDefinition, random: Random, tier: Int): Question {
        val x = random.nextInt(2, 6 + tier / 3)
        val y = random.nextInt(2, 6)
        val z = random.nextInt(1, 5)
        val target = x * y + z
        val answer = "$x × $y + $z"
        val noise = listOf("$y × $x − $z", "${x + 1} × $y + $z", "$x × $y + ${z + 1}")
        return q(game, "Smash the combo that hits $target.", picks(answer, random, noise), answer, "$answer = $target.", PlayStyle.Targets, picks(answer, random, noise))
    }

    private fun inference(game: GameDefinition, random: Random): Question {
        val banks = listOf(
            Triple("Rain began and the picnic was moved indoors.", "The picnic needed dry ground", listOf("Everyone disliked sandwiches", "The sun was brighter", "Buses were late")),
            Triple("The lab lights stayed on after closing time.", "Someone was still working", listOf("The building vanished", "Clocks stopped", "Windows cannot open")),
        )
        val item = banks[random.nextInt(banks.size)]
        return q(game, "${item.first} Smash the implied line.", (listOf(item.second) + item.third).shuffled(random), item.second, item.second, PlayStyle.Reading, item.first.split(" "))
    }

    private fun keyword(game: GameDefinition, random: Random): Question {
        val a = "Alpha report: river rose 4 cm."
        val b = "Beta report: river rose ${4 + random.nextInt(1, 4)} cm."
        val answer = "The rise amounts disagree"
        val noise = listOf("Both reports name the sea", "No numbers appear", "Alpha mentions a forest")
        return q(game, "A: $a  B: $b  Smash the decisive contrast.", picks(answer, random, noise), answer, "The centimetre counts differ.", PlayStyle.Reading, (a + " " + b).split(" "))
    }

    private fun logicAssign(game: GameDefinition, random: Random): Question {
        val people = listOf("Mira", "Ravi", "Nia", "Omar").shuffled(random)
        val jobs = listOf("pilot", "chef", "judge", "scout").shuffled(random)
        val answer = jobs[1]
        val clues = listOf("${people[0]} is ${jobs[0]}", "${people[2]} is ${jobs[2]}", "${people[3]} is ${jobs[3]}")
        return q(game, "Use the clue cards to identify ${people[1]}'s job.", jobs, answer, "${people[1]} maps to $answer.", PlayStyle.Deduction, clues)
    }

    private fun logicBridge(game: GameDefinition, random: Random): Question {
        val ruleIndex = random.nextInt(4)
        val label = when (ruleIndex) {
            0 -> "Even tiles hold the bridge."
            1 -> "Multiples of 3 hold the bridge."
            2 -> "Tiles greater than 10 hold the bridge."
            else -> "Tiles an odd distance from 20 hold the bridge."
        }
        val source = when (ruleIndex) {
            0 -> (1..12).toList()
            1 -> (2..18).toList()
            2 -> listOf(5, 8, 10, 11, 13, 15, 18, 20, 22)
            else -> (9..17).toList()
        }
        val accepts: (Int) -> Boolean = { value ->
            when (ruleIndex) {
                0 -> value % 2 == 0
                1 -> value % 3 == 0
                2 -> value > 10
                else -> kotlin.math.abs(20 - value) % 2 == 1
            }
        }
        val tiles = source.shuffled(random).take(6).toMutableList().also { picked ->
            if (picked.none(accepts)) picked[picked.lastIndex] = source.first(accepts)
        }
        val valid = tiles.filter(accepts).map { it.toString() }.sorted()
        val answer = valid.joinToString("-")
        val pool = tiles.map { it.toString() }
        return q(game, "$label Select every valid tile.", pool, answer, "Bridge tiles: ${valid.joinToString()}.", PlayStyle.MultiSelect, pool)
    }

    private fun switchCircuit(game: GameDefinition, random: Random): Question {
        val challenge = listOf("AND" to true, "OR" to false, "XOR" to true)[random.nextInt(3)]
        val gate = challenge.first
        val target = challenge.second
        val combinations = listOf(false to false, false to true, true to false, true to true)
        val valid = combinations.filter { (a, b) ->
            when (gate) {
                "AND" -> (a && b) == target
                "XOR" -> (a xor b) == target
                else -> (a || b) == target
            }
        }
        val answerPair = valid.first()
        val answer = "${answerPair.first},${answerPair.second}"
        return q(game, "Set the switches so the $gate output is ${if (target) "ON" else "OFF"}.", combinations.map { "${it.first},${it.second}" }, answer, "$gate reaches the target with A=${answerPair.first}, B=${answerPair.second}.", PlayStyle.Circuit, listOf(gate, target.toString()))
    }

    private fun truthLive(game: GameDefinition, random: Random): Question {
        val people = listOf("Asha", "Ben", "Ciro").shuffled(random)
        val truth = people[random.nextInt(people.size)]
        val liar = people.first { it != truth }
        val lines = people.map { speaker ->
            if (speaker == truth) "$speaker: $liar is lying."
            else "$speaker: $truth is lying."
        }
        return q(game, "Exactly one person tells the truth. ${lines.joinToString(" ")} Who is truthful?", people, truth, "$truth is the single truth-teller.", PlayStyle.Targets)
    }

    private fun gridLock(game: GameDefinition, random: Random): Question {
        val colors = listOf("Teal", "Gold", "Violet", "Coral")
        val right = colors[random.nextInt(colors.size)]
        val below = colors.filter { it != right }[random.nextInt(3)]
        val valid = colors.filter { it != right && it != below }
        val answer = valid.first()
        return q(game, "Fill ? without matching a touching colour.", colors, answer, "Legal colours: ${valid.joinToString()}.", PlayStyle.GridPlacement, listOf("?", right, below, "Locked", "valid:${valid.joinToString("|")}"))
    }

    private fun memoryWeave(game: GameDefinition, random: Random, tier: Int): Question {
        val shapes = listOf("▲", "●", "■", "◆", "★")
        val colors = listOf("Red", "Blue", "Green", "Yellow", "Purple")
        val length = (4 + tier / 4).coerceIn(4, 8)
        val seq = List(length) { "${shapes[random.nextInt(shapes.size)]} ${colors[random.nextInt(colors.size)]}" }
        val position = random.nextInt(seq.size)
        val answer = seq[position]
        val candidates = (listOf(answer) + List(8) { "${shapes[random.nextInt(shapes.size)]} ${colors[random.nextInt(colors.size)]}" })
            .distinct().take(4).toMutableList()
        while (candidates.size < 4) {
            val candidate = "${shapes[random.nextInt(shapes.size)]} ${colors[random.nextInt(colors.size)]}"
            if (candidate !in candidates) candidates += candidate
        }
        val probes = candidates.shuffled(random)
        return q(game, "Study the sequence. Which item was at position ${position + 1}?", probes, answer, "Position ${position + 1} was $answer.", PlayStyle.StudyChoice, seq + "|" + probes)
    }

    private fun ruleMatrix(game: GameDefinition, random: Random): Question {
        val a = listOf("●", "■", "▲")
        val row = a.shuffled(random)
        val answer = row[0]
        val grid = listOf(row[0], row[1], row[2], row[1], row[2], "?", row[2], row[0], row[1])
        return q(game, "Shift left each row. Select the missing tile.", a, answer, "The shift needs $answer.", PlayStyle.Matrix, grid)
    }

    private fun attentionSplit(game: GameDefinition, random: Random): Question {
        val token = listOf("RED 4", "BLUE 7", "GOLD 3", "TEAL 8")[random.nextInt(4)]
        val rule = if (random.nextBoolean()) "colour" else "odd"
        val answer = when (rule) {
            "colour" -> token.substringBefore(" ")
            else -> if (token.substringAfter(" ").toInt() % 2 == 1) "Tap" else "Wait"
        }
        val choices = if (rule == "colour") listOf("RED", "BLUE", "GOLD", "TEAL") else listOf("Tap", "Wait")
        return q(game, "Rule=$rule. Token $token. Smash the live answer.", choices, answer, "Follow $rule.", if (choices.size == 2) PlayStyle.GoNoGo else PlayStyle.Targets, choices)
    }

    private fun rapidProof(game: GameDefinition, random: Random): Question {
        val items = listOf(
            "All scouts are calm. Nia is a scout. So Nia is calm." to "Valid",
            "Some cats are grey. This is grey. So this is a cat." to "Invalid",
        )
        val item = items[random.nextInt(items.size)]
        return q(game, "${item.first} Smash Valid or Invalid.", listOf("Valid", "Invalid"), item.second, item.second, PlayStyle.GoNoGo)
    }

    private fun numberCipher(game: GameDefinition, random: Random, tier: Int): Question {
        val ruleIndex = random.nextInt(if (tier >= 10) 4 else 2)
        val calculation: (Int, Int) -> Int = when (ruleIndex) {
            0 -> { a, b -> a * 2 + b }
            1 -> { a, b -> a + b * 3 }
            2 -> { a, b -> a * a - b }
            else -> { a, b -> a * b - a }
        }
        val examples = List(3) {
            val a = random.nextInt(3, 10)
            val b = random.nextInt(1, if (ruleIndex == 2) a + 1 else 9)
            "$a, $b → ${calculation(a, b)}"
        }
        val a = random.nextInt(3, 10)
        val b = random.nextInt(1, if (ruleIndex == 2) a + 1 else 9)
        val answer = calculation(a, b).toString()
        val choices = listOf(answer, (answer.toInt() + 2).toString(), (answer.toInt() - 1).coerceAtLeast(0).toString(), (answer.toInt() + 5).toString()).distinct()
        return q(game, "Infer the hidden rule from the examples. Lock the value for $a, $b.", choices, answer, "The hidden rule gives $answer.", PlayStyle.Keypad, examples + "$a, $b → ?")
    }

    private fun constraintMaze(game: GameDefinition, random: Random): Question {
        val dirs = listOf("Up", "Right", "Down", "Left")
        val clockwise = random.nextBoolean()
        val start = random.nextInt(dirs.size)
        val path = List(4) { step -> dirs[(start + if (clockwise) step else -step).mod(dirs.size)] }
        val answer = path.last()
        return q(game, "The safe path turns ${if (clockwise) "clockwise" else "counter-clockwise"}: ${path.dropLast(1).joinToString(" → ")} → ?. Swipe the next step.", dirs, answer, "Next step $answer.", PlayStyle.Swipe)
    }

    private fun opSwitch(game: GameDefinition, random: Random, tier: Int): Question {
        val a = random.nextInt(2, 9 + tier)
        val b = random.nextInt(2, 8)
        val rules = listOf("SUM", "DIFFERENCE", "PRODUCT", "LARGER", "SMALLER").take((2 + tier / 5).coerceIn(2, 5))
        val rule = rules[random.nextInt(rules.size)]
        val answer = when (rule) {
            "SUM" -> a + b
            "DIFFERENCE" -> kotlin.math.abs(a - b)
            "PRODUCT" -> a * b
            "LARGER" -> maxOf(a, b)
            else -> minOf(a, b)
        }
        return q(game, "Rule: $rule. Apply it to $a and $b.", listOf(answer.toString(), (answer + 2).toString(), (answer - 1).coerceAtLeast(0).toString(), (answer + 5).toString()).distinct(), answer.toString(), "$rule gives $answer.", PlayStyle.Keypad, listOf(rule, "$a · $b"))
    }

    private fun spatialStack(game: GameDefinition, random: Random): Question {
        val face = listOf("N", "E", "S", "W")
        val start = random.nextInt(face.size)
        val turns = List((3 + game.difficulty / 2).coerceIn(3, 7)) { listOf(-1, 0, 1)[random.nextInt(3)] }
        var idx = start
        turns.forEach { idx = (idx + it).mod(4) }
        val answer = face[idx]
        val labels = turns.map { if (it < 0) "Left" else if (it > 0) "Right" else "Same" }
        return q(game, "Start ${face[start]}. Turns ${labels.joinToString(" → ")}. Select the final facing.", face, answer, "Facing $answer.", PlayStyle.Targets, listOf(face[start]) + labels)
    }

    private fun analogies(game: GameDefinition, random: Random): Question {
        val items = listOf(
            Triple("hot", "cold", "up" to "down"),
            Triple("bird", "nest", "bee" to "hive"),
            Triple("finger", "hand", "toe" to "foot"),
        )
        val item = items[random.nextInt(items.size)]
        val answer = item.third.second
        val noise = listOf("down", "hive", "foot", "cut", "out").filter { it != answer }
        return q(game, "${item.first} : ${item.second} :: ${item.third.first} : ?. Smash the linked word.", picks(answer, random, noise), answer, answer, PlayStyle.Targets, picks(answer, random, noise))
    }

    private fun shapeDetective(game: GameDefinition, random: Random): Question {
        val shapes = listOf("▲", "●", "■", "◆")
        val a = shapes[random.nextInt(shapes.size)]
        val b = shapes.filter { it != a }.random(random)
        val seq = listOf(a, b, a, b)
        val answer = a
        val opts = listOf(a, b, "★", "⬟")
        return q(game, "Rule: shapes take turns. ${seq.joinToString(" ")} ?. Smash the mystery tile.", opts, answer, "Alternate back to $a.", PlayStyle.Targets, seq + "?" + opts)
    }

    private fun oddToy(game: GameDefinition, random: Random): Question {
        val sets = listOf(
            Triple("Things with wheels", listOf("🚗", "🚌", "🚲", "🚜"), "🎈"),
            Triple("Things you can eat", listOf("🍎", "🍌", "🍕", "🥕"), "🧩"),
            Triple("Animals", listOf("🐶", "🐱", "🐰", "🐼"), "🚂"),
        )
        val item = sets[random.nextInt(sets.size)]
        val tiles = (item.second.take(3) + item.third).shuffled(random)
        return q(game, "${item.first}. Smash the odd toy.", tiles, item.third, "${item.third} does not fit.", PlayStyle.Targets, tiles)
    }

    private fun animalLine(game: GameDefinition, random: Random): Question {
        data class Animal(val icon: String, val name: String, val size: Int, val speed: Int, val legs: Int)
        val animals = listOf(
            Animal("🐭", "Mouse", 1, 2, 4), Animal("🐰", "Rabbit", 2, 4, 4), Animal("🐶", "Dog", 3, 3, 4),
            Animal("🐘", "Elephant", 5, 1, 4), Animal("🐢", "Turtle", 2, 1, 4), Animal("🦒", "Giraffe", 5, 3, 4),
            Animal("🐔", "Chicken", 2, 2, 2), Animal("🕷", "Spider", 1, 2, 8), Animal("🐍", "Snake", 3, 2, 0),
        )
        val rule = listOf("size", "speed", "legs")[random.nextInt(3)]
        val value: (Animal) -> Int = when (rule) {
            "speed" -> { animal -> animal.speed }
            "legs" -> { animal -> animal.legs }
            else -> { animal -> animal.size }
        }
        val pack = animals.shuffled(random).distinctBy(value).take(4).shuffled(random)
        val ordered = pack.sortedWith(compareBy<Animal> { value(it) }.thenBy { it.name })
        val answer = ordered.joinToString(" → ") { it.icon }
        val label = when (rule) { "speed" -> "slowest to fastest"; "legs" -> "fewest legs to most legs"; else -> "smallest to biggest" }
        return q(game, "Tap the animals $label.", listOf(answer, pack.joinToString(" → ") { it.icon }, pack.reversed().joinToString(" → ") { it.icon }), answer, "Correct order: $answer.", PlayStyle.Sequence, pack.map { it.icon })
    }

    private fun puzzlePath(game: GameDefinition, random: Random): Question {
        val path = buildList {
            var r = 0; var c = 0
            while (r < 3 || c < 3) {
                if (c < 3 && (r == 3 || random.nextBoolean())) { add("Right"); c++ } else { add("Down"); r++ }
            }
        }
        val answer = path.last()
        return q(game, "On a 4×4 grid, move top-left to bottom-right. Code ${path.dropLast(1).joinToString(" ")}. Swipe the final step.", listOf("Up", "Right", "Down", "Left"), answer, "Last code is $answer.", PlayStyle.Swipe)
    }

    private fun whatNext(game: GameDefinition, random: Random): Question {
        val pool = listOf("🍎", "🍌", "🍇").shuffled(random)
        val seq = listOf(pool[0], pool[1], pool[0], pool[1], pool[0])
        val answer = pool[1]
        return q(game, "Pattern ${seq.joinToString(" ")} ?. Smash what comes next.", pool, answer, "It alternates onto $answer.", PlayStyle.Targets, seq + "?" + pool)
    }

    private fun colorDiff(game: GameDefinition, random: Random, tier: Int): Question {
        val n = if (tier < 4) 9 else 12
        val odd = random.nextInt(n)
        val tiles = List(n) { if (it == odd) "odd" else "base" }
        return q(game, "Smash the tile with the tiny shade difference.", listOf(odd.toString(), ((odd + 1) % n).toString(), "0", "1").distinct(), odd.toString(), "Odd tile index $odd.", PlayStyle.Shade, tiles)
    }

    private fun rsvp(game: GameDefinition, random: Random): Question {
        val names = listOf("Mira", "Ravi", "Nia", "Omar")
        val name = names[random.nextInt(names.size)]
        val topic = listOf("sleep", "focus", "gardens", "sonar")[random.nextInt(4)]
        val words = "$name counted teal crystals after $topic practice.".split(" ")
        return q(game, "Words flash. Who counted?", picks(name, random, names), name, "The actor is $name.", PlayStyle.Reading, words)
    }

    private fun codeBreaker(game: GameDefinition, random: Random): Question {
        val digits = ('1'..'6').toList()
        val secret = digits.shuffled(random).take(3).joinToString("")
        val allCodes = digits.flatMap { a -> digits.filter { it != a }.flatMap { b -> digits.filter { it != a && it != b }.map { c -> "$a$b$c" } } }
        fun clue(guess: String): Pair<Int, Int> {
            val exact = guess.zip(secret).count { it.first == it.second }
            val common = guess.count { it in secret }
            return exact to (common - exact)
        }
        fun clueFor(code: String, guess: String): Pair<Int, Int> {
            val exact = guess.zip(code).count { it.first == it.second }
            val common = guess.count { it in code }
            return exact to (common - exact)
        }
        val guesses = mutableListOf<String>()
        var survivors = allCodes
        while (survivors.size > 1 && guesses.size < 4) {
            val guess = allCodes.filter { it != secret && it !in guesses }.minBy { candidate ->
                val expected = clue(candidate)
                survivors.count { code -> clueFor(code, candidate) == expected }
            }
            guesses += guess
            val expected = clue(guess)
            survivors = survivors.filter { code -> clueFor(code, guess) == expected }
        }
        check(survivors == listOf(secret)) { "Could not build a unique code puzzle" }
        while (guesses.size < 4) {
            val extra = allCodes.shuffled(random).first { it != secret && it !in guesses && clue(it).let { result -> result.first + result.second > 0 } }
            guesses += extra
        }
        val options = (listOf(secret) + allCodes.filter { it != secret }.shuffled(random).take(3)).toMutableList()
        val clueCards = guesses.map { guess -> val result = clue(guess); "$guess|${result.first}|${result.second}" }
        return q(game, "Use the clue cards to identify the only consistent 3-digit code.", options, secret, "Code was $secret.", PlayStyle.CodeBreaker, clueCards)
    }
}
