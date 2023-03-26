package one.devos.yiski.tiktok

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.*

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
        }

        if (response.status == HttpStatusCode.OK) {
            try {
                val body = response.body<WelibyteResponse>()
                if (body.success) return body.data!!
                else error(body.error!!)
            } catch (err: JsonConvertException) {
                val body = response.body<WelibyteException>()
                if (body.success) error(body.data.error)
                else error(body.error!!)
            }
        } else {
            error(response.status)
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

        @Serializable
        data class WelibyteException(
            val success: Boolean,
            val data: WelibyteError,
            val error: String? = null
        )

        @Serializable
        data class WelibyteError(
            val error: String
        )
    }
}