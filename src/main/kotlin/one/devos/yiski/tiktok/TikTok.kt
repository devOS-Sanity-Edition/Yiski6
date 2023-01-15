package one.devos.yiski.tiktok

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.jvm.Throws

class TikTok(private val api: String = "tiktok-tts.weilnet.workers.dev") {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }
    @Throws(Exception::class)
    suspend fun tts(voice: Voices, text: String): String {
        val response = client.post("https://$api/api/generation") {
            contentType(ContentType.Application.Json)
            setBody(WelibyteRequest(text,  voice.code))
        }.body<WelibyteResponse>()

        if (response.success) {
            return response.data!!
        } else {
            error(response.error!!)
        }
    }

    companion object {
        @Serializable
        data class WelibyteRequest(
            val text: String,
            val voice: String
        )

        @Serializable
        data class WelibyteResponse(
            val success: Boolean,
            val data: String? = null,
            val error: String? = null
        )
    }
}