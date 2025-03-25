package service

import entity.*
import kotlin.test.*

/**
 * Tests the Choose Tile function in [PlayerActionService].
 */
class ChooseTileTest {

    private lateinit var root: RootService
    private lateinit var refreshable: RefreshableTest
    private lateinit var gameState: GameState

    /**
     * Initializes a valid game state before each Test in which the player can choose a card.
     */
    @BeforeTest
    fun beforeTest(){
        root = RootService()
        // create a game state with players, zen cards and goal tiles
        val players = arrayOf(Player("Sidney Crosbey",PlayerType.LOCAL,Colors.BLACK),
                              Player("Derek Carr",PlayerType.EASY_BOT,Colors.PURPLE))

        val centerCards: Array<ZenCard?> = arrayOf(
            MasterCard(arrayOf(BonsaiTileType.LEAF, BonsaiTileType.FRUIT), id = 42),
            MasterCard(arrayOf(BonsaiTileType.LEAF, BonsaiTileType.WOOD), id = 43),
            ToolCard(2, id = 45), ParchmentCard(4, ParchmentCardType.FRUIT, id = 44)
        )

        val goals = mutableListOf(
            GoalTile(5,8,GoalTileType.WOOD),GoalTile(10,10,GoalTileType.WOOD),
            GoalTile(15,17,GoalTileType.WOOD),
            GoalTile(6,5,GoalTileType.LEAF),GoalTile(9,7,GoalTileType.LEAF),
            GoalTile(12,9,GoalTileType.LEAF),
            GoalTile(9,3,GoalTileType.FRUIT),GoalTile(11,4,GoalTileType.FRUIT),
            GoalTile(13,5,GoalTileType.FRUIT))
        gameState = GameState(players,0,centerCards,States.MEDITATE,goals, drawStack = mutableListOf())
        val  game = BonsaiGame(1.0,gameState)
        game.currentState.state = States.CHOOSING_2ND_PLACE_TILES
        root.game = game
        refreshable = RefreshableTest()
        root.playerActionService.addRefreshable(refreshable)
    }

    /**
     * Tests that the given Tile Type has been added to the current players storage after the method has been called.
     */
    @Test
    fun `test selected tile in storage afterwards`(){
        val game = checkNotNull(root.game)
        game.currentState.currentlyPlayedCard = ToolCard(id = 30)
        root.playerActionService.chooseTile(BonsaiTileType.WOOD)
        assertEquals(gameState.currentPlayer.storage.size, 1)
        assertEquals(gameState.currentPlayer.storage[0].type, BonsaiTileType.WOOD)
    }

    /**
     * Tests that refreshAfterTileChosen is called in the method.
     */
    @Test
    fun `test correct refresh is called`(){
        val game = checkNotNull(root.game)
        assertFalse(refreshable.refreshAfterTileChosenCalled)
        game.currentState.currentlyPlayedCard = ToolCard(id = 30)
        root.playerActionService.chooseTile(BonsaiTileType.WOOD)
        assertTrue(refreshable.refreshAfterTileChosenCalled)
    }

    /**
     * Tests that an [IllegalStateException] is thrown when the game instance is null.
     */
    @Test
    fun `check right exception on null game`(){
        root.game = null
        assertFailsWith<IllegalStateException>{root.playerActionService.chooseTile(BonsaiTileType.LEAF)}
    }

    /**
     * Tests that an [IllegalArgumentException] is thrown when the method is called with an invalid BonsaiTileType,
     * namely [BonsaiTileType.ANY].
     */
    @Test
    fun `check that fails with invalid TileType`(){
        assertNotNull(root.game)
        assertFailsWith<IllegalArgumentException> { root.playerActionService.chooseTile(BonsaiTileType.ANY) }
    }

    /**
     * Tests that the transition in the FSA is valid. Expected to transition from CHOOSING_2ND to either using the drawn
     * card, DISCARDING or END_TURN.
     */
    @Test
    fun `test valid state transition if storage not exceeded`(){
        val game = requireNotNull(root.game)
        assertEquals(game.currentState.state, States.CHOOSING_2ND_PLACE_TILES)
        game.currentState.currentlyPlayedCard = ToolCard(id = 30)
        root.playerActionService.chooseTile(BonsaiTileType.LEAF)
        assertTrue(when(game.currentState.state){
            States.USING_MASTER -> true
            States.USING_HELPER -> true
            States.TURN_END -> true
            else -> false
        })
    }

    /**
     * Tests whether the state transition is correct if player's storage is exceeded.
     */
    @Test
    fun `tests valid state transition if storage is exceeded`(){
        val game = requireNotNull(root.game)
        repeat(5){
            game.currentState.currentPlayer.storage.add(BonsaiTile(BonsaiTileType.WOOD))
        }
        assertEquals(game.currentState.state, States.CHOOSING_2ND_PLACE_TILES)
        game.currentState.currentlyPlayedCard = GrowthCard(BonsaiTileType.WOOD,id = 30)
        root.playerActionService.chooseTile(BonsaiTileType.WOOD)
        assertTrue(when(game.currentState.state){
            States.USING_MASTER -> true
            States.USING_HELPER -> true
            States.DISCARDING -> true
            else -> false
        })
    }

    /**
     * Tests the behavior of the method if called from an invalid FSA state. Namely CHOOSE_ACTION
     */
    @Test
    fun `tests behavior for call from invalid FSA state`(){
        assertNotNull(root.game)
        gameState.state = States.CHOOSE_ACTION
        assertFailsWith<IllegalStateException> { root.playerActionService.chooseTile(BonsaiTileType.WOOD) }
    }

    /**
     * Tests whether any unintended changes are done to the game state. Explicitly: In the entity layer
     * chooseTile should only change the FSA state and the player's storage.
     */
    @Test
    fun `tests for unintended changes in entity layer`(){
        var game = checkNotNull(root.game)
        val originalState = gameState.copy()
        val originalNonIdVars = listOf(gameState.lastRound,gameState.finalPlayer)
        game.currentState.currentlyPlayedCard = ParchmentCard(points = 1, ParchmentCardType.WOOD, id = 22)
        root.playerActionService.chooseTile(BonsaiTileType.WOOD)
        game = requireNotNull(root.game)
        originalState.state = game.currentState.state
        // set FSA state, player storage and discard pile (drawn card) to the new one. States should now be identical.
        originalState.currentPlayer.storage.clear()
        originalState.currentPlayer.storage.addAll(game.currentState.currentPlayer.storage)
        originalState.currentPlayer.discardPile.clear()
        originalState.currentPlayer.discardPile.addAll(game.currentState.currentPlayer.discardPile)
        val newNonIdVars = listOf(game.currentState.lastRound,game.currentState.finalPlayer)
        assertEquals(originalState,game.currentState)
        assertEquals(originalNonIdVars,newNonIdVars)
    }
}
