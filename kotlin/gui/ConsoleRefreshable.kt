package gui

import entity.*
import service.RootService

/**
 * [ConsoleRefreshable] is responsible for displaying updates related to the Bonsai game in the console.
 * It implements the Refreshable interface and provides methods to update the console display after various game events.
 *
 * @property root The RootService instance, which provides access to the game state.
 */
class ConsoleRefreshable(val root: RootService) : Refreshable {


    override fun refreshAfterGameStart() {
        val game = checkNotNull(root.game)
        println("🌱 A new game of Bonsai has started! 🌿")
        println("Current Center Cards:")
        game.currentState.centerCards.forEachIndexed { index, card ->
            println("  Slot $index: ${card ?: "Empty"}")
        }
        println("It's ${game.currentState.currentPlayer.name}'s turn!")
        if (game.currentState.currentPlayer.type == PlayerType.EASY_BOT || game.currentState.currentPlayer.type == PlayerType.HARD_BOT) {
            root.botService.makeBotMove()
        }
    }

    override fun refreshAfterChooseTiles(vararg tiles: BonsaiTileType) {
        println("🃏 Choose tiles: ${tiles.joinToString(" or ")}")

    }

    override fun refreshAfterDiscardTiles(tiles: List<BonsaiTileType>) {
        println("🗑️ Discarded tiles: ${tiles.joinToString(", ")}")
    }

    override fun refreshAfterGoalReached(goal: GoalTile) {
        println("🏆 Goal reached: ${goal.type} (Worth ${goal.points} points)")
    }

    override fun refreshAfterMeditate(cardIndex: Int) {
        println("🧘 Player meditated and took the card from slot $cardIndex.")
    }

    override fun refreshAfterPlaceBonsaiTile(tile: BonsaiTile) {
        println("🪴 Placed a ${tile.type} tile")
    }

    override fun refreshAfterClaimGoal(claimed: Boolean, goal: GoalTile) {
        println(if (claimed) "🎯 Claimed goal: ${goal.type}" else "🚫 Goal renounced: ${goal.type}")
    }

    override fun refreshAfterTileChosen(tile: BonsaiTileType) {
        println("🎴 Chose tile: $tile")
    }

    override fun refreshAfterTilesRemoved(tilePositions: Array<Vector>) {
        println("🗑️ Removed tiles from positions: ${tilePositions.joinToString(", ")}")
    }

    override fun refreshAfterCultivate() {
        println("🌿 Cultivation action called! The Bonsai grows stronger.")
    }

    override fun refreshAfterEndTurn() {
        val game = checkNotNull(root.game)
        println("🔄 Turn ended! Updated Center Cards:")
        game.currentState.centerCards.forEachIndexed { index, card ->
            println("  Slot $index: ${card ?: "Empty"}")
        }
        println("Drawstack remaining ${game.currentState.drawStack.size} cards")
        println("🎮 Next Player: ${game.currentState.currentPlayer.name}")

        if (game.currentState.currentPlayer.type == PlayerType.EASY_BOT || game.currentState.currentPlayer.type == PlayerType.HARD_BOT) {
            root.botService.makeBotMove()
        }
    }

    override fun refreshAfterEndGame(scores: Array<IntArray>, winner: String) {

        println("| Source           |" + (0 until scores.size).joinToString("|") { " Bot${it + 1}" } + " |")
        println("|------------------|" + " ----| ".repeat(scores.size))

        val categories = listOf("Leaf Points", "Flower Points", "Fruit Points", "Parchment Pts", "Goal Points")

        categories.forEachIndexed { categoryIndex, categoryName ->
            println("| %-15s  | ".format(categoryName) + scores.joinToString(" |  ") { "%3d".format(it[categoryIndex]) } + " |")
        }

        // ✅ Compute total score for each bot
        val totalScores = scores.map { it.sum() }

        // ✅ Print total scores
        println("| %-15s  | ".format("Total Score") + totalScores.joinToString(" |  ") { "%3d".format(it) } + " |")

        println("🏆 Winner is: $winner")
    }

}
