package ph.edu.auf.luisalfonso.rimando.gamelive.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ph.edu.auf.luisalfonso.rimando.gamelive.data.ValidationUtils
import ph.edu.auf.luisalfonso.rimando.gamelive.data.model.User

class RegistrationViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState

    fun validateAndRegister(name: String, email: String, password: String, confirmPassword: String, navController: NavController) {
        // Clear any previous errors
        clearError()

        // Validate name
        when (val nameResult = ValidationUtils.validateField(name, "Name")) {
            is ValidationUtils.ValidationResult.Error -> {
                setError(nameResult.message)
                return
            }
            else -> {}
        }

        // Validate email
        when (val emailResult = ValidationUtils.validateEmail(email)) {
            is ValidationUtils.ValidationResult.Error -> {
                setError(emailResult.message)
                return
            }
            else -> {}
        }

        // Validate password
        when (val passwordResult = ValidationUtils.validatePassword(password, confirmPassword)) {
            is ValidationUtils.ValidationResult.Error -> {
                setError(passwordResult.message)
                return
            }
            else -> {}
        }

        // If we get here, validation passed - proceed with registration
        register(name, email, password, navController)
    }

    private fun setError(message: String) {
        _errorState.value = message
    }

    private fun clearError() {
        _errorState.value = null
    }
    fun register(username: String, email: String, password: String, navController: NavController?) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    val user = User(uid = userId, email = email, username = username)
                    firestore.collection("users").document(userId).set(user)
                        .addOnSuccessListener {
                            navController?.navigate("login")
                        }
                        .addOnFailureListener { e ->
                            setError("An error occurred during registration: ${e.message}")
                        }
                } else {
                    setError("An error occurred during registration: ${task.exception?.message}")
                }
            }
    }
}