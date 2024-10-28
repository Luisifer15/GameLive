package ph.edu.auf.luisalfonso.rimando.gamelive.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import ph.edu.auf.luisalfonso.rimando.gamelive.data.model.GameEntry
import ph.edu.auf.luisalfonso.rimando.gamelive.data.model.GameProfileEntry
import ph.edu.auf.luisalfonso.rimando.gamelive.data.model.User
import ph.edu.auf.luisalfonso.rimando.gamelive.data.repository.GameRepository

class ProfileViewModel : ViewModel() {
    private val gameRepository = GameRepository()
    private val _user = MutableStateFlow(User())
    private val _gameEntries = MutableStateFlow<List<GameProfileEntry>>(emptyList())
    private val _gameEntriesBase = MutableStateFlow<List<GameEntry>>(emptyList())

    val user: StateFlow<User> = _user.asStateFlow()
    val gameEntries: StateFlow<List<GameProfileEntry>> = _gameEntries.asStateFlow()


    init {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            _user.value = fetchUserData(userId)

            val gameEntries = withContext(Dispatchers.IO) {
                gameRepository.getGameEntries(userId)
            }

            val gameProfileEntries = gameEntries.map { entry ->
                val gameDetails = withContext(Dispatchers.IO) {
                    gameRepository.getGameDetails(entry.gameId, entry.gameTitle)
                }
                GameProfileEntry(
                    gameId = entry.gameId,
                    status = entry.status,
                    rating = entry.rating,
                    review = entry.review,
                    gameTitle = gameDetails?.name ?: "", // Provide default value if null
                    gameCoverURL = gameDetails?.cover?.url
                )
            }
            _gameEntries.value = gameProfileEntries // Update _gameEntries with the new list
        }
    }

    private suspend fun fetchUserData(userId: String): User {
        return try {
            val userDoc = FirebaseFirestore.getInstance().collection("users").document(userId).get().await()
            val user = userDoc.toObject(User::class.java) ?: User()
            Log.d("ProfileViewModel", "Fetched user data: $user")
            user
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Error fetching user data", e)
            User()
        }
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
        _user.value = User() // Clear user data
        _gameEntries.value = emptyList() // Clear game entries
        Log.d("ProfileViewModel", "User logged out")
    }


    fun updateGameEntry(updatedEntry: GameEntry) {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            gameRepository.updateGameEntry(userId, updatedEntry) // Update the entry in the repository

            // Refresh the game entries list after updating
            val gameEntries = withContext(Dispatchers.IO) {
                gameRepository.getGameEntries(userId)
            }

            val gameProfileEntries = gameEntries.map { entry ->
                val gameDetails = withContext(Dispatchers.IO) {
                    gameRepository.getGameDetails(entry.gameId, entry.gameTitle)
                }
                GameProfileEntry(
                    gameId = entry.gameId,
                    status = entry.status,
                    rating = entry.rating,
                    review = entry.review,
                    gameTitle = gameDetails?.name ?: "", // Provide default value if null
                    gameCoverURL = gameDetails?.cover?.url
                )
            }
            _gameEntries.value = gameProfileEntries // Update _gameEntries with the new list
        }
    }
}