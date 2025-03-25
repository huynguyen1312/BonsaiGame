import entity.*
import gui.BonsaiGameApplication
import gui.ConsoleRefreshable
import service.GameConfig
import service.RootService

/**
 * Main entry point of the application.
 */
fun main() {
//    val root = RootService()
//
//    val player1 = Player("Hardy", PlayerType.HARD_BOT, Colors.RED)
//    val player2 = Player("Easy", PlayerType.EASY_BOT, Colors.BLUE)
//    val player3 = Player("Franky", PlayerType.HARD_BOT, Colors.PURPLE)
//    val player4 = Player("Dieter", PlayerType.HARD_BOT, Colors.BLACK)
//
//    root.addRefreshable(ConsoleRefreshable(root))
//
//    val config = GameConfig(
//        botSpeed = 0.0,
//        playerName = mutableListOf(player1.name, player2.name, player3.name, player4.name),
//        playerTypes = mutableListOf(player1.type, player2.type,player3.type, player4.type),
//        hotSeat = true,
//        randomizedPlayerOrder = false,
//        randomizedGoal = false,
//        goalTiles = mutableListOf(GoalTileType.LEAF, GoalTileType.FLOWER, GoalTileType.WOOD),
//        color = mutableListOf(player1.color, player2.color, player3.color, player4.color)
//    )
//
//    root.gameService.startGame(config)
    BonsaiGameApplication().show()
    println("Application ended.")

}