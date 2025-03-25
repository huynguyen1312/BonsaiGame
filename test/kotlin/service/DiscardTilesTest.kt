package service

import entity.*
import kotlin.test.*

/**
 * A class that tests whether the discardTiles method in [PlayerActionService] works properly.
 */
class DiscardTilesTest {

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

        // fill player storage with just too much wood
        repeat(6){
            gameState.currentPlayer.storage.add(BonsaiTile(BonsaiTileType.WOOD))
        }

        val game = BonsaiGame(1.0, gameState)

        game.currentState.state = States.DISCARDING
        root.game = game
        refreshable = RefreshableTest()
        root.playerActionService.addRefreshable(refreshable)
    }

    /**
     * Tests behavior on null game instance.
     */
    @Test
    fun `test method on null instance`(){
        root.game = null
        assertFailsWith<IllegalStateException> { root.playerActionService.discardTiles(mutableListOf()) }
    }

    /**
     * Tests behavior of method if games FSA is in wrong state
     */
    @Test
    fun `test if FSA in wrong state`(){
        gameState.state = States.CULTIVATE
        assertFailsWith<IllegalStateException> { root.playerActionService.discardTiles(mutableListOf()) }
        gameState.state = States.GAME_ENDED
        assertFailsWith<IllegalStateException> { root.playerActionService.discardTiles(mutableListOf()) }
        gameState.state = States.MEDITATE
        assertFailsWith<IllegalStateException> { root.playerActionService.discardTiles(mutableListOf()) }
    }

    /**
     *  Checks that method transitions into right FSA state (DISCARDING, END_TURN or GAME_ENDEN)
     */
    @Test
    fun  `check that discardTiles transitions into correct FSA state`(){
        assertNotNull(root.game)
        assertEquals(gameState.state, States.DISCARDING)
        root.playerActionService.discardTiles(mutableListOf(BonsaiTileType.WOOD,BonsaiTileType.WOOD))
        val game = requireNotNull(root.game)
        assertContains(listOf(States.TURN_END,States.GAME_ENDED),game.currentState.state)
    }

    /**
     * Check that discardTiles throws [IllegalStateException] if called with tolerable amount of tiles in storage.
     */
    @Test
    fun `call discardTiles with too few tiles in storage`(){
        assertNotNull(root.game)
        gameState.currentPlayer.storage.clear()
        gameState.currentPlayer.storage.addAll(listOf(BonsaiTile(BonsaiTileType.WOOD),BonsaiTile(BonsaiTileType.LEAF)))
        assertFailsWith<IllegalStateException> { root.playerActionService.discardTiles(mutableListOf()) }
    }

    /**
     *  Check that calling too few tiles leads to an [IllegalArgumentException] being thrown
     */
    @Test
    fun `discarding too few tiles throws exception`(){
        assertNotNull(root.game)
        assertFailsWith<IllegalArgumentException> { root.playerActionService.discardTiles(mutableListOf())}
        gameState.currentPlayer.storage.addAll(arrayOf(BonsaiTile(BonsaiTileType.WOOD),
            BonsaiTile(BonsaiTileType.WOOD)
        ))
        // now |storage| = 8 therefore discarding 2 tiles should not suffice
        assertFailsWith<IllegalArgumentException> { root.playerActionService.discardTiles(mutableListOf(
            BonsaiTileType.WOOD,BonsaiTileType.WOOD
        ))}
    }

    /**
     *  Check that tiles given to discardTiles are actually removed
     */
    @Test
    fun `check that tiles are actually removed`(){
        assertNotNull(root.game)
        val tile1 = BonsaiTile(BonsaiTileType.LEAF)
        val tile2 = BonsaiTile(BonsaiTileType.FRUIT)
        gameState.currentPlayer.storage.addAll(listOf(tile1,tile2))
        root.playerActionService.discardTiles(mutableListOf(tile1.type,tile2.type,BonsaiTileType.WOOD))
        val game = requireNotNull(root.game)
        assertEquals(game.currentState.currentPlayer.storage.size,5)
        assertFalse(game.currentState.currentPlayer.storage.contains(tile1))
        assertFalse(game.currentState.currentPlayer.storage.contains(tile2))
    }

    /**
     *  Checks behavior if called with senseless removal list (ANY tile)
     */
    @Test
    fun `check behavior if remove with ANY is called`(){
        assertNotNull(root.game)
        assertFailsWith<IllegalArgumentException> { root.playerActionService.discardTiles(
            mutableListOf(BonsaiTileType.WOOD,BonsaiTileType.FRUIT,BonsaiTileType.ANY)
        )}
    }

    /**
     *  Checks behavior if tiles to be removed are not in player's storage.
     */
    @Test
    fun `checks if tiles to be removed are not in player's storage`(){
        assertNotNull(root.game)
        assertFailsWith<IllegalArgumentException> { root.playerActionService.discardTiles(
            mutableListOf(BonsaiTileType.WOOD,BonsaiTileType.FRUIT)
        ) }
    }

    /**
     *  Check that nothing but the FSA state and the player's storage is changed through execution of discardTile
     */
    @Test
    fun `check discardTiles for unintended changes to game state`(){
        assertNotNull(root.game)

        val originalState = gameState
        root.playerActionService.discardTiles(mutableListOf(BonsaiTileType.WOOD))
        val game = requireNotNull(root.game)
        // change player storage and FSA state in original state to current one
        originalState.currentPlayer.storage.clear()
        originalState.currentPlayer.storage.addAll(game.currentState.currentPlayer.storage)
        originalState.state = game.currentState.state

        // states should now be equal
        assertEquals(originalState, game.currentState)
    }

    /**
     * Tests that refresh is called.
     */
    @Test
    fun `tests that discard tiles calls the corresponding refresh`(){
        assertNotNull(root.game)
        assertFalse(refreshable.refreshAfterDiscardTilesCalled)
        root.playerActionService.discardTiles(mutableListOf(BonsaiTileType.WOOD))
        assertTrue(refreshable.refreshAfterDiscardTilesCalled)
    }
}