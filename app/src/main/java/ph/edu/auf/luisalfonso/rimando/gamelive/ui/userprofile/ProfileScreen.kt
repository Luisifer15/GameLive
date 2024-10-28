package ph.edu.auf.luisalfonso.rimando.gamelive.ui.userprofile

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import ph.edu.auf.luisalfonso.rimando.gamelive.R
import ph.edu.auf.luisalfonso.rimando.gamelive.data.GameResult
import ph.edu.auf.luisalfonso.rimando.gamelive.data.GameStatusUtils
import ph.edu.auf.luisalfonso.rimando.gamelive.viewmodel.ProfileViewModel
import ph.edu.auf.luisalfonso.rimando.gamelive.data.model.GameProfileEntry
import ph.edu.auf.luisalfonso.rimando.gamelive.data.model.GameEntry

@Composable
fun ProfileScreen(navController: NavController, viewModel: ProfileViewModel = viewModel()) {
    val user by viewModel.user.collectAsState()
    val gameEntries by viewModel.gameEntries.collectAsState()
    var selectedGame by remember { mutableStateOf<GameProfileEntry?>(null) } // Add this line
    var showDialog by remember { mutableStateOf(false) } // Add this line

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF000B18),
                        Color(0xFF006064)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Profile Header Section with Navigation Buttons
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF001F2A)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Navigation Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { navController.navigate("gameList") },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = Color(0xFF00BCD4),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Return to Search",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = {
                                viewModel.logout()
                                navController.navigate("login") },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = Color(0xFFE57373),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Logout",
                                tint = Color.White
                            )
                        }
                    }

                    // User Info Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = user.username,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = user.email,
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        // Stats Card
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF00BCD4)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = gameEntries.size.toString(),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Games",
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Games Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Games",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                TextButton(onClick = { /* Filter functionality */ }) {
                    Text(
                        text = "Filter",
                        color = Color(0xFF00BCD4)
                    )
                }
            }

            // Games Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(gameEntries) { entry ->
                    GameEntryGridItem(entry) { game ->
                        selectedGame = game
                        showDialog = true
                    }
                }
            }
        }
    }

    if (showDialog && selectedGame != null) {
        ProfileGameReviewDialog(
            game = selectedGame!!, // Pass the selectedGame
            onDismiss = { showDialog = false },
            onSave = { rating, review, status ->
                val updatedEntry = GameEntry(
                    gameId = selectedGame!!.gameId,
                    gameTitle = selectedGame!!.gameTitle,
                    rating = rating,
                    review = review,
                    status = status
                )
                viewModel.updateGameEntry(updatedEntry) // Update the game entry in the ViewModel
                showDialog = false
            }
        )
    }
}

@Composable
fun GameEntryGridItem(entry: GameProfileEntry,  onGameClick: (GameProfileEntry) -> Unit) {
    val baseUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/"
    val coverUrl = entry.gameCoverURL?.let { baseUrl + it.substringAfterLast("/") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF001F2A)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.8f)
                    .clickable { onGameClick(entry) },
            ) {
                var isLoading by remember { mutableStateOf(true) }
                var imageFailedToLoad by remember { mutableStateOf(false) }

                AsyncImage(
                    model = coverUrl,
                    contentDescription = entry.gameTitle,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop,
                    onState = { state ->
                        isLoading = state is AsyncImagePainter.State.Loading
                        imageFailedToLoad = state is AsyncImagePainter.State.Error
                    }
                )

                // Status Badge

                val gameStatus = GameStatusUtils.getStatusString(entry.status)
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd),
                    colors = CardDefaults.cardColors(
                        containerColor = when (gameStatus) {
                            "Finished" -> Color(0xFF4CAF50) //Completed
                            "Playing" -> Color(0xFF2196F3) //Playing
                            "To be played" -> Color(0xFFFFC107) //Planned
                            else -> Color(0xFF9E9E9E)
                        }
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = gameStatus,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        color = Color.White
                    )
                }

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF00BCD4)
                    )
                }

                if (imageFailedToLoad) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF001F2A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.no_image),
                            contentDescription = "No image found",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.2f)
                    .padding(8.dp)
            ) {
                Text(
                    text = entry.gameTitle ?: "Unknown Title",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                entry.rating?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        repeat((it / 2).toInt()) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        repeat(5 - (it / 2).toInt()) {
                            Icon(
                                imageVector = Icons.Outlined.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileGameReviewDialog(
    game: GameProfileEntry,
    onDismiss: () -> Unit,
    onSave: (Int?, String?, Int) -> Unit
) {
    var rating by remember { mutableStateOf(game.rating) }
    var review by remember { mutableStateOf(game.review) }
    var status by remember { mutableIntStateOf(game.status) }
    var expanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = true,
            dismissOnBackPress = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF001F2A)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Title
                Text(
                    text = "Edit your Review for ${game.gameTitle}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                // Rating Section
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RATING",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${rating ?: 0}/10",
                            color = Color(0xFF00BCD4),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = (rating ?: 0).toFloat(),
                        onValueChange = { rating = it.toInt() },
                        valueRange = 0f..10f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00BCD4),
                            activeTrackColor = Color(0xFF00BCD4),
                            inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                        )
                    )
                }

                // Review Section
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "YOUR REVIEW",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                    TextField(
                        value = review ?: "",
                        onValueChange = { review = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        colors = TextFieldDefaults.textFieldColors(
                            focusedTextColor = Color.White,
                            cursorColor = Color.White,
                            unfocusedTextColor = Color.White,
                            containerColor = Color.White.copy(alpha = 0.1f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = {
                            Text(
                                "Share your thoughts about the game...",
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    )
                }

                // Status Section
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "PLAY STATUS",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            TextField(
                                value = GameStatusUtils.getStatusString(status),
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = expanded
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                colors = TextFieldDefaults.textFieldColors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    containerColor = Color.White.copy(alpha = 0.1f),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(Color(0xFF001F2A))
                            ) {
                                (0..3).forEach { value ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = GameStatusUtils.getStatusString(value),
                                                color = Color.White
                                            )
                                        },
                                        onClick = {
                                            status = value
                                            expanded = false
                                        },
                                        colors = MenuDefaults.itemColors(
                                            textColor = Color.White
                                        ),
                                        modifier = Modifier.background(
                                            color = if (status == value) {
                                                Color.White.copy(alpha = 0.1f)
                                            } else {
                                                Color.Transparent
                                            }
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Cancel",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { onSave(rating, review, status) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00BCD4)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Save",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}