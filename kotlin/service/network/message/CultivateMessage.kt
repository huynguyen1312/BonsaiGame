package service.network.message

import tools.aqua.bgw.net.common.GameAction
import tools.aqua.bgw.net.common.annotations.GameActionClass

/**
 * Game action sent when a player performs cultivate
 *
 * @property removedTilesAxialCoordinates The axial coordinates of tiles removed during this action (0,0 being the root)
 * @property playedTiles The tiles played during this action, with their axial coordinates (0,0 being the root)
 * @property claimedGoals The goals claimed during this action, with their tier
 * @property renouncedGoals The goals renounced during this action, with their tier
 */
@GameActionClass
data class CultivateMessage(
    val removedTilesAxialCoordinates: List<Pair<Int, Int>>,
    val playedTiles: List<Pair<TileTypeMessage, Pair<Int, Int>>>,
    val claimedGoals: List<Pair<GoalTileTypeMessage, Int>>,
    val renouncedGoals: List<Pair<GoalTileTypeMessage, Int>>
) : GameAction() {
    override fun toString(): String {
        val removedTilesString = removedTilesAxialCoordinates.joinToString(", ") { (r, q) -> "($r, $q)" }
        val playedTilesString = playedTiles.joinToString(", ") { (tile, coordinates) ->
            val (q, r) = coordinates
            "$tile ($q, $r)"
        }
        val claimedGoalsString = claimedGoals.joinToString(", ") { (goal, tier) -> "$goal ($tier)" }
        val renouncedGoalsString = renouncedGoals.joinToString(", ") { (goal, tier) -> "$goal ($tier)" }
        return "Played Cultivate:" +
                "\n\tremoved tiles: $removedTilesString" +
                "\n\tplayed tiles: $playedTilesString" +
                "\n\tclaimed goals: $claimedGoalsString" +
                "\n\trenounced goals: $renouncedGoalsString"
    }
}
