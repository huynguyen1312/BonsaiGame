package entity

import entity.BonsaiTileType.ANY
import kotlinx.serialization.Serializable

/**
 * Represents the different types of Bonsai tiles available in the game.
 *
 * Each tile type represents a different component of a Bonsai tree:
 * - [ANY]: Represents a wildcard type that can match any other tile type
 *
 * These tile types are used in [BonsaiTile] instances to define their characteristics
 *
 * @see BonsaiTile
 */
@Serializable
enum class BonsaiTileType {
    WOOD,
    LEAF,
    FLOWER,
    FRUIT,
    ANY
}
