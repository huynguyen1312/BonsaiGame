package gui

import entity.Colors
import entity.PlayerType
import service.RootService
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.core.Color

/**
 * The scene that is reached when wanting to join a network game.
 */
class LobbyScene(private val rootService: RootService):
    MenuScene(1920,1080, background = ImageVisual("Ground.jpg")), Refreshable {

    private val playerRolesListOnline = mutableListOf<PlayerType>()
    private val playerColorsListOnline = mutableListOf<Colors>()

    private val contentPane = Pane<UIComponent>(
        width = 800,
        height = 900,
        posX = 1920 / 2 - 800 / 2,
        posY = 1080 / 2 - 900 / 2,
        visual = ColorVisual(Color(255, 255, 255, 100))
    )

    private val titleLabel = Label(
        text = "Lobby",
        width = 700,
        height = 100,
        posX = 50,
        posY = 30,
        alignment = Alignment.CENTER,
        font = Font(40, Color(0x000000), "JetBrains Mono ExtraBold")
    )

    private val playerDefaultInput = TextField(
        prompt = "Name",
        width = 200,
        height = 75,
        posX = 50,
        posY = 150,
        font = Font(25, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D)),
    )
    private val playerRoleInput: ComboBox<PlayerType> = ComboBox<PlayerType>(
        width = 200,
        height = 75,
        posX = 300,
        posY = 150,
        prompt = "select Role",
        font = Font(25, Color(0x000000), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0xFFFFFF))
    ).apply {
        items = PlayerType.entries.filterNot { it == PlayerType.LOCAL }
        selectedItem = PlayerType.ONLINE
        formatFunction = {
            when(it) {
                PlayerType.EASY_BOT -> "Easy Bot"
                PlayerType.HARD_BOT -> "Hard Bot"
                PlayerType.ONLINE -> "Online"
                else -> error("No valid player type!")
            }
        }
        onItemSelected = { newValue ->
            when (newValue) {
                PlayerType.EASY_BOT -> playerRolesListOnline.add(newValue)
                PlayerType.HARD_BOT -> playerRolesListOnline.add(newValue)
                PlayerType.ONLINE -> playerRolesListOnline.add(newValue)
                else -> error("No valid player type!")
            }
        }
        opacity = 0.5
    }
    private val playerColorInput: ComboBox<Colors> = ComboBox<Colors>(
        width = 200,
        height = 75,
        posX = 550,
        posY = 150,
        prompt = "select Color",
        font = Font(25, Color(0x000000), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0xFFFFFF))
    ).apply {
        items = Colors.entries
        selectedItem = Colors.RED
        formatFunction = {
            when (it) {
                Colors.RED -> "Red"
                Colors.BLUE -> "Blue"
                Colors.BLACK -> "Black"
                Colors.PURPLE -> "Purple"
            }
        }
        onItemSelected = { newValue ->
            when (newValue) {
                Colors.RED -> playerColorsListOnline.add(newValue)
                Colors.BLUE-> playerColorsListOnline.add(newValue)
                Colors.BLACK -> playerColorsListOnline.add(newValue)
                Colors.PURPLE -> playerColorsListOnline.add(newValue)
                null -> error("no valid color selected")
            }
        }
        opacity = 0.5
    }

    private var randomizedOrder = false

    private val randomizeOrderButton = Button(
        text = "RANDOMIZE ORDER",
        width = 280,
        height = 60,
        posX = 100,
        posY = 700,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D))
    ).apply {
        onMouseClicked = {
            if(text == "RANDOMIZE ORDER" ){
                randomizedOrder = true
                text = "NOT RANDOMIZE ORDER"
            }
            else{
                randomizedOrder = false
                text = "RANDOMIZE ORDER"
            }
        }
    }

    val backToLastSceneButton = Button(
        text = "BACK",
        width = 280,
        height = 60,
        posX = 100,
        posY = 790,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D))
    )

    val startButton = Button(
        text = "START",
        width = 280,
        height = 60,
        posX = 420,
        posY = 790,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D))
    )
    private val setOrderButton = Button(
        text = "SET ORDER",
        width = 280,
        height = 60,
        posX = 420,
        posY = 700,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D))
    ).apply {
        if (randomizedOrder) { isDisabled = true }
        else { isDisabled = false }
    }

    // Initialize the scene by setting the background color and adding all components to the content pane
    init {
        contentPane.addAll(
            titleLabel,
            playerDefaultInput,
            startButton,
            backToLastSceneButton,
            randomizeOrderButton,
            setOrderButton,
            playerRoleInput,
            playerColorInput)
        addComponents(contentPane)
    }
}