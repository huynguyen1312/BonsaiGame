package service

import entity.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Test class for the [TreeService.placeTile] method covering all relevant scenarios.
 */
class PlaceTileTest {

    private lateinit var rootService: RootService
    private lateinit var treeService: TreeService
    private lateinit var player: Player
    private lateinit var refreshable: RefreshableTest

    /**
     * Initializes a valid game state before each test
     */
    @BeforeEach
    fun setup() {
        rootService = RootService()

        player = Player("Alice", PlayerType.LOCAL, Colors.RED).apply {
            storage.addAll(listOf(
                BonsaiTile(BonsaiTileType.WOOD),
                BonsaiTile(BonsaiTileType.LEAF)
            ))
            remainingGrowth[BonsaiTileType.WOOD.ordinal] = 20
            remainingGrowth[BonsaiTileType.LEAF.ordinal] = 20
            remainingGrowth[BonsaiTileType.FLOWER.ordinal] = 100
            remainingGrowth[BonsaiTileType.FRUIT.ordinal] = 100
        }

        val gameState = GameState(
            players = arrayOf(player, Player("Bob", PlayerType.LOCAL, Colors.BLUE)),
            currentPlayerIndex = 0,
            centerCards = arrayOf(
                GrowthCard(entity.BonsaiTileType.WOOD, 42),
                  MasterCard(arrayOf(BonsaiTileType.LEAF, BonsaiTileType.WOOD), 41),
                  ToolCard(2, 40),
                  ParchmentCard(4, ParchmentCardType.FRUIT, 39)
             ),
            state = States.CULTIVATE,
            goalTiles = mutableListOf(
                GoalTile(5, 8, GoalTileType.WOOD),
                GoalTile(10, 10, GoalTileType.WOOD),
                GoalTile(15, 17, GoalTileType.WOOD),
                GoalTile(6, 5, GoalTileType.LEAF),
                GoalTile(9, 7, GoalTileType.LEAF),
                GoalTile(12, 9, GoalTileType.LEAF),
                GoalTile(9, 1, GoalTileType.POSITION),
                GoalTile(11, 2, GoalTileType.POSITION),
                GoalTile(13, 3, GoalTileType.POSITION)
            ),
            drawStack =  mutableListOf(
                ToolCard(id = 38),
                GrowthCard(BonsaiTileType.LEAF, 37),
                MasterCard(arrayOf(BonsaiTileType.LEAF, BonsaiTileType.ANY), 36)
            )
        )

        rootService.game = BonsaiGame(
            botSpeed = 5.0,
            currentState = gameState
        )

        treeService = TreeService(rootService)
        refreshable = RefreshableTest()
        rootService.treeService.addRefreshable(refreshable)
    }

    /**
     * Tests placing a WOOD tile adjacent to another WOOD tile successfully.
     */
    @Test
    fun `place WOOD tile adjacent to another WOOD successfully`() {
        player.tree.tiles[Vector(0, -1)] = BonsaiTileType.WOOD
        val initialGrowth = player.remainingGrowth[BonsaiTileType.WOOD.ordinal]

        treeService.placeTile(BonsaiTile(BonsaiTileType.WOOD, null), Vector(0, -2))

        assertEquals(BonsaiTileType.WOOD, player.tree.tiles[Vector(0, -2)])
        assertEquals(initialGrowth - 1, player.remainingGrowth[BonsaiTileType.WOOD.ordinal])
    }

    /**
     * Tests that placing a tile on an already occupied position throws an exception.
     */
    @Test
    fun `cannot place tile if position is occupied`() {
        player.tree.tiles[Vector(0, -1)] = BonsaiTileType.LEAF

        assertThrows<IllegalArgumentException> {
            treeService.placeTile(BonsaiTile(BonsaiTileType.WOOD, null), Vector(0, -1))
        }
    }

    /**
     * Tests that placing a tile in an invalid game state throws an exception.
     */
    @Test
    fun `cannot place tile when game state is invalid`() {
        rootService.game!!.currentState.state = States.CHOOSING_2ND_PLACE_TILES

        assertThrows<IllegalStateException> {
            treeService.placeTile(BonsaiTile(BonsaiTileType.WOOD, null), Vector(0, -1))
        }
    }

