package entity
import kotlinx.serialization.Serializable

/**
 * Represents the game state of the bonsai game.
 *
 * @property botSpeed The speed of the bots.
 * @property currentState The current state of the game.
 * @property futureStates The future state of the game, if undo was used.
 * @property pastStates The past states of the game.
 */
@Serializable
data class BonsaiGame(
    val botSpeed: Double,
    var currentState: GameState,
    val futureStates: MutableList<GameState> = mutableListOf(),
    val pastStates: MutableList<GameState> = mutableListOf()
)
