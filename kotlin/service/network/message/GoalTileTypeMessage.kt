package service.network.message

import entity.GoalTileType

/**
 * Enum class representing different types of goal tiles in the game.
 */
enum class GoalTileTypeMessage {
    GREEN, BROWN, PINK, ORANGE, BLUE;

    /**
     * Converts a [GoalTileType] entity to its corresponding [GoalTileTypeMessage].
     */
    companion object{
        /**
         * Converts a [GoalTileType] entity to its corresponding [GoalTileTypeMessage].
         *
         * @param goal The [GoalTileType] to be converted.
         * @return The corresponding [GoalTileTypeMessage] representation.
         */
        fun toGoalTileMsg(goal: GoalTileType): GoalTileTypeMessage {
            return when (goal){
                GoalTileType.WOOD -> BROWN
                GoalTileType.LEAF -> GREEN
                GoalTileType.FRUIT -> ORANGE
                GoalTileType.FLOWER -> PINK
                GoalTileType.POSITION -> BLUE
            }
        }
    }

    /**
     * Converts a [GoalTileTypeMessage] back into its corresponding [GoalTileType] entity.
     *
     * @return The corresponding [GoalTileType] representation.
     */
    fun toGoalTileType(): GoalTileType{
        return when (this){
            BROWN -> GoalTileType.WOOD
            GREEN -> GoalTileType.LEAF
            ORANGE -> GoalTileType.FRUIT
            PINK -> GoalTileType.FLOWER
            BLUE -> GoalTileType.POSITION
        }
    }
}