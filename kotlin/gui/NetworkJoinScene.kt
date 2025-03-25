package gui

import entity.PlayerType
import service.ConnectionState
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
 * This scene is triggered from the [ModeScene] when the user wants to join an online game. It leads to ...
 */
class NetworkJoinScene(private val rootService: RootService):
    MenuScene(1920,1080, background = ImageVisual("Ground.jpg")), Refreshable {

    private val contentPane = Pane<UIComponent>(
        width = 700,
        height = 900,
        posX = 1920 / 2 - 700 / 2,
        posY = 1080 / 2 - 900 / 2,
        visual = ColorVisual(Color(0x0C2027))
    ).apply { opacity = 0.65 }

    private val titleLabel = Label(
        text = "Network Game",
        width = 700,
        height = 100,
        posX = 1920 / 2 - 700 / 2,
        posY = 1080 / 2 - 900 / 2,
        alignment = Alignment.CENTER,
        font = Font(40, Color(0xFFFFFF), "JetBrains Mono ExtraBold")
    )

    private val playerLabel = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 700 / 2,
        posY = 1080 / 2 - 900 / 2 + 150,
        text = "Your Name: ",
        font = Font(25, Color(0xFFFFFF), "JetBrains Mono ExtraBold")
    )

    val playerInput: TextField = TextField(
        width = 450,
        height = 50,
        posX = 1920 / 2 - 700 / 2 + 200,
        posY = 1080 / 2 - 900 / 2 + 142,
        font = Font(25, Color(0x000000), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0xFFFFFF)),
    ).apply {
        onKeyPressed = {
            startButton.isDisabled = buttonsDisabled()
        }
        opacity = 0.5
    }

    private val sessionLabel = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 700 / 2,
        posY = 1080 / 2 - 900 / 2 + 250,
        text = "Session ID: ",
        font = Font(25, Color(0xFFFFFF), "JetBrains Mono ExtraBold")
    )

    private val sessionInput: TextField = TextField(
        width = 450,
        height = 50,
        posX = 1920 / 2 - 700 / 2 + 200,
        posY = 1080 / 2 - 900 / 2 + 242,
        text = "",
        font = Font(25, Color(0x000000), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0xFFFFFF))
    ).apply {
        onKeyPressed = {
            startButton.isDisabled = buttonsDisabled()
        }
        opacity = 0.5
    }

    private val botModeLabel = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 700 / 2,
        posY = 1080 / 2 - 900 / 2 + 450,
        text = "Bot Mode: ",
        font = Font(25, Color(0xFFFFFF), "JetBrains Mono ExtraBold")
    )

    private val botModeInput: ComboBox<PlayerType> = ComboBox<PlayerType>(
        width = 450,
        height = 50,
        posX = 1920 / 2 - 700 / 2 + 200,
        posY = 1080 / 2 - 900 / 2 + 442,
        prompt = "select item",
        font = Font(25, Color(0x000000), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0xFFFFFF))
    ).apply {
        items = PlayerType.entries.filterNot { it == PlayerType.ONLINE }
        selectedItem = PlayerType.LOCAL
        formatFunction = {
            when(it) {
                PlayerType.EASY_BOT -> "Easy Bot"
                PlayerType.HARD_BOT -> "Hard Bot"
                PlayerType.LOCAL -> "Human"
                else -> "ERROR"
            }
        }
        opacity = 0.5
    }

    val backButton = Button(
        width = 200,
        height = 100,
        posX = 1920 / 2 - 700 / 2 + 80,
        posY = 1080 / 2 - 900 / 2 + 750,
        text = "Back",
        font = Font(26, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D)),
    )

    private val startButton = Button(
        width = 200,
        height = 100,
        posX = 1920 / 2 - 700 / 2 + 425,
        posY = 1080 / 2 - 900 / 2 + 750,
        text = "Join",
        font = Font(26, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D)),
    ).apply {
        onMouseClicked = {
            rootService.networkService.joinGame(sessionInput.text, playerInput.text)
        }
    }

    private val cancelButton = Button(
        width = 200,
        height = 100,
        posX = 1920 / 2 - 700 / 2 + 425,
        posY = 1080 / 2 - 900 / 2 + 750,
        text = "Cancel",
        font = Font(26, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D))
    ).apply {
        isVisible = false
        onMouseClicked = {
            rootService.networkService.disconnect()
        }
    }

    private val networkStatusArea = TextArea(
        width = 350,
        height = 50,
        posX = 1920 / 2 - 700 / 2 + 175,
        posY = 1080 / 2 - 900 / 2 + 642,
        text = "",
        font = Font(25, Color(0x000000), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0xFFFFFF))
    )
        .apply {
        isDisabled = true
        // only visible when the text is changed to something non-empty
        isVisible = false
        onTextChanged = { new ->
            isVisible = new.isNotEmpty()
        }
    }

    init {
        addComponents(
            contentPane,
            titleLabel,
            playerLabel, playerInput,
            sessionLabel, sessionInput,
            botModeLabel,
            botModeInput,
            startButton,
            cancelButton,
            backButton,
            networkStatusArea)
    }

    override fun refreshConnectionState(state: ConnectionState) {
        networkStatusArea.text = state.toUIText()
        val disconnected = state == ConnectionState.DISCONNECTED
        cancelButton.isVisible = !disconnected
        startButton.isVisible = disconnected
        backButton.isDisabled = !disconnected
    }


    private fun buttonsDisabled() = playerInput.text.isBlank() || sessionInput.text.isBlank()
}
