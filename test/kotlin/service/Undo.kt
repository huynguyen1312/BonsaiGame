package service

import entity.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests the undo function of the HistoryService.
 */
class Undo {

    private lateinit var root: RootService
    private lateinit var gameConfig: GameConfig
    private lateinit var game: GameState
    private lateinit var game2: GameState
    private lateinit var refreshable: RefreshableTest

    /**
     *  Set up the test environment.
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
            GoalTile(5, 8, GoalTileType.WOOD), GoalTile(10, 10, GoalTileType.WOOD),
            GoalTile(15, 17, GoalTileType.WOOD),
            GoalTile(6, 5, GoalTileType.LEAF), GoalTile(9, 7, GoalTileType.LEAF),
            GoalTile(12, 9, GoalTileType.LEAF),
            GoalTile(9, 3, GoalTileType.FRUIT), GoalTile(11, 4, GoalTileType.FRUIT),
            GoalTile(13, 5, GoalTileType.FRUIT)
        )
        game2 = GameState(
            players = arrayOf(player1, player2),
            currentPlayerIndex = 0,
            centerCards = arrayOf(toolCard, toolCard, toolCard, toolCard),
            state = States.MEDITATE,
            goalTiles = goals,
            drawStack = mutableListOf(ToolCard(1, id = 41))
        )
        refreshable = RefreshableTest()
        root.historyService.addRefreshable(refreshable)
    }

    /**
     * Tests the success of the undo function of the HistoryService & the canUndo function.
     */
    @Test
    fun `test undo when preconditions are met`() {
        //set up GameState
        val pastStates = requireNotNull(root.game) { "Expected game to be in progress" }.pastStates
        pastStates.add(game2)
        //test canUndo
        root.historyService.undo()
        //test if the future GameState is restored
        val newGame = requireNotNull(root.game) { "Expected game to be in progress" }.currentState
        assertEquals(newGame.drawStack[0], ToolCard(1, id = 41))
        //after action in the right state
        assertEquals(newGame.state, States.TURN_END)
    }

    /**
     * Tests the failure of the undo function of the HistoryService & the canUndo function.
     */
    @Test
    fun `test undo when preconditions are not met`() {
        val root1 = RootService()
        val game = requireNotNull(root.game)
        game.pastStates.clear()
        //no game is currently running
        assertThrows<IllegalStateException> {
            root1.historyService.undo()
        }
        //test canRedo false/ throws exception
        //no futureGame is available
        assertFalse(root.historyService.canUndo())
        assertThrows<IllegalStateException> { root.historyService.canUndo(true) }

    }

    /**
     * Test if refresh is called in undo
     */
     @Test
    fun `refresh test on undo`(){
        val pastStates = requireNotNull(root.game) { "Expected game to be in progress" }.pastStates
        pastStates.add(game2)
        assertFalse(refreshable.refreshAfterLoadGameCalled)
        root.historyService.undo()
        assertTrue(refreshable.refreshAfterLoadGameCalled)
    }
}
