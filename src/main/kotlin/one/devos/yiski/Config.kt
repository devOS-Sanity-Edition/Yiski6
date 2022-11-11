package one.devos.yiski

import com.akuleshov7.ktoml.file.TomlFileReader
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

object Config {
    private val logger: Logger = LoggerFactory.getLogger(Yiski::class.java)
    private val configPath: String = System.getProperty("yiski_config", "config.toml")

    fun loadConfig(): YiskiConfig {
        logger.info("Loading config from $configPath...")
        try {
            return TomlFileReader.decodeFromFile(serializer(), configPath)
        } catch (e: Exception) {
            logger.error("Failed to load config", e)
            exitProcess(1)
        }
    }

    @Serializable
    data class YiskiConfig(
        val bot: BotConfig,
        val database: DatabaseConfig,
        val tiktok: TikTokConfig
    ) {
        @Serializable
        data class BotConfig(
            val token: String,
            val activity: String = "LISTENING",
            val status: String = "To the voices in my head",
            val admins: Set<Long>,
            val testGuilds: MutableSet<Long>,
            val enabledByDefault: Boolean
        )

        @Serializable
        data class DatabaseConfig(
            val driver: String = "org.h2.Driver",
            val url: String = "jdbc:h2:./database.h2"
        )

        @Serializable
        data class TikTokConfig(
            var sessionId: String,
        )
    }

}