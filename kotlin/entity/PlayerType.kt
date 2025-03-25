package entity

import kotlinx.serialization.Serializable

/**
 * Represents the different types of players in the Bonsai game.
 *
 * @see Player
 */
@Serializable
enum class PlayerType {
    LOCAL,
    ONLINE,
    EASY_BOT,
    HARD_BOT
}
