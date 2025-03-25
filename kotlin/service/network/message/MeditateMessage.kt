package service.network.message

import tools.aqua.bgw.net.common.GameAction
import tools.aqua.bgw.net.common.annotations.GameActionClass

/**
 * Game action sent when a player performs meditation
 *
 * @property removedTilesAxialCoordinates The axial coordinates of tiles removed during this action (0,0 being the root)
 * @property chosenCardPosition The position of the card chosen by the player
 * Must be between 0 (inclusive) and 4 (exclusive) and starts from left
 * @property playedTiles The tiles played during this action, with their axial coordinates
 * @property drawnTiles The tiles drawn during this action
 * @property claimedGoals The goals claimed during this action, with their tier
 * @property renouncedGoals The goals renounced during this action, with their tier
 * @property discardedTiles The tiles discarded during this action, with their tier
 */
@GameActionClass
data class MeditateMessage(
    val removedTilesAxialCoordinates: List<Pair<Int, Int>>,
    val chosenCardPosition: Int,
    val playedTiles: List<Pair<TileTypeMessage, Pair<Int, Int>>>,
    val drawnTiles: List<TileTypeMessage>,
    val claimedGoals: List<Pair<GoalTileTypeMessage, Int>>,
    val renouncedGoals: List<Pair<GoalTileTypeMessage, Int>>,
    val discardedTiles: List<TileTypeMessage>
) : GameAction(){
    override fun toString(): String {
        var output = ""
        if (removedTilesAxialCoordinates.isNotEmpty()) {
            val prunes = removedTilesAxialCoordinates.map { "(${it.first}, ${it.second})" }
            output += "Pruned tiles at $prunes.\n"
        }
        output += "Drawn Card at position $chosenCardPosition.\n"
        if (playedTiles.isNotEmpty()) {
            val plays = playedTiles.map { "${it.first}@(${it.second.first}, ${it.second.second})" }
            output += "Played tiles from Helper Card: ${plays.joinToString(separator = " & ")}.\n"
        }
        if(drawnTiles.isNotEmpty()){
            output += "Drawn following tiles: $drawnTiles.\n"
        }
        if (claimedGoals.isNotEmpty()) {
            val claims = claimedGoals.map { "${it.first} as Tier ${it.second + 1}/3" }
            output += "Claimed following goals: $claims.\n"
        }
        if(renouncedGoals.isNotEmpty()) {
            val rejects = renouncedGoals.map { "${it.first} as Tier ${it.second + 1}/3" }
            output += "Rejected following goals: $rejects.\n"
        }
        if (discardedTiles.isNotEmpty()) {
            output += "Discarded following tiles from supply: $discardedTiles.\n"
        }
        if(output == ""){
            output = "Did nothing (Pass)."
        }
        return output
    }
}
