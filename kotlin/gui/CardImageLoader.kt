package gui

import entity.*
import tools.aqua.bgw.components.gamecomponentviews.CardView
import tools.aqua.bgw.components.gamecomponentviews.HexagonView
import tools.aqua.bgw.util.BidirectionalMap
import tools.aqua.bgw.visual.ImageVisual

private const val ZENCARD = "TotalDeck.jpg"
private const val ZEN_WIDTH = 372
private const val ZEN_HEIGHT = 521

/**
 * A class responsible for loading images from the resource folder into the game's GUI.
 * This class provides image generation for game components such as cards, tiles, and goal tiles.
 */
class CardImageLoader {

    /**
     * Retrieves an image visual based on sprite sheet coordinates.
     *
     * @param x The x-coordinate in the sprite sheet.
     * @param y The y-coordinate in the sprite sheet.
     * @return An [ImageVisual] representing the requested image section.
     */
    fun getImageByCoordinates(x:Int, y:Int) = ImageVisual(
        ZENCARD,
        ZEN_WIDTH,
        ZEN_HEIGHT,
        x * ZEN_WIDTH,
        y * ZEN_HEIGHT,
    )

    /**
     * Retrieves the summary card image.
     *
     * @return An [ImageVisual] representing the summary card.
     */
    val summaryCard: ImageVisual get() = getImageByCoordinates(8, 3)

    /**
     * Retrieves the back image of a Zen card.
     *
     * @return An [ImageVisual] representing the back of a Zen card.
     */
    val backZenImage: ImageVisual get() = getImageByCoordinates(0, 0)

    /**
     * Generates and populates a mapping of goal tiles to their corresponding card views.
     *
     * @param goalTiles A bidirectional map that will be populated with goal tile mappings.
     */
    fun goalTileImageGenerator(goalTiles: BidirectionalMap<GoalTile, CardView> = BidirectionalMap()){
        goalTiles.clear()

        //Wood goals
        goalTiles[GoalTile(points = 5, threshold = 8, type = GoalTileType.WOOD)] =
            CardView(posX = 0, posY = 0, width = 120, height = 80, front = ImageVisual("WoodGoal1.jpg"))
        goalTiles[GoalTile(points = 10, threshold = 10, type = GoalTileType.WOOD)] =
            CardView(posX = 0, posY = 0, width = 140, height = 80, front = ImageVisual("WoodGoal2.jpg"))
        goalTiles[GoalTile(points = 15, threshold = 12, type = GoalTileType.WOOD)] =
            CardView(posX = 0, posY = 0, width = 179, height = 80, front = ImageVisual("WoodGoal3.jpg"))

        //Flower goals
        goalTiles[GoalTile(points = 8, threshold = 3, type = GoalTileType.FLOWER)] =
            CardView(posX = 0, posY = 0, width = 120, height = 80, front = ImageVisual("FlowerGoal1.jpg"))
        goalTiles[GoalTile(points = 12, threshold = 4, type = GoalTileType.FLOWER)] =
            CardView(posX = 0, posY = 0, width = 140, height = 80, front = ImageVisual("FlowerGoal2.jpg"))
        goalTiles[GoalTile(points = 16, threshold = 5, type = GoalTileType.FLOWER)] =
            CardView(posX = 0, posY = 0, width = 179, height = 80, front = ImageVisual("FlowerGoal3.jpg"))

        //Leaf goals
        goalTiles[GoalTile(points = 6, threshold = 5, type = GoalTileType.LEAF)] =
            CardView(posX = 0, posY = 0, width = 120, height = 80, front = ImageVisual("LeafGoal1.jpg"))
        goalTiles[GoalTile(points = 9, threshold = 7, type = GoalTileType.LEAF)] =
            CardView(posX = 0, posY = 0, width = 140, height = 80, front = ImageVisual("LeafGoal2.jpg"))
        goalTiles[GoalTile(points = 12, threshold = 9, type = GoalTileType.LEAF)] =
            CardView(posX = 0, posY = 0, width = 179, height = 80, front = ImageVisual("LeafGoal3.jpg"))

        //Fruit goals
        goalTiles[GoalTile(points = 9, threshold = 3, type = GoalTileType.FRUIT)] =
            CardView(posX = 0, posY = 0, width = 120, height = 80, front = ImageVisual("FruitGoal1.jpg"))
        goalTiles[GoalTile(points = 11, threshold = 4, type = GoalTileType.FRUIT)] =
            CardView(posX = 0, posY = 0, width = 140, height = 80, front = ImageVisual("FruitGoal2.jpg"))
        goalTiles[GoalTile(points = 13, threshold = 5, type = GoalTileType.FRUIT)] =
            CardView(posX = 0, posY = 0, width = 179, height = 80, front = ImageVisual("FruitGoal3.jpg"))

        //Position goals
        goalTiles[GoalTile(points = 7, threshold = 7, type = GoalTileType.POSITION)] =
            CardView(posX = 0, posY = 0, width = 120, height = 80, front = ImageVisual("PositionGoal1.jpg"))
        goalTiles[GoalTile(points = 10, threshold = 10, type = GoalTileType.POSITION)] =
            CardView(posX = 0, posY = 0, width = 140, height = 80, front = ImageVisual("PositionGoal2.jpg"))
        goalTiles[GoalTile(points = 14, threshold = 14, type = GoalTileType.POSITION)] =
            CardView(posX = 0, posY = 0, width = 179, height = 80, front = ImageVisual("PositionGoal3.jpg"))
    }

