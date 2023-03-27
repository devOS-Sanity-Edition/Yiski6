package one.devos.yiski.audio.lavaplayer

import com.sedmelluq.discord.lavaplayer.tools.io.ExtendedBufferedInputStream
import com.sedmelluq.discord.lavaplayer.tools.io.SeekableInputStream
import com.sedmelluq.discord.lavaplayer.track.info.AudioTrackInfoProvider
import java.io.IOException

class ByteSeekableInputStream(bytes: ByteArray) : SeekableInputStream(bytes.size.toLong(), 0) {
    private val inputStream = bytes.inputStream()
    private val bufferedStream = ExtendedBufferedInputStream(inputStream)

    private var position: Long = 0

    @Throws(IOException::class)
    override fun available(): Int = bufferedStream.available()

    override fun getPosition(): Long = position

    override fun markSupported(): Boolean = false

    override fun canSeekHard(): Boolean = true

    @Throws(IOException::class)
    override fun read(): Int {
        val result = bufferedStream.read()

        if (result >= 0) {
            position++
        }

        return result
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val read = bufferedStream.read(b, off, len)
        position += read
        return read
    }

    @Throws(IOException::class)
    override fun skip(n: Long): Long {
        val skipped = bufferedStream.skip(n)
        position += skipped
        return skipped
    }

    @Throws(IOException::class)
    override fun seekHard(position: Long) {
        this.position = position
        bufferedStream.discardBuffer()
    }

    override fun getTrackInfoProviders(): MutableList<AudioTrackInfoProvider> = mutableListOf()
}