package service.bot

import entity.*
import service.BotService
import service.PlayerActionService
import service.RootService
import service.TreeService


/**
 * A specialized BotService for a 1v1 competitive match.
 * Operates with a time limit and advanced strategy logic.
 */
class CompetitiveBotService(
    root: RootService, private val strategy: CompetitiveBotStrategy
) : BotService(root) {

    /**
     * Shorthand to retrieve the current [BonsaiGame] from the [RootService].
     * @throws IllegalStateException if no game is currently running.
     */
    private val game: BonsaiGame
        get() = checkNotNull(root.game) { "No Game is currently running" }

    /**
     * Shorthand to retrieve the current [GameState] from the [BonsaiGame].
     * @throws IllegalStateException if there is no active game state.
     */
    private val currentState: GameState
        get() = checkNotNull(game.currentState) { "There isn't a current game running" }

    /**
     * A reference to the [PlayerActionService], used for high-level game actions
     * like cultivate, meditate, discard, etc.
     */
    private val playerActionS: PlayerActionService
        get() = root.playerActionService

    /**
     * A reference to the [TreeService], which handles tile placement logic
     * (e.g., adjacency rules, hex coordinates, etc.).
     */
    private val treeS: TreeService
        get() = root.treeService

    private val botMemory = BotMemory()
    private val phaseManager = BotPhaseManager()
    private var currentPhase: BotPhase = BotPhase.EARLY

    /**
     * Called at the start or each time the bot's turn begins.
     * Must finish within 10 seconds, so we keep track of the start time.
     */
    override fun makeBotMove() {
        val startTime = System.currentTimeMillis()
        val maxIterations = 50 // Prevents infinite loop
        var iteration = 0

        require(currentState.currentPlayer.type in listOf(PlayerType.EASY_BOT, PlayerType.HARD_BOT)) {
            "Player must be a bot"
        }

        updateBotMemory()
        val newPhase = phaseManager.determineCurrentPhase(botMemory)
        if (phaseManager.shouldTransitionPhase(currentPhase, newPhase)) {
            handlePhaseTransition(newPhase)
        }

        while (System.currentTimeMillis() - startTime < 10_000 && currentState.currentPlayer.type in listOf(
                PlayerType.EASY_BOT, PlayerType.HARD_BOT
            ) && currentState.state != States.TURN_END && currentState.state != States.GAME_ENDED && iteration < maxIterations
        ) {

            val forcedHandled = strategy.handleForcedState(currentState.state, botMemory)
            if (forcedHandled) continue

            when (currentPhase) {
                BotPhase.EARLY -> handleEarlyGameTurn()
                BotPhase.MID -> handleMidGameTurn()
                BotPhase.LATE -> handleLateGameTurn()
            }

            iteration++
        }
    }


    /**
     * Updates [botMemory] by scanning the current game state:
     * - Count the deck size
     * - Check how many Growth, Tool, Master, Helper, Parchment are left
     * - Track opponent tile counts, goals, etc.
     */
    private fun updateBotMemory() {
        botMemory.deckSize = currentState.drawStack.size

        // Remove those cards that have been drawn
        botMemory.remainingCards.removeAll(currentState.centerCards.filterNotNull().toSet())
    }


    /**
     * Handles phase transition logic (e.g., call strategy onEnterMidGame).
     */
    private fun handlePhaseTransition(newPhase: BotPhase) {
        // Call exit logic if needed

        // Switch to new phase
        when (newPhase) {
            BotPhase.EARLY -> strategy.onEnterEarlyGame(botMemory)
            BotPhase.MID -> strategy.onEnterMidGame(botMemory)
            BotPhase.LATE -> strategy.onEnterLateGame(botMemory)
        }
        currentPhase = newPhase
    }

    /**
     * Called if the current phase is EARLY and the bot is free to choose.
     */
    private fun handleEarlyGameTurn() {

        val bestCardIndex = pickBestMeditateCard()

        // Decide based on best available option
        if (bestCardIndex != -1 && currentState.centerCards[bestCardIndex] is GrowthCard) {
            playerActionS.meditate(bestCardIndex)
        } else {
            playerActionS.cultivate()
        }
    }


    /**
     * Called if the current phase is MID and the bot is free to choose.
     */
    private fun handleMidGameTurn() {
        val nextAction = strategy.playMidGameTurn(botMemory)
        TODO()
    }

    /**
     * Called if the current phase is LATE and the bot is free to choose.
     */
    private fun handleLateGameTurn() {
        val nextAction = strategy.playLateGameTurn(botMemory)
        TODO()
    }

    /**
     * Choose best option from meditate cards
     */
    private fun pickBestMeditateCard(): Int {
        val availableCards = currentState.centerCards

        var bestIndex = -1
        var bestValue = Int.MIN_VALUE

        for (i in availableCards.indices) {
            val card = availableCards[i] ?: continue
            var score = 0

            when (card) {
                is GrowthCard -> {
                    score += when (currentPhase) {
                        BotPhase.EARLY -> 6
                        BotPhase.MID -> 2
                        BotPhase.LATE -> 0
                    }
                }

                is ParchmentCard -> {
                    score += when (currentPhase) {
                        BotPhase.EARLY -> -2
                        BotPhase.MID -> 2
                        BotPhase.LATE -> 6
                    }
                }

                is HelperCard -> score += when (currentPhase) {
                    BotPhase.EARLY -> 4
                    BotPhase.MID -> 4
                    BotPhase.LATE -> 2
                }

                is MasterCard -> score += when (currentPhase) {
                    BotPhase.EARLY -> 3
                    BotPhase.MID -> 4
                    BotPhase.LATE -> 4
                }

                is ToolCard -> {
                    score += when (currentPhase) {
                        BotPhase.EARLY -> if (currentState.currentPlayer.storage.size >= currentState.currentPlayer.maxCapacity - 1) 2 else 0
                        BotPhase.MID -> 4
                        BotPhase.LATE -> 0
                    }
                }
            }

            when (i) {
                1 -> {
                    val preferredTile = if (currentPhase == BotPhase.EARLY || botMemory.deckSize > 10) {
                        BonsaiTileType.WOOD
                    } else {
                        BonsaiTileType.LEAF
                    }
                    score += getTileBonusValue(preferredTile)
                }

                2, 3 -> {
                    val (bonus1, bonus2) = botMemory.nextTileBonuses[i]
                    if (bonus1 != null) score += getTileBonusValue(bonus1)
                    if (bonus2 != null) score += getTileBonusValue(bonus2)
                }
            }

            if (score > bestValue) {
                bestValue = score
                bestIndex = i
            }
        }

        return if (bestIndex != -1) bestIndex else availableCards.indexOfFirst { it != null }
    }

    /**
     * get tile bonus value base on bonsai tiles
     */
    private fun getTileBonusValue(tileType: BonsaiTileType): Int {
        return when (tileType) {
            BonsaiTileType.WOOD -> 3
            BonsaiTileType.LEAF -> 3
            BonsaiTileType.FLOWER -> if (currentPhase == BotPhase.MID) 4 else 2
            BonsaiTileType.FRUIT -> if (currentPhase == BotPhase.MID) 5 else 2
            else -> 1
        }
    }
}
