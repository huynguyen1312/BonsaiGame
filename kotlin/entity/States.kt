package entity

import entity.States.*
import kotlinx.serialization.Serializable

/**
 * Represents the possible game states in the Bonsai game.
 *
 * These states control the flow of the game and determine valid actions:
 * - [CHOOSE_ACTION]: Player selects their main action for the turn
 * - [MEDITATE]: Drawing Zen Cards
 * - [CULTIVATE]: Placing tiles on the Bonsai tree
 * - [USING_MASTER]: Executing a master effect
 * - [USING_HELPER]: Executing a helper effect
 * - [DISCARDING]: Removing tiles from storage
 * - [REMOVE_TILES]: Removing tiles from tree
 * - [CLAIMING_GOALS]: Processing goal tile completion
 * - [CHOOSING_2ND_PLACE_TILES]: Selecting tiles for second place card
 * - [GAME_ENDED]: Final state when game is complete
 * - [TURN_END]: End of player turn
 *
 */
@Serializable
enum class States {
    CHOOSE_ACTION,
    MEDITATE,
    CULTIVATE,
    USING_MASTER,
    USING_HELPER,
    DISCARDING,
    REMOVE_TILES,
    CLAIMING_GOALS,
    CHOOSING_2ND_PLACE_TILES,
    GAME_ENDED,
    TURN_END
}
