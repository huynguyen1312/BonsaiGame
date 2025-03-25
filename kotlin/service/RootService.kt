package service

import entity.BonsaiGame
import gui.Refreshable
import service.network.NetworkService

/**
 * The root service class that connects all services together
 */
class RootService{
    var game : BonsaiGame? = null
    val botService = BotService(this)
    val fileService = FileService(this)
    val gameService = GameService(this)
    val historyService = HistoryService(this)
    val playerActionService = PlayerActionService(this)
    val scoreService = ScoreService(this)
    val treeService = TreeService(this)
    val networkService = NetworkService(this)

    /**
     * Adds the provided [newRefreshable] to all services connected
     * to this root service
     */
    fun addRefreshable(newRefreshable: Refreshable) {
        botService.addRefreshable(newRefreshable)
        fileService.addRefreshable(newRefreshable)
        gameService.addRefreshable(newRefreshable)
        historyService.addRefreshable(newRefreshable)
        playerActionService.addRefreshable(newRefreshable)
        scoreService.addRefreshable(newRefreshable)
        treeService.addRefreshable(newRefreshable)
        networkService.addRefreshable(newRefreshable)
    }

    /**
     * Adds each of the provided [newRefreshables] to all services
     * connected to this root service
     */
    fun addRefreshables(vararg newRefreshables: Refreshable) {
        newRefreshables.forEach { addRefreshable(it) }
    }
}
