package service

import entity.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Tests for the `endTurn()` function in `GameService`
 */
class EndTurnTest {

    private lateinit var rootService: RootService
    private lateinit var gameConfig: GameConfig

    /**
     * Initializes a valid game state before each test
     */
    @BeforeEach
    fun setup() {
        rootService = RootService()
        gameConfig = GameConfig(
            botSpeed = 5.0,
            playerName = mutableListOf("Alice", "Bob"),
            playerTypes = mutableListOf(PlayerType.LOCAL, PlayerType.LOCAL),
            hotSeat = false,
            randomizedPlayerOrder = false,
            randomizedGoal = false,
            goalTiles = mutableListOf(GoalTileType.WOOD, GoalTileType.LEAF, GoalTileType.FRUIT),
            color = mutableListOf(Colors.RED, Colors.BLUE)
        )

        // Start a game session to ensure we have an active game state
        rootService.gameService.startGame(gameConfig)
    }

    /**
     * Tests that `endTurn()` correctly switches to the next player.
     */
    @Test
    fun `endTurn should switch to next player`() {
        checkNotNull(rootService.game).currentState.state = States.TURN_END
        val initialPlayerIndex = rootService.game?.currentState?.currentPlayerIndex ?: fail("Game is not initialized")
        rootService.gameService.endTurn()
        val newPlayerIndex = rootService.game?.currentState?.currentPlayerIndex ?: fail("Game is not initialized")

        assertNotEquals(initialPlayerIndex, newPlayerIndex, "The turn should switch to the next player.")
        assertEquals((initialPlayerIndex + 1) % gameConfig.playerName.size, newPlayerIndex)
    }

    /**
     * Tests that `endTurn()` throws an exception if no active game is running.
     */
    @Test
    fun `endTurn should throw exception if no active game`() {
        val newRootService = RootService() // No game is started here
        assertThrows(IllegalStateException::class.java) {
            newRootService.gameService.endTurn()
        }
    }

    /**
     * Tests that `endTurn()` correctly saves the current game state before switching players.
     */
    @Test
    fun `endTurn should store current game state before switching players`() {
        checkNotNull(rootService.game).currentState.state = States.TURN_END
        val initialState = checkNotNull(rootService.game).currentState
        rootService.gameService.endTurn()
        assertTrue(
            rootService.game?.pastStates?.contains(initialState) ?: false,
            "Previous state should be saved before switching players."
        )
    }

    /**
     * Tests that `endTurn()` loops back to the first player after the last player's turn.
     */
    @Test
    fun `endTurn should loop back to first player after last player's turn`() {
        checkNotNull(rootService.game).currentState.state = States.TURN_END
        rootService.game?.currentState?.currentPlayerIndex = gameConfig.playerName.size - 1
        rootService.gameService.endTurn()
        assertEquals(
            0, rootService.game?.currentState?.currentPlayerIndex,
            "After last player, turn should return to first player."
        )
    }

    /**
     * Tests that `endTurn()` updates the game state correctly.
     */
    @Test
    fun `endTurn should update game state correctly`() {
        checkNotNull(rootService.game).currentState.state = States.TURN_END
        rootService.gameService.endTurn()
        assertEquals(
            States.CHOOSE_ACTION, rootService.game?.currentState?.state,
            "Game state should remain in CHOOSE_ACTION after ending turn."
        )
    }

    /**
     * Tests that `endTurn()` does not allow a turn switch if the game is in an invalid state.
     */
    @Test
    fun `endTurn should not allow turn switch if game is in an invalid state`() {
        rootService.game?.currentState?.state = States.GAME_ENDED
        assertThrows(IllegalStateException::class.java) {
            rootService.gameService.endTurn()
        }
    }
}
