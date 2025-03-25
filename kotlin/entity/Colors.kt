package entity

import kotlinx.serialization.Serializable

/**
 * Represents the available player colors in the Bonsai game.
 */
@Serializable
enum class Colors {
    RED,
    BLUE,
    BLACK,
    PURPLE,
}

