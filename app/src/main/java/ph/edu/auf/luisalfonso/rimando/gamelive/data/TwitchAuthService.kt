package ph.edu.auf.luisalfonso.rimando.gamelive.data

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody

class TwitchAuthService {
    private val httpClient = OkHttpClient()
    private val clientId = "cjvy9npugnzcab2b6hainknpwut5fm"

    fun getAccessToken(): String {
        val requestBody = "client_id=$clientId" +
                "&client_secret=oolg2ewzs1nsj15fh8ug9ambv9pgfv" +
                "&grant_type=client_credentials"
        val request = okhttp3.Request.Builder()
            .url("https://id.twitch.tv/oauth2/token")
            .post(requestBody.toRequestBody())
            .build()
        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string()

        val authResponse = Gson().fromJson(responseBody, TwitchAuthResponse::class.java)
        return authResponse.accessToken
    }

    fun getClientId(): String = clientId
}

data class TwitchAuthResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("token_type") val tokenType: String
)