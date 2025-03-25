package entity

import kotlinx.serialization.Serializable

/**
 * Represents a tool card in the Bonsai game.
 *
 * @property capacity The number of extended Capacity this tool gives (default: 2)
 *
 */
@Serializable
data class ToolCard(
    val capacity: Int = 2,override val id : Int
) : ZenCard() {



    override fun copy(): ToolCard {
        return ToolCard(capacity, id)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ToolCard

        if (capacity != other.capacity) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = capacity
        result = 31 * result + id
        return result
    }
}
