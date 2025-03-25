package entity

import kotlinx.serialization.Serializable

/**
 * Represents a Bonsai tile in the game.
 *
 * @property type The type of the Bonsai tile.
 * @property vector The position of the tile on the game board, can be null if
 *                 the tile is not yet placed.
 *
 * @see BonsaiTileType
 * @see Vector
 */
@Serializable
data class BonsaiTile(
    val type: BonsaiTileType,
    val vector: Vector? = null
)
