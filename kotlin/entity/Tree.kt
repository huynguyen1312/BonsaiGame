package entity

import kotlinx.serialization.Serializable

/**
 * Represents a Bonsai tree in the game, consisting of placed tiles.
 *
 * The tree maintains a collection of [BonsaiTileType]s mapped to their positions
 * in the hexagonal grid using [Vector] coordinates.
 *
 * @property tiles Map storing the tree's structure, where keys are [Vector] positions
 *                 and values are the corresponding [BonsaiTileType]s
 *
 * @see Vector
 * @see BonsaiTileType
 * @see Player
 */
@Serializable
data class Tree(var tiles: MutableMap<Vector, BonsaiTileType> = mutableMapOf()) {

    /**
     * ToString implementation for Tree
     */
    override fun toString(): String {
        return "Tree(tiles=$tiles)"
    }

    /**
     * Gives back a list of all neighbors of a given position.
     *
     * @param position The position to get the neighbors of
     *
     * @return List of [BonsaiTileType]s that are neighbors of the given position
     */
    fun neighbors(position: Vector): List<BonsaiTileType>{
        val neighbors: List<BonsaiTileType?> = listOf(
            tiles[position + Vector.right],
            tiles[position + Vector.upRight],
            tiles[position + Vector.upLeft],
            tiles[position + Vector.left],
            tiles[position + Vector.downLeft],
            tiles[position + Vector.downRight],
        )
        return neighbors.filterNotNull()
    }

    /**
     * Gives back a list of all neighbors of a given position.
     *
     * @param position The position to get the neighbors of
     *
     * @return List of [BonsaiTileType]s that are neighbors of the given position
     */
    fun neighborsVectors(position: Vector): List<Vector>{
        return listOf(
            position + Vector.right,
            position + Vector.upRight,
            position + Vector.upLeft,
            position + Vector.left,
            position + Vector.downLeft,
            position + Vector.downRight
        )
    }
}

