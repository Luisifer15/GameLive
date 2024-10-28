package ph.edu.auf.luisalfonso.rimando.gamelive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.*
import ph.edu.auf.luisalfonso.rimando.gamelive.ui.gamelist.GameListScreen
import ph.edu.auf.luisalfonso.rimando.gamelive.ui.login.LoginScreen
import ph.edu.auf.luisalfonso.rimando.gamelive.ui.login.RegistrationScreen
import ph.edu.auf.luisalfonso.rimando.gamelive.ui.userprofile.ProfileScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavigation()
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
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
        } // Add this line
    }
}