package entity

import kotlinx.serialization.Serializable

/**
 * Represents a Master card in the Bonsai game.
 *
 * A Master card is a type of [ZenCard] that lets the player get more tiles.
 *
 * @property tiles Array of [BonsaiTileType]s available through this Master card
 */
@Serializable
data class MasterCard(
    val tiles: Array<BonsaiTileType>, override val id : Int
) : ZenCard() {

    override fun copy(): MasterCard {
        val copy = tiles.copyOf()
        return MasterCard(copy, id)
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MasterCard

        if (!tiles.contentEquals(other.tiles)) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return tiles.contentHashCode()
    }

}

