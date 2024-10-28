package ph.edu.auf.luisalfonso.rimando.gamelive.ui.gamelist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import ph.edu.auf.luisalfonso.rimando.gamelive.R
import ph.edu.auf.luisalfonso.rimando.gamelive.data.GameResult
import ph.edu.auf.luisalfonso.rimando.gamelive.data.GameStatusUtils
import ph.edu.auf.luisalfonso.rimando.gamelive.data.model.GameEntry
import ph.edu.auf.luisalfonso.rimando.gamelive.viewmodel.GameListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameListScreen(navController: NavController, viewModel: GameListViewModel = viewModel()) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    var selectedGame by remember { mutableStateOf<GameResult?>(null) }
    var showDialog by remember { mutableStateOf(false) }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF000B18),  // Dark blue
                        Color(0xFF006064)   // Teal
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top Bar with Profile Button and Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GAMELIVE",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00BCD4)
                )

                IconButton(
                    onClick = { navController.navigate("profile") },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color(0xFF001F2A),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        Icons.Filled.AccountCircle,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Search Bar
            TextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.searchGames(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = {
                    Text(
                        "Search your favorite games...",
                        color = Color.White.copy(alpha = 0.5f)
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                },
                colors = TextFieldDefaults.textFieldColors(
                    focusedTextColor = Color.White,
                    cursorColor = Color.White,
                    unfocusedTextColor = Color.White,
                    containerColor = Color.White.copy(alpha = 0.1f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Game Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(searchResults) { game ->
                   GameCoverItem(game) {
                        selectedGame = it
                        showDialog = true
                    }
                }
            }
        }

        // Dialog
        if (showDialog && selectedGame != null) {
            GameReviewDialog(
                game = selectedGame!!,
                onDismiss = { showDialog = false },
                onSave = { rating, review, status ->
                    viewModel.addGameEntry(
                        GameEntry(
                            gameId = selectedGame!!.id,
                            gameTitle = selectedGame!!.name,
                            rating = rating,
                            review = review,
                            status = status
                        )
                    )
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun GameCoverItem(game: GameResult, onGameClick: (GameResult) -> Unit) {
    val baseUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/"
    val coverUrl = game.cover?.url?.let { baseUrl + it.substringAfterLast("/") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .clickable { onGameClick(game) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF001F2A)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Game Cover Image
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.3f)
                    )
            ) {
                var isLoading by remember { mutableStateOf(true) }
                var imageFailedToLoad by remember { mutableStateOf(false) }

                AsyncImage(
                    model = coverUrl,
                    contentDescription = game.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onState = { state ->
                        isLoading = state is AsyncImagePainter.State.Loading
                        imageFailedToLoad = state is AsyncImagePainter.State.Error
                    }
                )

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

                // Gradient overlay for text visibility
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF000B18).copy(alpha = 0.9f)
                                ),
                                startY = 300f,
                                endY = 900f
                            )
                        )
                )

                // Game Title
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = game.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameReviewDialog(
    game: GameResult,
    onDismiss: () -> Unit,
    onSave: (Int?, String?, Int) -> Unit
) {
    var rating by remember { mutableStateOf<Int?>(null) }
    var review by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableIntStateOf(0) }
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
                    text = "Review ${game.name}",
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
                            onExpandedChange = { expanded = !it }
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
