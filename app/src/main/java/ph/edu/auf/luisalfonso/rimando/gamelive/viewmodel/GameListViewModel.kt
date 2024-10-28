package ph.edu.auf.luisalfonso.rimando.gamelive.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ph.edu.auf.luisalfonso.rimando.gamelive.data.GameResult
import ph.edu.auf.luisalfonso.rimando.gamelive.data.TwitchAuthService
import ph.edu.auf.luisalfonso.rimando.gamelive.data.model.GameEntry
import ph.edu.auf.luisalfonso.rimando.gamelive.data.repository.GameRepository

class GameListViewModel: ViewModel() {
    private val gameRepository = GameRepository()
    private val _gameEntries = MutableStateFlow<List<GameEntry>>(emptyList())
    private val _searchResults = MutableStateFlow<List<GameResult>>(emptyList())
    private val _accessToken = MutableStateFlow("")
    val gameEntries: StateFlow<List<GameEntry>> = _gameEntries.asStateFlow()
    val searchResults: StateFlow<List<GameResult>> = _searchResults.asStateFlow()


    init {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            _gameEntries.value = gameRepository.getGameEntries(userId)
            _accessToken.value = try {
                gameRepository.getAccessToken()
            } catch (e: Exception) {
                Log.e("GameListViewModel", "Error fetching access token", e)
                ""
            }
        }
    }

    fun addGameEntry(gameEntry: GameEntry) {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            gameRepository.addGameEntry(userId, gameEntry)
            _gameEntries.value = gameRepository.getGameEntries(userId)
        }
    }

    fun searchGames(query: String) {
        viewModelScope.launch {
            val results = gameRepository.searchGames(query).map { game ->
                GameResult(
                    id = game.id,
                    name = game.name,
                    cover = game.cover
                )
            }
            _searchResults.value = results // Pass accessToken to searchGames
        }
    }
}
