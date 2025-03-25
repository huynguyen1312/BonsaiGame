package gui

import entity.BonsaiTile
import entity.BonsaiTileType
import entity.GoalTile
import entity.Vector
import service.ConnectionState

/**
 * Interface for classes that can be refreshed.
 */
@Suppress("TooManyFunctions")
//It is necessary to have many functions in this interface, as it is used to refresh the GUI after different actions.
interface Refreshable {
    /**
     * Refreshes after the game has started.
     */
    fun refreshAfterGameStart(){}

    /**
     * Refreshes after the game has ended.
     *
     * @param scores The final scores of the players
     * @param winner The name of the winner
     *
     */
    fun refreshAfterEndGame(scores: Array<IntArray>, winner: String){}

    /**
     * Refreshes after the current player's turn has ended.
     */
    fun refreshAfterEndTurn(){}

    /**
     * Refreshes after tiles have been removed from the board.
     *
     * @param tilePositions The positions of the removed tiles
     */
    fun refreshAfterTilesRemoved(tilePositions: Array<Vector>){}

    /**
     * Refreshes after a meditate action has been performed.
     *
     * @param cardIndex The index of the taken card
     */
    fun refreshAfterMeditate(cardIndex: Int){}

    /**
     * Refreshes after a bonsai tile has been placed.
     *
     * @param tile The placed bonsai tile
     */
    fun refreshAfterPlaceBonsaiTile(tile: BonsaiTile){}

    /**
     * Refreshes after a cultivate action has been performed.
     */
    fun refreshAfterCultivate(){}

    /**
     * Refreshes after a goal has been claimed or renounced.
     *
     * @param claimed Whether the goal has been claimed or renounced
     * @param goal The goal that has been claimed or renounced
     *
     */
    fun refreshAfterClaimGoal(claimed: Boolean, goal: GoalTile){}

    /**
     * Refreshes after a goal has been reached.
     *
     * @param goal The goal that has been reached
     */
    fun refreshAfterGoalReached(goal: GoalTile){}

    /**
     * Refreshes after a tile has been chosen.
     *
     * @param tile The chosen tile
     */
    fun refreshAfterTileChosen(tile: BonsaiTileType){}

    /**
     * Refreshes after the game has been loaded.
     *
     * Also used after Undo and Redo.
     */
    fun refreshAfterLoadGame(){}

    /**
     * Refreshes after tiles have been discarded from the storage.
     *
     * @param tiles The discarded tiles
     */
    fun refreshAfterDiscardTiles(tiles: List<BonsaiTileType>){}

    /**
     * Refreshes after tiles have to be chosen.
     *
     * @param tiles The tiles to choose from
     */
    fun refreshAfterChooseTiles(vararg tiles: BonsaiTileType){}

    /**
     * refreshes the network connection status with the given information
     *
     * @param state the information to show
     */
    fun refreshConnectionState(state: ConnectionState) {}

    /**
     * refreshes after a new player joins an online game
     *
     * @param name is the name of the new player
     */
    fun refreshAfterJoin(name: String){}
}
