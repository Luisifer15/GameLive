package ph.edu.auf.luisalfonso.rimando.gamelive.data

import android.util.Log
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class AuthenticationInterceptor(private val accessToken: String, private val clientId: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original: Request = chain.request()
        val request: Request = original.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .header("Client-ID", clientId)
            .build()
        return chain.proceed(request)
    }
}