package service

import entity.*

/**
 * Manages player actions in the Bonsai game.
 */
class PlayerActionService(
    private val rootService: RootService
) : AbstractRefreshingService() {

    private val root
        get() = checkNotNull(rootService.game) { "No Game is currently running" }
    private val currentGame
        get() = root.currentState

    /**
     * Removes the specified tiles from the player's personal supply.
     *
     * This function allows the player to discard tiles from their personal supply.
     *
     * # Preconditions:
     * - The game must be currently active.
     * - The player must have too much [BonsaiTileType] in their storage.
     * - The current State of the Game should be in the Meditate Action.
     *
     * # Post-conditions:
     * - The specified tiles are removed from the player's personal supply.
     * - The game state is updated to reflect the tile removal.
     * - The refresh AfterDiscardTiles is triggered.
     *
     * @param tilesToDiscard A list of [BonsaiTileType] to be discarded.
     *
     * @throws IllegalStateException If the game is not in action or
     *                              the player has not chosen the meditate action or
     *                              the discraded tiles are not enough to put the player under the limit.
     * @throws IllegalArgumentException If the player does not have the specified tiles in their personal supply.
     * @see needsToDiscardTiles
     */
    fun discardTiles(tilesToDiscard: MutableList<BonsaiTileType>) {
        //needs to discards tiles, throw error is nothing is discarded
        needsToDiscardTiles(true)

        //validate current state
        checkStateIsValid(States.DISCARDING)

        //validate no any tile is in list
        require(!tilesToDiscard.contains(BonsaiTileType.ANY)) {
            "ANY tile is not allowed " +
                    "to discard, tried to discard: $tilesToDiscard"
        }

        //if player doesn't have the tiles
        val storage = currentGame.currentPlayer.storage
        val storageMap: List<BonsaiTileType> = storage.map { tile -> tile.type }
        val hasAllElements = storageMap.groupingBy { it }.eachCount().all { (element, count) ->
            tilesToDiscard.count { it == element } >= count
        }
        require(!hasAllElements) {
            "Player has not all required tiles to discard, " +
                    "tiles to Discard $tilesToDiscard, " +
                    "current Tiles {${currentGame.currentPlayer.storage}}"
        }


        //check if enough tiles are discarded
        require(
            currentGame.currentPlayer.storage.size - tilesToDiscard.size <=
                    currentGame.currentPlayer.maxCapacity
        ) {
            "Not enough tiles are discarded, wanted to discard $tilesToDiscard"
        }

        //remove tiles
        for (tile in tilesToDiscard) {
            // Remove the first element that matches the condition
            val index = storage.indexOfFirst { it.type == tile }
            if (index == -1) {
                throw IllegalArgumentException("Tile $tile is not in storage from player (storage is: $storage)")
            }
            storage.removeAt(index)
        }

        //change State
        currentGame.state = States.TURN_END

        //call refresh
        onAllRefreshables { refreshAfterDiscardTiles(tilesToDiscard) }
    }

    /**
     * Checks if the player must discard tiles from their personal supply.
     *
     * This function can be called to determine if the player has exceeded the tile limit in their personal supply.
     *
     * # Preconditions:
     * - The game must be currently active.
     * - The player must have chosen the meditate action.
     *
     * @param throwsError indicates whether this method throws an error or retruns false
     *
     * @return A boolean value indicating whether the player must discard tiles.
     * @throws IllegalStateException If the game is not active or the player has not chosen the meditate action.
     * @see discardTiles
     */
    fun needsToDiscardTiles(throwsError: Boolean = false): Boolean {
        if (currentGame.currentPlayer.storage.size > currentGame.currentPlayer.maxCapacity
        ) {
            //return true if player has too much tiles
            return true
        } else if (throwsError) {
            //throw Error
            throw IllegalStateException(
                "" +
                        "Does not need to discard, current tile size ${currentGame.currentPlayer.storage.size}, " +
                        "max Capacity ${currentGame.currentPlayer.maxCapacity}"
            )
        }
        //else return false
        return false
    }

    /**
     * Performs a cultivation in the Bonsai game.
     *
     * This function change the current game state into 'CULTIVATE'
     *
     * # Preconditions:
     * - The game must currently be running.
     * - The player must click on cultivate button.
     *
     * # Post-conditions:
     * - Game state must be changed into 'CULTIVATE'
     *
     * @return A new GameState object representing the game state after cultivation
     */
    fun cultivate() {
        //validate current state
        checkStateIsValid(States.CHOOSE_ACTION)
        //change state to cultivate
        currentGame.state = States.CULTIVATE
        currentGame.currentPlayer.remainingGrowth = currentGame.currentPlayer.growthLimit.copyOf()
        onAllRefreshables { refreshAfterCultivate() }
    }

    /**
     * Performs a meditation in the Bonsai game.
     *
     * This function change the current game state into 'MEDITATE'
     *
     * # Preconditions:
     * - The game must currently be running.
     * - The player must click on meditate button.
     *
     * # Post-conditions:
     * - Game state must be changed into 'MEDITATE'
     *
     * @return A new GameState object representing the game state after meditation
     */
    fun meditate(cardIndex: Int) {
        //if state is correct
        checkStateIsValid(States.CHOOSE_ACTION)
        require(cardIndex in 0..3) { "Card index must be between 0 and 3, was $cardIndex" }

        //if cardIndex has card
        val card = requireNotNull(currentGame.centerCards[cardIndex]) { "Card at index $cardIndex is null" }.copy()
        currentGame.state = States.MEDITATE


        //take card and refill
        currentGame.currentlyPlayedCard = card
        currentGame.centerCards[cardIndex] = null
        val meditateHelper = MeditateHelper(currentGame)
        meditateHelper.refillCards()

        //if second place
        when (cardIndex) {
            1 -> {
                //go to state choose Wood or Leaf
                currentGame.state = States.CHOOSING_2ND_PLACE_TILES
                onAllRefreshables { refreshAfterChooseTiles(BonsaiTileType.WOOD, BonsaiTileType.LEAF) }
                return
            }

            2 -> {
                currentGame.currentPlayer.storage.add(BonsaiTile(BonsaiTileType.WOOD))
                currentGame.currentPlayer.storage.add(BonsaiTile(BonsaiTileType.FLOWER))
            }

            3 -> {
                currentGame.currentPlayer.storage.add(BonsaiTile(BonsaiTileType.LEAF))
                currentGame.currentPlayer.storage.add(BonsaiTile(BonsaiTileType.FRUIT))
            }
        }
        meditateHelper.resolveCardEffect(cardIndex)
        //resolve card effect and activate refreshable
        if(currentGame.state == States.USING_MASTER)
        {
            val masterCard = card as MasterCard
            onAllRefreshables { refreshAfterChooseTiles(*masterCard.tiles) }
        }
        if (currentGame.state == States.TURN_END && needsToDiscardTiles()) {
            currentGame.state = States.DISCARDING
        }
        onAllRefreshables { refreshAfterMeditate(cardIndex) }

    }

    /**
     * Allows the player to choose a Bonsai tile and attempt to place it in their personal supply.
     *
     * # Functionality:
     * - Validates whether the selected tile is available and can be chosen according to game rules.
     * - Adds the tile to the player's supply if the selection is valid.
     * - Updates the game state and refreshes the UI if necessary.
     *
     * # Preconditions:
     * - The game must be currently active.
     * - The game state must be in meditate.
     *
     * # Post-conditions:
     * - The selected Bonsai tile is added to the player's personal supply.
     * - The game state is updated to reflect the tile selection.
     * - UI refresh is triggered.
     *
     * @param tile The Bonsai tile type that the player wants to choose.
     * @throws IllegalArgumentException If the selected tile is not available.
     * @throws IllegalStateException If the game state is not in meditate.
     */
    fun chooseTile(tile: BonsaiTileType) {
        val meditateHelper = MeditateHelper(currentGame)

        //if state second
        if (currentGame.state == States.CHOOSING_2ND_PLACE_TILES) {
            //if tile is correct
            if (tile == BonsaiTileType.WOOD || tile == BonsaiTileType.LEAF) {
                //add to storage
                currentGame.currentPlayer.storage.add(BonsaiTile(tile))
                //resolve Card effect
                meditateHelper.resolveCardEffect(1)
                if(currentGame.state == States.USING_MASTER)
                {
                    val masterCard = currentGame.currentlyPlayedCard as MasterCard
                    onAllRefreshables { refreshAfterChooseTiles(*masterCard.tiles) }
                }

                if (currentGame.state == States.TURN_END && needsToDiscardTiles()) {
                    currentGame.state = States.DISCARDING
                }
                onAllRefreshables {
                    refreshAfterTileChosen(tile)
                    refreshAfterMeditate(1)
                }
                return
            }
            //else throw error
            throw IllegalArgumentException("Tile $tile is not allowed to choose")
        }
        //else choose master
        checkStateIsValid(States.USING_MASTER)
        //if master can choose tile
        check(currentGame.currentlyPlayedCard is MasterCard) { "Only MasterCard can choose tiles" }
        check((currentGame.currentlyPlayedCard as MasterCard).tiles.contains(BonsaiTileType.ANY)) {
            "MasterCard does not contain ANY tile, contains: ${(currentGame.currentlyPlayedCard as MasterCard).tiles}"
        }
        //add to storage
        //resolve Card effect
        currentGame.currentPlayer.storage.add(BonsaiTile(tile))
        if (currentGame.state == States.TURN_END && needsToDiscardTiles()) {
            currentGame.state = States.DISCARDING
        }
        onAllRefreshables { refreshAfterTileChosen(tile) }
        currentGame.state = States.TURN_END
    }

    /**
     * Allows the player to claim a goal tile if they meet the required conditions.
     *
     * # Functionality:
     * - Validates whether the player meets the conditions for the specified goal.
     * - Removes the claimed goal tile from the available goal pool.
     * - Triggers game state updates and potential UI refreshes.
     *
     * # Preconditions:
     * - The game must be currently active.
     * - The specified goal tile must be available to claim.
     * - The player must meet all conditions associated with the goal tile.
     *
     * # Post-conditions:
     * - The goal tile is removed from the general pool and assigned to the player.
     * - The game state is refreshed to reflect the new goal ownership.
     * - If necessary, the UI is updated to show the claimed goal.
     * - The Player can not claim another Goal with higher level.
     *
     *
     * @param goal The goal tile the player wishes to claim.
     * @throws IllegalArgumentException If the goal tile is not available.
     * @throws IllegalStateException If the player does not meet the goal's requirements.
     */
    fun claimGoal(goal: GoalTile) {
        //if state is correct
        checkStateIsValid(States.CLAIMING_GOALS)
        val reachableGoals = calculateReachedGoals()
        require(currentGame.goalTiles.contains(goal)) {
            "Goal $goal is not in game " +
                    "(Goal in game are ${currentGame.goalTiles})"
        }
        check(reachableGoals.contains(goal)) { "Goal $goal is not reachable" }
        check(!currentGame.currentPlayer.goalTiles.contains(goal)) { "Goal $goal is not claimed" }
        check(!currentGame.currentPlayer.renouncedGoals.contains(goal)) { "Goal $goal is not renounced" }
        //give player goal
        currentGame.currentPlayer.goalTiles.add(goal)
        //remove from available pool
        currentGame.goalTiles.remove(goal)
        reachableGoals.remove(goal)
        //renounce all other goals from category
        currentGame.currentPlayer.renouncedGoals.addAll(currentGame.goalTiles.filter { it.type == goal.type })
        reachableGoals.removeAll(currentGame.goalTiles.filter { it.type == goal.type })
        //call refresh
        if (reachableGoals.isEmpty()) {
            currentGame.state = States.CULTIVATE
        } else {
            currentGame.state = States.CLAIMING_GOALS
        }
        onAllRefreshables { refreshAfterClaimGoal(true, goal) }
    }

    private fun possibleGoalTile(tile: BonsaiTileType): GoalTileType {
        return when (tile) {
            BonsaiTileType.WOOD -> GoalTileType.WOOD
            BonsaiTileType.LEAF -> GoalTileType.LEAF
            BonsaiTileType.FLOWER -> GoalTileType.FLOWER
            BonsaiTileType.FRUIT -> GoalTileType.FRUIT
            BonsaiTileType.ANY -> throw IllegalArgumentException("${BonsaiTileType.ANY} is not playable")
        }
    }

    private fun calculateReachedGoals(): MutableList<GoalTile> {
        //if goal is reached
        var reachedGoals = mutableListOf<GoalTile>().toMutableSet()
        for ((i,t) in currentGame.currentPlayer.tree.tiles) {
            when (t) {
                BonsaiTileType.WOOD, BonsaiTileType.FRUIT -> {
                    reachedGoals += currentGame.goalTiles.filter {
                        (it.type == GoalTileType.WOOD || it.type == GoalTileType.FRUIT) &&
                                rootService.treeService.getTileCount(t, currentGame.currentPlayer.tree) >= it.threshold
                    }
                }

                BonsaiTileType.LEAF -> {
                    val leafCount = rootService.treeService.countConnectedLeaves(i, currentGame.currentPlayer.tree)
                    reachedGoals += currentGame.goalTiles.filter { it.type == GoalTileType.LEAF && leafCount >= it.threshold }
                }

                BonsaiTileType.FLOWER -> {
                    val isOverhanging = when {
                        i.q < -1 - (i.r + 3) / 2 -> 1 //overhanging left
                        i.q > 3 - (i.r + 2) / 2 -> 2 //overhanging right
                        else -> 0
                    }
                    val amount = when (isOverhanging) {
                        1 -> currentGame.currentPlayer.tree.tiles.filter { it.key.q <= (-1 - ((it.key.r + 3) / 2)) && it.value == BonsaiTileType.FLOWER}
                        2 -> currentGame.currentPlayer.tree.tiles.filter { it.key.q >= (3 - ((it.key.r - 2) / 2)) && it.value == BonsaiTileType.FLOWER}
                        else -> emptyMap()
                    }
                    reachedGoals += currentGame.goalTiles.filter {
                        it.type == GoalTileType.FLOWER && amount.count() >= it.threshold
                    }
                }

                BonsaiTileType.ANY -> throw IllegalArgumentException("${BonsaiTileType.ANY} is not playable")
            }

            reachedGoals += currentGame.goalTiles.filter {
                it.type == GoalTileType.POSITION && it.threshold in 1..3 && rootService.treeService.checkBonsaiProtrusion(it.threshold)
            }
        }

        reachedGoals += currentGame.goalTiles.filter {
            it.type == GoalTileType.POSITION && it.threshold in 1..3 &&
                    rootService.treeService.checkBonsaiProtrusion(it.threshold)
        }

        val reachedGoalList = reachedGoals.toMutableList()
        //if goal is not already renounced
        for (i in reachedGoals.indices.reversed()) {
            val reachedGoal = reachedGoalList[i]
            if (currentGame.currentPlayer.renouncedGoals.contains(reachedGoal)) {
                reachedGoalList.removeAt(i)
            }
        }

        return reachedGoalList.toMutableList()
    }

    /**
     * Allows the player to renounce (decline) a previously claimed goal tile.
     *
     * # Functionality:
     * - Returns the goal tile to the general goal pool.
     * - puts it in renouncedGoals in playerService.
     * - Updates the game state and triggers UI refreshes if needed.
     *
     * # Preconditions:
     * - The game must be currently active.
     * - The player must have previously achieved and renounced the specified goal tile.
     *
     * # Post-conditions:
     * - The goal tile is added to the player's renounced goals.
     * - The game state is updated to reflect the renounced goal.
     * - If necessary, the UI is refreshed to show the change.
     *
     * @param goal The goal tile the player wishes to renounce.
     * @throws IllegalArgumentException If the specified goal tile was never achieved by the player.
     * @throws IllegalStateException If renouncing goals is not allowed by the game rules.
     */
    fun renounceGoal(goal: GoalTile) {
        //if state is correct
        checkStateIsValid(States.CLAIMING_GOALS)
        val reachableGoals = calculateReachedGoals()
        require(currentGame.goalTiles.contains(goal)) {
            "Goal $goal is not in game " +
                    "(Goal in game are ${currentGame.goalTiles})"
        }
        check(reachableGoals.contains(goal)) { "Goal $goal is not reachable" }
        check(!currentGame.currentPlayer.goalTiles.contains(goal)) { "Goal $goal is not reachable" }
        //renounce goal
        currentGame.currentPlayer.renouncedGoals.add(goal)
        reachableGoals.removeAll(currentGame.goalTiles.filter { it.type == goal.type })
        //call refresh
        if (reachableGoals.isEmpty()) {
            currentGame.state = States.CULTIVATE
        } else {
            currentGame.state = States.CLAIMING_GOALS
        }
        onAllRefreshables { refreshAfterClaimGoal(false, goal) }
    }

    /**
     * Checks if the game is in a valid state for the player to claim a goal tile.
     *
     * @throws IllegalStateException If the game state is not valid for claiming a goal tile.
     */
    private fun checkStateIsValid(vararg acceptedStates: States) {
        check(currentGame.state in acceptedStates) {
            "Not in required state " +
                    "(required are ${acceptedStates.asList()}), current state ${currentGame.state}"
        }
    }

}
