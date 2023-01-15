package one.devos.yiski

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
import net.dv8tion.jda.api.audio.AudioSendHandler
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.managers.AudioManager
import one.devos.yiski.lavaplayer.ByteAudioSourceManager
import one.devos.yiski.tiktok.Voices
import java.nio.ByteBuffer
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

object Audio {
    val players: MutableMap<Long, PlayerUnity> = mutableMapOf()

    val playerManager = DefaultAudioPlayerManager().apply {
        AudioSourceManagers.registerRemoteSources(this)
        AudioSourceManagers.registerLocalSource(this)
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
            true
        } else {
            false
        }
    }

    suspend fun executeTikTok(text: String, voice: Voices, guild: Guild) {
        val (_, trackScheduler) = getOrCreatePlayer(guild)

        val parsedText = text
            .let { Regex("http\\S+", RegexOption.MULTILINE).replace(it, "link") }
            .let { Regex("[^(\\x00-\\xFF)]+(?:\$|\\s*)", RegexOption.MULTILINE).replace(it, "") }
            .replace("\n", ", ")
            .replace("&", "and")
            .replace("%", "percent")
            .replace("+", "plus")

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

    data class PlayerUnity(val player: AudioPlayer, val scheduler: TrackScheduler)

    class TrackScheduler(private val player: AudioPlayer, var queue: BlockingQueue<AudioTrack> = LinkedBlockingQueue()) : AudioEventAdapter() {
        /**
         * Play the track right away
         *
         * @param track The track to play
         */
        fun play(track: AudioTrack) {
            player.startTrack(track, false)
        }

        /**
         * Add the next track to queue or play right away if nothing is in the queue.
         *
         * @param track The track to play or add to queue.
         */
        fun queue(track: AudioTrack) {
            // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
            // something is playing, it returns false and does nothing. In that case the player was already playing so this
            // track goes to the queue instead.
            if (!player.startTrack(track, true)) {
                queue.offer(track)
            }
        }

        /**
         * Start the next track, stopping the current one if it is playing.
         */
        fun nextTrack() {
            // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
            // giving null to startTrack, which is a valid argument and will simply stop the player.
            player.startTrack(queue.poll(), false)
        }

        override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
            // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
            if (endReason.mayStartNext) {
                nextTrack()
            }
        }
    }

    class AudioPlayerSendHandler(private val audioPlayer: AudioPlayer) : AudioSendHandler {
        private var lastFrame: AudioFrame? = null
        override fun canProvide(): Boolean {
            lastFrame = audioPlayer.provide()
            return lastFrame != null
        }

        override fun provide20MsAudio(): ByteBuffer {
            return ByteBuffer.wrap(lastFrame!!.data)
        }

        override fun isOpus(): Boolean {
            return true
        }
    }
}
