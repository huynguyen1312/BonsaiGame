package service

import entity.Colors
import entity.GoalTileType
import entity.PlayerType

/**
 * Configuration class for setting up a game instance.
 * This class holds various settings related to gameplay, including player details, game mode configurations,
 * and visual preferences.
 *
 * @property botSpeed The speed at which AI-controlled bots operate in the game.
 *                    Default value: 5.0 (inactive or default speed).
 * @property playerName A list of player names participating in the game.
 * @property playerTypes  A list defining the types of players in the game. A player can be either a bot or a real
 *                        person.
 * @property hotSeat Determines whether the game is in "Hot Seat" mode.
 *                   When true, multiple players take turns on the same device.
 *                   Default value: false.
 * @property randomizedPlayerOrder Indicates that player order is randomized.
 *                                 Default value: false.
 * @property randomizedGoal Indicates that goal tiles are randomized chosen.
 *                           Default value: false.
 * @property goalTiles A list of goal tiles used in the game.
 *                     These tiles define objectives or targets for players.
 * @property color A list of colors assigned to players.
 *                 This allows customization of visual representation in the game.
 */
data class GameConfig(
    var botSpeed: Double = 5.0,
    val playerName: MutableList<String> = mutableListOf(),
    val playerTypes: MutableList<PlayerType> = mutableListOf(),
    var hotSeat: Boolean = false,
    var randomizedPlayerOrder: Boolean = false,
    var randomizedGoal: Boolean = false,
    val goalTiles: MutableList<GoalTileType> = mutableListOf(),
    val color: MutableList<Colors> = mutableListOf()
)
