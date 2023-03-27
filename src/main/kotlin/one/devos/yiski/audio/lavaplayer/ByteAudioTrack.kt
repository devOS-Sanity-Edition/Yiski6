package one.devos.yiski.audio.lavaplayer

import com.sedmelluq.discord.lavaplayer.container.MediaContainerDescriptor
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack
import com.sedmelluq.discord.lavaplayer.track.InternalAudioTrack
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor
import java.util.*

class ByteAudioTrack(
    trackInfo: AudioTrackInfo,
    val containerTrackFactory: MediaContainerDescriptor,
    private val sourceManager: ByteAudioSourceManager
) : DelegatedAudioTrack(trackInfo) {
    private val byteArray: ByteArray = createBufferedByteArray()

    private fun createBufferedByteArray(): ByteArray {
        val decodedTrack = Base64.getDecoder().decode(trackInfo.identifier)
        val paddingSize = 6
        val newArray = ByteArray(decodedTrack.size + paddingSize)
        System.arraycopy(decodedTrack, 0, newArray, paddingSize, decodedTrack.size - paddingSize)
        return newArray
    }


    override fun process(executor: LocalAudioTrackExecutor) {
        ByteSeekableInputStream(byteArray).use { inputStream ->
            processDelegate(
                containerTrackFactory.createTrack(
                    trackInfo,
                    inputStream
                ) as InternalAudioTrack, executor
            )
        }
    }

    override fun makeShallowClone(): AudioTrack = ByteAudioTrack(trackInfo, containerTrackFactory, sourceManager)

    override fun getSourceManager(): AudioSourceManager = sourceManager
}