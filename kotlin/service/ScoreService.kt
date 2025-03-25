package service

import entity.*

/**
 * Manages score calculation for players during the game.
 *
 * This service is responsible for determining and updating the current score of players
 * based on the game rules.
 */
class ScoreService(
    private val root: RootService
) : AbstractRefreshingService() {

    /**
     * Calculates and updates the current score of a [player] based on the game rules.
     *
     * # Preconditions:
     * - The provided [player] must be part of the ongoing game.
     *
     * # Postconditions:
     * - The [player]'s score is recalculated and updated according to the game rules.
     *
     * @param player The [player] whose score needs to be calculated and updated.
     * @return Integer representing the current score of the provided [player]
     * @throws IllegalArgumentException If the provided [player] does not exist or is not part of the game.
     */
    fun getCurrentScore(player: Player): Int {
        if (root.game == null) throw IllegalStateException("Game must be initialized properly")
        val game = requireNotNull(root.game)
        if(player !in game.currentState.players)
            throw IllegalArgumentException("Player does not participate in current game")
        return calculatePointsBySource(player).sum()
    }

    /**
     * Calculates a detailed overview of the source and amount of achieved points for every player.
     *
     * # Preconditions:
     * * Game must be running or finished (not null or uninitialized).
     *
     * # Example
     * ```kotlin
     * getFinalScore()[0][2]
     * ```
     * if you want to retrieve the points **player0** gained through **fruit tiles** in their tree
     *
     *The result is formatted as illustrated in this table:
     *
     * | dim0 →, dim1 ↓   	| Player0 	| Player1 	|
     * |-----------------	|---------	|---------	|
     * | leafPoints      	| x       	| y       	|
     * | flowerPoints    	| x       	| y       	|
     * | fruitPoints     	| x       	| y       	|
     * | parchmentPoints 	| x       	| y       	|
     * | goalPoints      	| x       	| y       	|
     *
     * so dim0 describes the playerIndex and dim1 the kind of point source that is requested.
     *
     * @return A matrix (nested array) that shows every players points categorized by source in the format described
     * above.
     *
     * @throws IllegalStateException If the game is null.
     */
    fun getFinalScores(): Array<IntArray> {
        if (root.game == null) throw IllegalStateException("Game must be initialized properly")
        val game = requireNotNull(root.game)
        val players = game.currentState.players
        // initialize 0 result array of shape (#players , numberOfPointSources)
        val result = Array(players.size) { IntArray(5) { _ -> 0 } }
        players.forEachIndexed { index, player ->
            val pointsByCategory = calculatePointsBySource(player)
            result[index] = pointsByCategory
        }
        return result
    }

    /**
     * Calculates the points by category as described in [getFinalScores]. So dim1 for a given Player.
     *
     * @param player The player for which the points are to be calculated
     * @return An int array in the format described in [getFinalScores].
     */
    private fun calculatePointsBySource(player: Player) : IntArray{
        val counts = countTilesAndFlowerPoints(player)
        val woods = counts[0]
        val leafs = counts[1]
        val flowers = counts[2]
        val fruits = counts[3]
        val flowerPoints = counts[4]

        var parchmentPoints = 0
        var helpers = 0
        var masters = 0

        player.discardPile.forEach { card ->
            if (card !is ParchmentCard) {
                helpers += if (card is HelperCard) 1 else 0
                masters += if (card is MasterCard) 1 else 0
                return@forEach
            }
            parchmentPoints += when (card.type) {
                ParchmentCardType.WOOD -> woods * card.points
                ParchmentCardType.LEAF -> leafs * card.points
                ParchmentCardType.FLOWER -> flowers * card.points
                ParchmentCardType.FRUIT -> fruits * card.points
                ParchmentCardType.HELPER -> masters * card.points
                ParchmentCardType.MASTER -> helpers * card.points
                else -> player.growthCardPile.size * card.points
            }
        }
        val goalPoints = player.goalTiles.sumOf { it.points }
        return intArrayOf(leafs*3,flowerPoints,fruits*7,parchmentPoints,goalPoints)
    }

    private fun countTilesAndFlowerPoints(player: Player): IntArray {
        // starting bud should be placed as wood tile into tree origin
        var woods = 0
        var leafs = 0
        var flowers = 0
        var fruits = 0
        var flowerPoints = 0
        // count tiles in players tree and for each fruit the amount of free adjacent tiles (6 - #neighbors)
        player.tree.tiles.forEach { entry ->
            when (entry.value) {
                BonsaiTileType.WOOD -> woods++
                BonsaiTileType.LEAF -> leafs++
                BonsaiTileType.FRUIT -> fruits++
                BonsaiTileType.FLOWER -> {
                    flowers++
                    flowerPoints += (6 - player.tree.neighbors(entry.key).size)
                }

                else -> print(entry.value)
            }
        }
        return intArrayOf(woods,leafs,flowers,fruits,flowerPoints)
    }
}
