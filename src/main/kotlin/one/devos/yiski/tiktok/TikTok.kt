package one.devos.yiski.tiktok

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.jvm.Throws

class TikTok(private val sessionId: String, private val api: String = "https://api22-normal-c-useast1a.tiktokv.com") {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true

            })
        }
    }
    private val userAgent = "com.zhiliaoapp.musically/2022600030 (Linux; U; Android 7.1.2; es_ES; SM-G988N; Build/NRD90M;tt-ok/3.12.13.1)"

    @Throws(Exception::class)
    suspend fun tts(voice: Voices, text: String): String {
        val response = client.post("$api/media/api/text/speech/invoke/?text_speaker=${voice.code}&req_text=$text&speaker_map_type=0&aid=1233") {
            headers {
                append("User-Agent", userAgent)
                append("Cookie", "sessionid=$sessionId")
            }
        }.body<TikTokResponse>()

        when (response.message) {
            "Couldn't load speech. Try again." -> error("Session ID is invalid")
            "Text too long to create speech audio" -> error("Text too long to create speech audio")
        }

        return response.data!!.audio
    }

    companion object {
        @Serializable
        data class TikTokResponse(
            val message: String,
            @SerialName("status_code")
            val statusCode: Int,
            @SerialName("status_msg")
            val statusMessage: String,
            val data: TikTokResponse.Data?
        ) {
            @Serializable
            data class Data(
                @SerialName("v_str")
                val audio: String,
                val speaker: String
            )
        }
    }
}