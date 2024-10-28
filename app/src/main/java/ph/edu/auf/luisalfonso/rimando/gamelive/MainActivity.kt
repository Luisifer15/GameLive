package ph.edu.auf.luisalfonso.rimando.gamelive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import ph.edu.auf.luisalfonso.rimando.gamelive.ui.gamelist.GameListScreen
import ph.edu.auf.luisalfonso.rimando.gamelive.ui.login.LoginScreen
import ph.edu.auf.luisalfonso.rimando.gamelive.ui.login.RegistrationScreen
import ph.edu.auf.luisalfonso.rimando.gamelive.ui.userprofile.ProfileScreen

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            setContent {
                AppNavigation(currentUser)
            }
        }

        auth.addAuthStateListener(authStateListener)
    }
    override fun onDestroy() {
        super.onDestroy()
        auth.removeAuthStateListener(authStateListener)
    }

}


@Composable
fun AppNavigation(currentUser: FirebaseUser?) {
    val navController = rememberNavController()

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            navController.navigate("gameList") {
                popUpTo("login") { inclusive = true }
            }
        } else {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = if (currentUser != null) "gameList" else "login") {
        composable("login") {
            LoginScreen(navController)
        }
        composable("gameList") {
            GameListScreen(navController)
        }
        composable("registration") {
            RegistrationScreen(navController)
        }
        composable("profile") {
            ProfileScreen(navController)
        }
    }
}