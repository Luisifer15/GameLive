package ph.edu.auf.luisalfonso.rimando.gamelive.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import ph.edu.auf.luisalfonso.rimando.gamelive.data.GameResult
import ph.edu.auf.luisalfonso.rimando.gamelive.data.IgdbApi
import ph.edu.auf.luisalfonso.rimando.gamelive.data.TwitchAuthService
import ph.edu.auf.luisalfonso.rimando.gamelive.data.model.GameEntry

class GameRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val twitchAuthService = TwitchAuthService()


    suspend fun getGameEntries(userId: String): List<GameEntry> {
        return try {
            val userGamesCollection = firestore.collection("users").document(userId).collection("games")
            val gameEntries = userGamesCollection.get().await().toObjects(GameEntry::class.java)
            Log.d("GameRepository", "Fetched game entries: $gameEntries")
            gameEntries
        } catch (e: Exception) {
            Log.e("GameRepository", "Error fetching game entries", e)
            emptyList()
        }
    }

    suspend fun addGameEntry(userId: String, gameEntry: GameEntry) {
        try {
            val userGamesCollection = firestore.collection("users").document(userId).collection("games")
            userGamesCollection.document(gameEntry.gameId.toString()).set(gameEntry).await()
        } catch (e: Exception) {
            Log.e("GameRepository", "Error adding game entry", e)
        }
    }

    suspend fun getAccessToken(): String = withContext(Dispatchers.IO) {
        val twitchAuthService = TwitchAuthService()
        twitchAuthService.getAccessToken()
    }

    suspend fun searchGames(query: String): List<GameResult> = withContext(Dispatchers.IO) {
        try {
            val accessToken = twitchAuthService.getAccessToken()
            val clientId = twitchAuthService.getClientId()
            val igdbApiService = IgdbApi.createService(accessToken, clientId)

            val games = igdbApiService.searchGames(query).map { game ->
                GameResult(
                    id = game.id,
                    name = game.name,
                    cover = game.cover
                )
            }
            Log.d("GameRepository", "Games found: ${games.size}")
            games.forEach { game ->
                Log.d("GameRepository", "Game: ${game.name}")
            }
            games
        } catch (e: Exception) {
            Log.e("GameRepository", "Error searching games", e)
            emptyList()
        }
    }

    suspend fun getGameDetails(gameId: Long, gameTitle: String?): GameResult? = coroutineScope {
        try {
            val accessToken = async { twitchAuthService.getAccessToken() }.await()
            val clientId = twitchAuthService.getClientId()
            val igdbApiService = IgdbApi.createService(accessToken, clientId)

            val whereClause = "id = $gameId"
            Log.d("GameRepository", "Where clause: $whereClause")

            val response = igdbApiService.searchGamesFromId(
                query = gameTitle,
                whereClause = whereClause,
                fields = "name,cover.url"
            )

            // Log the response body
            Log.d("Token", "accessToken: $accessToken - clientId: $clientId")
            Log.d("IgdbApiService", "Response body: ${response}")

            if (response.isNotEmpty()) {
                val gameResult = response[0]
                Log.d("GameRepository", "Fetched game details: $gameResult")
                gameResult
            } else {
                Log.d("GameRepository", "No game details found for gameId: $gameId")
                null
            }
        } catch (e: Exception) {
            Log.e("GameRepository", "Error fetching game details", e)
            null
        }
    }
}