    /**
     * Generates and populates a mapping of player pot images.
     *
     * @param pots A bidirectional map that will be populated with player pot image mappings.
     */
    fun potImageGenerator(pots: BidirectionalMap<Colors, ImageVisual> = BidirectionalMap()){
        pots.clear()

        pots[Colors.RED] = ImageVisual("Pot3.png")
        pots[Colors.BLUE] = ImageVisual("Pot2.png")
        pots[Colors.BLACK] = ImageVisual("Pot1.png")
        pots[Colors.PURPLE] = ImageVisual("Pot4.png")
    }

    /**
     * Generates and populates a mapping of Bonsai tile images.
     *
     * @param bonsaiTiles A bidirectional map that will be populated with Bonsai tile image mappings.
     */
    fun bonsaiTilesImageGenerator (bonsaiTiles: BidirectionalMap<BonsaiTile, HexagonView> = BidirectionalMap()) {
        bonsaiTiles.clear()

        bonsaiTiles[BonsaiTile(BonsaiTileType.WOOD)] =
            HexagonView(posX = 0, posY = 0, size = 35, visual = ImageVisual("Wood.png"))
        bonsaiTiles[BonsaiTile(BonsaiTileType.LEAF)] =
            HexagonView(posX = 0, posY = 0, size = 35, visual = ImageVisual("Leaf.png"))
        bonsaiTiles[BonsaiTile(BonsaiTileType.FLOWER)] =
            HexagonView(posX = 0, posY = 0, size = 35, visual = ImageVisual("Flower.png"))
        bonsaiTiles[BonsaiTile(BonsaiTileType.FRUIT)] =
            HexagonView(posX = 0, posY = 0, size = 35, visual = ImageVisual("Fruit.png"))
    }