    /**
     * Tests that attempting to place a tile when no game is active throws an exception.
     */
    @Test
    fun `throws exception if no active game`() {
        rootService.game = null

        assertThrows<IllegalStateException> {
            treeService.placeTile(BonsaiTile(BonsaiTileType.WOOD, null), Vector(0, -1))
        }
    }

    /**
     * Tests placing a FRUIT tile correctly between two adjacent LEAF tiles.
     */
    @Test
    fun `placing FRUIT correctly between two adjacent LEAF tiles`() {
        player.tree.tiles[Vector(0, -1)] = BonsaiTileType.LEAF
        player.tree.tiles[Vector(1, -1)] = BonsaiTileType.LEAF

        treeService.placeTile(BonsaiTile(BonsaiTileType.FRUIT, null), Vector(1, -2))

        assertEquals(BonsaiTileType.FRUIT, player.tree.tiles[Vector(1, -2)])
    }

    /**
     * Tests that placing a FRUIT tile adjacent to another FRUIT tile is not allowed.
     */
    @Test
    fun `cannot place FRUIT tile adjacent to another FRUIT tile`() {
        player.tree.tiles[Vector(0, -1)] = BonsaiTileType.LEAF
        player.tree.tiles[Vector(1, -1)] = BonsaiTileType.LEAF
        player.tree.tiles[Vector(1, -2)] = BonsaiTileType.FRUIT

        assertThrows<IllegalArgumentException> {
            treeService.placeTile(BonsaiTile(BonsaiTileType.FRUIT, null), Vector(1, -3))
        }
    }

    /**
     * Tests that placing a tile correctly reduces the player's remaining growth limit.
     */
    @Test
    fun `placing tile reduces remainingGrowth properly`() {
        player.tree.tiles[Vector(0, -1)] = BonsaiTileType.WOOD
        val initialLeafGrowth = player.remainingGrowth[BonsaiTileType.LEAF.ordinal]

        treeService.placeTile(BonsaiTile(BonsaiTileType.LEAF, null), Vector(1, -2))

        assertEquals(BonsaiTileType.LEAF, player.tree.tiles[Vector(1, -2)])
        assertEquals(initialLeafGrowth - 1, player.remainingGrowth[BonsaiTileType.LEAF.ordinal])
    }

    /**
     * Test if reached goals are detected
     */
    @Test
    fun `test that reached goals are detected`(){
        // reach some position and wood goal
        for( i in 0..17){
            player.tree.tiles[Vector(i,-i)] = BonsaiTileType.WOOD
        }
        assertFalse(refreshable.refreshAfterGoalReachedCalled)
        rootService.treeService.placeTile(BonsaiTile(BonsaiTileType.WOOD),Vector(18,-18))
        assertTrue(refreshable.refreshAfterGoalReachedCalled)
    }

    /**
     * Test if reached goals are detected
     */
    @Test
    fun `test position left goals are detected`(){
        // third position
        for( i in 0..17){
            player.tree.tiles[Vector(i,-i)] = BonsaiTileType.WOOD
        }
        player.tree.tiles[Vector(0,-1)] = BonsaiTileType.WOOD
        player.tree.tiles[Vector(0,-2)] = BonsaiTileType.WOOD
        player.tree.tiles[Vector(0,-3)] = BonsaiTileType.WOOD
        for( i in 0..17){
            player.tree.tiles[Vector(-i,i-3)] = BonsaiTileType.WOOD
        }
        assertFalse(refreshable.refreshAfterGoalReachedCalled)
        rootService.treeService.placeTile(BonsaiTile(BonsaiTileType.WOOD),Vector(18,-18))
        assertTrue(refreshable.refreshAfterGoalReachedCalled)
    }

//    /**
//     *Test with flower and any
//     */
//    @Test
//    fun `test can Place tile with flower and any in remaining tiles`(){
//        val game = checkNotNull(rootService.game)
//        game.currentState.currentPlayer.remainingGrowth[0] = 10
//        game.currentState.currentPlayer.remainingGrowth[1] = 10
//        game.currentState.currentPlayer.remainingGrowth[2] = 10
//        game.currentState.currentPlayer.remainingGrowth[3] = 10
//        rootService.treeService.placeTile(BonsaiTile(BonsaiTileType.ANY), Vector(0,-1))
//    }
    private val initialGrowth: Int
        get() = player.remainingGrowth[BonsaiTileType.WOOD.ordinal]
}
