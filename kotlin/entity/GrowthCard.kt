package entity

import kotlinx.serialization.Serializable

/**
 * Represents a Growth card in the Bonsai game.
 *
 * Growth cards are a type of [ZenCard] that allow players to add more
 * Growth per Cultivate.
 *
 * @property tile The type of Bonsai tile this card allows to be placed
 */
@Serializable
data class GrowthCard(
    val tile: BonsaiTileType, override val id: Int
) : ZenCard() {

    override fun copy(): GrowthCard {
        return GrowthCard(tile, id)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GrowthCard

        if (tile != other.tile) return false
        if (id != other.id) return false

        return true
    }

}
