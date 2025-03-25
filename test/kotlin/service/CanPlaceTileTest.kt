package service

import entity.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Comprehensive test class for [TreeService.canPlaceTile], covering all relevant scenarios.
 */
class CanPlaceTileTest {

    private lateinit var rootService: RootService
    private lateinit var treeService: TreeService
    private lateinit var player1: Player
    private lateinit var player2: Player
    private lateinit var refreshable: RefreshableTest

    /**
     * Initializes a valid game state before each test
     */
    @BeforeEach
    fun setup() {
        rootService = RootService()

        player1 = Player("Alice", PlayerType.LOCAL, Colors.RED).apply {
            storage.addAll(listOf(
                BonsaiTile(BonsaiTileType.WOOD),
                BonsaiTile(BonsaiTileType.LEAF)
            ))
            remainingGrowth[BonsaiTileType.WOOD.ordinal] = 2
            remainingGrowth[BonsaiTileType.LEAF.ordinal] = 2
            remainingGrowth[BonsaiTileType.FLOWER.ordinal] = 1
            remainingGrowth[BonsaiTileType.FRUIT.ordinal] = 1
        }

        player2 = Player("Bob", PlayerType.LOCAL, Colors.BLUE).apply {
            storage.addAll(listOf(
                BonsaiTile(BonsaiTileType.WOOD),
                BonsaiTile(BonsaiTileType.LEAF),
                BonsaiTile(BonsaiTileType.FLOWER)
            ))
            remainingGrowth[BonsaiTileType.WOOD.ordinal] = 2
            remainingGrowth[BonsaiTileType.LEAF.ordinal] = 2
            remainingGrowth[BonsaiTileType.FLOWER.ordinal] = 1
            remainingGrowth[BonsaiTileType.FRUIT.ordinal] = 1
        }

        val centerCards : Array<ZenCard?> = arrayOf(
            GrowthCard(BonsaiTileType.WOOD, 42),
            MasterCard(arrayOf(BonsaiTileType.LEAF, BonsaiTileType.WOOD), 41),
            ToolCard(2, 40),
            ParchmentCard(4, ParchmentCardType.FRUIT, 39)
        )

        val goalTiles = mutableListOf(
            GoalTile(5, 8, GoalTileType.WOOD), GoalTile(10, 10, GoalTileType.WOOD), GoalTile(15, 17, GoalTileType.WOOD),
            GoalTile(6, 5, GoalTileType.LEAF), GoalTile(9, 7, GoalTileType.LEAF), GoalTile(12, 9, GoalTileType.LEAF),
            GoalTile(9, 3, GoalTileType.FRUIT), GoalTile(11, 4, GoalTileType.FRUIT), GoalTile(13, 5, GoalTileType.FRUIT)
        )

        val drawStack = mutableListOf(
            ToolCard(id = 38),
            GrowthCard(BonsaiTileType.LEAF, 37),
            MasterCard(arrayOf(BonsaiTileType.LEAF, BonsaiTileType.ANY), 36)
        )

        val gameState = GameState(
            players = arrayOf(player1, player2),
            currentPlayerIndex = 0,
            centerCards = centerCards,
            state = States.MEDITATE,
            goalTiles = goalTiles,
            drawStack = drawStack
        )

        rootService.game = BonsaiGame(
            botSpeed = 5.0,
            currentState = gameState
        )

        rootService.game!!.currentState.state = States.CULTIVATE

        refreshable = RefreshableTest()
        rootService.playerActionService.addRefreshable(refreshable)

        treeService = TreeService(rootService)
    }

    /**
     * Tests that a WOOD tile can be placed adjacent to another WOOD tile.
     */
    @Test
    fun `can place WOOD tile adjacent to another WOOD tile`() {
        val currentPlayer = rootService.game!!.currentState.currentPlayer
        currentPlayer.tree.tiles[Vector(0, 0)] = BonsaiTileType.WOOD

        val result = treeService.canPlaceTile(BonsaiTileType.WOOD, Vector(0, -1))
        assertTrue(result, "WOOD should be placeable adjacent to another WOOD tile.")
    }

    /**
     * Tests that a WOOD tile cannot be placed if there is no adjacent WOOD tile.
     */
    @Test
    fun `cannot place WOOD tile without adjacent WOOD tile`() {
        val result = treeService.canPlaceTile(BonsaiTileType.WOOD, Vector(1, -2))
        assertFalse(result, "WOOD cannot be placed if not adjacent to another WOOD tile.")
    }

