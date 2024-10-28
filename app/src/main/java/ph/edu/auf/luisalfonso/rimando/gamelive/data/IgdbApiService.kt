package ph.edu.auf.luisalfonso.rimando.gamelive.data

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface IgdbApiService {
    @GET("games")
    suspend fun searchGames(
        @Query("search") query: String,
        @Query("fields") fields: String = "name,cover.url"
    ): List<GameResult>

    @POST("games")
    suspend fun searchGamesFromId(
        @Query("search") query: String?,
        @Query("where") whereClause: String,
        @Query("fields") fields: String = "name,cover.url"
    ): List<GameResult>
}

data class GameResult(
    val id: Long,
    val name: String,
    val cover: Cover?,
)

data class Cover(
    val url: String?
)

object IgdbApi {
    private const val BASE_URL = "https://api.igdb.com/v4/"

    fun createService(accessToken: String, clientId: String): IgdbApiService {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthenticationInterceptor(accessToken, clientId))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(IgdbApiService::class.java)
    }
}