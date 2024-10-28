package ph.edu.auf.luisalfonso.rimando.gamelive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ph.edu.auf.luisalfonso.rimando.gamelive.data.ValidationUtils
import ph.edu.auf.luisalfonso.rimando.gamelive.data.repository.GameRepository

class LoginViewModel: ViewModel() {
    val gameRepository = GameRepository()
    private val auth = FirebaseAuth.getInstance()
    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState



    fun validateAndLogin(email: String, password: String, navController: NavController) {
        // Clear any previous errors
        clearError()

        // Validate email
        when (val emailResult = ValidationUtils.validateEmail(email)) {
            is ValidationUtils.ValidationResult.Error -> {
                setError(emailResult.message)
                return
            }
            else -> {}
        }

        // Validate password
        when (val passwordResult = ValidationUtils.validateLoginPassword(password)) {
            is ValidationUtils.ValidationResult.Error -> {
                setError(passwordResult.message)
                return
            }
            else -> {}
        }

        // If we get here, validation passed - proceed with login
        login(email, password, navController)
    }

    private fun setError(message: String) {
        _errorState.value = message
    }

    private fun clearError() {
        _errorState.value = null
    }


    fun login (email: String, password: String, navController: NavController) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                {
                    // Navigate to game list screen
                    navController.navigate("gameList")
                } else {
                    setError("An error occurred during login")
                }
            }

    }
}