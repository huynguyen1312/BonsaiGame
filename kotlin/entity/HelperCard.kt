package entity

import kotlinx.serialization.Serializable

/**
 * Represents a Helper card in the Bonsai game.
 *
 * A Helper card is a type of [ZenCard] that let the player place tiles on the tree.
 *
 * @property tiles Array of [BonsaiTileType]s that can be placed on the tree.
 */
@Serializable
data class HelperCard(
    val tiles: Array<BonsaiTileType>, override val id : Int
) : ZenCard() {

    override fun copy(): HelperCard {
        val copy = tiles.copyOf()
        return HelperCard(copy, id)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HelperCard

        if (!tiles.contentEquals(other.tiles)) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return tiles.contentHashCode()
    }

}

