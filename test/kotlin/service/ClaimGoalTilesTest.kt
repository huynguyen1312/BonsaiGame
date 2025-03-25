package service

import entity.*
import kotlin.test.*

/**
 * Test class to verify functionality of claimGoal method in [PlayerActionService].
 */
class ClaimGoalTilesTest {

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

        val game = BonsaiGame(1.0, gameState)

        game.currentState.state = States.CLAIMING_GOALS
        root.game = game
        refreshable = RefreshableTest()
        root.playerActionService.addRefreshable(refreshable)
    }

    /**
     * Tests behavior on null game instance
     */
    @Test
    fun `test behavior on null game`(){
        root.game = null
        assertFailsWith<IllegalStateException> { root.playerActionService.claimGoal(gameState.goalTiles[0]) }
    }

    /**
     *  Tests that an [IllegalArgumentException] is thrown if a not included goal tile is tried to be claimed.
     */
    @Test
    fun `try to claim non-included goal tile`(){
        assertNotNull(root.game)
        // fill tree with wood for goal tile
        for (i in 0..<25){
            // creates 25 tiles long line of wood diagonally to the upper left from center.
            val tile = Vector(0,-i)
            gameState.currentPlayer.tree.tiles[tile] = BonsaiTileType.WOOD
        }
        assertFailsWith<IllegalArgumentException> {
            root.playerActionService.claimGoal(GoalTile(42,20,GoalTileType.WOOD))
        }
    }

    /**
     * Checks that method fails if gaol is not reached.
     */
    @Test
    fun `claim goal even though goal is not reached`(){
        assertNotNull(root.game)
        for (i in 0..<12){
            // creates 12 tiles long line of wood diagonally to the upper left from center.
            val tile = Vector(0,-i)
            gameState.currentPlayer.tree.tiles[tile] = BonsaiTileType.WOOD
        }

        //try to claim 17 wood goal
        assertFailsWith<IllegalStateException> { root.playerActionService.claimGoal(gameState.goalTiles[2]) }
    }

    /**
     *  Try to claim a goal that has already been renounced.
     */
    @Test
    fun `try to claim goal that was renounced`(){
        assertNotNull(root.game)
        gameState.currentPlayer.renouncedGoals.add(gameState.goalTiles[0])

        for (i in 0..<12){
            // creates 12 tiles long line of wood diagonally to the upper left from center.
            val tile = Vector(0,-i)
            gameState.currentPlayer.tree.tiles[tile] = BonsaiTileType.WOOD
        }

        //claiming first goal should fail.
        assertFailsWith<IllegalStateException> { root.playerActionService.claimGoal(gameState.goalTiles[0])}
    }

    /**
     *  Tests if the refreshAfterClaimGoal has been called
     */
    @Test
    fun `refresh test`(){
        assertNotNull(root.game)
        for (i in 0..<12){
            // creates 12 tiles long line of wood diagonally to the upper left from center.
            val tile = Vector(0,-i)
            gameState.currentPlayer.tree.tiles[tile] = BonsaiTileType.WOOD
        }
        assertFalse(refreshable.refreshAfterClaimGoalCalled)
        root.playerActionService.claimGoal(gameState.goalTiles[0])
        assertTrue(refreshable.refreshAfterClaimGoalCalled)
    }

    /**
     *  Tests if claimed goal is removed from available goals and added to player's goals.
     */
    @Test
    fun `tests that claimed goal gets moved from game state to player`(){
        assertNotNull(root.game)
        for (i in 0..<12){
            // creates 12 tiles long line of wood diagonally to the upper left from center.
            val tile = Vector(0,-i)
            gameState.currentPlayer.tree.tiles[tile] = BonsaiTileType.WOOD
        }

        val claimedGoal = gameState.goalTiles[0]
        root.playerActionService.claimGoal(claimedGoal)
        val game = requireNotNull(root.game)
        assertContains(game.currentState.currentPlayer.goalTiles, claimedGoal)
        assertFalse(game.currentState.goalTiles.contains(claimedGoal))
    }

    /**
     *  Tests if another goal tile for the same TileType can be claimed if one is already claimed
     */
    @Test
    fun `try to claim another goal of same TileType`(){
        assertNotNull(root.game)
        for (i in 0..<12){
            // creates 12 tiles long line of wood diagonally to the upper left from center.
            val tile = Vector(0,-i)
            gameState.currentPlayer.tree.tiles[tile] = BonsaiTileType.WOOD
        }

        val firstGoal = gameState.goalTiles[0]
        val secondGoal = gameState.goalTiles[1]

        // claim first wood goal and verify it has been claimed
        root.playerActionService.claimGoal(firstGoal)
        assertContains(gameState.currentPlayer.goalTiles, firstGoal)
        val game = requireNotNull(root.game)
        game.currentState.state = States.CHOOSING_2ND_PLACE_TILES
        // claim second wood goal
        assertFailsWith<IllegalStateException> { root.playerActionService.claimGoal(secondGoal) }
        assertContains( gameState.goalTiles, secondGoal)
        assertFalse(gameState.currentPlayer.goalTiles.contains(secondGoal))
    }

    /**
     *  Tests the behavior of ClaimGoalTiles if called from an invalid FSA state (any state that is not claiming goals).
     */
    @Test
    fun `test claimGoal from illegal FSA state`(){
        assertNotNull(root.game)
        for (i in 0..<12){
            // creates 12 tiles long line of wood diagonally to the upper left from center.
            val tile = Vector(0,-i)
            gameState.currentPlayer.tree.tiles[tile] = BonsaiTileType.WOOD
        }
        gameState.state = States.CULTIVATE
        assertFailsWith<IllegalStateException> { root.playerActionService.claimGoal(gameState.goalTiles[0]) }
        gameState.state = States.MEDITATE
        assertFailsWith<IllegalStateException> { root.playerActionService.claimGoal(gameState.goalTiles[0]) }
    }

    /**
     * Tests that method transitions back into cultivate, end turn or game ended state
     */
    @Test
    fun `check that state afterwards is valid`(){
        assertNotNull(root.game)
        for (i in 0..<12){
            // creates 12 tiles long line of wood diagonally to the upper left from center.
            val tile = Vector(0,-i)
            gameState.currentPlayer.tree.tiles[tile] = BonsaiTileType.WOOD
        }
        root.playerActionService.claimGoal(gameState.goalTiles[0])
        val game = requireNotNull(root.game)
        // game's FSA should be in either one of the mentioned states
        assertContains(listOf(States.CULTIVATE,States.TURN_END,States.GAME_ENDED), game.currentState.state )
    }

    /**
     * Tests that nothing but player goals, game state goals and game's FSA state changed
     */
    @Test
    fun `test that claim goal does not make unintended changes to game state`(){
        assertNotNull(root.game)
        for (i in 0..<12){
            // creates 12 tiles long line of wood diagonally to the upper left from center.
            val tile = Vector(0,-i)
            gameState.currentPlayer.tree.tiles[tile] = BonsaiTileType.WOOD
        }
        val originalState = gameState
        root.playerActionService.claimGoal(gameState.goalTiles[0])
        val game = requireNotNull(root.game)
        // change remaining goal tiles, player goal tiles and FSA state in original state to current one
        originalState.goalTiles.clear()
        originalState.goalTiles.addAll(game.currentState.goalTiles)
        originalState.currentPlayer.goalTiles.clear()
        originalState.currentPlayer.goalTiles.addAll(game.currentState.currentPlayer.goalTiles)
        originalState.state = game.currentState.state

        // states should now be equal
        assertEquals(originalState, game.currentState)
    }
}