package service

import entity.*
import kotlin.test.*

/**
 * A class that tests the correct behavior of the cultivate function in [RootService].
 */
class CultivateTest {

    private lateinit var root: RootService
    private lateinit var gameState: GameState

    /**
     * Initializes a valid game state in which the cultivate function can be called.
     */
    @BeforeTest
    fun initialize(){
            root = RootService()
            // create a game state with players, zen cards and goal tiles
            val players = arrayOf(
                Player("Michael Phelps", PlayerType.LOCAL, Colors.BLACK),
                Player("Roger Federer", PlayerType.EASY_BOT, Colors.RED)
            )

            val cards: Array<ZenCard?> = arrayOf(
                MasterCard(arrayOf(BonsaiTileType.LEAF, BonsaiTileType.FRUIT), id = 42),
                MasterCard(arrayOf(BonsaiTileType.LEAF, BonsaiTileType.WOOD), id = 43),
                ToolCard(2, id = 45), ParchmentCard(4, ParchmentCardType.FRUIT, id = 44)
            )

            val goals = mutableListOf(
                GoalTile(5,8, GoalTileType.WOOD), GoalTile(10,10, GoalTileType.WOOD),
                GoalTile(15,17, GoalTileType.WOOD),
                GoalTile(6,5, GoalTileType.LEAF), GoalTile(9,7, GoalTileType.LEAF),
                GoalTile(12,9, GoalTileType.LEAF),
                GoalTile(9,3, GoalTileType.FRUIT), GoalTile(11,4, GoalTileType.FRUIT),
                GoalTile(13,5, GoalTileType.FRUIT)
            )
            gameState = GameState(players,0,cards, States.MEDITATE,goals, drawStack = mutableListOf())
            val  game = BonsaiGame(1.0,gameState)
            game.currentState.state = States.CHOOSE_ACTION
            root.game = game
    }

    /**
     * Validates that the method throws an [IllegalStateException] if the game instance is null.
     */
    @Test
    fun `tests behavior if game is null`(){
        root.game = null
        assertFailsWith<IllegalStateException> { root.playerActionService.cultivate() }
    }

    /**
     * Tests that method throws [IllegalStateException] if game's FSA is not in CHOOSE_ACTION (and executes if it is).
     * Here REMOVING_TILES and END_TURN is used.
     */
    @Test
    fun `tests behavior if game in invalid state`(){
        assertNotNull(root.game)
        gameState.state = States.CHOOSE_ACTION
        root.playerActionService.cultivate()
        gameState.state = States.REMOVE_TILES
        assertFailsWith<IllegalStateException> {root.playerActionService.cultivate() }
        gameState.state = States.TURN_END
        assertFailsWith<IllegalStateException> {root.playerActionService.cultivate() }
        }

    /**
     * Tests that game's FSA state after execution changes to CULTIVATE.
     */
    @Test
    fun `tests that the state after execution is correct`(){
        assertNotNull(root.game)
        root.playerActionService.cultivate()
        val game = requireNotNull(root.game)
        assertEquals(game.currentState.state, States.CULTIVATE)
    }

    /**
     * Tests that nothing but FSA state and remaining growth is changed in execution
     */
    @Test
    fun `checks for unintended changes`(){
        assertNotNull(root.game)
        val originalState = gameState.copy()
        val originalNonIdVars = listOf(gameState.lastRound,gameState.finalPlayer)
        root.playerActionService.cultivate()
        val game = requireNotNull(root.game)
        originalState.state = game.currentState.state
        originalState.currentPlayer.remainingGrowth = game.currentState.currentPlayer.remainingGrowth
        val newNonIdVars = listOf(game.currentState.lastRound,game.currentState.finalPlayer)
        assertEquals(originalState,game.currentState)
        assertEquals(originalNonIdVars,newNonIdVars)
    }
}
