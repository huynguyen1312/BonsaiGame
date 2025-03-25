package service

import entity.BonsaiTile
import entity.BonsaiTileType
import entity.GoalTile
import entity.Vector
import gui.Refreshable

/**
 * Test class for the [Refreshable] interface covering all relevant scenarios.
 */
class RefreshableTest : Refreshable {

    var refreshAfterGameStartCalled: Boolean = false
        private set
    var refreshAfterEndGameCalled: Boolean = false
        private set
    var refreshAfterEndTurnCalled: Boolean = false
        private set
    var refreshAfterTilesRemovedCalled: Boolean = false
        private set
    var refreshAfterMeditateCalled: Boolean = false
        private set
    var refreshAfterPlaceBonsaiTileCalled: Boolean = false
        private set
    var refreshAfterCultivateCalled: Boolean = false
        private set
    var refreshAfterClaimGoalCalled: Boolean = false
        private set
    var refreshAfterGoalReachedCalled: Boolean = false
        private set
    var refreshAfterTileChosenCalled: Boolean = false
        private set
    var refreshAfterLoadGameCalled: Boolean = false
        private set
    var refreshAfterDiscardTilesCalled: Boolean = false
        private set

    /**
     * Resets all *Called properties to false
     */
    fun reset() {
        refreshAfterGameStartCalled = false
        refreshAfterEndGameCalled = false
        refreshAfterEndTurnCalled = false
        refreshAfterTilesRemovedCalled = false
        refreshAfterMeditateCalled = false
        refreshAfterPlaceBonsaiTileCalled = false
        refreshAfterCultivateCalled = false
        refreshAfterClaimGoalCalled = false
        refreshAfterGoalReachedCalled = false
        refreshAfterTileChosenCalled = false
        refreshAfterLoadGameCalled = false
        refreshAfterDiscardTilesCalled = false
    }

    override fun refreshAfterGameStart() {
        refreshAfterGameStartCalled = true
    }

    override fun refreshAfterEndGame(scores: Array<IntArray>, winner: String) {
        refreshAfterEndGameCalled = true
    }

    override fun refreshAfterEndTurn() {
        refreshAfterEndTurnCalled = true
    }

    override fun refreshAfterTilesRemoved(tilePositions: Array<Vector>) {
        refreshAfterTilesRemovedCalled = true
    }

    override fun refreshAfterMeditate(cardIndex: Int) {
        refreshAfterMeditateCalled = true
    }

    override fun refreshAfterPlaceBonsaiTile(tile: BonsaiTile) {
        refreshAfterPlaceBonsaiTileCalled = true
    }

    override fun refreshAfterCultivate() {
        refreshAfterCultivateCalled = true
    }

    override fun refreshAfterClaimGoal(claimed: Boolean, goal: GoalTile) {
        refreshAfterClaimGoalCalled = true
    }

    override fun refreshAfterGoalReached(goal: GoalTile) {
        refreshAfterGoalReachedCalled = true
    }

    override fun refreshAfterChooseTiles(vararg tiles: BonsaiTileType) {
        refreshAfterTileChosenCalled = true
    }

    override fun refreshAfterTileChosen(tile: BonsaiTileType) {
        refreshAfterTileChosenCalled = true
    }

    override fun refreshAfterLoadGame() {
        refreshAfterLoadGameCalled = true
    }

    override fun refreshAfterDiscardTiles(tiles: List<BonsaiTileType>) {
        refreshAfterDiscardTilesCalled = true
    }
}
