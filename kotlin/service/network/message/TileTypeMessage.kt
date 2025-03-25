package service.network.message

import entity.BonsaiTileType

/**
 * Enum for communicating tiles-types over the network
 */
enum class TileTypeMessage {
    WOOD,
    LEAF,
    FLOWER,
    FRUIT;

    /**
     * Converts a [BonsaiTileType] to its corresponding [TileTypeMessage].
     */
    companion object{
        /**
         * Converts a [BonsaiTileType] to its corresponding [TileTypeMessage].
         *
         * @param tile The [BonsaiTileType] to be converted.
         * @return The corresponding [TileTypeMessage].
         */
        fun toTileTypeMsg(tile: BonsaiTileType): TileTypeMessage {
            return when(tile){
                BonsaiTileType.WOOD -> WOOD
                BonsaiTileType.LEAF -> LEAF
                BonsaiTileType.FLOWER -> FLOWER
                BonsaiTileType.FRUIT -> FRUIT
                BonsaiTileType.ANY -> error("Can't send ANY tile.")
            }
        }
    }

    /**
     * Converts this [TileTypeMessage] to its corresponding [BonsaiTileType].
     *
     * @return The corresponding [BonsaiTileType].
     */
    fun toEntityTile(): BonsaiTileType{
        return when(this){
            WOOD -> BonsaiTileType.WOOD
            LEAF -> BonsaiTileType.LEAF
            FLOWER -> BonsaiTileType.FLOWER
            FRUIT -> BonsaiTileType.FRUIT
        }
    }
}
