package service

import entity.*
import kotlin.test.*

/**
 * A class that tests the functionality of [PlayerActionService.meditate]
 */
class MeditateTest {
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
            MasterCard(arrayOf(BonsaiTileType.LEAF, BonsaiTileType.FRUIT), id = 43),
            MasterCard(arrayOf(BonsaiTileType.LEAF, BonsaiTileType.WOOD), id = 44),
            ToolCard(2, id = 46), ParchmentCard(4, ParchmentCardType.FRUIT, id = 45)
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
            ToolCard(id = 59),
            GrowthCard(BonsaiTileType.LEAF, id = 56),
            MasterCard(arrayOf(BonsaiTileType.LEAF, BonsaiTileType.ANY), id = 70)
        )
        gameState = GameState(players, 0, centerCards, States.MEDITATE, goals, drawStack = drawCards)
        val game = BonsaiGame(1.0, gameState)
        game.currentState.state = States.CHOOSE_ACTION
        root.game = game
        refreshable = RefreshableTest()
        root.playerActionService.addRefreshable(refreshable)
    }

    /**
     * Tests behavior if game instance is null
     */
    @Test
    fun `tests null game`() {
        root.game = null
        assertFailsWith<IllegalStateException> { root.playerActionService.meditate(0) }
    }

    /**
     * Checks that mediate fails if called from an invalid state
     */
    @Test
    fun `call meditate from invalid state`() {
        gameState.state = States.MEDITATE
        assertFailsWith<IllegalStateException> { root.playerActionService.meditate(0) }
        gameState.state = States.GAME_ENDED
        assertFailsWith<IllegalStateException> { root.playerActionService.meditate(0) }
    }

    /**
     *  Tests meditate for weird card indices. Should be in range [0,3].
     */
    @Test
    fun `test with stupid card indices`() {
        assertNotNull(root.game)
        assertFailsWith<IllegalArgumentException> { root.playerActionService.meditate(-1) }
        assertFailsWith<IllegalArgumentException> { root.playerActionService.meditate(5) }
        assertFailsWith<IllegalArgumentException> { root.playerActionService.meditate(4) }
        assertFailsWith<IllegalArgumentException> { root.playerActionService.meditate(Int.MAX_VALUE) }
        assertFailsWith<IllegalArgumentException> { root.playerActionService.meditate(Int.MIN_VALUE) }
        assertFailsWith<IllegalArgumentException> { root.playerActionService.meditate(42) }
        assertFailsWith<IllegalArgumentException> { root.playerActionService.meditate(1238) }
    }

    /**
     *  Tests that Bonsai tiles associated with given cardIndex actually land in the players storage after mediate.
     */
    @Test
    fun `test bonsai tiles land in player's storage`() {
        assertNotNull(root.game)
        // draw tool card on index 0 --> no tiles added
        gameState.centerCards[0] = ToolCard(id = 70)
        root.playerActionService.meditate(0)
        var game = requireNotNull(root.game)
        assertEquals(game.currentState.currentPlayer.storage.size, 0)

        beforeTest()
        //draw parchment card on index 1 --> leaf or wood tile received
        gameState.centerCards[1] = ParchmentCard(2, ParchmentCardType.GROWTH, id = 71)
        root.playerActionService.meditate(1)
        game = requireNotNull(root.game)
        assertEquals(States.CHOOSING_2ND_PLACE_TILES, game.currentState.state)
        root.playerActionService.chooseTile(BonsaiTileType.WOOD)
        assertTrue(
            when (game.currentState.currentPlayer.storage[0].type) {
                BonsaiTileType.WOOD -> true
                BonsaiTileType.LEAF -> true
                else -> false
            }
        )
        assertEquals(game.currentState.currentPlayer.storage.size, 1)

        beforeTest()
        //draw growth card from index 2 --> receive wood and flower card
        gameState.centerCards[2] = GrowthCard(BonsaiTileType.FRUIT, id = 72)
        root.playerActionService.meditate(2)
        game = requireNotNull(root.game)
        var playerStorage = game.currentState.currentPlayer.storage
        assertEquals(game.currentState.currentPlayer.storage.size, 2)
        assertTrue(
            when (playerStorage[0].type) {
                BonsaiTileType.WOOD -> playerStorage[1].type == BonsaiTileType.FLOWER
                BonsaiTileType.FLOWER -> playerStorage[1].type == BonsaiTileType.WOOD
                else -> false
            }
        )

        beforeTest()
        // draw tool card from index 3 --> receive leaf and fruit
        gameState.centerCards[3] = ToolCard(id = 73)
        root.playerActionService.meditate(3)
        game = requireNotNull(root.game)
        playerStorage = game.currentState.currentPlayer.storage
        assertEquals(game.currentState.currentPlayer.storage.size, 2)
        assertTrue(
            when (playerStorage[0].type) {
                BonsaiTileType.LEAF -> playerStorage[1].type == BonsaiTileType.FRUIT
                BonsaiTileType.FRUIT -> playerStorage[1].type == BonsaiTileType.LEAF
                else -> false
            }
        )
    }

    /**
     * Tests whether meditate transitions to a valid state (so DISCARDING or END_TURN).
     */
    @Test
    fun `checks that mediate transitions into correct state`() {
        // case: no discard necessary
        assertNotNull(root.game)
        assertEquals(gameState.state, States.CHOOSE_ACTION)
        root.playerActionService.meditate(0)
        var game = requireNotNull(root.game)
        assertEquals(game.currentState.state, States.TURN_END)

        beforeTest()

        // case: discard necessary
        game = requireNotNull(root.game)
        repeat(10) { game.currentState.currentPlayer.storage.add(BonsaiTile(BonsaiTileType.WOOD)) }
        assertEquals(gameState.state, States.CHOOSE_ACTION)
        game.currentState.centerCards[0] = ParchmentCard(2, ParchmentCardType.GROWTH, id = 75)
        root.playerActionService.meditate(0)
        assertEquals(game.currentState.state, States.DISCARDING)
    }

    /**
     *  Check that drawing a tool card puts card in tool stack and increases capacity
     */
    @Test
    fun `test effects of drawing tool card`() {
        assertNotNull(root.game)
        //default capacity: 5, no tool cards
        assertEquals(gameState.currentPlayer.maxCapacity, 5)
        assertEquals(gameState.currentPlayer.toolCardPile.size, 0)
        //draw tool card
        val drawnCard = gameState.centerCards[2] as ToolCard
        root.playerActionService.meditate(2)
        val game = requireNotNull(root.game)
        assertContains(game.currentState.currentPlayer.toolCardPile, drawnCard)
        assertEquals(game.currentState.currentPlayer.toolCardPile.size, 1)
        assertEquals(game.currentState.currentPlayer.maxCapacity, 5 + drawnCard.capacity)
    }

    /**
     * Check that drawing a growth card puts card in growth card stack and increases growth limit
     */
    @Test
    fun `test effects of drawing growth card`() {
        assertNotNull(root.game)
        // wood, leaf, fruit, flower, any
        val defaultLimits = intArrayOf(1, 1, 0, 0, 1)
        assertContentEquals(gameState.currentPlayer.growthLimit, defaultLimits)
        assertEquals(gameState.currentPlayer.growthCardPile.size, 0)

        gameState.centerCards[0] = GrowthCard(BonsaiTileType.WOOD, id = 76)
        var drawnCard = gameState.centerCards[0]
        root.playerActionService.meditate(0)

        var expectedLimits = intArrayOf(2, 1, 0, 0, 1)
        assertContentEquals(gameState.currentPlayer.growthLimit, expectedLimits)
        assertEquals(gameState.currentPlayer.growthCardPile.size, 1)
        assertEquals(gameState.currentPlayer.growthCardPile[0], drawnCard)

        beforeTest()
        gameState.centerCards[0] = GrowthCard(BonsaiTileType.LEAF, id = 77)
        drawnCard = gameState.centerCards[0]
        root.playerActionService.meditate(0)
        expectedLimits = intArrayOf(1, 2, 0, 0, 1)
        assertContentEquals(gameState.currentPlayer.growthLimit, expectedLimits)
        assertEquals(gameState.currentPlayer.growthCardPile.size, 1)
        assertEquals(gameState.currentPlayer.growthCardPile[0], drawnCard)

        beforeTest()
        gameState.centerCards[0] = GrowthCard(BonsaiTileType.FRUIT, id = 78)
        drawnCard = gameState.centerCards[0]
        root.playerActionService.meditate(0)
        expectedLimits = intArrayOf(1, 1, 0, 1, 1)
        assertContentEquals(gameState.currentPlayer.growthLimit, expectedLimits)
        assertEquals(gameState.currentPlayer.growthCardPile.size, 1)
        assertEquals(gameState.currentPlayer.growthCardPile[0], drawnCard)

        beforeTest()
        gameState.centerCards[0] = GrowthCard(BonsaiTileType.FLOWER, id = 79)
        drawnCard = gameState.centerCards[0]
        root.playerActionService.meditate(0)
        expectedLimits = intArrayOf(1, 1, 1, 0, 1)
        assertContentEquals(gameState.currentPlayer.growthLimit, expectedLimits)
        assertEquals(gameState.currentPlayer.growthCardPile.size, 1)
        assertEquals(gameState.currentPlayer.growthCardPile[0], drawnCard)
    }

    /**
     * checks that meditate calls refreshAfterMeditate
     */
    @Test
    fun `check that refreshAfterMeditate is called`() {
        assertNotNull(root.game)
        assertFalse(refreshable.refreshAfterMeditateCalled)
        root.playerActionService.meditate(2)
        assertTrue(refreshable.refreshAfterMeditateCalled)
    }

    /**
     *  Checks that after a Master card has been drawn (only) the right tiles are added to players storage.
     */
    @Test
    fun `checks master card adds correct tiles`() {
        assertNotNull(root.game)
        // draws leaf/fruit master from pos 0
        gameState.centerCards[0] = MasterCard(arrayOf(BonsaiTileType.LEAF, BonsaiTileType.FRUIT), id = 80)
        root.playerActionService.meditate(0)
        var game = requireNotNull(root.game)
        var playerStorage = game.currentState.currentPlayer.storage
        assertEquals(game.currentState.currentPlayer.storage.size, 2)
        assertTrue(
            when (playerStorage[0].type) {
                BonsaiTileType.LEAF -> playerStorage[1].type == BonsaiTileType.FRUIT
                BonsaiTileType.FRUIT -> playerStorage[1].type == BonsaiTileType.LEAF
                else -> false
            }
        )

        beforeTest()
        // draws leaf/wood master from pos 1
        gameState.centerCards[2] = MasterCard(arrayOf(BonsaiTileType.LEAF, BonsaiTileType.WOOD), id = 100)
        root.playerActionService.meditate(2)
        game = requireNotNull(root.game)
        playerStorage = game.currentState.currentPlayer.storage
        assertEquals(playerStorage.size, 4)
        val expectedTypes = mutableListOf(
            BonsaiTileType.LEAF, BonsaiTileType.WOOD,
            BonsaiTileType.WOOD, BonsaiTileType.FLOWER
        )
        //should yield empty list
        playerStorage.forEach { expectedTypes.remove(it.type) }
        assertEquals(expectedTypes.size, 0)
    }

    /**
     * Tests that meditate adds drawn parchment cards to player's discard pile.
     */
    @Test
    fun `check parchment cards land in discard pile`() {
        assertNotNull(root.game)
        // draw parchment card
        val drawnCard = gameState.centerCards[0]
        root.playerActionService.meditate(0)
        val game = requireNotNull(root.game)
        // discard pile should now contain that one drawn card
        assertContains(game.currentState.currentPlayer.discardPile, drawnCard)
        assertEquals(game.currentState.currentPlayer.discardPile.size, 1)
    }

    /**
     * Tests that the center cards are in correct order after meditate.
     */
    @Test
    fun `tests order of cards after meditate`() {
        assertNotNull(root.game)
        var previousTopDrawCard = gameState.drawStack.last()
        var previousCenterCards = gameState.centerCards
        root.playerActionService.meditate(3)
        //first of previous draw stack (ToolCard) should be first in center cards, rest the same as before
        var game = requireNotNull(root.game)
        assertEquals(game.currentState.centerCards[0], previousTopDrawCard)
        assertEquals(game.currentState.centerCards[1], previousCenterCards[1])
        assertEquals(game.currentState.centerCards[2], previousCenterCards[2])
        assertEquals(game.currentState.centerCards[3], previousCenterCards[3])

        beforeTest()
        previousTopDrawCard = gameState.drawStack.last()
        previousCenterCards = gameState.centerCards.clone()
        root.playerActionService.meditate(2)
        game = requireNotNull(root.game)
        // first and second should have changed
        assertEquals(game.currentState.centerCards[0], previousTopDrawCard)
        assertEquals(game.currentState.centerCards[1], previousCenterCards[0])
        assertEquals(game.currentState.centerCards[2], previousCenterCards[1])
        assertEquals(game.currentState.centerCards[3], previousCenterCards[3])

        beforeTest()
        previousTopDrawCard = gameState.drawStack.last()
        previousCenterCards = gameState.centerCards.clone()
        root.playerActionService.meditate(1)
        game = requireNotNull(root.game)
        // first,second and third should have changed
        assertEquals(game.currentState.centerCards[0], previousTopDrawCard)
        assertEquals(game.currentState.centerCards[1], previousCenterCards[0])
        assertEquals(game.currentState.centerCards[2], previousCenterCards[2])
        assertEquals(game.currentState.centerCards[3], previousCenterCards[3])

        beforeTest()
        previousTopDrawCard = gameState.drawStack.last()
        previousCenterCards = gameState.centerCards
        root.playerActionService.meditate(0)
        game = requireNotNull(root.game)
        // every card should have been shifted (and first one redrawn)
        assertEquals(game.currentState.centerCards[0], previousTopDrawCard)
        assertEquals(game.currentState.centerCards[1], previousCenterCards[1])
        assertEquals(game.currentState.centerCards[2], previousCenterCards[2])
        assertEquals(game.currentState.centerCards[3], previousCenterCards[3])
    }

    /**
     * Checks how that card drawing fails if index is referring to a card slot that is not filled anymore (At the end
     * of the game)
     */
    @Test
    fun `tests cardIndex in less-than-four-cards situation`() {
        assertNotNull(root.game)
        gameState.drawStack.clear()
        gameState.centerCards[0] = null
        gameState.centerCards[1] = null

        assertFailsWith<IllegalArgumentException> { root.playerActionService.meditate(0) }
        assertFailsWith<IllegalArgumentException> { root.playerActionService.meditate(1) }
        root.playerActionService.meditate(3)
    }
}
