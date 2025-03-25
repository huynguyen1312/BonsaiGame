package service.network

import entity.*
import service.ConnectionState
import service.network.message.CultivateMessage
import service.network.message.GoalTileTypeMessage
import service.network.message.MeditateMessage
import service.network.message.StartGameMessage
import tools.aqua.bgw.net.client.BoardGameClient
import tools.aqua.bgw.net.client.NetworkLogging
import tools.aqua.bgw.net.common.annotations.GameActionReceiver
import tools.aqua.bgw.net.common.notification.PlayerJoinedNotification
import tools.aqua.bgw.net.common.response.*

/**
 * [BoardGameClient] implementation for network communication.
 *
 * @param networkService the [NetworkService] to potentially forward received messages to.
 * @param host name of the host
 * @param playerName name of the player on this pc
 */
class BonsaiNetworkClient(
    playerName: String,
    host: String,
    var networkService: NetworkService
): BoardGameClient(playerName, host, "baum25", NetworkLogging.VERBOSE) {
    var sessionID: String? = null

    /**
     * When creating an online game, player receives a response message and state is updated.
     * If you cannot create game, "unexpected" error
     */
    @GameActionReceiver
    override fun onCreateGameResponse(response: CreateGameResponse) {
        check(networkService.connection == ConnectionState.WAITING_FOR_HOST_CONFIRMATION){
            "unexpected CreateGameResponse"}

        when(response.status){
            CreateGameResponseStatus.SUCCESS ->{
                networkService.updateConnectionState(ConnectionState.WAITING_FOR_GUEST)
                sessionID=response.sessionID
            }
            else -> disconnectAndError(response.status)
        }
    }

    /**
     * After joining an online game, the player receives a response message and the state updates.
     * If connection is not successful, "unexpected" error.
     */
    @GameActionReceiver
    override fun onJoinGameResponse(response: JoinGameResponse) {
        check(networkService.connection == ConnectionState.WAITING_FOR_JOIN_CONFIRMATION){
            "unexpected JoinGameResponse"}

        when(response.status){
            JoinGameResponseStatus.SUCCESS -> {
                networkService.updateConnectionState(ConnectionState.WAITING_FOR_INIT)
                println("successfully joined game")
            }
            else -> disconnectAndError(response.status)
        }
    }


    /**
     * Handles the event when a new player joins an online game session.
     *
     * **Preconditions:**
     * - The connection state must be [ConnectionState.WAITING_FOR_GUEST], meaning the host is expecting new players.
     *
     * **Post conditions:**
     * - The joined player's name is added to the list of players in [NetworkService].
     * - The player's type is set to [PlayerType.ONLINE].
     * - The UI is refreshed to reflect the new player.
     *
     * @param notification Contains information about the player who joined, including their name.
     * @throws IllegalStateException If the game is not in a state where guests are expected.
     */
    @GameActionReceiver
    override fun onPlayerJoined(notification: PlayerJoinedNotification) {
        check(networkService.connection == ConnectionState.WAITING_FOR_GUEST){
            "Not awaiting any guests."}

        println(notification.sender + " has joined.")
        networkService.players.add(notification.sender)
        networkService.playerTypes.add(PlayerType.ONLINE)
        networkService.onAllRefreshables { refreshAfterJoin(notification.sender) }
    }

    /**
     * Receives the start message and sends it to the networkService.
     */
    @Suppress("unused")
    @GameActionReceiver
    fun onStartGameMessageReceived(message: StartGameMessage, sender: String) {
        println("start game message received")
        networkService.startNewJoinedGame(message)
    }

    /**
     * Handles the actions taken when a player ends their turn in an online game.
     *
     * **Preconditions:**
     * - A game must be active and running.
     * - The `message` must contain valid information about the turn actions.
     * - If tiles were removed, they must have been removed according to the game's rules.
     * - If tiles were placed, they must be placed legally.
     *
     * **Post conditions:**
     * - The removed tiles are validated. If they are incorrect, the game disconnects and an error message is shown.
     * - The meditation action is executed based on the selected card.
     * - If a second card was drawn, the UI is updated accordingly.
     * - Tiles are placed based on the message information.
     * - If goal conditions are met, the player claims or renounces goals.
     * - The turn ends, and the next player begins their turn.
     *
     * @param message Contains details about the player's turn, including removed tiles, played tiles,
     *                claimed goals, and discarded tiles.
     * @throws IllegalStateException If the game is not currently running.
     * @throws IllegalArgumentException If the previous player made an invalid move.
     */
    @Suppress("unused")
    @GameActionReceiver
    fun onEndTurnMessageReceived(message: MeditateMessage, sender: String){
        val game = requireNotNull(networkService.root.game)
        val gameState = requireNotNull(game.currentState.state)

        //removed tiles
        val coords = mutableListOf<Vector>()
        if(message.removedTilesAxialCoordinates.isNotEmpty()){
            message.removedTilesAxialCoordinates.forEach {
                coords.add(Vector(it.first, it.second))
            }
            if(!networkService.root.treeService.validateRemoveTiles(coords.toTypedArray())){
                disconnectAndError("Previous player removed wrong tiles.")
            }
        }

        //meditate with chosen card
        networkService.root.playerActionService.meditate(message.chosenCardPosition)
        //if player chose second card decide on tile
        if(gameState == States.CHOOSING_2ND_PLACE_TILES){
            networkService.onAllRefreshables{ refreshAfterTileChosen(
                message.drawnTiles.first().toEntityTile()
            )}
        }

        //place tiles
        message.playedTiles.forEach {
            networkService.root.treeService.placeTile(
                BonsaiTile(it.first.toEntityTile()),
                Vector(it.second.first, it.second.second)
            )
            while(game.currentState.state == States.CLAIMING_GOALS){
                message.claimedGoals.forEach {
                    networkService.root.playerActionService.claimGoal(
                        asGoalTile(it.first, it.second)
                    )
                }
                message.renouncedGoals.forEach {
                    networkService.root.playerActionService.claimGoal(
                        asGoalTile(it.first, it.second)
                    )
                }
            }
        }

        val discard = mutableListOf<BonsaiTile>()
        message.discardedTiles.forEach {
            discard.add(BonsaiTile(it.toEntityTile()))
        }


        if(game.currentState.currentPlayer.type == PlayerType.LOCAL){
            networkService.updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        }
        networkService.root.gameService.endTurn()
    }

    /**
     * The method imitates the turn an online player made, if the turn was Cultivate.
     *
     * @param message contains the cultivateMessage, that contains all parts of the move.
     *
     * 1) If tiles were removed from the tree, also remove each tile. DoubleCheck that the
     * removedTiles are a minimal amount. Then remove all tiles.
     * @throws disconnectAndError if the removedTiles are not minimal.
     * 2) Play the played tiles.
     * @throws disconnectAndError if the Tiles are placed wrongly.
     * 3) Check if the player can claim a goal and check the message if the goals are claimed
     * or renounced.
     */
    @Suppress("unused")
    @GameActionReceiver
    fun onEndTurnMessageReceived(message: CultivateMessage, sender: String){
        networkService.root.playerActionService.cultivate()
        val game = requireNotNull(networkService.root.game)
        val coords = mutableListOf<Vector>()
        if(message.removedTilesAxialCoordinates.isNotEmpty()){
            message.removedTilesAxialCoordinates.forEach {
                coords.add(Vector(it.first, it.second))
            }
            if(!networkService.root.treeService.validateRemoveTiles(coords.toTypedArray())){
                disconnectAndError("Previous player removed wrong tiles.")
            }
        }
        networkService.root.treeService.removeTiles(coords.toTypedArray())

        message.playedTiles.forEach{
            val tileType = it.first.toEntityTile()
            val coord = Vector(it.second.first, it.second.first)
            if (!networkService.root.treeService.canPlaceTile(tileType, coord)){
                disconnectAndError("Previous player placed a wrong tile.")
            }
            val tile =  BonsaiTile(tileType)
            networkService.root.treeService.placeTile(tile, coord)

            while(game.currentState.state == States.CLAIMING_GOALS){
                message.claimedGoals.forEach {
                    networkService.root.playerActionService.claimGoal(
                        asGoalTile(it.first, it.second)
                    )
                }
                message.renouncedGoals.forEach {
                    networkService.root.playerActionService.claimGoal(
                        asGoalTile(it.first, it.second)
                    )
                }
            }
        }


        //end turn
        networkService.root.gameService.endTurn()

        if(game.currentState.currentPlayer.type == PlayerType.LOCAL){
            networkService.updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        }
    }

    /**
     * If undefined behaviour happens during an online game, the player exits the game
     * and an error message is displayed.
     */
    private fun disconnectAndError(message: Any){
        networkService.disconnect()
        error(message)
    }

    /**
     * Function that maps a Goal Tile Type and tier to a card instance in the current game state.
     *
     * @param type The [GoalTileTypeMessage] that corresponds to the goal that is to be found in the game state.
     * @param tier The difficulty tier of the goal (Goal Tiles have 3 tiers so index must be in [0;2]).
     *
     * @return The Goal Tile matching the parameters if it is still claimable. Null if none matches or a player already
     * claimed it.
     * @throws IllegalStateException If the game is null (not running)
     * @throws IllegalArgumentException If the [tier] index is out of bounds.
     */
    private fun asGoalTile(type: GoalTileTypeMessage, tier: Int): GoalTile{
        // check argument validity
        if (tier !in 0..2) throw IllegalArgumentException("Goal tile tiers must range from tier 0 to tier 2")
        val game = checkNotNull(networkService.root.game){"Game must be in a running state"}
        //collect available goals from the given type, sort and return the desired tier if available.
        val claimedType = type.toGoalTileType()
        return game.currentState.goalTiles.filter { it.type == claimedType }.sortedBy { it.threshold }[tier]
    }

}