    /**
     * Generates and populates a full Zen card stack mapping.
     *
     * @param zenCards A bidirectional map that will be populated with Zen card mappings.
     */
    fun createdFullZenCardsStack(zenCards: BidirectionalMap<ZenCard, CardView> = BidirectionalMap()) {
        zenCards.clear()

        //master cards
        zenCards[MasterCard(arrayOf(BonsaiTileType.WOOD, BonsaiTileType.WOOD), 21)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(5,1), back = backZenImage)
        zenCards[MasterCard(arrayOf(BonsaiTileType.LEAF,BonsaiTileType.LEAF),22)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(6,1), back = backZenImage)
        zenCards[MasterCard(arrayOf(BonsaiTileType.WOOD,BonsaiTileType.LEAF),23)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(0,2), back = backZenImage)
        zenCards[MasterCard(arrayOf(BonsaiTileType.ANY),24)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(4,1), back = backZenImage)
        zenCards[MasterCard(arrayOf(BonsaiTileType.ANY),25)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(4,1), back = backZenImage)
        zenCards[MasterCard(arrayOf(BonsaiTileType.LEAF,BonsaiTileType.LEAF),26)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(6,1), back = backZenImage)
        zenCards[MasterCard(arrayOf(BonsaiTileType.LEAF,BonsaiTileType.FRUIT),27)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(7,1), back = backZenImage)
        zenCards[MasterCard(arrayOf(BonsaiTileType.ANY),28)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(1,2), back = backZenImage)
        zenCards[MasterCard(arrayOf(BonsaiTileType.WOOD,BonsaiTileType.LEAF),29)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(2,2), back = backZenImage)
        zenCards[MasterCard(arrayOf(BonsaiTileType.WOOD,BonsaiTileType.LEAF),30)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(2,2), back = backZenImage)
        zenCards[MasterCard(arrayOf(BonsaiTileType.WOOD,BonsaiTileType.LEAF,BonsaiTileType.FLOWER), 31)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(4,2), back = backZenImage)
        zenCards[MasterCard(arrayOf(BonsaiTileType.WOOD,BonsaiTileType.LEAF,BonsaiTileType.FRUIT),32)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(3,2), back = backZenImage)
        zenCards[MasterCard(arrayOf(BonsaiTileType.LEAF,BonsaiTileType.FLOWER,BonsaiTileType.FLOWER),33)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(5,2), back = backZenImage)

        //tool card
        zenCards[ToolCard(2,41)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(1,0), back = backZenImage)
        zenCards[ToolCard(2,42)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(1,0), back = backZenImage)
        zenCards[ToolCard(2,43)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(1,0), back = backZenImage)
        zenCards[ToolCard(2,44)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(2,0), back = backZenImage)
        zenCards[ToolCard(2,45)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(2,0), back = backZenImage)
        zenCards[ToolCard(2,46)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(3,0), back = backZenImage)

        //helper cards
        zenCards[HelperCard(arrayOf(BonsaiTileType.ANY,BonsaiTileType.WOOD),14)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(8,2), back = backZenImage)
        zenCards[HelperCard(arrayOf(BonsaiTileType.ANY,BonsaiTileType.WOOD),15)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(8,2), back = backZenImage)
        zenCards[HelperCard(arrayOf(BonsaiTileType.ANY,BonsaiTileType.WOOD),16)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(8,2), back = backZenImage)
        zenCards[HelperCard(arrayOf(BonsaiTileType.ANY,BonsaiTileType.LEAF),17)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(0,3), back = backZenImage)
        zenCards[HelperCard(arrayOf(BonsaiTileType.ANY,BonsaiTileType.LEAF),18)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(0,3), back = backZenImage)
        zenCards[HelperCard(arrayOf(BonsaiTileType.ANY,BonsaiTileType.FLOWER),19)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(6,2), back = backZenImage)
        zenCards[HelperCard(arrayOf(BonsaiTileType.ANY,BonsaiTileType.FRUIT),20)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(7,2), back = backZenImage)

        //growth cards
        zenCards[GrowthCard(BonsaiTileType.WOOD,0)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(4,0), back = backZenImage)
        zenCards[GrowthCard(BonsaiTileType.WOOD,1)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(4,0), back = backZenImage)
        zenCards[GrowthCard(BonsaiTileType.LEAF,2)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(7,0), back = backZenImage)
        zenCards[GrowthCard(BonsaiTileType.LEAF,3)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(7,0), back = backZenImage)
        zenCards[GrowthCard(BonsaiTileType.FLOWER,4)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(0,1), back = backZenImage)
        zenCards[GrowthCard(BonsaiTileType.FLOWER,5)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(0,1), back = backZenImage)
        zenCards[GrowthCard(BonsaiTileType.FRUIT,6)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(2,1), back = backZenImage)
        zenCards[GrowthCard(BonsaiTileType.FRUIT,7)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(2,1), back = backZenImage)
        zenCards[GrowthCard(BonsaiTileType.WOOD,8)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(5,0), back = backZenImage)
        zenCards[GrowthCard(BonsaiTileType.LEAF,9)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(8,0), back = backZenImage)
        zenCards[GrowthCard(BonsaiTileType.LEAF,10)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(8,0), back = backZenImage)
        zenCards[GrowthCard(BonsaiTileType.FLOWER,11)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(1,1), back = backZenImage)
        zenCards[GrowthCard(BonsaiTileType.WOOD,12)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(6,0), back = backZenImage)
        zenCards[GrowthCard(BonsaiTileType.FRUIT,13)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(3,1), back = backZenImage)

        //parchment cards
        zenCards[ParchmentCard(2,ParchmentCardType.FLOWER,37)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(1,3), back = backZenImage)
        zenCards[ParchmentCard(2,ParchmentCardType.FRUIT,38)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(2,3), back = backZenImage)
        zenCards[ParchmentCard(1,ParchmentCardType.WOOD,40)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(3,3), back = backZenImage)
        zenCards[ParchmentCard(1,ParchmentCardType.LEAF,39)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(4,3), back = backZenImage)
        zenCards[ParchmentCard(2,ParchmentCardType.HELPER,36)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(5,3), back = backZenImage)
        zenCards[ParchmentCard(2,ParchmentCardType.MASTER,34)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(6,3), back = backZenImage)
        zenCards[ParchmentCard(2,ParchmentCardType.GROWTH,35)] =
            CardView(posX = 0, posY = 0, width = 131, height = 200,
                front = getImageByCoordinates(7,3), back = backZenImage)
    }
}
