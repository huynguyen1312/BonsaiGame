package entity

import kotlinx.serialization.Serializable

/**
 * Defines the different types of Parchment cards available in the Bonsai game.
 *
 * These types are used in [ParchmentCard] instances to determine
 * how points are awarded during scoring.
 *
 * @see ParchmentCard
 */
@Serializable
enum class ParchmentCardType {
    HELPER,
    MASTER,
    GROWTH,
    WOOD,
    LEAF,
    FRUIT,
    FLOWER
}
