package service

import entity.*
import kotlin.test.*

/**
 * A class that tests the renounceGoalMethod in [PlayerActionService].
 */
class RenounceGoalTest {

    private lateinit var root: RootService
    private lateinit var refreshable: RefreshableTest
    private lateinit var gameState: GameState

    /**
     * Initializes a valid game state before each Test in which the player can choose a card.
     */
    @BeforeTest
    fun beforeTest() {
        root = RootService()
        // create a game state with players, zen cards and goal tiles
        val players = arrayOf(
            Player("Bo Jackson", PlayerType.LOCAL, Colors.PURPLE),
            Player("Carson Wentz", PlayerType.EASY_BOT, Colors.RED)
        )

        val centerCards: Array<ZenCard?> = arrayOf(
            MasterCard(arrayOf(BonsaiTileType.LEAF, BonsaiTileType.FRUIT), id = 42),
            MasterCard(arrayOf(BonsaiTileType.LEAF, BonsaiTileType.WOOD), id = 43),
            ToolCard(2, id = 45), ParchmentCard(4, ParchmentCardType.FRUIT, id = 44)
        )

        val goals = mutableListOf(
            GoalTile(5, 8, GoalTileType.WOOD), GoalTile(10, 10, GoalTileType.WOOD),
            GoalTile(15, 17, GoalTileType.WOOD),
            GoalTile(6, 5, GoalTileType.LEAF), GoalTile(9, 7, GoalTileType.LEAF),
            GoalTile(12, 9, GoalTileType.LEAF),
            GoalTile(9, 3, GoalTileType.FRUIT), GoalTile(11, 4, GoalTileType.FRUIT),
            GoalTile(13, 5, GoalTileType.FRUIT)
        )

        val drawCards = mutableListOf(
            ToolCard(id = 47),
            GrowthCard(BonsaiTileType.LEAF, id = 46),
            MasterCard(arrayOf(BonsaiTileType.LEAF, BonsaiTileType.ANY), id = 48)
        )
        gameState = GameState(players, 0, centerCards, States.MEDITATE, goals, drawStack = drawCards)

        // fill tree with wood for goal tile
        for (i in 0..<25){
            // creates 25 tiles long line of wood diagonally to the upper left from center.
            val tile = Vector(0,-i)
            gameState.currentPlayer.tree.tiles[tile] = BonsaiTileType.WOOD
        }

        val game = BonsaiGame(1.0, gameState)

        game.currentState.state = States.CLAIMING_GOALS
        root.game = game
        refreshable = RefreshableTest()
        root.playerActionService.addRefreshable(refreshable)
    }

    /**
     *  Tests behavior on null game.
     */
    @Test
    fun `test renounce on null game`(){
        root.game = null
        assertFailsWith<IllegalStateException> { root.playerActionService.renounceGoal(gameState.goalTiles[0]) }
    }

    /**
     *  Tests behavior if game's FSA is in an invalid state for this action.
     */
    @Test
    fun `test renounce if FSA in invalid sate`() {
        assertNotNull(root.game)

        gameState.state = States.CULTIVATE
        assertFailsWith<IllegalStateException> { root.playerActionService.renounceGoal(gameState.goalTiles[0]) }
        gameState.state = States.MEDITATE
        assertFailsWith<IllegalStateException> { root.playerActionService.renounceGoal(gameState.goalTiles[0]) }
    }
    /**
     * Tests that method transitions back into cultivate, end turn or game ended state
     */
    @Test
    fun `check that FSA state after renounce goal is valid`(){
        assertNotNull(root.game)

        root.playerActionService.renounceGoal(gameState.goalTiles[0])
        val game = requireNotNull(root.game)
        // game's FSA should be in either one of the mentioned states
        assertContains(listOf(States.CULTIVATE,States.TURN_END,States.GAME_ENDED), game.currentState.state )
    }

    /**
     *  Tests that an [IllegalArgumentException] is thrown if a not included goal tile is tried to be claimed.
     */
    @Test
    fun `try to renounce non-included goal tile`(){
        assertNotNull(root.game)
        assertFailsWith<IllegalArgumentException> {
            root.playerActionService.renounceGoal(GoalTile(42,20,GoalTileType.WOOD))
        }
    }

    /**
     * Checks that method fails if gaol is not reached.
     */
    @Test
    fun `renounce goal even though goal is not reached`(){
        assertNotNull(root.game)

        gameState.currentPlayer.tree.tiles.clear()
        for (i in 0..<12){
            // creates 12 tiles long line of wood diagonally to the upper left from center.
            val tile = Vector(0,-i)
            gameState.currentPlayer.tree.tiles[tile] = BonsaiTileType.WOOD
        }

        //try to renounce 17 wood goal
        assertFailsWith<IllegalStateException> { root.playerActionService.renounceGoal(gameState.goalTiles[2]) }
    }

    /**
     *  Try to claim a goal that has already been claimed.
     */
    @Test
    fun `try to claim goal that was renounced`(){
        assertNotNull(root.game)
        gameState.currentPlayer.goalTiles.add(gameState.goalTiles[0])

        //renouncing claimed first goal should fail.
        assertFailsWith<IllegalStateException> { root.playerActionService.renounceGoal(gameState.goalTiles[0])}
    }

    /**
     * Tests that after renouncing a goal the corresponding refresh method is called
     */
    @Test
    fun `refresh test for renounce goal`(){
        assertNotNull(root.game)
        assertFalse(refreshable.refreshAfterClaimGoalCalled)
        root.playerActionService.renounceGoal(gameState.goalTiles[0])
        assertTrue(refreshable.refreshAfterClaimGoalCalled)
    }

    /**
     *  Tests that after renouncing a goal nothing but the FSA state and the players renounced goals change
     */
    @Test
    fun `check that game state does not change unintendedly after renounce`(){
        assertNotNull(root.game)

        val originalState = gameState
        root.playerActionService.renounceGoal(gameState.goalTiles[0])
        val game = requireNotNull(root.game)
        // change FSA state and player's renounced goals
        originalState.currentPlayer.renouncedGoals.clear()
        originalState.currentPlayer.renouncedGoals.addAll(game.currentState.currentPlayer.renouncedGoals)
        originalState.state = game.currentState.state

        // states should now be equal
        assertEquals(originalState, game.currentState)
    }

    /**
     * Tests that renounced goal is in player's renounced goals list
     */
    @Test
    fun `test that renounced goals are saved in list`(){
        assertNotNull(root.game)
        val renouncedGoal = gameState.goalTiles[0]
        root.playerActionService.renounceGoal(renouncedGoal)
        val game = requireNotNull(root.game)
        assertContains(game.currentState.currentPlayer.renouncedGoals, renouncedGoal)
    }

}
