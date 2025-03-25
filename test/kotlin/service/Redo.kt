package service

import entity.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import kotlin.test.*

/**
 * Tests the redo function of the HistoryService.
 */
class Redo {

    private lateinit var root: RootService
    private lateinit var gameConfig: GameConfig
    private lateinit var game: GameState
    private lateinit var game2: GameState
    private lateinit var refreshable : RefreshableTest

    /**
     * Set up the test environment.
     */
    @BeforeEach
    fun setup() {
        root = RootService()
        gameConfig = GameConfig(
            2.0,
            mutableListOf("John", "Joe"),
            mutableListOf(PlayerType.LOCAL, PlayerType.EASY_BOT),
            false,
            false,
            false,
            mutableListOf(GoalTileType.WOOD, GoalTileType.LEAF, GoalTileType.FRUIT),
            mutableListOf(Colors.PURPLE, Colors.BLACK)
        )
        root.gameService.startGame(gameConfig)
        game = requireNotNull(root.game) { "Expected game to be in progress" }.currentState
        val player1 = Player("John", PlayerType.LOCAL, Colors.PURPLE)
        val player2 = Player("Joe", PlayerType.EASY_BOT, Colors.BLACK)
        val toolCard = ToolCard(1, id = 42)
        val goals = mutableListOf(
            GoalTile(5,8,GoalTileType.WOOD),GoalTile(10,10,GoalTileType.WOOD),
            GoalTile(15,17,GoalTileType.WOOD),
            GoalTile(6,5,GoalTileType.LEAF),GoalTile(9,7,GoalTileType.LEAF),
            GoalTile(12,9,GoalTileType.LEAF),
            GoalTile(9,3,GoalTileType.FRUIT),GoalTile(11,4,GoalTileType.FRUIT),
            GoalTile(13,5,GoalTileType.FRUIT))
        game2 = GameState(
            arrayOf(player1, player2), 0, arrayOf(toolCard, toolCard, toolCard, toolCard),
            States.MEDITATE, goals, mutableListOf(ToolCard(1, id = 41))
        )
        refreshable = RefreshableTest()
        root.historyService.addRefreshable(refreshable)
    }

    /**
     * Tests the success of the redo function of the HistoryService & the canRedo function.
     */
    @Test
    fun `test redo when preconditions are met`() {
        //set up GameState
        val futureState = requireNotNull(root.game) { "Expected game to be in progress" }.futureStates
        futureState.add(game2)
        //historyService.redo()
        root.historyService.redo()
        //test if the future GameState is restored
        val newGame = requireNotNull(root.game) { "Expected game to be in progress" }.currentState
        assertEquals(newGame.drawStack[0], ToolCard(1, id = 41))
        //after action in the right state
        assertEquals(newGame.state, States.TURN_END)
    }

    /**
     * Tests the failure of the redo function of the HistoryService & the canRedo function.
     */
    @Test
    fun `test redo when preconditions are not met`() {
        val root1 = RootService()
        //no game is currently running
        assertThrows<IllegalStateException> {
            root1.historyService.redo()
        }
        //test canUndo false/ throws exception
        //no pastGame is available
        assertFalse(root.historyService.canRedo())
        assertThrows<IllegalStateException> { root.historyService.canRedo(true) }
    }

    /**
     * Tests the method with a null game
     */
    @Test
    fun `test redo with null game`(){
        root.game = null
        assertFailsWith<IllegalStateException> { root.historyService.canRedo(throwsError = true) }
        assertFalse(root.historyService.canRedo())
    }

    /**
     * Test refreshable
     */
    @Test
    fun `test that the refresh is called`(){
        val futureState = requireNotNull(root.game) { "Expected game to be in progress" }.futureStates
        futureState.add(game2)
        assertFalse(refreshable.refreshAfterLoadGameCalled)
        root.historyService.redo()
        assertTrue(refreshable.refreshAfterLoadGameCalled)
    }

    /**
     * Test with empty future states
     */
    @Test
    fun `test canRedo without fututre states`(){
        val game = checkNotNull(root.game)
        game.futureStates.clear()
        assertFailsWith<IllegalStateException> { root.historyService.redo()}
    }
}
