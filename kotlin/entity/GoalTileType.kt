package entity

import kotlinx.serialization.Serializable

/**
 * Defines the different types of goals that can be achieved in the Bonsai game.
 *
 * These types are used in [GoalTile] instances to define what kind of pattern
 * or arrangement players need to achieve to claim the goal's points.
 *
 * @see GoalTile
 */
@Serializable
enum class GoalTileType {
    WOOD,
    LEAF,
    FRUIT,
    FLOWER,
    POSITION
}
