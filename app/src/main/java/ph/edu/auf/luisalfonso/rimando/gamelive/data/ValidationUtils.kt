package ph.edu.auf.luisalfonso.rimando.gamelive.data

object ValidationUtils {
    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }

    fun validateField(value: String, fieldName: String): ValidationResult {
        return when {
            value.isBlank() -> ValidationResult.Error("$fieldName cannot be empty")
            else -> ValidationResult.Success
        }
    }

    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Error("Email cannot be empty")
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                ValidationResult.Error("Please enter a valid email address")
            else -> ValidationResult.Success
        }
    }

    fun validateLoginPassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Error("Password cannot be empty")
            password.length < 6 -> ValidationResult.Error("Password must be at least 6 characters")
            else -> ValidationResult.Success
        }
    }

    fun validatePassword(password: String, confirmPassword: String): ValidationResult {
        return when {
            password != confirmPassword -> ValidationResult.Error("Passwords do not match")
            password.isBlank() -> ValidationResult.Error("Password cannot be empty")
            password.length < 6 -> ValidationResult.Error("Password must be at least 6 characters")
            else -> ValidationResult.Success
        }
    }
}