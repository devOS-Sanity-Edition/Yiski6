package one.devos.yiski.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer

data class PlayerUnity(val player: AudioPlayer, val scheduler: TrackScheduler)