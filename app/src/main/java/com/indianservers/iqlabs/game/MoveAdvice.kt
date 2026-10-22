package com.indianservers.iqlabs.game

/** Small bounded solvers for the stateful shared boards. */
object MoveAdvice {
    fun tower(pegs: List<List<Int>>, holding: Int?): String {
        if (holding != null) {
            val legal = pegs.indices.filter { pegs[it].lastOrNull()?.let { top -> holding < top } ?: true }
            val destination = legal.minByOrNull { peg -> distance(pegs.mapIndexed { i, stack -> if (i == peg) stack + holding else stack }) }
            return destination?.let { "Place disc $holding on peg ${'A' + it}." } ?: "Restart the tower."
        }
        val moves = pegs.indices.flatMap { from ->
            val disc = pegs[from].lastOrNull()
            if (disc == null) emptyList() else pegs.indices.filter { to -> to != from && (pegs[to].lastOrNull()?.let { disc < it } ?: true) }.map { from to it }
        }
        val best = moves.minByOrNull { (from,to) -> distance(pegs.mapIndexed { i, stack -> when(i) { from -> stack.dropLast(1); to -> stack + pegs[from].last(); else -> stack } }) }
        return best?.let { "Lift disc ${pegs[it.first].last()} from peg ${'A' + it.first}, then place it on ${'A' + it.second}." } ?: "Tower complete."
    }
    private fun distance(initial: List<List<Int>>): Int {
        val count = initial.sumOf { it.size }
        val queue = ArrayDeque<Pair<List<List<Int>>,Int>>()
        val seen = hashSetOf(initial)
        queue.add(initial to 0)
        while (queue.isNotEmpty()) {
            val (state,d) = queue.removeFirst()
            if (state[2].size == count) return d
            for (from in 0..2) {
                val disc = state[from].lastOrNull() ?: continue
                for (to in 0..2) if (to != from && (state[to].lastOrNull()?.let { disc < it } ?: true)) {
                    val next = state.mapIndexed { i,s -> when(i) { from -> s.dropLast(1); to -> s + disc; else -> s } }
                    if (seen.add(next)) queue.add(next to d+1)
                }
            }
        }
        return Int.MAX_VALUE
    }
    fun latin(cells: List<Int>, given: List<Int>): String {
        if (cells.size != 16 || given.size != 16) return "Use 1-4 once in every row and column."
        fun solve(input: List<Int>): List<Int>? {
            val grid = input.toMutableList()
            fun fill(index: Int): Boolean {
                if (index == 16) return true
                val options = if (grid[index] == 0) 1..4 else grid[index]..grid[index]
                for (n in options) {
                    if (grid.indices.any { it != index && (it/4 == index/4 || it%4 == index%4) && grid[it] == n }) continue
                    val old = grid[index]; grid[index] = n
                    if (fill(index+1)) return true
                    grid[index] = old
                }
                return false
            }
            return if(fill(0)) grid else null
        }
        val current = solve(cells)
        val solution = current ?: solve(given) ?: return "Restart this grid."
        val index = cells.indices.firstOrNull { given[it] == 0 && cells[it] != solution[it] } ?: return "Tap Check grid."
        return "Select row ${index/4+1}, column ${index%4+1} and enter ${solution[index]}."
    }
}
