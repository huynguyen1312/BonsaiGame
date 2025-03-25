package service

import entity.BonsaiTile
import entity.BonsaiTileType
import entity.GoalTile
import entity.Vector
import gui.*
import service.network.NetworkService

/**
 * [NetworkRefreshable] handles updates related to the Bonsai game over the network.
 * It implements the Refreshable interface and interacts with NetworkService to send game state updates.
 *
 * @property root The RootService instance, which provides access to the game state.
 * @property networkService The service responsible for handling network communications.
 * @property message The helper class that stores network messages.
 */
class NetworkRefreshable(private val root: RootService, private val networkService: NetworkService, val message: NetworkMessageHelper) : Refreshable{

    override fun refreshAfterCultivate() {
        message.meditate = false
    }

    override fun refreshAfterEndTurn() {
        if(networkService.connection == ConnectionState.PLAYING_MY_TURN){
            if(message.meditate){
                networkService.sendMeditateMessage(message)
            } else{
                networkService.sendCultivateMessage(message)
            }
            message.clear()
        }
    }

    override fun refreshAfterClaimGoal(claimed: Boolean, goal: GoalTile) {
        message.goals.add(Pair(goal, claimed))
    }

    override fun refreshAfterPlaceBonsaiTile(tile: BonsaiTile) {
        val type = requireNotNull(tile.type)
        val vector = requireNotNull(tile.vector)
        message.placedTiles.add(Pair(type, vector))
    }

    override fun refreshAfterTilesRemoved(tilePositions: Array<Vector>) {
        message.removedTiles = tilePositions.toMutableList()
    }

    override fun refreshAfterMeditate(cardIndex: Int) {
        message.meditate = true
        message.cardIndex = cardIndex
        if(cardIndex != 1){
            message.getDrawnTiles(cardIndex)
        }
    }

    override fun refreshAfterChooseTiles(vararg tiles: BonsaiTileType) {
        message.drawnTiles = tiles.toMutableList()
    }
}