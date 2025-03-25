package gui

import entity.*
import service.HistoryService
import service.RootService
import tools.aqua.bgw.components.ComponentView
import tools.aqua.bgw.components.container.CardStack
import tools.aqua.bgw.components.container.HexagonGrid
import tools.aqua.bgw.components.container.LinearLayout
import tools.aqua.bgw.components.gamecomponentviews.CardView
import tools.aqua.bgw.components.gamecomponentviews.HexagonView
import tools.aqua.bgw.components.layoutviews.CameraPane
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.Orientation
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.core.Color
import tools.aqua.bgw.util.BidirectionalMap
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.CompoundVisual
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.TextVisual
import kotlin.concurrent.fixedRateTimer

/**
 * The game scene in which the game is played.
 *
 * @param rootService The RootService instance, which provides access to the game state.
 */
class GameScene(private val rootService: RootService) :
    BoardGameScene(1920, 1080, background = ImageVisual("Ground.jpg")), Refreshable {

    private val gameService = rootService.gameService
    private val treeService = rootService.treeService
    private val cardImageLoader = CardImageLoader()
    private val playerActionService = rootService.playerActionService
    private var currentGoal: GoalTile? = null
    private val removeTileList: MutableSet<Vector> = mutableSetOf()

    private val zenCards: BidirectionalMap<ZenCard, CardView> = BidirectionalMap()

    // bonsai tile cannot be identified by one Hexagon view (as those are solely defined by the bonsai type).
    // temporary fix: map from the Hexagon visuals path (the picture) to the type and create new HexagonViews from
    // that visual when needed.
    private val bonsaiTileType = mapOf(
        "Wood.png" to BonsaiTileType.WOOD,
        "Leaf.png" to BonsaiTileType.LEAF,
        "Fruit.png" to BonsaiTileType.FRUIT,
        "Flower.png" to BonsaiTileType.FLOWER
    )
    private val bonsaiTiles: BidirectionalMap<BonsaiTile, HexagonView> = BidirectionalMap()
    private val potsImages: BidirectionalMap<Colors, ImageVisual> = BidirectionalMap()
    private val goalTilesImages: BidirectionalMap<GoalTile, CardView> = BidirectionalMap()

    private fun generatedObjects() {
        zenCards.clear()
        cardImageLoader.createdFullZenCardsStack(zenCards)
        bonsaiTiles.clear()
        cardImageLoader.bonsaiTilesImageGenerator(bonsaiTiles)
        potsImages.clear()
        cardImageLoader.potImageGenerator(potsImages)
        goalTilesImages.clear()
        cardImageLoader.goalTileImageGenerator(goalTilesImages)
    }

    private val overlayPaneForShowingTreeOrTiles = Pane<ComponentView>(
        posX = 0, posY = 0, width = 1920, height = 1080, visual = ColorVisual(Color(12, 32, 39, 240))
    ).apply {
        opacity = 0.95
        isVisible = false
    }

    // make the main background "Ground.jpg" unclear so that other game elements can be better visible
    private val overlayPane = Pane<ComponentView>(
        posX = 0, posY = 0, width = 1920, height = 1080, visual = ColorVisual(Color(0xB5A481))
    ).apply {
        opacity = .85
        isVisible = true
    }

    // board, where zen cards stack and draw cards are placed
    private val boardLabel = Label(
        posX = 1920 / 2 - 375,
        posY = 1080 / 2 - 470,
        width = 750,
        height = 250,
        alignment = Alignment.CENTER,
        visual = ImageVisual("Board.png"),
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold")
    )

    // current players tree
    private val currentPlayerPotLabel = Label(
        posX = 2000 / 2 - 150 - 100,
        posY = 2000 / 2 - 100 - 50,
        width = 300,
        height = 150,
        text = "Name1",
        alignment = Alignment.CENTER,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold")
    )

    private val opponentPlayerPotLabel1 = Label(
        posX = 1920 / 2 - 150,
        posY = 1080 / 2,
        width = 300,
        height = 150,
        text = "Name2",
        alignment = Alignment.CENTER,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold")
    ).apply {
        isVisible = false
    }

    private val opponentPlayerPotLabel2 = Label(
        posX = 1920 / 2 - 150,
        posY = 1080 / 2,
        width = 300,
        height = 150,
        text = "Name3",
        alignment = Alignment.CENTER,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold")
    ).apply {
        isVisible = false
    }

    private val opponentPlayerPotLabel3 = Label(
        posX = 1920 / 2 - 150,
        posY = 1080 / 2,
        width = 300,
        height = 150,
        text = "Name4",
        alignment = Alignment.CENTER,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold")
    ).apply {
        isVisible = false
    }

    // Button, which is used to show opponents tree
    private val opponent1Tree = Button(
        width = 200,
        height = 100,
        posX = 1920 / 2 - 850,
        posY = 1080 / 2 -150,
        text = "Opponent 1",
        font = Font(26, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ImageVisual("Pot2.png"),
    ).apply {
        isVisible = false
        onMouseEntered = { scale = 1.05 }
        onMouseExited = { scale = 1.0 }
        onMouseClicked = {
            val opponentCopy = gameState.players.clone().toMutableList()
            //remove active Player, because it is no opponent
            opponentCopy.remove(gameState.currentPlayer)
            overlayPaneForShowingTreeOrTiles.clear()
            overlayPaneForShowingTreeOrTiles.isVisible = true
            // Add components related to wishing a suit to the overlayPane
            overlayPaneForShowingTreeOrTiles.addAll(buildShowTreePane(opponentCopy[0]))
        }
    }


    private val opponent2Tree = Button(
        width = 200,
        height = 100,
        posX = 1920 / 2 - 850,
        posY = 1080 / 2 ,
        text = "Opponent 2",
        font = Font(26, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ImageVisual("Pot3.png"),
    ).apply {
        isVisible = false
        onMouseEntered = { scale = 1.05 }
        onMouseExited = { scale = 1.0 }
        onMouseClicked = {
            val opponentCopy = gameState.players.clone().toMutableList()
            //remove active Player, because it is no opponent
            opponentCopy.remove(gameState.currentPlayer)
            overlayPaneForShowingTreeOrTiles.clear()
            overlayPaneForShowingTreeOrTiles.isVisible = true
            // Add components related to wishing a suit to the overlayPane
            overlayPaneForShowingTreeOrTiles.addAll(buildShowTreePane(opponentCopy[1]))
        }
    }


    private val opponent3Tree = Button(
        width = 200,
        height = 100,
        posX = 1920 / 2 - 850,
        posY = 1080 / 2 + 150,
        text = "Opponent 3",
        font = Font(26, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ImageVisual("Pot4.png"),
    ).apply {
        isVisible = false
        onMouseEntered = { scale = 1.05 }
        onMouseExited = { scale = 1.0 }
        onMouseClicked = {
            val opponentCopy = gameState.players.clone().toMutableList()
            //remove active Player, because it is no opponent
            opponentCopy.remove(gameState.currentPlayer)
            overlayPaneForShowingTreeOrTiles.clear()
            overlayPaneForShowingTreeOrTiles.isVisible = true
            // Add components related to wishing a suit to the overlayPane
            overlayPaneForShowingTreeOrTiles.addAll(buildShowTreePane(opponentCopy[2]))
        }
    }


    // draw stack position
    private val drawStack = CardStack<CardView>(
        posX = 1920 / 2 - 350,
        posY = 1080 / 2 - 450,
        width = 130,
        height = 180,
        visual = cardImageLoader.backZenImage,
    ).apply {
        isVisible = true
    }

    private val drawStackText = Label(
        posX = 1920 / 2 - 350,
        posY = 1080 / 2 - 450,
        width = 130,
        height = 180,
        text = "47",
        font = Font(50, Color(0xFFFFFF), "JetBrains Mono ExtraBold")
    )

    private val positionZen1 = CardView(
        posX = 1920 / 2 - 190,
        posY = 1080 / 2 - 450,
        width = 130,
        height = 180,
        front = cardImageLoader.getImageByCoordinates(4, 1)
    ).apply {
        isVisible = true
        onMouseEntered = { scale = 1.05 }
        onMouseExited = { scale = 1.0 }
        onMouseClicked = {
            if (gameState.state == States.CHOOSE_ACTION) {
                playerActionService.meditate(0)
            } else {
                showMessage("CANT MEDITATE!!!!")
            }
        }
    }

    private val positionZen2 = CardView(
        posX = 1920 / 2 - 55,
        posY = 1080 / 2 - 450,
        width = 130,
        height = 180,
        front = cardImageLoader.getImageByCoordinates(4, 1)
    ).apply {
        isVisible = true
        onMouseEntered = { scale = 1.05 }
        onMouseExited = { scale = 1.0 }
        onMouseClicked = {
            if (gameState.state == States.CHOOSE_ACTION) {
                playerActionService.meditate(1)
            } else {
                showMessage("CANT MEDITATE!!!!")
            }
        }
    }

    private val positionZen3 = CardView(
        posX = 1920 / 2 + 80,
        posY = 1080 / 2 - 450,
        width = 130,
        height = 180,
        front = cardImageLoader.getImageByCoordinates(4, 1)
    ).apply {
        isVisible = true
        onMouseEntered = { scale = 1.05 }
        onMouseExited = { scale = 1.0 }
        onMouseClicked = {
            if (gameState.state == States.CHOOSE_ACTION) {
                playerActionService.meditate(2)
            } else {
                showMessage("CANT MEDITATE!!!!")
            }
        }
    }

    private val positionZen4 = CardView(
        posX = 1920 / 2 + 215,
        posY = 1080 / 2 - 450,
        width = 130,
        height = 180,
        front = cardImageLoader.getImageByCoordinates(4, 1)
    ).apply {
        isVisible = true
        onMouseEntered = { scale = 1.05 }
        onMouseExited = { scale = 1.0 }
        onMouseClicked = {
            if (gameState.state == States.CHOOSE_ACTION) {
                playerActionService.meditate(3)
            } else {
                showMessage("CANT MEDITATE!!!!")
            }
        }
    }

    /**
     * Displays an error message if the player tries to do something that is not allowed
     */
    private val message = Button(
        width = 450,
        height = 100,
        posX = 49,
        posY = 150,
        font = Font(size = 16, Color(0x000000)),
        visual = ColorVisual(Color(0xFF4D4D))
    ).apply {
        isVisible = false
        onMouseClicked = {
            this.isVisible = false
        }
        fixedRateTimer("hideError", false, 0, 5000) {
            this@apply.isVisible = false
        }
    }

    /**
     * Displays an error message.
     */
    private fun showMessage(message: String) {
        this.message.text = message
        this.message.isVisible = true
    }

    // tool cards, which belong to current player
    private val currentPlayerToolCard = LinearLayout<CardView>(
        posX = 1920 / 2 - 200 - 369,
        posY = 1080 / 2 + 275,
        width = 500,
        height = 200,
        alignment = Alignment.TOP_RIGHT,
        spacing = 2
    )

    // growth cards, which belong to current player
    private val currentPlayerGrowthCard = LinearLayout<CardView>(
        posX = 1920 / 2 + 70,
        posY = 1080 / 2 + 275,
        width = 500,
        height = 200,
        alignment = Alignment.TOP_LEFT,
        spacing = -97
    )

    // card stack to save other drawn cards from current player after meditating except growth and tool cards
    private val currentPlayerMeditatedStack = CardStack<CardView>(
        posX = 1920 / 2 - 700,
        posY = 1080 / 2 + 275,
        width = 131,
        height = 200,
        visual = cardImageLoader.backZenImage,
    ).apply {
        isVisible = true
    }

    // current players supply
    private val currentPlayerSupply = LinearLayout<HexagonView>(
        posX = 1920 / 2 - 150,
        posY = 1080 / 2 + 175,
        width = 500,
        height = 64,
        alignment = Alignment.TOP_CENTER,
        spacing = 5
    )

    private val seiShiLabel = Label(
        posX = 1920 / 2 - 75,
        posY = 1080 / 2 + 275,
        width = 150,
        height = 250,
        alignment = Alignment.CENTER,
        visual = ImageVisual("Seishi.png"),
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold")
    )

    private val goalTile1Label = LinearLayout<CardView>(
        posX = 1920 / 2 + 800,
        posY = 1080 / 2 - 300,
        width = 120,
        height = 80,
        alignment = Alignment.CENTER,
        orientation = Orientation.VERTICAL,
        spacing = -150
    )

    private val goalTile2Label = LinearLayout<CardView>(
        posX = 1920 / 2 + 800,
        posY = 1080 / 2 - 50,
        width = 120,
        height = 80,
        alignment = Alignment.CENTER,
        orientation = Orientation.VERTICAL,
        spacing = -150
    )

    private val goalTile3Label = LinearLayout<CardView>(
        posX = 1920 / 2 + 800,
        posY = 1080 / 2 + 200,
        width = 120,
        height = 80,
        alignment = Alignment.CENTER,
        orientation = Orientation.VERTICAL,
        spacing = -150
    )

    private val woodLabel = Label(
        posX = 1920 / 2 - 900,
        posY = 1080 / 2 + 400,
        width = 63,
        height = 72,
        text = "0",
        alignment = Alignment.CENTER,
        visual = ImageVisual("Wood.png"),
        font = Font(25, Color(0xFFFFFFF), "JetBrains Mono ExtraBold")
    )

    private val leafLabel = Label(
        posX = 1920 / 2 - 800,
        posY = 1080 / 2 + 400,
        width = 63,
        height = 72,
        text = "0",
        alignment = Alignment.CENTER,
        visual = ImageVisual("Leaf.png"),
        font = Font(25, Color(0xFFFFFFF), "JetBrains Mono ExtraBold")
    )

    private val flowerLabel = Label(
        posX = 1920 / 2 - 900,
        posY = 1080 / 2 + 300,
        width = 63,
        height = 72,
        text = "0",
        alignment = Alignment.CENTER,
        visual = ImageVisual("Flower.png"),
        font = Font(25, Color(0xFFFFFFF), "JetBrains Mono ExtraBold")
    )

    private val fruitLabel = Label(
        posX = 1920 / 2 - 800,
        posY = 1080 / 2 + 300,
        width = 63,
        height = 72,
        text = "0",
        alignment = Alignment.CENTER,
        visual = ImageVisual("Fruit.png"),
        font = Font(25, Color(0xFFFFFFF), "JetBrains Mono ExtraBold")
    )

    private val anyLabel = Label(
        posX = 1920 / 2 - 850,
        posY = 1080 / 2 + 350,
        width = 63,
        height = 72,
        text = "0",
        alignment = Alignment.CENTER,
        visual = ImageVisual("Any.png"),
        font = Font(25, Color(0xFFFFFFF), "JetBrains Mono ExtraBold")
    )

    private val placedTilesCount = Label(
        posX = 1920 / 2 - 900,
        posY = 1080 / 2 + 395,
        width = 170,
        height = 200,
        text = "0 placed tiles",
        font = Font(25, Color(0x0), "JetBrains Mono ExtraBold")
    )

    private val summaryCardLabel = Label(
        posX = 1920 / 2 + 500,
        posY = 1080 / 2 + 275,
        width = 112,
        height = 156,
        alignment = Alignment.CENTER,
        visual = cardImageLoader.summaryCard,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold")
    )

    // Button for cultivating (placing a tile on the tree)
    private val cultivateButton = Button(
        width = 150,
        height = 65,
        posX = 1920 / 2 - 700 / 2 - 20,
        posY = 1080 / 2 - 900 / 2 - 100,
        text = "cultivate",
        font = Font(26, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D)),
    ).apply {
        onMouseClicked = {
            if (game.currentState.state == States.CHOOSE_ACTION) {
                playerActionService.cultivate()
            }
        }
    }


    private val endTurnButton = Button(
        width = 150,
        height = 65,
        posX = 1920 / 2 - 700 / 2 + 290,
        posY = 1080 / 2 - 900 / 2 - 100,
        text = "End Turn",
        font = Font(26, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D)),
    ).apply {
        isVisible = false
        onMouseClicked = {
            gameService.endTurn()
        }
    }

    private val undoButton = Button(
        width = 100,
        height = 65,
        posX = 1920 / 2 - 700 / 2 + 450,
        posY = 1080 / 2 - 900 / 2 - 100,
        text = "Undo",
        font = Font(26, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D)),
    ).apply {
        onMouseClicked = {
            if (historyService.canUndo()) {
                historyService.undo()
            } else {
                showMessage("Can not undo!")
            }
        }
    }

    private val redoButton = Button(
        width = 100,
        height = 65,
        posX = 1920 / 2 - 700 / 2 + 560,
        posY = 1080 / 2 - 900 / 2 - 100,
        text = "Redo",
        font = Font(26, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D)),
    ).apply {
        onMouseClicked = {
            if (historyService.canRedo()) {
                historyService.redo()
            } else {
                showMessage("Can not redo!")
            }
        }
    }

    private val removeButton = Button(
        width = 150,
        height = 65,
        posX = 1920 / 2 - 700 / 2 + 670,
        posY = 1080 / 2 - 900 / 2 - 100,
        text = "Remove",
        font = Font(26, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D)),
    ).apply {
        onMouseClicked = {
            if (game.currentState.state == States.CHOOSE_ACTION && !treeService.canPlaceWood(currentPlayer.tree)) {
                gameState.state = States.REMOVE_TILES
                overlayPaneForShowingTreeOrTiles.clear()
                overlayPaneForShowingTreeOrTiles.isVisible = true
                overlayPaneForShowingTreeOrTiles.addAll(showRemoveTree())
            }
        }
    }


    val saveGameButton = Button(
        width = 150,
        height = 65,
        posX = 1920 / 2 + 625,
        posY = 1080 / 2 - 900 / 2 - 100,
        text = "Save Game",
        font = Font(26, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D)),
    ).apply {
        onMouseClicked = {
            rootService.fileService.saveGame()
        }
    }
    private val claimGoalButton = Button(
        width = 100,
        height = 100,
        posX = 1200,
        posY = 500,
        text = "Claim Goal",
        font = Font(26, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D)),
    ).apply {
        isVisible = false
        onMouseClicked = {
            // Perform the cultivate action only if a game is running
            playerActionService.claimGoal(checkNotNull(currentGoal))
        }
    }

    private val treeGrid = HexagonGrid<HexagonView>(
        240, 324, coordinateSystem = HexagonGrid.CoordinateSystem.AXIAL
    )

    private val pane = Pane<ComponentView>(
        posX = 0,
        posY = 0,
        width = 2000,
        height = 2000,
    )

    private val cameraPane = CameraPane(
        posX = 1920 / 2 - 400,
        posY = 1080 / 2 - 200,
        width = 800,
        height = 350,
        target = pane
    )


    init {
        addComponents(
            overlayPane,
            cameraPane,
            boardLabel, seiShiLabel,
//            treeGrid,
            currentPlayerGrowthCard, currentPlayerToolCard, currentPlayerSupply, currentPlayerMeditatedStack,
            summaryCardLabel,
//            currentPlayerPotLabel,
            opponentPlayerPotLabel1, opponentPlayerPotLabel2, opponentPlayerPotLabel3,
            woodLabel, leafLabel, flowerLabel, fruitLabel, anyLabel,
            cultivateButton,
            endTurnButton,
            claimGoalButton,
            undoButton, redoButton,
            saveGameButton,
            drawStack, drawStackText,
            positionZen1, positionZen2, positionZen3, positionZen4,
            goalTile1Label, goalTile2Label, goalTile3Label,
            placedTilesCount,
            opponent1Tree, opponent2Tree, opponent3Tree,
            message,
            removeButton,
            overlayPaneForShowingTreeOrTiles,
        )
        initializeTreeGrid()
        pane.add(treeGrid)
        pane.add(currentPlayerPotLabel)
        cameraPane.interactive = true
    }

    private val game: BonsaiGame
        get() = requireNotNull(rootService.game)
    private val gameState: GameState
        get() = game.currentState
    private val currentPlayer: Player
        get() = gameState.currentPlayer
    private val historyService: HistoryService
        get() = rootService.historyService

    /**
     * This function initializes an empty tree grid with [HexagonView]s that show their coordinates
     */
    private fun initializeTreeGrid() {
        for (row in -10..10) {
            for (col in -10..10) {/* Only add hexagons that would fit in a circle */
                if (row + col in -10..10) {
                    val hexagon = HexagonView(
                        visual = CompoundVisual(
                            ColorVisual(Color(0xc6ff6e)),
                            TextVisual(
                                text = "$col, $row", font = Font(10.0, Color(0x0f141f))
                            )
                        ), size = 35
                    ).apply {
                        isVisible = true
                        dropAcceptor = { dragEvent ->
                            when (dragEvent.draggedComponent) {
                                is HexagonView -> {
                                    val type = checkNotNull(
                                        bonsaiTileType[((dragEvent.draggedComponent as HexagonView).visual as ImageVisual).path]
                                    )
                                    rootService.treeService.canPlaceTile(type, Vector(col, row))
                                }

                                else -> {
                                    showMessage("Can not place!")
                                    false
                                }
                            }
                        }
                        onDragDropped = { event ->
                            val bonsaiType =
                                checkNotNull(bonsaiTileType[((event.draggedComponent as HexagonView).visual as ImageVisual).path])
                            treeService.placeTile(BonsaiTile(bonsaiType), Vector(col, row))
                        }
                    }
                    treeGrid[col, row] = hexagon
                }
            }
        }
    }

    /**
     * This function initializes an empty tree grid with [HexagonView]s that show their coordinates
     */
    private fun initializeRemovalTreeGrid(removalTreeGrid: HexagonGrid<HexagonView>) {
        for (row in -10..10) {
            for (col in -10..10) {/* Only add hexagons that would fit in a circle */
                if (row + col in -10..10) {
                    val hexagon = HexagonView(
                        visual = CompoundVisual(
                            ColorVisual(Color(0xc6ff6e)),
                            TextVisual(
                                text = "$col, $row", font = Font(10.0, Color(0x0f141f))
                            )
                        ), size = 35
                    )
                    if (Vector(row, col) in currentPlayer.tree.tiles) {
                        hexagon.visual = when (currentPlayer.tree.tiles[Vector(row, col)]) {
                            BonsaiTileType.WOOD -> ImageVisual("Wood.png")
                            BonsaiTileType.FLOWER -> ImageVisual("Flower.png")
                            BonsaiTileType.FRUIT -> ImageVisual("Fruit.png")
                            BonsaiTileType.LEAF -> ImageVisual("Leaf.png")
                            else -> throw IllegalArgumentException("WTF HOW DID YOU DO THAT")
                        }
                        hexagon.onMouseClicked = {
                            removeTileList.add(Vector(row, col))
                            hexagon.opacity = 0.5
                            println(removeTileList)
                        }
                    }
                    hexagon.isVisible = true
                    removalTreeGrid[row, col] = hexagon
                }
            }
        }
    }

    private fun updateTree(player: Player) {
        initializeTreeGrid()
        player.tree.tiles.forEach { tilesPair ->
            val type = tilesPair.value
            val vector = tilesPair.key
            val hexagon = treeGrid[vector.q, vector.r]
            checkNotNull(hexagon).apply {
                when (type) {
                    BonsaiTileType.WOOD -> this.visual = ImageVisual("Wood.png")
                    BonsaiTileType.FLOWER -> this.visual = ImageVisual("Flower.png")
                    BonsaiTileType.FRUIT -> this.visual = ImageVisual("Fruit.png")
                    BonsaiTileType.LEAF -> this.visual = ImageVisual("Leaf.png")
                    BonsaiTileType.ANY -> throw IllegalArgumentException("WTF HOW DID YOU DO THAT")
                }
                treeGrid[vector.q, vector.r] = this
            }
        }
        treeGrid.isVisible = true
    }

    /**
     * Updates the whole gui for the new player
     */
    private fun updateCurrentPlayer() {
        currentPlayerPotLabel.text = currentPlayer.name
        currentPlayerPotLabel.visual = potsImages[currentPlayer.color]
        currentPlayerPotLabel.isVisible = true
        updateActivePlayerSupply(currentPlayer)
        updateActivePlayerCards(currentPlayer)
        println(currentPlayer.color)
    }

    /**
     * Updates the storage for the given player
     *
     * @param player Player to update the storage for
     */
    private fun updateActivePlayerCards(player: Player) {
        currentPlayerGrowthCard.clear()
        currentPlayerToolCard.clear()
        currentPlayerMeditatedStack.clear()
        for (toolCard in player.toolCardPile) {
            currentPlayerToolCard.add(zenCards[toolCard].apply { showFront() })
        }
        for (growthCard in player.growthCardPile) {
            currentPlayerGrowthCard.add(zenCards[growthCard].apply { showFront() })
        }
        for (meditatedCard in player.discardPile) {
            val posX = zenCards[meditatedCard].posX
            val posY = zenCards[meditatedCard].posY
            val height = zenCards[meditatedCard].height
            val width = zenCards[meditatedCard].width
            val front = zenCards[meditatedCard].frontVisual
            val back = zenCards[meditatedCard].backVisual
            val zenView = CardView(posX, posY, width, height, front, back)
            currentPlayerMeditatedStack.add(zenView)
        }
    }

    /**
     * Updates the player's supply of resources
     *
     * @param player Player to update the supply for
     */
    private fun updateActivePlayerSupply(player: Player) {
        // show current player supply
        woodLabel.text = "${player.growthLimit[0]}"
        leafLabel.text = "${player.growthLimit[1]}"
        flowerLabel.text = "${player.growthLimit[2]}"
        fruitLabel.text = "${player.growthLimit[3]}"
        anyLabel.text = "${player.growthLimit[4]}"
        placedTilesCount.text = "${currentPlayer.tree.tiles.size} placed tiles"
        currentPlayerSupply.clear()
        for (bonsaiTile in player.storage) {
            val posX = bonsaiTiles[bonsaiTile].posX
            val posY = bonsaiTiles[bonsaiTile].posY
            val size = bonsaiTiles[bonsaiTile].size
            val visual = bonsaiTiles[bonsaiTile].visual
            val hex = HexagonView(posX, posY, size, visual).apply {
                isDraggable = true
                onMouseEntered = { scale = 1.05 }
                onMouseExited = { scale = 1.0 }
            }
            currentPlayerSupply.add(hex)
        }
    }

    /**
     * Updates the opponents for the current player
     */
    private fun updateOpponents() {
        val opponentLabels = listOf(opponentPlayerPotLabel1, opponentPlayerPotLabel2, opponentPlayerPotLabel3)
        val opponentTree = listOf(opponent1Tree, opponent2Tree, opponent3Tree)
        val opponentCopy = gameState.players.clone().toMutableList()
        //remove active Player, because it is no opponent
        opponentCopy.remove(gameState.currentPlayer)
        opponentCopy.forEachIndexed { index, player ->
            opponentLabels[index].text = player.name
            opponentLabels[index].visual = potsImages[player.color]
            opponentLabels[index].isVisible = false
            opponentTree[index].isVisible = true
            opponentTree[index].visual = potsImages[player.color]
            opponentTree[index].text = player.name
        }
    }

    /**
     * Updates the center cards
     */
    private fun updateCenterCards() {
        val zenCardViews: MutableList<CardView> = mutableListOf(positionZen1, positionZen2, positionZen3, positionZen4)
        gameState.centerCards.forEachIndexed { index, card ->
            if (card != null) {
                zenCardViews[index].apply {
                    frontVisual = zenCards[card].frontVisual
                    isVisible = true
                    showFront()
                }
            } else {
                zenCardViews[index].isVisible = false
            }
        }
        // Update draw stack count
        drawStackText.text = gameState.drawStack.size.toString()
    }

    /**
     * Shows the available Goal Tiles
     */
    private fun updateAvailableGoalTiles() {
        val goalTileViews = listOf(goalTile1Label, goalTile2Label, goalTile3Label)
        //if no goal are available, don't need to update
        if (gameState.goalTiles.isEmpty()) {
            goalTileViews.forEach { goalTileLabel ->
                goalTileLabel.clear()
            }
            return
        }
        gameState.goalTiles.sortWith(compareBy({ it.type }, { -it.points }))
        var i = 1
        var lastSeenType = gameState.goalTiles[0].type
        goalTile1Label.clear()
        goalTile2Label.clear()
        goalTile3Label.clear()
        gameState.goalTiles.forEach { goalTile ->
            if (lastSeenType != goalTile.type) {
                lastSeenType = goalTile.type
                i++
            }
            when (i) {
                1 -> goalTile1Label.add(
                    goalTilesImages[goalTile].apply {
//                        onMouseEntered = { scale = 1.05 }
//                        onMouseExited = { scale = 1.0 }
                    })
                2 -> goalTile2Label.add(
                    goalTilesImages[goalTile].apply {
//                        onMouseEntered = { scale = 1.05 }
//                        onMouseExited = { scale = 1.0 }
                    })
                3 -> goalTile3Label.add(
                    goalTilesImages[goalTile].apply {
//                        onMouseEntered = { scale = 1.05 }
//                        onMouseExited = { scale = 1.0 }
                    })
            }
        }
    }

    private fun startTurnButtons() {
        // Enable action buttons
        cultivateButton.isVisible = true
        endTurnButton.isVisible = false
    }

    override fun refreshAfterGameStart() {
        // Initialize images for Zen cards, potsImages and Bonsai tiles
        generatedObjects()

        // Update the current player's name and corresponding pot image
        updateCurrentPlayer()

        // Set opponent names and visibility based on the number of players
        updateOpponents()

        // Update the Zen card positions in the center
        updateCenterCards()

        // Update goal tiles and display them
        updateAvailableGoalTiles()

        // Enable action buttons
        startTurnButtons()

        showMessage("Game started! First player: ${currentPlayer.name}")
    }

    override fun refreshAfterDiscardTiles(tiles: List<BonsaiTileType>) {
        updateActivePlayerSupply(currentPlayer)
        endTurnButton.isVisible = true
        showMessage("${currentPlayer.name} discarded tiles: ${tiles.joinToString()}")
    }

    override fun refreshAfterMeditate(cardIndex: Int) {
        updateCenterCards()
        updateActivePlayerSupply(currentPlayer)
        updateActivePlayerCards(currentPlayer)
        updateTree(currentPlayer)

        // Handle possible discarding state (if the player exceeds the max storage)
        if (rootService.playerActionService.needsToDiscardTiles()) {
            showMessage("${currentPlayer.name} needs to discard tiles!")
        }

        // Ensure action buttons are enabled
        cultivateButton.isVisible = false
        when (gameState.state) {
            States.CHOOSING_2ND_PLACE_TILES -> {
                overlayPaneForShowingTreeOrTiles.clear()
                overlayPaneForShowingTreeOrTiles.isVisible = true
                // Add components related to wishing a suit to the overlayPane
                overlayPaneForShowingTreeOrTiles.addAll(buildWishPane())
                endTurnButton.isVisible = true
            }

            States.USING_HELPER -> {
//                overlayPaneForShowingTreeOrTiles.clear()
                showMessage("PLACE CARDS")
                endTurnButton.isVisible = true
            }

            States.DISCARDING -> {
                showMessage("DISCARD TILES")
                // Clear the overlayPane from possible previous components and make it visible
                overlayPaneForShowingTreeOrTiles.clear()
                overlayPaneForShowingTreeOrTiles.isVisible = true

                removeButton.isVisible = false
                // Add components related to wishing a suit to the overlayPane
                overlayPaneForShowingTreeOrTiles.addAll(buildChooseDiscardPane())
            }

            else -> {
                endTurnButton.isVisible = true
            }
        }
        showMessage("${currentPlayer.name} meditated and took a card from position ${cardIndex + 1}.")
    }

    override fun refreshAfterGoalReached(goal: GoalTile) {
        currentGoal = goal
        showMessage("${currentPlayer.name} reached a goal: ${goal.type} (${goal.points} )")
        // Clear the overlayPane from possible previous components and make it visible
        overlayPaneForShowingTreeOrTiles.clear()
        overlayPaneForShowingTreeOrTiles.isVisible = true
        // Add components related to wishing a suit to the overlayPane
        overlayPaneForShowingTreeOrTiles.addAll(buildClaimOrRenouncePane())
    }

    override fun refreshAfterPlaceBonsaiTile(tile: BonsaiTile) {

        // Update the bonsai tree UI with the newly placed tile
        showMessage("${currentPlayer.name} placed a ${tile.type} tile.")
        // Update current players tree
        updateTree(currentPlayer)
        // Update player's resources after placing the tile
        updateActivePlayerSupply(currentPlayer)

        // Update placed tile count
        placedTilesCount.text = "${currentPlayer.tree.tiles.size - 1} placed tiles"

        showMessage("${currentPlayer.name} successfully placed a tile.")
    }

    override fun refreshAfterChooseTiles(vararg tiles: BonsaiTileType) {
        // Clear the overlayPane from possible previous components and make it visible
        overlayPaneForShowingTreeOrTiles.clear()
        overlayPaneForShowingTreeOrTiles.isVisible = true

        // Add components related to wishing a suit to the overlayPane
        overlayPaneForShowingTreeOrTiles.addAll(buildWishPane())
    }

    override fun refreshAfterTileChosen(tile: BonsaiTileType) {
        updateActivePlayerSupply(currentPlayer)
    }


    override fun refreshAfterTilesRemoved(tilePositions: Array<Vector>) {
        tilePositions.forEach {
            treeGrid[it.r, it.q] = HexagonView(
                visual = CompoundVisual(
                    ColorVisual(Color(0xc6ff6e)),
                    TextVisual(
                        text = "$it.r, $it.q", font = Font(10.0, Color(0x0f141f))
                    )
                ), size = 35
            ).apply {
                isVisible = true
                dropAcceptor = { dragEvent ->
                    when (dragEvent.draggedComponent) {
                        is HexagonView -> {
                            val type = checkNotNull(
                                bonsaiTileType[((dragEvent.draggedComponent as HexagonView).visual as ImageVisual).path]
                            )
                            rootService.treeService.canPlaceTile(type, Vector(it.q, it.r))
                        }

                        else -> {
                            showMessage("Can not place!")
                            false
                        }
                    }
                }
            }
            // Log removed tiles
            showMessage("${currentPlayer.name} removed tiles at positions: ${tilePositions.joinToString()}")
        }

        updateTree(currentPlayer)
        // Log removed tiles
        showMessage("${currentPlayer.name} removed tiles at positions: ${tilePositions.joinToString()}")

    }

    override fun refreshAfterCultivate() {
        cultivateButton.isVisible = false
        removeButton.isVisible = false
        endTurnButton.isVisible = true
        updateTree(currentPlayer)
    }

    override fun refreshAfterEndTurn() {
        // if end turn was called with too many cards call discard instead
        if (gameState.state == States.DISCARDING) {
            showMessage("DISCARD TILES")
            // Clear the overlayPane from possible previous components and make it visible
            overlayPaneForShowingTreeOrTiles.clear()
            overlayPaneForShowingTreeOrTiles.isVisible = true
            // Add components related to wishing a suit to the overlayPane
            overlayPaneForShowingTreeOrTiles.addAll(buildChooseDiscardPane())
            return
        }
        removeButton.isVisible = false
        updateCurrentPlayer()
        updateOpponents()
        updateTree(currentPlayer)
        startTurnButtons()
        showMessage("Turn ended. Next player: ${currentPlayer.name}")
    }

    override fun refreshAfterClaimGoal(claimed: Boolean, goal: GoalTile) {
        updateAvailableGoalTiles()
        showMessage("Player ${if (claimed) "accepted" else "declined"} the goal: ${goal.type}: ${goal.points}")
    }

    override fun refreshAfterLoadGame() {
        updateCurrentPlayer()
        updateTree(currentPlayer)
        updateAvailableGoalTiles()
        updateCenterCards()
        updateOpponents()
    }

    /**
     *   The buildWishPane method is used to create the components for the
     *   overlayPane when the player has to choose a tile
     *   It creates a LinearLayout with the [BonsaiTileType] to be wished and adds a
     *   label with the current player's name.
     *
     *   @return A list of components to be added to the overlayPane
     */
    private fun buildWishPane(): MutableList<ComponentView> {

        // Add a label to inform the player about the suits to be wished to the overlayPane
        val text = Label(
            posX = 0,
            posY = 1080 / 2 - 130,
            width = 1920,
            height = 40,
            alignment = Alignment.CENTER,
            font = Font(40, Color(0xE7EFF2), "JetBrains Mono ExtraBold")
        )
        // Create a LinearLayout to display the suits to be wished
        val pane = LinearLayout<HexagonView>(
            posX = 0, posY = 1080 / 2 + 55, width = 1920, spacing = 30, alignment = Alignment.CENTER
        )

        println(game.currentState.state)
        when (game.currentState.state) {
            States.CHOOSING_2ND_PLACE_TILES -> {
                text.text = "Choose one tile:"
                pane.addAll(HexagonView(posX = 0, posY = 0, size = 50, visual = ImageVisual("Wood.png")).apply {
                    onMouseEntered = { scale = 1.05 }
                    onMouseExited = { scale = 1.0 }
                    onMouseClicked = {
                        overlayPaneForShowingTreeOrTiles.isVisible = false
                        playerActionService.chooseTile(BonsaiTileType.WOOD)
                        // Hide the overlayPane
//                        if (gameState.state != States.DISCARDING) overlayPaneForShowingTreeOrTiles.isVisible = false
                    }
                }, HexagonView(posX = 0, posY = 0, size = 50, visual = ImageVisual("Leaf.png")).apply {
                    onMouseEntered = { scale = 1.05 }
                    onMouseExited = { scale = 1.0 }
                    onMouseClicked = {
                        overlayPaneForShowingTreeOrTiles.isVisible = false
                        playerActionService.chooseTile(BonsaiTileType.LEAF)
                        // Hide the overlayPane
//                        if (gameState.state != States.DISCARDING) overlayPaneForShowingTreeOrTiles.isVisible = false
                    }
                })
            }

            States.USING_MASTER -> {
                text.text = "Choose one Bonsai type to have:"
                pane.addAll(
                    HexagonView(posX = 0, posY = 0, size = 50, visual = ImageVisual("Wood.png")).apply {
                        onMouseEntered = { scale = 1.05 }
                        onMouseExited = { scale = 1.0 }
                        onMouseClicked = {
                            playerActionService.chooseTile(BonsaiTileType.WOOD)
                            // Hide the overlayPane
                            overlayPaneForShowingTreeOrTiles.isVisible = false
                        }
                    },
                    HexagonView(posX = 0, posY = 0, size = 50, visual = ImageVisual("Leaf.png")).apply {
                        onMouseEntered = { scale = 1.05 }
                        onMouseExited = { scale = 1.0 }
                        onMouseClicked = {
                            playerActionService.chooseTile(BonsaiTileType.LEAF)
                            // Hide the overlayPane
                            overlayPaneForShowingTreeOrTiles.isVisible = false
                        }
                    },
                    HexagonView(posX = 0, posY = 0, size = 50, visual = ImageVisual("Flower.png")).apply {
                        onMouseEntered = { scale = 1.05 }
                        onMouseExited = { scale = 1.0 }
                        onMouseClicked = {
                            playerActionService.chooseTile(BonsaiTileType.FLOWER)
                            // Hide the overlayPane
                            overlayPaneForShowingTreeOrTiles.isVisible = false
                        }
                    },
                    HexagonView(posX = 0, posY = 0, size = 50, visual = ImageVisual("Fruit.png")).apply {
                        onMouseEntered = { scale = 1.05 }
                        onMouseExited = { scale = 1.0 }
                        onMouseClicked = {
                            playerActionService.chooseTile(BonsaiTileType.FRUIT)
                            // Hide the overlayPane
                            overlayPaneForShowingTreeOrTiles.isVisible = false
                        }
                    })
            }

            else -> {
                endTurnButton.isVisible = true
            }
        }

        // Add a label with the current player's name to the overlayPane
        val name = Label(
            posX = 0,
            posY = 1080 / 2 - 180,
            width = 1920,
            height = 35,
            text = currentPlayer.name,
            alignment = Alignment.CENTER,
            font = Font(35, Color(0xE7EFF2), "JetBrains Mono ExtraBold")
        )
        // Return the components to be added to the overlayPane
        return mutableListOf(pane, text, name)
    }

    /**
     * build pane for choosing discarded tile from current player supply
     *
     * @return A list of components to be added to the overlayPane
     */
    private fun buildChooseDiscardPane(): MutableList<ComponentView> {
        // Add a label to inform the player about the suits to be wished to the overlayPane
        val text = Label(
            posX = 0,
            posY = 1080 / 2 - 130,
            width = 1920,
            height = 40,
            alignment = Alignment.CENTER,
            font = Font(40, Color(0xE7EFF2), "JetBrains Mono ExtraBold")
        )
        // Create a LinearLayout to display the suits to be wished
        val pane = LinearLayout<HexagonView>(
            posX = 0, posY = 1080 / 2 + 55, width = 1920, spacing = 30, alignment = Alignment.CENTER
        )
        if (game.currentState.state == States.DISCARDING) {
            val tileToDiscardList = mutableListOf<BonsaiTileType>()
            var needToDiscardSize = currentPlayer.storage.size - currentPlayer.maxCapacity
            text.text = "Choose $needToDiscardSize discarded tiles:"
            currentPlayer.storage.forEach { tile ->
                pane.add(
                    when (tile.type) {
                        BonsaiTileType.WOOD -> HexagonView(
                            posX = 0,
                            posY = 0,
                            size = 50,
                            visual = ImageVisual("Wood.png")
                        ).apply {
                            onMouseEntered = { scale = 1.05 }
                            onMouseExited = { scale = 1.0 }
                            onMouseClicked = {
                                tileToDiscardList.add(BonsaiTileType.WOOD)
                                needToDiscardSize--
                                pane.remove(this)
                                if (needToDiscardSize == 0) {
                                    playerActionService.discardTiles(tileToDiscardList)
                                    // Hide the overlayPane
                                    overlayPaneForShowingTreeOrTiles.isVisible = false
                                }
                            }
                        }

                        BonsaiTileType.FLOWER -> HexagonView(
                            posX = 0,
                            posY = 0,
                            size = 50,
                            visual = ImageVisual("Flower.png")
                        ).apply {
                            onMouseEntered = { scale = 1.05 }
                            onMouseExited = { scale = 1.0 }
                            onMouseClicked = {
                                tileToDiscardList.add(BonsaiTileType.FLOWER)
                                needToDiscardSize--
                                pane.remove(this)
                                if (needToDiscardSize == 0) {
                                    playerActionService.discardTiles(tileToDiscardList)
                                    // Hide the overlayPane
                                    overlayPaneForShowingTreeOrTiles.isVisible = false
                                }
                            }
                        }

                        BonsaiTileType.FRUIT -> HexagonView(
                            posX = 0,
                            posY = 0,
                            size = 50,
                            visual = ImageVisual("Fruit.png")
                        ).apply {
                            onMouseEntered = { scale = 1.05 }
                            onMouseExited = { scale = 1.0 }
                            onMouseClicked = {
                                tileToDiscardList.add(BonsaiTileType.FRUIT)
                                needToDiscardSize--
                                pane.remove(this)
                                if (needToDiscardSize == 0) {
                                    playerActionService.discardTiles(tileToDiscardList)
                                    // Hide the overlayPane
                                    overlayPaneForShowingTreeOrTiles.isVisible = false
                                }

                            }
                        }

                        BonsaiTileType.LEAF -> HexagonView(
                            posX = 0,
                            posY = 0,
                            size = 50,
                            visual = ImageVisual("Leaf.png")
                        ).apply {
                            onMouseEntered = { scale = 1.05 }
                            onMouseExited = { scale = 1.0 }
                            onMouseClicked = {
                                tileToDiscardList.add(BonsaiTileType.LEAF)
                                needToDiscardSize--
                                pane.remove(this)
                                if (needToDiscardSize == 0) {
                                    playerActionService.discardTiles(tileToDiscardList)
                                    // Hide the overlayPane
                                    overlayPaneForShowingTreeOrTiles.isVisible = false
                                }
                            }
                        }

                        else -> error("Unexpected tile type ${tile.type}")
                    })
            }
        }
        // Add a label with the current player's name to the overlayPane
        val name = Label(
            posX = 0,
            posY = 1080 / 2 - 180,
            width = 1920,
            height = 35,
            text = currentPlayer.name,
            alignment = Alignment.CENTER,
            font = Font(35, Color(0xE7EFF2), "JetBrains Mono ExtraBold")
        )
        // Return the components to be added to the overlayPane
        return mutableListOf(pane, text, name)
    }

    /**
     * build pane for claiming or renouncing goal
     *
     * @return A list of components to be added to the overlayPane
     */
    private fun buildClaimOrRenouncePane(): MutableList<ComponentView> {

        // Add a label to inform the player about the suits to be wished to the overlayPane
        val text = Label(
            posX = 0,
            posY = 1080 / 2 - 130,
            width = 1920,
            height = 40,
            text = "Claim or Renounce this goal:",
            alignment = Alignment.CENTER,
            font = Font(40, Color(0xE7EFF2), "JetBrains Mono ExtraBold")
        )

        val renounceGoalButton = Button(
            text = "Renounce",
            width = 280,
            height = 60,
            posX = 1920 / 2 - 280 / 2,
            posY = 1080 / 2 + 200,
            font = Font(30, Color(0x000000), "JetBrains Mono ExtraBold"),
            visual = ColorVisual(Color(0xFFFFFF))
        )
        // Create a LinearLayout to display the suits to be wished
        val pane = LinearLayout<CardView>(posX = 1920 / 2 - 75, posY = 1080 / 2, width = 120, height = 80)
        if (game.currentState.state == States.CLAIMING_GOALS) {
            val goal = currentGoal

            if (goal != null) {
                val posX = 1920 / 2
                val posY = 1080 / 2
                val width = goalTilesImages[goal].width * 2
                val height = goalTilesImages[goal].height * 2
                val visual = goalTilesImages[goal].visual
                val hex = CardView(posX, posY, width, height, visual)

                pane.add(hex.apply {
                    onMouseEntered = { scale = 1.05 }
                    onMouseExited = { scale = 1.0 }
                    onMouseClicked = {
                        playerActionService.claimGoal(goal)
                        // Hide the overlayPane
                        overlayPaneForShowingTreeOrTiles.isVisible = false
                    }
                })

                renounceGoalButton.apply {
                    onMouseEntered = { scale = 1.05 }
                    onMouseExited = { scale = 1.0 }
                    onMouseClicked = {
                        playerActionService.renounceGoal(goal)
                        // Hide the overlayPane
                        overlayPaneForShowingTreeOrTiles.isVisible = false
                    }
                }
            }
        }
        // Add a label with the current player's name to the overlayPane
        val name = Label(
            posX = 0,
            posY = 1080 / 2 - 180,
            width = 1920,
            height = 35,
            text = currentPlayer.name,
            alignment = Alignment.CENTER,
            font = Font(35, Color(0xE7EFF2), "JetBrains Mono ExtraBold")
        )
        // Return the components to be added to the overlayPane
        return mutableListOf(pane, text, name, renounceGoalButton)

    }

    /**
     * display some pick-up-line on console
     */
    override fun refreshAfterEndGame(scores: Array<IntArray>, winner: String) {
        println("HUUUUUUUUUUU")
    }

    /**
     * show Tree after removing tile for getting more places
     *
     * @return A list of components to be added to the overlayPane
     */
    private fun showRemoveTree(): List<ComponentView> {
        removeButton.isVisible = false
        // Add a label to inform the player about the suits to be wished to the overlayPane
        val text = Label(
            posX = 0,
            posY = 1080 / 2 - 330,
            width = 1920,
            height = 40,
            text = "Click tiles to add them to removal collection",
            alignment = Alignment.CENTER,
            font = Font(40, Color(0xE7EFF2), "JetBrains Mono ExtraBold")
        )
        val removalPane = Pane<ComponentView>(
            posX = 240,
            posY = 328,
            width = 2000,
            height = 2000,
        ).apply {
            isVisible = false
        }

        val removalCameraPane = CameraPane(
            posX = 1920 / 2 - 400,
            posY = 1080 / 2 - 200,
            width = 800,
            height = 350,
            target = removalPane
        ).apply {
            interactive = true
        }

        val removalPlayerPotLabel = Label(
            posX = 2000 / 2 - 150 - 100,
            posY = 2000 / 2 - 100 - 50,
            width = 300,
            height = 150,
            text = " ",
            alignment = Alignment.CENTER,
            font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold")
        )


        val removalTreeGrid = HexagonGrid<HexagonView>(
            240, 328, coordinateSystem = HexagonGrid.CoordinateSystem.AXIAL
        )
        initializeRemovalTreeGrid(removalTreeGrid)
        removalPane.add(removalTreeGrid)
        removalPane.add(removalPlayerPotLabel)
        removalPlayerPotLabel.text = currentPlayer.name
        removalPlayerPotLabel.visual = potsImages[currentPlayer.color]
        removalPlayerPotLabel.isVisible = true
        removalPane.isVisible = true

        // Add a label with the current player's name to the overlayPane
        val quit = Button(
            width = 150,
            height = 65,
            posX = 1920 / 2 - 700 / 2 + 670,
            posY = 1080 / 2 - 900 / 2 - 100,
            text = "Finished",
            alignment = Alignment.CENTER,
            font = Font(26, Color.WHITE, "JetBrains Mono ExtraBold"),
            visual = ColorVisual(Color(0x84b719))
        ).apply {
            onMouseClicked = {
                overlayPaneForShowingTreeOrTiles.isVisible = false
                treeService.removeTiles(removeTileList.toTypedArray())
                removeTileList.clear()
                overlayPane.clear()
            }
        }
        val abort = Button(
            width = 150,
            height = 65,
            posX = 1920 / 2 - 700 / 2 + 670,
            posY = 1080 / 2 - 900 / 2 - 25,
            text = "Reset",
            alignment = Alignment.CENTER,
            font = Font(26, Color.WHITE, "JetBrains Mono ExtraBold"),
            visual = ColorVisual(Color(0xf60100))
        ).apply {
            onMouseClicked = {
                    removeTileList.clear()
                    currentPlayer.tree.tiles.forEach{
                        removalTreeGrid[it.key.q,it.key.r]?.opacity = 1.0
                }
            }
        }
        return listOf(removalCameraPane, text, quit,abort)
    }

    /**
     * build Pane for showing opponent tree
     *
     * @return A list of components to be added to the overlayPane
     */
    private fun buildShowTreePane(player: Player): MutableList<ComponentView> {

        val backToGame = Button(
            text = "Back",
            width = 280,
            height = 60,
            posX = 1920 / 2 + 650,
            posY = 1080 / 2 ,
            font = Font(30, Color(0x000000), "JetBrains Mono ExtraBold"),
            visual = ColorVisual(Color(0xFFFFFF))
        ).apply {
            onMouseEntered = { scale = 1.05 }
            onMouseExited = { scale = 1.0 }
            onMouseClicked = {
                overlayPaneForShowingTreeOrTiles.isVisible = false
            }
        }

        // Add a label with the current player's name to the overlayPane
        val name = Label(
            posX = 0,
            posY = 1080 / 2 - 400,
            width = 1920,
            height = 35,
            text = "${player.name}s tree:",
            alignment = Alignment.CENTER,
            font = Font(35, Color(0xE7EFF2), "JetBrains Mono ExtraBold")
        )

        val newTreeGrid =  HexagonGrid<HexagonView>(
            1920 / 2 - 14 * 30, 1080 / 2 - 9 * 35, coordinateSystem = HexagonGrid.CoordinateSystem.AXIAL
        )

        for (row in -6..6) {
            for (col in -6..6) {/* Only add hexagons that would fit in a circle */
                if (row + col in -6..6) {
                    val newHexagon = HexagonView(
                        visual = CompoundVisual(
                            ColorVisual(Color(0xc6ff6e)),
                            TextVisual(
                                text = "$col, $row", font = Font(10.0, Color(0x0f141f))
                            )
                        ), size = 35
                    )
                    newTreeGrid[col, row] = newHexagon
                }
            }
        }

        val playerPotLabel = Label(
            posX = 1920 / 2 - 150,
            posY = 1080 / 2,
            width = 300,
            height = 150,
            text = player.name,
            alignment = Alignment.CENTER,
            font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
            visual = potsImages[player.color]
        ).apply { isVisible = true }

        player.tree.tiles.forEach { tilesPair ->
            val type = tilesPair.value
            val vector = tilesPair.key
            val hexagon = newTreeGrid[vector.q, vector.r]
            checkNotNull(hexagon).apply {
                when (type) {
                    BonsaiTileType.WOOD -> this.visual = ImageVisual("Wood.png")
                    BonsaiTileType.FLOWER -> this.visual = ImageVisual("Flower.png")
                    BonsaiTileType.FRUIT -> this.visual = ImageVisual("Fruit.png")
                    BonsaiTileType.LEAF -> this.visual = ImageVisual("Leaf.png")
                    BonsaiTileType.ANY -> throw IllegalArgumentException("WTF HOW DID YOU DO THAT")
                }
                newTreeGrid[vector.q, vector.r] = this
            }
        }
        newTreeGrid.isVisible = true

        return mutableListOf(name, backToGame, newTreeGrid, playerPotLabel)
    }
}


