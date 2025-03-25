package service

import entity.*

/**
 * The service class responsible for all tree related actions
 */
class TreeService(
    private val root: RootService
) : AbstractRefreshingService() {

    /**
     * Places a [BonsaiTile] tile on the Bonsai tree at the specified [position].
     *
     * @param tile The [BonsaiTile] being evaluated for placement.
     * @param position The spatial [Vector] where the tile would be placed.
     *
     * This function is responsible for placing the tile within the player's
     * tree, ensuring that the tile's properties align with the game rules:
     *
     * - A wood tile must be placed adjacent to another wood tile (i.e., with at least one side touching a wood tile).
     * - A leaf tile must be placed adjacent to a wood tile.
     * - A flower tile must be placed adjacent to a leaf tile.
     * - A fruit tile must be placed between two leaf tiles which are adjacent to each other.
     *   One side of the fruit tile must touch the first leaf and the following side must touch the second leaf.
     *   You cannot place a fruit adjacent to another fruit.
     *
     * preconditions:
     * - There must be an ongoing game: `rootService.game` is not `null`.
     * - The [Player] must still be able to place the requested [BonsaiTileType]
     *   (i.e., not exceeded their `remainingGrowth` limit).
     * - The [position] must be unoccupied.
     * - The [GameState] must be `CULTIVATE` or `USING HELPER`.
     *
     * postconditions:
     * - The [BonsaiTileType] is reduced by 1 in the `remainingGrowth` limit.
     * - At the [position] is a [BonsaiTileType] [BonsaiTile] in the [Tree].
     * - Reachable [GoalTile]s `threshold`s should be checked and 'claimGoal' or 'renounceGoal' should have been called.
     *
     * @sample PlayerActionService.claimGoal
     * @sample PlayerActionService.renounceGoal
     * @sample canPlaceTile
     */
    fun placeTile(tile: BonsaiTile, position: Vector) {
        val currGame = checkNotNull(root.game).currentState
        canPlaceTile(tile.type, position, throwsError = true)

        val remainingGrowthIndex = when (tile.type) {
            BonsaiTileType.WOOD -> 0
            BonsaiTileType.LEAF -> 1
            BonsaiTileType.FLOWER -> 2
            BonsaiTileType.FRUIT -> 3
            BonsaiTileType.ANY -> 4
        }

        if (currGame.currentPlayer.remainingGrowth[remainingGrowthIndex] > 0)
            currGame.currentPlayer.remainingGrowth[remainingGrowthIndex]--
        //4 = ANY index
        else currGame.currentPlayer.remainingGrowth[4]--

        currGame.currentPlayer.tree.tiles[position] = tile.type
        currGame.currentPlayer.storage.remove(tile)
        //update BonsaiTile Vector

        onAllRefreshables { refreshAfterPlaceBonsaiTile(tile) }

        val reachedGoals = getReachedGoals(tile.type, position)

        if (reachedGoals.isNotEmpty()) {
            currGame.state = States.CLAIMING_GOALS
            reachedGoals.forEach { onAllRefreshables { refreshAfterGoalReached(it) } }
        }
    }

    /**
     * Gets a List of the goals that a [Player] reached
     *
     * @param tile [BonsaiTileType] which has been placed
     *
     * @return A List of the goals reached after a [BonsaiTile] has been placed
     *
     * @throws IllegalArgumentException if the [BonsaiTileType] is [BonsaiTileType.ANY] which is not playable
     */
    private fun getReachedGoals(tile: BonsaiTileType, position: Vector): MutableList<GoalTile> {
        val currGame = checkNotNull(root.game).currentState
        val reachedGoals = mutableListOf<GoalTile>()

        when (tile) {
            BonsaiTileType.WOOD, BonsaiTileType.FRUIT -> {
                reachedGoals += currGame.goalTiles.filter {
                    (it.type == GoalTileType.WOOD || it.type == GoalTileType.FRUIT) &&
                            getTileCount(tile, currGame.currentPlayer.tree) >= it.threshold
                }
            }

            BonsaiTileType.LEAF -> {
                val leafCount = countConnectedLeaves(position, currGame.currentPlayer.tree)
                reachedGoals += currGame.goalTiles.filter { it.type == GoalTileType.LEAF && leafCount >= it.threshold }
            }

            BonsaiTileType.FLOWER -> {
                val isOverhanging = when {
                    position.q < -1 - (position.r + 3) / 2 -> 1 //overhanging left
                    position.q > 3 - (position.r + 2) / 2 -> 2 //overhanging right
                    else -> 0
                }
                val amount = when (isOverhanging) {
                    1 -> currGame.currentPlayer.tree.tiles.filter {
                        it.key.q <= (-1 - ((it.key.r + 3) / 2)) && it.value == BonsaiTileType.FLOWER
                    }
                    2 -> currGame.currentPlayer.tree.tiles.filter {
                        it.key.q >= (3 - ((it.key.r + 2) / 2)) && it.value == BonsaiTileType.FLOWER
                    }
                    else -> emptyMap()
                }
                reachedGoals += currGame.goalTiles.filter {
                    it.type == GoalTileType.FLOWER && amount.count() >= it.threshold
                }
            }

            BonsaiTileType.ANY -> throw IllegalArgumentException("${BonsaiTileType.ANY} is not playable")
        }

        reachedGoals += currGame.goalTiles.filter {
            it.type == GoalTileType.POSITION && it.threshold in 1..3 && checkBonsaiProtrusion(it.threshold)
        }

        reachedGoals.removeAll { it in currGame.currentPlayer.renouncedGoals }

        return reachedGoals
    }

    /**
     * Counts all connected [BonsaiTileType.LEAF] to the given [Vector]
     *
     * @param position the start [Vector] from which the leafs are counted
     * @param tree the [Tree] on which the leafs are counted
     * @param visited a mutable set of [Vector]s which have recursively been visited
     *
     * @return the number of connected leafs at the given position
     */
    internal fun countConnectedLeaves(position: Vector, tree: Tree, visited: MutableSet<Vector> = mutableSetOf()): Int {
        if (position in visited || tree.tiles[position] != BonsaiTileType.LEAF) return 0
        visited.add(position)

        val neighbors = listOf(
            position + Vector.left, position + Vector.upLeft, position + Vector.upRight,
            position + Vector.right, position + Vector.downRight, position + Vector.downLeft
        )

        return 1 + neighbors.sumOf { countConnectedLeaves(it, tree, visited) }
    }

    /**
     * Checks for Protrusion on the [Tree]
     *
     * @param type which type of Protrusion is checked:
     *              1: Protrusion at any side of the tree
     *              2: Protrusion at both sides of the tree
     *              3: Protrusion at both sides and below the tree
     *
     * @return true if the [Tree] protrudes for the given [type], otherwise false
     */
    internal fun checkBonsaiProtrusion(type: Int): Boolean {
        val possiblePlacements = getPossiblePlacements()
        var left = false
        var right = false
        var below = false

        for (it in possiblePlacements) {
            if (it.r < 0) {
                if (it.q < -1 - ((it.r + 3) / 2)) {
                    left = true
                }
                if (it.q > (3 - ((it.r - 2) / 2))) {
                    right = true
                }
            }
        }

        if (left && right) {
            for (it in possiblePlacements) {
                if (it.r > 1) {
                    below = true
                    break
                }
            }
        }

        return when (type) {
            1 -> right
            2 -> right && left
            else -> below
        }
    }

    /**
     * Determines the amount of a specific [BonsaiTileType] in a [Tree]
     *
     * @param tile The [BonsaiTileType] which will be counted
     * @param tree The [Tree] on which the [BonsaiTileType]s will be counted
     *
     * @return Amount of [BonsaiTileType]s which have been counted in [Tree]
     */
    internal fun getTileCount(tile: BonsaiTileType, tree: Tree): Int {
        return tree.tiles.values.count { it == tile }
    }

    /**
     * Determines if a [BonsaiTileType] tile can be placed at the specified [position] on the Bonsai tree.
     *
     * @param tile The [BonsaiTileType] being evaluated for placement.
     * @param position The spatial [Vector] where the tile would be placed.
     *
     * This function checks if the tile’s placement adheres to the game rules regarding adjacency
     * and tile arrangement constraints:
     *
     * - A wood tile must be placed adjacent to another wood tile (i.e., with at least one side touching a wood tile).
     * - A leaf tile must be placed adjacent to a wood tile.
     * - A flower tile must be placed adjacent to a leaf tile.
     * - A fruit tile must be placed between two leaf tiles which are adjacent to each other.
     *   One side of the fruit tile must touch the first leaf and the following side must touch the second leaf.
     *   You cannot place a fruit adjacent to another fruit.
     *
     * preconditions:
     * - There must be an ongoing game: `rootService.game` is not `null`.
     * - The [Player] must still be able to place the requested [BonsaiTileType]
     *   (i.e., not exceeded their `remainingGrowth` limit).
     * - The [position] must be unoccupied.
     * - The [GameState] must be `CULTIVATE` or `USING HELPER`.
     *
     * postconditions:
     * - Returns `true` if the [BonsaiTileType] can validly be placed at [position],
     *   `false` otherwise.
     *
     * @return `true` if the tile may be placed at [position] following the game rules, `false` otherwise.
     * @see placeTile
     */
    fun canPlaceTile(tile: BonsaiTileType, position: Vector, throwsError: Boolean = false): Boolean {
        val pot = getPotTiles()

        try {
            val game = checkNotNull(root.game) { "There is currently no game running" }
            check(game.currentState.state in setOf(States.CULTIVATE, States.USING_HELPER)) {
                "Player is currently not allowed to place BonsaiTile"
            }

            val remainingGrowthIndex = when (tile) {
                BonsaiTileType.WOOD -> 0
                BonsaiTileType.LEAF -> 1
                BonsaiTileType.FLOWER -> 2
                BonsaiTileType.FRUIT -> 3
                BonsaiTileType.ANY -> 4
            }

            require(
                game.currentState.currentPlayer.remainingGrowth[remainingGrowthIndex] > 0 ||
                        game.currentState.currentPlayer.remainingGrowth[4] > 0
            ) { "Player is currently not allowed to place any more $tile tiles" }

            require(
                !game.currentState.currentPlayer.tree.tiles.containsKey(position) &&
                        !pot.contains(position) && position != Vector.zero
            ) { "Tree vector already occupied" }
        } catch (e: IllegalStateException) {
            if (throwsError) throw e else return false
        } catch (e: IllegalArgumentException) {
            if (throwsError) throw e else return false
        }

        val currGameTree = checkNotNull(root.game).currentState.currentPlayer.tree.tiles

        currGameTree.putIfAbsent(Vector.zero, BonsaiTileType.WOOD)

        val adjacentVector = arrayOf(
            (position + Vector.left), (position + Vector.upLeft), (position + Vector.upRight),
            (position + Vector.right), (position + Vector.downRight), (position + Vector.downLeft)
        )

        val adjacentTiles = adjacentVector.map { currGameTree[it] }

        val valid = isPlacementValid(tile, adjacentTiles)

        if (throwsError) require(valid) { "Tile placement is not possible in the given location" }
        return valid
    }

    /**
     * Checks whether a placement is valid, based on the [adjacentTiles]
     *
     * @param tile the [BonsaiTileType] which is being placed
     * @param adjacentTiles A List of [BonsaiTileType] surrounding [tile] with 'null' if there is no tile
     *
     * @return 'true' if the placement is valid, otherwise false
     */
    private fun isPlacementValid(tile: BonsaiTileType, adjacentTiles: List<BonsaiTileType?>): Boolean {
        return when (tile) {
            BonsaiTileType.WOOD, BonsaiTileType.LEAF -> BonsaiTileType.WOOD in adjacentTiles
            BonsaiTileType.FLOWER -> BonsaiTileType.LEAF in adjacentTiles
            else -> BonsaiTileType.FRUIT !in adjacentTiles && hasTwoAdjacentLeaves(adjacentTiles)
        }
    }

    /**
     * Checks, if at least two adjacent tiles, of a cyclic array, are leafs
     *
     * @param tiles A List containing already placed [BonsaiTileType]s or null
     * of the surrounding [Vector], where a tile is meant to be placed
     *
     * @return 'true' if there are two adjacent Leafs, 'false' if otherwise
     */
    private fun hasTwoAdjacentLeaves(tiles: List<BonsaiTileType?>): Boolean {
        for (i in tiles.indices) {
            val nextIndex = (i + 1) % tiles.size
            if (tiles[i] == BonsaiTileType.LEAF && tiles[nextIndex] == BonsaiTileType.LEAF) {
                return true
            }
        }
        return false
    }

    /**
     * Removes any tiles located at the specified [positions] from the Bonsai tree.
     *
     * @param positions The [Vector] coordinates of the tiles to be removed. If null none is removed
     *
     * This function is typically used in the edge case where a player cannot legally
     * place a wood tile according to the standard placement rules, and must remove
     * the minimal necessary tiles to enable wood placement again. It clears out the
     * tiles at the provided [positions].
     *
     * preconditions:
     * - An active game session: `rootService.game` must not be `null`.
     * - The [positions] must be occupied by at least one [BonsaiTile].
     * - [GameState] should allow tile removal (e.g., `REMOVE_TILES`).
     *
     * postconditions:
     * - The [BonsaiTile]s at [positions] are removed from the [Tree].
     * - Future wood placements shall be valid again.
     *
     * @sample validateRemoveTiles
     */
    fun removeTiles(positions: Array<Vector>) {
        val game = checkNotNull(root.game) { "No active game running." }
        val playerTree = game.currentState.currentPlayer.tree

        require(game.currentState.state == States.REMOVE_TILES) { "Tiles can only be removed in REMOVE_TILES state." }

        // Validate once before removing, ensuring removal enables wood placement
        validateRemoveTiles(positions, throwsError = true)

        // Remove the specified tiles from the tree
        positions.forEach { playerTree.tiles.remove(it) }

        // Since `validateRemoveTiles` already checked if wood can be placed, directly proceed to next state
        game.currentState.state = States.CHOOSE_ACTION

        // Notify the UI about the changes
        onAllRefreshables { refreshAfterTilesRemoved(positions) }
    }

    /**
     * Validates whether the tiles located at [positions] can be removed, according to the Bonsai game rules.
     *
     * @param positions The [Vector] coordinates of the tiles to be removed.
     * @param throwsError If `true`, an exception will be thrown when removal is invalid;
     * otherwise, the function returns `false`.
     *
     * This function checks if removing these tiles would enable valid placements
     * (e.g., allowing a blocked wood tile placement) while ensuring that only the minimal
     * necessary tiles are removed. The primary use case is the scenario in which the player
     * cannot place a new wood tile due to adjacency constraints and must remove a tile or set
     * of tiles to restore valid positioning options.
     *
     * preconditions:
     * - There must be an active game: `rootService.game` is not `null`.
     * - [positions] must be occupied by one or more [BonsaiTile]s in the current player's [Tree].
     * - [GameState] should allow tile removal (e.g., `REMOVE_TILES`).
     *
     * postconditions:
     * - Returns `true` if removing the tiles at [positions] follows the rule
     *   that only the minimal necessary tiles can be removed to enable future legal placements.
     *   Returns `false` if removal at [positions] is invalid or unnecessary.
     * - If `throwsError` is `true`, an exception is thrown in case of an invalid removal attempt.
     *
     * @return `true` if removing the tiles at [positions] is valid; `false` otherwise.
     * @throws IllegalStateException If `throwsError` is `true` and no active game exists.
     * @throws IllegalArgumentException If `throwsError` is `true`
     * and [positions] is not occupied or removal is not allowed.
     * @see removeTiles
     */
    fun validateRemoveTiles(positions: Array<Vector>, throwsError: Boolean = false): Boolean {
        try {
            val game = checkNotNull(root.game) { "No active game." }

            // Verify that tile removal is allowed in the current game state
            check(game.currentState.state == States.REMOVE_TILES) {
                "Tiles cannot be removed in the current game state."
            }
        } catch (e: IllegalStateException) { if (throwsError) throw e else return false }

        val playerTree = checkNotNull(root.game).currentState.currentPlayer.tree
        val originalTree = playerTree.tiles.toMutableMap()
        val tilesToRemove = positions.mapNotNull { playerTree.tiles[it] }

        // Ensure that the specified positions contain tiles
        if (tilesToRemove.isEmpty()) {
            if (throwsError) throw IllegalArgumentException("No tiles found at the given positions.")
            return false
        }

        // Create a temporary copy of the tree to simulate tile removal
        val tempTree = playerTree.tiles.toMutableMap()
        playerTree.tiles = tempTree

        // is minimal? if size = 1 -> minimal
        // no leaf can be removed s.t. the tree is valid
        if(positions.size == 2 ){
            tempTree.forEach{removedLeaf ->
                val treeWithoutLeaf = mutableMapOf<Vector,BonsaiTileType>()
                treeWithoutLeaf.putAll(tempTree)
                treeWithoutLeaf.remove(removedLeaf.key)
                if(isValidTree(Tree(treeWithoutLeaf))){
                    require(!throwsError){"tiles are not minimal"}
                    return false
                }
            }
        }
        // every leaf must have two flowers or one flower and one fruit. for every leaf: remove it with one of its
        // flower / fruit neighbors and check if tree still valid
        if( positions.size == 3){
            tempTree.forEach leafs@{ removedLeaf ->
                val treeWithoutLeaf = mutableMapOf<Vector,BonsaiTileType>()
                treeWithoutLeaf.putAll(tempTree)
                treeWithoutLeaf.remove(removedLeaf.key)
                playerTree.neighborsVectors(removedLeaf.key).forEach { removedNeighborPos ->
                    if(treeWithoutLeaf[removedNeighborPos] !in listOf(BonsaiTileType.FRUIT,BonsaiTileType.FLOWER))
                        return@leafs
                    val temp = mutableMapOf<Vector, BonsaiTileType>().also {
                        it.putAll(treeWithoutLeaf)
                        it.remove(removedNeighborPos)
                    }
                    if(isValidTree(Tree(temp)))
                        require(!throwsError){"tiles are not minimal"}
                        return false
                }
            }
        }
        // ;)
        if(positions.size == 4){
            return playerTree.tiles.all {
                            it.value != BonsaiTileType.LEAF
                        || (playerTree.neighbors(it.key).contains(BonsaiTileType.FLOWER)
                                    &&(playerTree.neighbors(it.key) - BonsaiTileType.FLOWER).containsAll(
                                            listOf(BonsaiTileType.FLOWER,BonsaiTileType.FRUIT)))
                        || (playerTree.neighbors(it.key).contains(BonsaiTileType.FRUIT)
                                    &&(playerTree.neighbors(it.key) - BonsaiTileType.FRUIT).containsAll(
                                        listOf(BonsaiTileType.FLOWER,BonsaiTileType.FRUIT)
                                    ))
            }
        }

        if(positions.size > 4) {
            require(!throwsError) { "tiles are not minimal" }
            return false
        }

        // Remove the specified tiles from the temporary tree
        positions.forEach { tempTree.remove(it) }

        // Determine if any valid placements for a wood tile exist after removal
        val validPlacementExists = canPlaceWood(playerTree)

        // Reset the tree
        playerTree.tiles = originalTree

        // If no valid placement exists, the removal does not meet the game rules
        if (throwsError) require(validPlacementExists) {
            "Removing these tiles does not restore valid Wood placements."
        }

        return validPlacementExists
    }

    /**
     * Checks if a wood tile can be placed at the given [position] in the Bonsai tree.
     *
     * This method validates if placing a wood tile follows the game rules. It ensures:
     * 1. The wood tile is adjacent to at least one other wood tile.
     * 2. Removing a tile does not break the placement conditions of adjacent tiles.
     * 3. Fruit tiles remain valid by ensuring they still have two adjacent leaf tiles.
     * 4. Leaves remain valid by ensuring they are adjacent to at least one wood tile.
     * 5. Flower tiles remain valid by ensuring they are adjacent to at least one leaf tile.
     *
     * @param tree The current state of the Bonsai tree as a map of tile positions.
     * @param position The vector where the wood tile is intended to be placed.
     * @return `true` if the wood tile can be placed according to the rules, `false` otherwise.
     */
    private fun canPlaceWoodManually(tree: Map<Vector, BonsaiTileType>, position: Vector): Boolean {
        // Define the six adjacent positions surrounding the given position.
        val adjacentVectors = arrayOf(
            position + Vector.left, position + Vector.upLeft, position + Vector.upRight,
            position + Vector.right, position + Vector.downRight, position + Vector.downLeft
        )

        // A wood tile must be placed adjacent to at least one existing wood tile.
        if (adjacentVectors.none { neighbor -> tree[neighbor] == BonsaiTileType.WOOD }) {
            return false
        }

        // Iterate through each adjacent tile to check for rule violations.
        for (neighbor in adjacentVectors) {
            val tile = tree[neighbor]

            // A fruit tile must remain between two adjacent leaf tiles.
            if (tile == BonsaiTileType.FRUIT) {
                val fruitAdjacentVectors = arrayOf(
                    neighbor + Vector.left, neighbor + Vector.upLeft, neighbor + Vector.upRight,
                    neighbor + Vector.right, neighbor + Vector.downRight, neighbor + Vector.downLeft
                )

                // Count the number of adjacent leaf tiles.
                val leafCount = fruitAdjacentVectors.count { tree[it] == BonsaiTileType.LEAF }

                // If a fruit is no longer between two leaves, placement is invalid.
                if (leafCount < 2) {
                    return false
                }

                // No two fruit tiles can be directly adjacent to each other.
                if (fruitAdjacentVectors.any { tree[it] == BonsaiTileType.FRUIT }) {
                    return false
                }
            }

            // A leaf tile must remain adjacent to at least one wood tile.
            if (tile == BonsaiTileType.LEAF) {
                val leafAdjacentVectors = arrayOf(
                    neighbor + Vector.left, neighbor + Vector.upLeft, neighbor + Vector.upRight,
                    neighbor + Vector.right, neighbor + Vector.downRight, neighbor + Vector.downLeft
                )

                // If a leaf no longer touches a wood tile, placement is invalid.
                if (leafAdjacentVectors.none { tree[it] == BonsaiTileType.WOOD }) {
                    return false
                }
            }

            // A flower tile must remain adjacent to at least one leaf tile.
            if (tile == BonsaiTileType.FLOWER) {
                val flowerAdjacentVectors = arrayOf(
                    neighbor + Vector.left, neighbor + Vector.upLeft, neighbor + Vector.upRight,
                    neighbor + Vector.right, neighbor + Vector.downRight, neighbor + Vector.downLeft
                )

                // If a flower no longer touches a leaf tile, placement is invalid.
                if (flowerAdjacentVectors.none { tree[it] == BonsaiTileType.LEAF }) {
                    return false
                }
            }
        }

        // If all conditions are met, the wood tile can be placed.
        return true
    }

    /**
     * Gives an Array of possible [Vector]s, at which a [BonsaiTile] can be placed.
     * Basically creating a Silhouette of [Vector]s around the Bonsai [Tree].
     *
     * precondition:
     * - Game must not be 'null'
     *
     * @return an Array of [Vector]s, at which a [BonsaiTile] can be placed e.g. not at the position of the pot.
     * @throws IllegalStateException if no game is currently running
     */
    fun getPossiblePlacements(): Array<Vector> {
        val pot = getPotTiles()
        checkNotNull(root.game).currentState.currentPlayer.tree.tiles.putIfAbsent(Vector.zero, BonsaiTileType.WOOD)

        val occupied = checkNotNull(root.game).currentState.currentPlayer.tree.tiles.keys
        val possiblePlacements = mutableSetOf<Vector>()
        val adjacent = listOf(
            Vector.left, Vector.upLeft, Vector.upRight, Vector.right, Vector.downRight, Vector.downLeft
        )

        occupied.forEach { tile ->
            adjacent.forEach { adj ->
                val neighbor = tile + adj
                if (neighbor !in occupied && neighbor !in pot) {
                    possiblePlacements.add(neighbor)
                }
            }
        }

        return possiblePlacements.toTypedArray()
    }

    /**
     * Determines the pot in which the Bonsai [Tree] is being build
     */
    private fun getPotTiles(): Set<Vector> {
        val pot = setOf(
            Vector(-2, 0), Vector(-1, 0), Vector(1, 0), Vector(2, 0), Vector(3, 0),
            Vector(-2, 1), Vector(-1, 1), Vector(0, 1), Vector(1, 1), Vector(2, 1),
            Vector(-2, 2), Vector(-1, 2), Vector(0, 2), Vector(1, 2)
        )
        return pot
    }

    /**
     * check if tree was correct built
     *
     * @param tree current player tree
     */

    private fun isValidTree(tree: Tree): Boolean{
        tree.tiles.forEach{ tile ->
            // for every tile in tree check if conditions for their placement are fulfilled
            val valid = when(tile.value) {
                BonsaiTileType.WOOD, BonsaiTileType.LEAF -> BonsaiTileType.WOOD in tree.neighbors(tile.key)
                BonsaiTileType.FLOWER -> BonsaiTileType.LEAF in tree.neighbors(tile.key)
                else->BonsaiTileType.FRUIT !in tree.neighbors(tile.key)&&hasTwoAdjacentLeaves(tree.neighbors(tile.key))
            }
            if(!valid) return false
        }
        return true
    }

    /**
     * check if player can place wood tile
     *
     * @param tree current player tree
     */

    fun canPlaceWood(tree: Tree): Boolean{
        val places = getPossiblePlacements()
        places.forEach {
            if(tree.neighbors(it).contains(BonsaiTileType.WOOD)) return true
        }
        return false
    }
}
