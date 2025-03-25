package service.bot

import entity.States

/**
 * Outlines the decision-making approach for each game phase.
 */
interface CompetitiveBotStrategy {

    /**
     * Called once when entering Early Game.
     * Possibly do precomputation or reset counters.
     */
    fun onEnterEarlyGame(memory: BotMemory)

    /**
     * Called each turn in Early Game to decide the action:
     * Meditate or Cultivate or special logic.
     */
    fun playEarlyGameTurn(memory: BotMemory): States

    /**
     * Called once when entering Mid Game.
     */
    fun onEnterMidGame(memory: BotMemory)

    /**
     * Called each turn in Mid-Game for decisions.
     */
    fun playMidGameTurn(memory: BotMemory): States

    /**
     * Called once when entering Late Game.
     */
    fun onEnterLateGame(memory: BotMemory)

    /**
     * Called each turn in Late Game for decisions.
     */
    fun playLateGameTurn(memory: BotMemory): States

    /**
     * Called to handle forced goal claiming or other forced states
     * (e.g., DISCARDING, CLAIMING_GOALS, USING_HELPER, etc.).
     * Return true if we handled it fully, false if not.
     */
    fun handleForcedState(currentState: States, memory: BotMemory): Boolean
}
