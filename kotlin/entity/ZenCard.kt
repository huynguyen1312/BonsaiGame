package entity

import kotlinx.serialization.Serializable

/**
 * Represents a Zen card in the Bonsai game.
 */
@Serializable
sealed class ZenCard {
    /**
     * The unique identifier of the Zen card.
     */
    abstract val id : Int

    /**
     * Creates a deep copy of the current Zen card.
     */
    abstract fun copy(): ZenCard
}
