package entity

import kotlinx.serialization.Serializable

/**
 * Represents a position in a hexagonal grid using cube coordinates.
 *
 * The Vector uses an axial coordinate system with two primary axes [q] and [r],
 * where [s] is derived from them to satisfy the constraint q + r + s = 0.
 * This coordinate system is used for positioning [BonsaiTile]s on the game board.
 *
 * @property q The q-axis coordinate in the hexagonal grid
 * @property r The r-axis coordinate in the hexagonal grid
 * @property s Computed third axis coordinate to maintain the cubic constraint
 */
@Serializable
class Vector(
    val q: Int, val r: Int
) {
    val s: Int
        get() = -q - r

    /**
     * Adds Helper methods to the Vector class
     */
    companion object {
        /**
         * vector going right
         */
        val right: Vector
            get() = Vector(1, 0)

        /**
         * vector going left
         */
        val left: Vector
            get() = Vector(-1, 0)

        /**
         * vector going down and right
         */
        val downRight: Vector
            get() = Vector(0, 1)

        /**
         * vector going up and left
         */
        val upLeft: Vector
            get() = Vector(0, -1)

        /**
         * vector going down and left
         */
        val downLeft: Vector
            get() = Vector(-1, 1)

        /**
         * vector going up and right
         */
        val upRight: Vector
            get() = Vector(1, -1)

        /**
         * vector going nowhere it stays here
         */
        val zero: Vector
            get() = Vector(0, 0)
    }

    /**
     * toString implementation for Vector
     *
     * @return string representing a vector
     */
    override fun toString(): String {
        return "($q, $r, $s)"
    }

    /**
     * compares if equals
     *
     * @param other object to compare to
     * @return false if not equal true if equal
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vector

        if (q != other.q) return false
        return r == other.r
    }

    /**
     * Gives a hashCode for the vector
     *
     * @return Int hashCode
     */
    override fun hashCode(): Int {
        var result = q
        result = 31 * result + r
        return result
    }

    /**
     * adds a vector to the given one
     *
     * @param b the vector to add
     * @return sum of the two vectors
     */
    operator fun plus(b: Vector): Vector = Vector(this.q + b.q, this.r + b.r)

    /**
     * Substract a vector from the given one
     *
     * @param b the vector to substract
     * @return given vector minus b vector
     */
    operator fun minus(b: Vector): Vector = Vector(this.q - b.q, this.r - b.r)

    /**
     * CAN ONLY BE USED THIS WAY
     *
     * @param scalar integer to multiply the given vector
     * @return the scaled vector
     */
    operator fun times(scalar: Int): Vector = Vector(this.q * scalar, this.r * scalar)

    /**
     * This is a positive vector
     *
     * @return a positive vector
     */
    operator fun unaryPlus(): Vector = this

    /**
     * This is a negative vector
     *
     * @return the negated vector
     */
    operator fun unaryMinus(): Vector = this * -1
}


