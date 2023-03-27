package one.devos.yiski

import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.audio.hooks.ConnectionListener
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.managers.AudioManager
import one.devos.yiski.audio.AudioPlayerSendHandler
import one.devos.yiski.audio.PlayerUnity
import one.devos.yiski.audio.TrackScheduler
import one.devos.yiski.audio.lavaplayer.ByteAudioSourceManager
import one.devos.yiski.tiktok.Voices

object Audio {
    /**
     * A [MutableMap] with the [Guild]'s idLong as the key and [PlayerUnity] as the value.
     */
    val players: MutableMap<Long, PlayerUnity> = mutableMapOf()

    private val playerManager = DefaultAudioPlayerManager().apply {
        AudioSourceManagers.registerRemoteSources(this)
        AudioSourceManagers.registerLocalSource(this)
        configuration.resamplingQuality = AudioConfiguration.ResamplingQuality.HIGH
        registerSourceManager(ByteAudioSourceManager())
    }

    fun getOrCreatePlayer(guild: Guild): PlayerUnity {
        if (players[guild.idLong] != null) return players[guild.idLong]!!

        val player = playerManager.createPlayer()
        val trackScheduler = TrackScheduler(player)

        player.addListener(trackScheduler)

        guild.audioManager.sendingHandler = AudioPlayerSendHandler(player)

        val unity = PlayerUnity(player, trackScheduler)

        players[guild.idLong] = unity
        return unity
    }

    @Throws(IllegalArgumentException::class, UnsupportedOperationException::class, InsufficientPermissionException::class)
    fun tryConnect(audioManager: AudioManager, channel: VoiceChannel): Boolean {
        return if (!audioManager.isConnected) {
            audioManager.openAudioConnection(channel)
            audioManager.connectionListener = object : ConnectionListener {
                override fun onPing(ping: Long) {
                    return
                }

                override fun onStatusChange(status: ConnectionStatus) {
                    when(status) {
                        ConnectionStatus.DISCONNECTED_KICKED_FROM_CHANNEL,
                        ConnectionStatus.DISCONNECTED_REMOVED_FROM_GUILD,
                        ConnectionStatus.DISCONNECTED_CHANNEL_DELETED,
                        ConnectionStatus.DISCONNECTED_LOST_PERMISSION,
                        ConnectionStatus.DISCONNECTED_REMOVED_DURING_RECONNECT -> {
                            val (_, schedule) = players[audioManager.guild.idLong] ?: return
                            schedule.player.stopTrack()
                            schedule.queue.clear()
                        }

                        else -> return
                    }
                }

                override fun onUserSpeaking(user: User, speaking: Boolean) {

                }
            }
            true
        } else {
            false
        }
    }

    suspend fun executeTikTok(text: String, voice: Voices, guild: Guild) {
        val (_, trackScheduler) = getOrCreatePlayer(guild)

        // Filtering the text before it gets sent to the API
        val parsedText = text
            .let { Regex("http\\S+", RegexOption.MULTILINE).replace(it, "link") }
            .let { Regex("[^(\\x00-\\xFF)]+(?:\$|\\s*)", RegexOption.MULTILINE).replace(it, "") }
            .replace("\n", ", ")
            .replace("&", "and")
            .replace("%", "percent")
            .replace("+", "plus")
            .replace("*", "")

        // Chunking the messages because the API has a character limit
        parsedText.chunked(300).forEach {
            val data = Yiski.tiktok.tts(voice, it)

            loadItem(data, trackScheduler)
        }
    }

    fun loadItem(data: String, scheduler: TrackScheduler, instant: Boolean = false) {
        playerManager.loadItem(data, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                if (instant) scheduler.play(track) else scheduler.queue(track)
            }

            override fun playlistLoaded(playlist: AudioPlaylist?) {}

            override fun noMatches() {}

            override fun loadFailed(exception: FriendlyException?) {}
        })
    }
}
