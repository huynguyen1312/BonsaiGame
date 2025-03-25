package service

import entity.BonsaiGame
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlin.io.path.Path

/**
 * Handles the saving and loading of the game state.
 *
 * This service is responsible for persisting the game state and restoring it when needed.
 * It interacts with the [RootService] to maintain consistency between game logic and player actions.
 *
 * @property rootService The central service managing game logic and player actions.
 */
class FileService(private val rootService: RootService) : AbstractRefreshingService() {

    /**
     * Loads a saved game state from a file and restores it.
     *
     * **Preconditions:**
     * - A valid save file must exist.
     *
     * **Postconditions:**
     * - The game state is restored to the last saved state.
     * - If loading fails, the current game state remains unchanged.
     *
     * @throws IOException If an error occurs while accessing the save file.
     * @throws IllegalStateException If the file contains invalid or incomplete data.
     */
    fun loadGame() {
        val file = File("./saves/SaveGame.json")
        if (!file.exists()) throw IllegalStateException("No saved game found")
        val jsonString = file.readText(Charsets.UTF_8)
        val json = Json {
            allowStructuredMapKeys = true
            encodeDefaults = true
        }
        val game = json.decodeFromString<BonsaiGame>(jsonString)
        rootService.game = game
        onAllRefreshables { refreshAfterLoadGame() }
    }

    /**
     * Saves the current game state to a file.
     *
     * # Preconditions:
     * - The game must be in a valid state.
     *
     * # Postconditions:
     * - The current game state is stored in a file and can be reloaded later.
     * - If saving fails, the current game state remains unchanged.
     *
     * @throws IOException If an error occurs while writing to the save file.
     */
    fun saveGame() {
        if (rootService.game == null) throw IllegalStateException("Game must be initialized")
        if (!File("./saves").exists()) Files.createDirectory(Path("./saves"))
        val game = requireNotNull(rootService.game)
        val json = Json {
            allowStructuredMapKeys = true
            encodeDefaults = true
        }
        val jsonString = json.encodeToString(game)
        val file = File("./saves/SaveGame.json")
        file.writeText(text = jsonString, charset = Charsets.UTF_8)
    }
}