    /**
     * Tests that a LEAF tile can be placed adjacent to a WOOD tile.
     */
    @Test
    fun `can place LEAF tile adjacent to WOOD tile`() {
        val currentPlayer = rootService.game!!.currentState.currentPlayer
        currentPlayer.tree.tiles[Vector(0, 0)] = BonsaiTileType.WOOD

        val result = treeService.canPlaceTile(BonsaiTileType.LEAF, Vector(1, -1))
        assertTrue(result, "LEAF should be placeable adjacent to a WOOD tile.")
    }

    /**
     * Tests that a FLOWER tile cannot be placed without an adjacent LEAF tile.
     */
    @Test
    fun `cannot place FLOWER tile without adjacent LEAF tile`() {
        val currentPlayer = rootService.game!!.currentState.currentPlayer
        currentPlayer.tree.tiles[Vector(0, 0)] = BonsaiTileType.WOOD

        val result = treeService.canPlaceTile(BonsaiTileType.FLOWER, Vector(1, -1))
        assertFalse(result, "FLOWER cannot be placed without adjacent LEAF tile.")
    }

    /**
     * Tests that a FRUIT tile can be placed correctly between two adjacent LEAF tiles.
     */
    @Test
    fun `can place FRUIT tile between two adjacent LEAF tiles`() {
        val currentPlayer = rootService.game!!.currentState.currentPlayer
        currentPlayer.tree.tiles[Vector(0, -1)] = BonsaiTileType.LEAF
        currentPlayer.tree.tiles[Vector(1, -1)] = BonsaiTileType.LEAF

        val result = treeService.canPlaceTile(BonsaiTileType.FRUIT, Vector(1, -2))
        assertTrue(result, "FRUIT should be validly placed between two adjacent LEAF tiles.")
    }

    /**
     * Tests that a FRUIT tile cannot be placed adjacent to another FRUIT tile.
     */
    @Test
    fun `cannot place FRUIT tile adjacent to another FRUIT tile`() {
        val currentPlayer = rootService.game!!.currentState.currentPlayer
        currentPlayer.tree.tiles[Vector(0, -1)] = BonsaiTileType.LEAF
        currentPlayer.tree.tiles[Vector(1, -1)] = BonsaiTileType.LEAF
        currentPlayer.tree.tiles[Vector(1, -2)] = BonsaiTileType.FRUIT

        val result = treeService.canPlaceTile(BonsaiTileType.FRUIT, Vector(2, -2))
        assertFalse(result, "FRUIT tile cannot be placed adjacent to another FRUIT tile.")
    }

    /**
     * Tests that an IllegalStateException is thrown if no active game exists.
     */
    @Test
    fun `throws IllegalStateException if game is null`() {
        rootService.game = null
        assertThrows<IllegalStateException> {
            treeService.canPlaceTile(BonsaiTileType.WOOD, Vector(0, -1), throwsError = true)
        }
    }

    /**
     * Tests that an IllegalStateException is thrown if the game state is neither CULTIVATE nor USING_HELPER.
     */
    @Test
    fun `throws IllegalStateException if state is neither CULTIVATE nor USING_HELPER`() {
        rootService.game!!.currentState.state = States.MEDITATE

        assertThrows<IllegalStateException> {
            treeService.canPlaceTile(BonsaiTileType.WOOD, Vector(0, -1), throwsError = true)
        }
    }

    /**
     * Tests that placing a tile fails if the player's growth limit for that tile type is reached.
     */
    @Test
    fun `returns false if tile limit reached`() {
        val currentPlayer = rootService.game!!.currentState.currentPlayer
        currentPlayer.remainingGrowth[BonsaiTileType.WOOD.ordinal] = 0
        currentPlayer.tree.tiles[Vector(0, -1)] = BonsaiTileType.WOOD

        val result = treeService.canPlaceTile(BonsaiTileType.WOOD, Vector(0, -2))
        assertFalse(result, "Tile cannot be placed if player's remaining growth for that type is 0.")
    }

    /**
     * Tests that placing a tile on an already occupied position returns false.
     */
    @Test
    fun `returns false if position is occupied`() {
        val currentPlayer = rootService.game!!.currentState.currentPlayer
        currentPlayer.tree.tiles[Vector(0, -1)] = BonsaiTileType.WOOD

        val result = treeService.canPlaceTile(BonsaiTileType.WOOD, Vector(0, -1))
        assertFalse(result, "Tile cannot be placed on an already occupied position.")
    }

    /**
     * can place tile with any Tile
     */
    @Test
    fun `test canPlaceTile with ANY tile`(){
        rootService.treeService.canPlaceTile(BonsaiTileType.ANY,Vector(0,-1))
    }
}
