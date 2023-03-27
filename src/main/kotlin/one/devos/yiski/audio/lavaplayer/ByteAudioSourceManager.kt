package one.devos.yiski.audio.lavaplayer

import com.sedmelluq.discord.lavaplayer.container.*
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.ProbingAudioSourceManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioItem
import com.sedmelluq.discord.lavaplayer.track.AudioReference
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException
import java.util.*

class ByteAudioSourceManager(containerRegistry: MediaContainerRegistry = MediaContainerRegistry.DEFAULT_REGISTRY) : ProbingAudioSourceManager(containerRegistry) {
    override fun getSourceName(): String = "Byte"

    override fun loadItem(manager: AudioPlayerManager, reference: AudioReference): AudioItem {
        return handleLoadResult(detectContainerForByteArray(reference))
    }

    private fun detectContainerForByteArray(reference: AudioReference): MediaContainerDetectionResult {
        try {
            ByteSeekableInputStream(Base64.getDecoder().decode(reference.identifier)).use { inputStream ->
                return MediaContainerDetection(
                    containerRegistry, reference, inputStream,
                    MediaContainerHints.from(null, null)
                ).detectContainer()
            }
        } catch (e: IOException) {
            throw FriendlyException("Failed to read ByteArray.", FriendlyException.Severity.SUSPICIOUS, e)
        }
    }

    override fun isTrackEncodable(track: AudioTrack): Boolean = true

    override fun encodeTrack(track: AudioTrack, output: DataOutput) {
        encodeTrackFactory((track as ByteAudioTrack).containerTrackFactory, output)
    }

    override fun decodeTrack(trackInfo: AudioTrackInfo, input: DataInput): AudioTrack? {
        val containerTrackFactory = decodeTrackFactory(input)

        if (containerTrackFactory != null) {
            return ByteAudioTrack(trackInfo, containerTrackFactory, this)
        }

        return null
    }

    override fun shutdown() {
        // I am a potato
    }

    override fun createTrack(trackInfo: AudioTrackInfo, containerTrackFactory: MediaContainerDescriptor): AudioTrack {
        return ByteAudioTrack(trackInfo, containerTrackFactory, this)
    }

}