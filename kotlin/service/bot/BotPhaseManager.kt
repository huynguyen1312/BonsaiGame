package service.bot

/**
 * Manages phase transitions for the bot (Early, Mid, Late).
 * Decides when to switch based on deckSize or other triggers.
 */
class BotPhaseManager {

    /**
     * Determines the bot's current phase (EARLY, MID, or LATE)
     * based on the BotMemory or game state.
     */
    fun determineCurrentPhase(memory: BotMemory): BotPhase {
        // Implementation will check memory.deckSize (or other logic)
        // Return BotPhase.EARLY, MID, or LATE
        TODO()
    }

    /**
     * Optionally a method that checks
     * if a phase transition is triggered.
     */
    fun shouldTransitionPhase(oldPhase: BotPhase, newPhase: BotPhase): Boolean {
        // Compare oldPhase and newPhase
        TODO()
    }
}
