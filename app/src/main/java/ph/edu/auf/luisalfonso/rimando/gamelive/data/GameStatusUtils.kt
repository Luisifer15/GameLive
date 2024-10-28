package ph.edu.auf.luisalfonso.rimando.gamelive.data

object GameStatusUtils {
    fun getStatusString(status: Int): String {
        return when (status) {
            0 -> "Not played"
            1 -> "To be played"
            2 -> "Playing"
            3 -> "Finished"
            else -> "Unknown status"
        }
    }
}