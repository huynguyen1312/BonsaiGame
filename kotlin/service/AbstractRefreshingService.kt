package service

import gui.Refreshable

/**
 * Abstract service class that manages UI refreshes for the "Bonsai" game.
 * This class provides functionality to store and update multiple [Refreshable] instances.
 *
 */
abstract class AbstractRefreshingService {

    /**
     * A list of all registered [Refreshable] instances that should be updated when game state changes.
     */
    private val refreshables = mutableListOf<Refreshable>()

    /**
     * Adds a new [Refreshable] instance to the list of elements that should be updated.
     *
     * @param newRefreshable The refreshable instance to add.
     */
    fun addRefreshable(newRefreshable: Refreshable) {
        refreshables.add(newRefreshable)
    }

    /**
     * Invokes the given method on all registered [Refreshable] instances.
     *
     * @param method A lambda function that operates on a [Refreshable] instance.
     */
    fun onAllRefreshables(method: Refreshable.() -> Unit) {
        refreshables.forEach { it.method() }
    }
}
