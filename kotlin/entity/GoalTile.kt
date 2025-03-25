package entity

import kotlinx.serialization.Serializable

/**
 * Represents a goal tile in the Bonsai game.
 *
 * @property points The number of victory points this goal is worth
 * @property threshold The minimum requirement needed to claim this goal
 * @property type The specific pattern or requirement type for this goal
 *
 * @see GoalTileType
 */
@Serializable
data class GoalTile(
    val points: Int,
    val threshold: Int,
    val type: GoalTileType
) {

    /**
     * Creates a deep copy of the current goal tile.
     */
    fun copy(): GoalTile{
        return GoalTile(points, threshold, type)
    }
}
