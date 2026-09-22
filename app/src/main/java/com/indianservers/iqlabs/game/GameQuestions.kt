package com.indianservers.iqlabs.game

import com.indianservers.iqlabs.model.GameDefinition
import kotlin.random.Random

/** Routes every visible game to its native IQ Lab-web mechanic. */
internal object GameQuestions {
    fun create(game: GameDefinition, seed: Int, count: Int, tier: Int): List<Question> {
        val random = Random(seed.toLong() xor game.id.hashCode().toLong() * 0x9E3779B97F4A7C15UL.toLong())
        return List(count) { round ->
            WebNativePack.create(game, random, round, tier.coerceIn(1, LevelBlueprint.MAX_LEVEL))
                ?: error("No native IQ Lab-web implementation for ${game.id}")
        }.mapIndexed { round, question ->
            question.copy(
                choices = question.choices.shuffled(random),
                roundKey = "${game.id}:$seed:$tier:$round",
            )
        }
    }
}
