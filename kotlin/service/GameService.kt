package service

import entity.*

/**
 * The [GameService] provides methods to initialize the game, handle player turns, enforce game-ending conditions
 * based on the current state of the game and so on.
 */
class GameService(
    private val root: RootService
) : AbstractRefreshingService() {

    /**
     * Starts a new game session based on the given [gameConfig].
     *
     * # Functionalities:
     * 1. Game configure base on [gameConfig].
     * 2. Randomly or manual (by players) selects three colors.
     * 3. Prepares the Zen card deck based on the number of players.
     * 4. Assigns each player: A Pot tile of their chosen color.
     * 5. Determines the first player (the oldest player starts or random player) and distributes starting bonsai
     * tiles.
     *
     * # Preconditions:
     * - 'gameConfig' must be a valid, non-null instance.
     * - The number of players must be between 2 and 4 (inclusive).
     * - Each player must have a unique identifier and be assigned a player type.
     * - No game should already be in progress.
     *
     * # Post-conditions:
     * - The game board, bonsai tiles, and goal tiles must be available after setup.
     * - The deck of Zen cards is shuffled and placed on the board.
     * - Each player is assigned: A Pot tile of their chosen color.
     * - The first player is determined based on the predefined rule (oldest player starts or random player).
     * - The game is now in a running state, allowing turns to proceed.
     *
     * @param gameConfig The configuration object containing game settings.
     * @throws IllegalStateException if a game is already running.
     * @see GameConfig
     */
    fun startGame(gameConfig: GameConfig) {
        // Create the 47 Zen cards
        // Growth Cards (13 Cards)
        val card1 = GrowthCard(BonsaiTileType.WOOD, 0)
        val card2 = GrowthCard(BonsaiTileType.WOOD, 1)
        val card3 = GrowthCard(BonsaiTileType.LEAF, 2)
        val card4 = GrowthCard(BonsaiTileType.LEAF, 3)
        val card5 = GrowthCard(BonsaiTileType.FLOWER, 4)
        val card6 = GrowthCard(BonsaiTileType.FLOWER, 5)
        val card7 = GrowthCard(BonsaiTileType.FRUIT, 6)
        val card8 = GrowthCard(BonsaiTileType.FRUIT, 7)
        val card9 = GrowthCard(BonsaiTileType.WOOD, 8) // minPlayer 3
        val card10 = GrowthCard(BonsaiTileType.LEAF, 9) // minPlayer 3
        val card11 = GrowthCard(BonsaiTileType.LEAF, 10) // minPlayer 3
        val card12 = GrowthCard(BonsaiTileType.FLOWER, 11) // minPlayer 3
        val card13 = GrowthCard(BonsaiTileType.WOOD, 12) // minPlayer 4
        val card14 = GrowthCard(BonsaiTileType.FRUIT, 13) // minPlayer 4

        // Helper Cards (7 Cards)
        val card15 = HelperCard(arrayOf(BonsaiTileType.ANY, BonsaiTileType.WOOD), 14)
        val card16 = HelperCard(arrayOf(BonsaiTileType.ANY, BonsaiTileType.WOOD), 15)
        val card17 = HelperCard(arrayOf(BonsaiTileType.ANY, BonsaiTileType.WOOD), 16)
        val card18 = HelperCard(arrayOf(BonsaiTileType.ANY, BonsaiTileType.LEAF), 17)
        val card19 = HelperCard(arrayOf(BonsaiTileType.ANY, BonsaiTileType.LEAF), 18)
        val card20 = HelperCard(arrayOf(BonsaiTileType.ANY, BonsaiTileType.FLOWER), 19)
        val card21 = HelperCard(arrayOf(BonsaiTileType.ANY, BonsaiTileType.FRUIT), 20)

        // Master Cards (9 Cards)
        val card22 = MasterCard(arrayOf(BonsaiTileType.WOOD, BonsaiTileType.WOOD), 21)
        val card23 = MasterCard(arrayOf(BonsaiTileType.LEAF, BonsaiTileType.LEAF), 22)
        val card24 = MasterCard(arrayOf(BonsaiTileType.WOOD, BonsaiTileType.LEAF), 23)
        val card25 = MasterCard(arrayOf(BonsaiTileType.ANY), 24)
        val card26 = MasterCard(arrayOf(BonsaiTileType.ANY), 25)
        val card27 = MasterCard(arrayOf(BonsaiTileType.LEAF, BonsaiTileType.LEAF), 26)
        val card28 = MasterCard(arrayOf(BonsaiTileType.LEAF, BonsaiTileType.FRUIT), 27)
        val card29 = MasterCard(arrayOf(BonsaiTileType.ANY), 28) // minPlayer 3
        val card30 = MasterCard(arrayOf(BonsaiTileType.WOOD, BonsaiTileType.LEAF), 29) // minPlayer 3
        val card31 = MasterCard(arrayOf(BonsaiTileType.WOOD, BonsaiTileType.LEAF), 30) // minPlayer 3
        // minPlayer 3
        val card32 = MasterCard(arrayOf(BonsaiTileType.WOOD, BonsaiTileType.LEAF, BonsaiTileType.FLOWER), 31)
        // minPlayer 3
        val card33 = MasterCard(arrayOf(BonsaiTileType.WOOD, BonsaiTileType.LEAF, BonsaiTileType.FRUIT), 32)
        // minPlayer 4
        val card34 = MasterCard(arrayOf(BonsaiTileType.LEAF, BonsaiTileType.FLOWER, BonsaiTileType.FLOWER), 33)

        // Parchment Cards (7 Cards)
        val card35 = ParchmentCard(2, ParchmentCardType.MASTER, 34)
        val card36 = ParchmentCard(2, ParchmentCardType.GROWTH, 35)
        val card37 = ParchmentCard(2, ParchmentCardType.HELPER, 36)
        val card38 = ParchmentCard(2, ParchmentCardType.FLOWER, 37)
        val card39 = ParchmentCard(2, ParchmentCardType.FRUIT, 38)
        val card40 = ParchmentCard(1, ParchmentCardType.LEAF, 39)
        val card41 = ParchmentCard(1, ParchmentCardType.WOOD, 40)

        // Tool Cards (6 Cards)
        val card42 = ToolCard(2, 41)
        val card43 = ToolCard(2, 42)
        val card44 = ToolCard(2, 43)
        val card45 = ToolCard(3, 44) // minPlayer 3
        val card46 = ToolCard(3, 45) // minPlayer 3
        val card47 = ToolCard(4, 46) // minPlayer 4

        // Combine all cards into one deck
        val zenCards = mutableListOf(
            card1,
            card2,
            card3,
            card4,
            card5,
            card6,
            card7,
            card8,
            card9,
            card10,
            card11,
            card12,
            card13,
            card14,
            card15,
            card16,
            card17,
            card18,
            card19,
            card20,
            card21,
            card22,
            card23,
            card24,
            card25,
            card26,
            card27,
            card28,
            card29,
            card30,
            card31,
            card32,
            card33,
            card34,
            card35,
            card36,
            card37,
            card38,
            card39,
            card40,
            card41,
            card42,
            card43,
            card44,
            card45,
            card46,
            card47
        )

        // Adjust Draw Stack based on Player Count
        val drawStack = when (gameConfig.playerName.size) {
            2 -> zenCards - listOf(
                card9,
                card10,
                card11,
                card12,
                card13,
                card14,
                card29,
                card30,
                card31,
                card32,
                card33,
                card34,
                card45,
                card46,
                card47
            ).toSet() // Remove 15 cards
            3 -> zenCards - listOf(card13, card14, card34, card47).toSet() // Remove 4 cards
            else -> zenCards // Keep all 47 cards for 4 players
        }.shuffled().toMutableList()

        startGame(gameConfig, drawStack)
    }


    /**
     * Starts a new network-based game session with a predefined draw stack of Zen cards.
     *
     * This method is primarily used for **network play**, where a controlled setup ensures
     * synchronization between multiple players in an online game environment.
     *
     * # Functionalities:
     * 1. Initializes a new game session based on the provided game configuration.
     * 2. Uses a predefined `drawStack` to ensure all players receive the same deck order.
     *
     * # Preconditions:
     * - `gameConfig` must be a valid, non-null instance.
     * - `drawStack` must be a properly initialized array of ZenCard objects.
     * - The number of players must be between **2 and 4 (inclusive)**.
     * - No game should already be in progress for the current session.
     *
     * # Post-conditions:
     * - The predefined `drawStack` is used as the Zen card deck, ensuring synchronization.
     * - The first player is determined, and turn order is synchronized across the network.
     * - The game is now in a running state, allowing turns to proceed.
     *
     * @param gameConfig The configuration object containing game settings.
     * @param drawStack The predefined stack of Zen cards used for network synchronization.
     * @throws IllegalStateException if a game is already running or if network sync fails.
     */
    fun startGame(gameConfig: GameConfig, drawStack: List<ZenCard>) {
        // Preconditions
        require(root.game == null) { "A game is already in progress." }
        require(gameConfig.playerName.size in 2..4) { "Number of players must be between 2 and 4." }
        require(gameConfig.color.distinct().size == gameConfig.color.size) { "Each player must have a unique color." }

        // if random player order shuffle player order
        if (gameConfig.randomizedPlayerOrder) gameConfig.playerName.shuffle()

        // Create Players and Assign Initial Bonsai Tile
        val players = gameConfig.playerName.mapIndexed { index, name ->
            Player(name, gameConfig.playerTypes[index], gameConfig.color[index])
        }.toTypedArray()

        // Generate Goal Tiles
        val goalTiles = mutableListOf<GoalTile>()

        // Select 3 goal types: Random if randomizedGoal is true, otherwise use predefined ones from gameConfig
        val selectedGoalTypes = if (gameConfig.randomizedGoal) {
            GoalTileType.entries.shuffled().take(3) // Randomly select 3 types
        } else {
            gameConfig.goalTiles.take(3)
        }

        // Assign goal tile values based on type
        for (goalType in selectedGoalTypes) {
            if (goalType == GoalTileType.WOOD) {
                goalTiles.add(GoalTile(points = 5, threshold = 8, type = GoalTileType.WOOD))
                goalTiles.add(GoalTile(points = 10, threshold = 10, type = GoalTileType.WOOD))
                goalTiles.add(GoalTile(points = 15, threshold = 12, type = GoalTileType.WOOD))
            } else if (goalType == GoalTileType.FRUIT) {
                goalTiles.add(GoalTile(points = 9, threshold = 3, type = GoalTileType.FRUIT))
                goalTiles.add(GoalTile(points = 11, threshold = 4, type = GoalTileType.FRUIT))
                goalTiles.add(GoalTile(points = 13, threshold = 5, type = GoalTileType.FRUIT))
            } else if (goalType == GoalTileType.LEAF) {
                goalTiles.add(GoalTile(points = 6, threshold = 5, type = GoalTileType.LEAF))
                goalTiles.add(GoalTile(points = 9, threshold = 7, type = GoalTileType.LEAF))
                goalTiles.add(GoalTile(points = 12, threshold = 9, type = GoalTileType.LEAF))
            } else if (goalType == GoalTileType.FLOWER) {
                goalTiles.add(GoalTile(points = 8, threshold = 3, type = GoalTileType.FLOWER))
                goalTiles.add(GoalTile(points = 12, threshold = 4, type = GoalTileType.FLOWER))
                goalTiles.add(GoalTile(points = 16, threshold = 5, type = GoalTileType.FLOWER))
            } else if (goalType == GoalTileType.POSITION) {
                goalTiles.add(GoalTile(points = 7, threshold = 1, type = GoalTileType.POSITION))
                goalTiles.add(GoalTile(points = 10, threshold = 2, type = GoalTileType.POSITION))
                goalTiles.add(GoalTile(points = 14, threshold = 3, type = GoalTileType.POSITION))
            }
        }


        // Distribute Center Cards & Prepare Draw Stack
        val centerCards: Array<ZenCard?> = drawStack.take(4).toTypedArray()
        val remainingDrawStack = drawStack.drop(4).toMutableList()

        // Assign Initial Bonsai Tiles based on Player Order
        players.forEachIndexed { index, player ->
            val bonsaiTiles = mutableListOf<BonsaiTile>()

            if (index >= 0) bonsaiTiles.add(BonsaiTile(BonsaiTileType.WOOD))
            if (index >= 1) bonsaiTiles.add(BonsaiTile(BonsaiTileType.LEAF))
            if (index >= 2) bonsaiTiles.add(BonsaiTile(BonsaiTileType.FLOWER))
            if (index >= 3) bonsaiTiles.add(BonsaiTile(BonsaiTileType.FRUIT))

            player.storage.addAll(bonsaiTiles)
        }

        // Initialize Game State
        val gameState = GameState(
            players = players,
            currentPlayerIndex = 0,
            centerCards = centerCards,
            state = States.CHOOSE_ACTION,
            goalTiles = goalTiles,
            drawStack = remainingDrawStack
        )

        val game = BonsaiGame(botSpeed = gameConfig.botSpeed, currentState = gameState)
        root.game = game
        game.pastStates.add(game.currentState.copy())
        // Notify UI Refresh
        if(gameConfig.hotSeat){
            onAllRefreshables { refreshAfterGameStart() }
        }
    }


    /**
     * Ends the current player's turn and transitions to the next player.
     *
     * # Functionalities:
     * 1. Validates if the current player has completed their required actions.
     * 2. Updates the turn to the next player in sequence.
     * 3. Game state and pastState must be saved.
     *
     * # Preconditions:
     * - A game must currently be running.
     * - The current player must have completed all mandatory actions for their turn.
     * - The next player must be available in the turn sequence.
     *
     * # Post-conditions:
     * - The current player’s turn is officially ended.
     * - The turn is passed to the next player in the sequence.
     * - Game state and pastState must be saved.
     *
     * @throws IllegalStateException if no game is currently running.
     */
    fun endTurn() {
        val game = checkNotNull(root.game) { "There is currently no game running" }
        if (States.CULTIVATE == game.currentState.state) {
            game.currentState.state = States.TURN_END
        }
        if (States.USING_HELPER == game.currentState.state) {
            game.currentState.state = States.TURN_END
        }
        if (game.currentState.state == States.TURN_END && root.playerActionService.needsToDiscardTiles()) {
            game.currentState.state = States.DISCARDING
            onAllRefreshables { refreshAfterEndTurn() }
            return
        }
        check(game.currentState.state == States.TURN_END) { "Turn is not completed" }
        game.currentState.currentPlayer.remainingGrowth = intArrayOf(0, 0, 0, 0, 0)
        if (game.currentState.drawStack.isEmpty()) {
            println(game.currentState.finalPlayer)
            if (game.currentState.finalPlayer == game.currentState.currentPlayerIndex) {
                game.currentState.state = States.GAME_ENDED
                endGame()
                return
            } else {
                if (game.currentState.finalPlayer == -1) {
                    game.currentState.finalPlayer = game.currentState.currentPlayerIndex
                }
            }
        }
        if (game.currentState.currentPlayerIndex + 1 == game.currentState.players.size) {
            game.currentState.currentPlayerIndex = 0
        } else {
            game.currentState.currentPlayerIndex++
        }
        game.currentState.state = States.CHOOSE_ACTION
        game.pastStates.add(game.currentState.copy())

        onAllRefreshables { refreshAfterEndTurn() }
    }

    /**
     * Ends the game, calculates final scores, and determines the winner.
     *
     * This method is triggered when the last card from the deck is revealed. Scores are then calculated,
     * and the player with the highest total points is declared the winner.
     *
     * # Functionalities:
     * 1. Determines the winner based on the highest total points:
     *    - If there is a **tie**, the winner is the player **farther in clockwise order** from the starting player.
     * 2. Declares the final game state and announces the winner.
     *
     * # Preconditions:
     * - The game must currently be running.
     * - Game FSA need to be in GAME_ENDED
     * - The last card from the **Zen card deck** must have been drawn.
     *
     * # Post-conditions:
     * - Game is set to be null
     * - The winner is determined based on the highest total points.
     * - If tied, the player **farther from the starting player (clockwise)** is declared the winner.
     * - The game state is marked, and no further actions can be taken.
     *
     * @throws IllegalStateException if no game is currently running.
     */
    fun endGame() {
        val game = checkNotNull(root.game) { "Game must be initialized" }
        if (game.currentState.drawStack.isNotEmpty()) {
            throw IllegalStateException("Not all Zen Cards have been revealed")
        }
        if (game.currentState.state != States.GAME_ENDED) throw IllegalStateException("The game has not ended yet")
        val scores = root.scoreService.getFinalScores()

        val winnerIndex = scores.indices.maxByOrNull { scores[it].sum() } ?: 0
        val winnerName = game.currentState.players[winnerIndex].name

        onAllRefreshables { refreshAfterEndGame(scores, winnerName) }

        // reset game to null since game has ended
        root.game = null
    }
}
