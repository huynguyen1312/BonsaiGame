package entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a Parchment card in the Bonsai game.
 *
 * Parchment cards are scoring cards that provide players with victory points.
 * Each Parchment card specifies a number of [points] and a specific [type]
 * that determines how much each [ParchmentCardType] is worth.
 *
 * @property points The number of victory points this card is worth
 * @property type The specific type of Parchment card that defines its scoring conditions
 *
 * @see ParchmentCardType
 */
@Serializable
data class ParchmentCard(
    val points: Int, @SerialName("ParchmentType") val type: ParchmentCardType, override val id : Int
) : ZenCard() {
    override fun copy(): ParchmentCard {
        return ParchmentCard(points, type, id)
    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParchmentCard

        if (points != other.points) return false
        if (id != other.id) return false

        return true
    }
}
