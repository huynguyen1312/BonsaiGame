package gui

import service.RootService
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.UIComponent
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.core.Color

/**
 * This scene shows the result after a game is over. ...
 */
class ResultMenuScene(private val rootService: RootService):
    MenuScene(1920,1080, background = ImageVisual("Ground.jpg")), Refreshable  {

    private val spacing = 130

    private val contentPane = Pane<UIComponent>(
        width = 900,
        height = 900,
        posX = 1920 / 2 - 800,
        posY = 1080 / 2 - 900 / 2,
        visual = ImageVisual("ScorePad.jpg")
    ).apply { opacity = 0.75 }

    private val showWinnerPane = Pane<UIComponent>(
        width = 600,
        height = 450,
        posX = 1920 / 2 + 200,
        posY = 1080 / 2 - 300,
        visual = ColorVisual(Color(0xB5A481))
    ).apply { opacity = 0.95 }

    private val titleWinner = Label(
        text = "Winner",
        width = 700,
        height = 100,
        posX = 1920 / 2 + 150,
        posY = 1080 / 2 - 300,
        alignment = Alignment.CENTER,
        font = Font(50, Color(0x000000), "JetBrains Mono ExtraBold")
    )

    private val player1Name = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 800 + 135,
        posY = 1080 / 2 - 900 / 2 + 40,
        text = "Name1",
        font = Font(30, Color(0x000000), "JetBrains Mono ExtraBold")
    )
    private val player1Leaf = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 800 + 135,
        posY = 1080 / 2 - 900 / 2 + 140,
        text = "Name1",
        font = Font(30, Color(0x000000), "JetBrains Mono ExtraBold")
    )
    private val player1Flower = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 800 + 135,
        posY = 1080 / 2 - 900 / 2 + 140+ spacing,
        text = "Name1",
        font = Font(30, Color(0x000000), "JetBrains Mono ExtraBold")
    )
    private val player1Fruit = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 800 + 135,
        posY = 1080 / 2 - 900 / 2 + 140 + 2* spacing,
        text = "Name1",
        font = Font(30, Color(0x000000), "JetBrains Mono ExtraBold")
    )
    private val player1Parchment = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 800 + 135,
        posY = 1080 / 2 - 900 / 2 + 140 + 3*spacing,
        text = "Name1",
        font = Font(30, Color(0x000000), "JetBrains Mono ExtraBold")
    )
    private val player1Goals = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 800 + 135,
        posY = 1080 / 2 - 900 / 2 + 140 + 4*spacing,
        text = "Name1",
        font = Font(30, Color(0x000000), "JetBrains Mono ExtraBold")
    )
    private val player1Points = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 800 + 135,
        posY = 1080 / 2 - 900 / 2 +140 + 5*spacing,
        text = "Name1",
        font = Font(30, Color(0x000000), "JetBrains Mono ExtraBold")
    )

    private val player2Name = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 800 + 325,
        posY = 1080 / 2 - 900 / 2 + 40,
        text = "Name2",
        font = Font(30, Color(0x000000), "JetBrains Mono ExtraBold")
    )
    private val player2Leaf = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 800 + 325,
        posY = 1080 / 2 - 900 / 2 + 140,
        text = "Name1",
        font = Font(30, Color(0x000000), "JetBrains Mono ExtraBold")
    )
    private val player2Flower = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 800 + 325,
        posY = 1080 / 2 - 900 / 2 + 140+ spacing,
        text = "Name1",
        font = Font(30, Color(0x000000), "JetBrains Mono ExtraBold")
    )
    private val player2Fruit = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 800 + 325,
        posY = 1080 / 2 - 900 / 2 + 140 + 2* spacing,
        text = "Name1",
        font = Font(30, Color(0x000000), "JetBrains Mono ExtraBold")
    )
    private val player2Parchment = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 800 + 325,
        posY = 1080 / 2 - 900 / 2 + 140 + 3*spacing,
        text = "Name1",
        font = Font(30, Color(0x000000), "JetBrains Mono ExtraBold")
    )
    private val player2Goals = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 800 + 325,
        posY = 1080 / 2 - 900 / 2 + 140 + 4*spacing,
        text = "Name1",
        font = Font(30, Color(0x000000), "JetBrains Mono ExtraBold")
    )
    private val player2Points = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 800 + 325,
        posY = 1080 / 2 - 900 / 2 +140 + 5*spacing,
        text = "Name1",
        font = Font(30, Color(0x000000), "JetBrains Mono ExtraBold")
    )

    private val player3Name = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 800 + 505,
        posY = 1080 / 2 - 900 / 2 + 40,
        text = "Name3",
        font = Font(30, Color(0x000000), "JetBrains Mono ExtraBold")
    )
    private val player3Leaf = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 800 + 505,
        posY = 1080 / 2 - 900 / 2 + 140,
        text = "Name1",
        font = Font(30, Color(0x000000), "JetBrains Mono ExtraBold")
    )
    private val player3Flower = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 800 + 505,
        posY = 1080 / 2 - 900 / 2 + 140+ spacing,
        text = "Name1",
        font = Font(30, Color(0x000000), "JetBrains Mono ExtraBold")
    )
    private val player3Fruit = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 800 + 505,
        posY = 1080 / 2 - 900 / 2 + 140 + 2* spacing,
        text = "Name1",
        font = Font(30, Color(0x000000), "JetBrains Mono ExtraBold")
    )
    private val player3Parchment = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 800 + 505,
        posY = 1080 / 2 - 900 / 2 + 140 + 3*spacing,
        text = "Name1",
        font = Font(30, Color(0x000000), "JetBrains Mono ExtraBold")
    )
    private val player3Goals = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 800 + 505,
        posY = 1080 / 2 - 900 / 2 + 140 + 4*spacing,
        text = "Name1",
        font = Font(30, Color(0x000000), "JetBrains Mono ExtraBold")
    )
    private val player3Points = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 800 + 505,
        posY = 1080 / 2 - 900 / 2 +140 + 5*spacing,
        text = "Name1",
        font = Font(30, Color(0x000000), "JetBrains Mono ExtraBold")
    )

    private val player4Name = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 800 + 700,
        posY = 1080 / 2 - 900 / 2 + 40,
        text = "Name4",
        font = Font(30, Color(0x000000), "JetBrains Mono ExtraBold")
    )
    private val player4Leaf = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 800 + 700,
        posY = 1080 / 2 - 900 / 2 + 140,
        text = "Name1",
        font = Font(30, Color(0x000000), "JetBrains Mono ExtraBold")
    )
    private val player4Flower = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 800 + 700,
        posY = 1080 / 2 - 900 / 2 + 140+ spacing,
        text = "Name1",
        font = Font(30, Color(0x000000), "JetBrains Mono ExtraBold")
    )
    private val player4Fruit = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 800 + 700,
        posY = 1080 / 2 - 900 / 2 + 140 + 2* spacing,
        text = "Name1",
        font = Font(30, Color(0x000000), "JetBrains Mono ExtraBold")
    )
    private val player4Parchment = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 800 + 700,
        posY = 1080 / 2 - 900 / 2 + 140 + 3*spacing,
        text = "Name1",
        font = Font(30, Color(0x000000), "JetBrains Mono ExtraBold")
    )
    private val player4Goals = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 800 + 700,
        posY = 1080 / 2 - 900 / 2 + 140 + 4*spacing,
        text = "Name1",
        font = Font(30, Color(0x000000), "JetBrains Mono ExtraBold")
    )
    private val player4Points = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 800 + 700,
        posY = 1080 / 2 - 900 / 2 +140 + 5*spacing,
        text = "Name1",
        font = Font(30, Color(0x000000), "JetBrains Mono ExtraBold")
    )

    val backToMenuButton = Button(
        text = "Menu",
        width = 200,
        height = 75,
        posX = 1920 / 2 + 400,
        posY = 1080 / 2 - 900 / 2 + 700,
        font = Font(26, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D)),
    )

    val exitButton = Button(
        text = "Quit",
        width = 200,
        height = 75,
        posX = 1920 / 2 + 400,
        posY = 900,
        font = Font(26, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D))
    )


    init {
        addComponents(
            contentPane,
            player1Name,
            player2Name,
            player3Name,
            player4Name,
            showWinnerPane,
            titleWinner,
            backToMenuButton,
            exitButton,
            player1Leaf,
            player1Fruit,
            player1Flower,
            player1Points,
            player1Goals,
            player1Parchment,
            player2Leaf,
            player2Fruit,
            player2Flower,
            player2Points,
            player2Goals,
            player2Parchment,
            player3Leaf,
            player3Fruit,
            player3Flower,
            player3Points,
            player3Goals,
            player3Parchment,
            player4Leaf,
            player4Fruit,
            player4Flower,
            player4Points,
            player4Goals,
            player4Parchment,
        )
    }

    override fun refreshAfterEndGame(scores: Array<IntArray>, winner: String) {
        // Ensure an active game exists
        val game = rootService.game ?: return
        val gameState = game.currentState

        // Get player names
        val playerNames = gameState.players.map { it.name }

        // Update labels with player names
        val playerLabels = listOf(player1Name, player2Name, player3Name, player4Name)
        playerNames.forEachIndexed { index, name ->
            playerLabels[index].text = "$name:"
            playerPoints(index)[0].text = "${scores[index][0]}"
            playerPoints(index)[1].text = "${scores[index][1]}"
            playerPoints(index)[2].text = "${scores[index][2]}"
            playerPoints(index)[3].text = "${scores[index][3]}"
            playerPoints(index)[4].text = "${scores[index][4]}"
            playerPoints(index)[5].text = "${scores[index].sum()}"
            playerLabels[index].isVisible = true
        }

        // Hide unused player labels if fewer than 4 players
        playerLabels.drop(playerNames.size).forEach { it.isVisible = false }
        listOf(playerPoints(0),playerPoints(1),playerPoints(2),playerPoints(3)).drop(playerNames.size).forEach{
            it.forEach { label ->
                label.isVisible = false
            }
        }

        // Update winner display
        titleWinner.text = "Winner: $winner 🎉"

        // Show the result scene
        println("Game ended! Winner: $winner with ${scores.maxOf { it.sum() }} points")
    }

    private fun playerPoints(i: Int): List<Label>{
        return when(i){
            0 -> listOf(player1Leaf,
                        player1Fruit,
                        player1Flower,
                        player1Parchment,
                        player1Goals,
                        player1Points
                    )
            1 -> listOf(player2Leaf,
                        player2Fruit,
                        player2Flower,
                        player2Parchment,
                        player2Goals,
                        player2Points
            )
            2 -> listOf(player3Leaf,
                        player3Fruit,
                        player3Flower,
                        player3Parchment,
                        player3Goals,
                        player3Points
            )
            3 -> listOf(player4Leaf,
                        player4Fruit,
                        player4Flower,
                        player4Parchment,
                        player4Goals,
                        player4Points
            )
            else -> listOf()
        }
    }

}