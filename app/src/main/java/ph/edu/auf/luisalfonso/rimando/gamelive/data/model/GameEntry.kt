package ph.edu.auf.luisalfonso.rimando.gamelive.data.model

data class GameEntry (
    val gameId: Long = 0, // ID of the game from IGDB
    val gameTitle: String? = null,
    val status: Int = 0, // 0 - Not played, 1 - To be played, 2 - Playing, 3 - Finished
    val rating: Int? = null,
    val review: String? = null,

)