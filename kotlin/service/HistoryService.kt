package service

import entity.States

/**
 * The HistoryService class is responsible for keeping track of previous game states which can be undone and those that
 * have been undone and can be restored.
 *
 * @param root is the RootService to manage communication game and services.
 */
class HistoryService(
    private val root: RootService
) : AbstractRefreshingService() {

    /**
     *  Redo allows the current player to restore an undone game state if all the preconditions are fulfilled.
     *  Redo works complementary to [undo] so that every undone action can be restored (as long as the current state has
     *  not been altered) and every restored state can be undone.
     *
     * # Preconditions:
     *  - The [entity.BonsaiGame] instance in [RootService] has a valid future state.
     *  - The [entity.BonsaiGame] instance is not null (The game must be running).
     *
     * # Postconditions:
     *  - The most recent restorable [entity.GameState] (so the first item in [entity.BonsaiGame.futureStates])
     *      is set to be the current state ([entity.BonsaiGame.currentState]).
     *  - The restored [entity.GameState] is removed from the future states.
     *  - The replaced game state is stored in the history ([entity.BonsaiGame.pastStates]).
     *
     *  @return This method does not have a return value.
     *
     *  @throws IllegalStateException If one of the Preconditions is not met.
     */
    fun redo() {
        val game = checkNotNull(root.game) // Ensure an active game exists
        canRedo(throwsError = true)

        val pastGameState = game.currentState // Save current state before redo
        val futureGameState = game.futureStates.removeFirst() // Retrieve the next future state

        game.pastStates.add(pastGameState) // Store current state in past states for undo
        game.currentState = futureGameState // Set new current state

        game.currentState.state = States.TURN_END

        onAllRefreshables { refreshAfterLoadGame() }
    }

    /**
     * Undo allows the current player to undo a previously made action if all the preconditions are fulfilled.
     * Undo works complementary to [redo] so that every undone action can be restored (as long as the current state has
     * not been altered) and every restored state can be undone.
     *
     * # Preconditions:
     * - The [entity.BonsaiGame] instance in [RootService] has a non-empty game state history (so there must have
     *      been a played turn in the game).
     * - That [entity.BonsaiGame] instance is not null.
     *
     * # Postconditions:
     * - The [entity.GameState] object that was the current one when undo was called is replaced by the state of the
     *      preceding turn (so the first item in the [entity.BonsaiGame.pastState] list).
     * - The replaced game state is added to the front of the [entity.BonsaiGame.futureStates] list.
     * - The game state which is to which the player returns is removed from the past states.
     *
     * @return This method does not have a return value.
     *
     * @throws IllegalStateException When any of the preconditions is not met.
     */
    fun undo() {
        val game = checkNotNull(root.game) // game instance is not null
        require(game.pastStates.isNotEmpty()) // game has a non-empty game state history

        val pastGameState = game.pastStates.removeLast() // the last saved game state in history
        val currentGameState = game.currentState // the current object when undo was called

        game.futureStates.add(currentGameState) // adding the current gameState to futureStates for redo()
        game.currentState = pastGameState

        game.currentState.state = States.TURN_END

        onAllRefreshables { refreshAfterLoadGame() }
    }

    /**
     * Tests if the current player can perform the Undo actions. It tests if the preconditions specified in [undo] are
     * all fulfilled.
     *
     * @param throwsError A boolean parameter that indicates whether in case of error an exceptions should be thrown or
     * just false returned.
     * @return A boolean value that indicates whether the current player can call the undo routine.
     * @throws IllegalStateException If one of the preconditions of [undo] is not fulfilled.
     */
    fun canUndo(throwsError: Boolean = false): Boolean {
        val game =
            root.game ?: if (throwsError) throw IllegalStateException("No active game to undo.") else return false

        val canUndo = game.pastStates.isNotEmpty()

        if (!canUndo && throwsError) {
            throw IllegalStateException("No past states available to undo.")
        }

        return canUndo
    }

    /**
     * Tests if the current player can restore undone game states. It tests if all the preconditions specified in [redo]
     * are fulfilled.
     *
     * @param throwsError A boolean parameter that indicates whether in case of error an exceptions should be thrown or
     * just false returned.
     * @return A boolean value that indicates whether the current player can call the redo routine.
     * @throws IllegalStateException If one of the preconditions of [redo] is not fulfilled.
     */
    fun canRedo(throwsError: Boolean = false): Boolean {
        val game = root.game ?: return if (throwsError) {
            throw IllegalStateException("No active game to redo.")
        } else {
            false
        }
        return if (game.futureStates.isNotEmpty()) {
            true
        } else {
            if (throwsError) throw IllegalStateException("No future game states available to redo.")
            false
        }
    }

}
