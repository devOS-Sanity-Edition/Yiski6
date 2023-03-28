package one.devos.yiski.commands.slash

import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import one.devos.yiski.audio.Audio
import xyz.artrinix.aviation.command.slash.SlashContext
import xyz.artrinix.aviation.command.slash.annotations.Description
import xyz.artrinix.aviation.command.slash.annotations.SlashCommand
import xyz.artrinix.aviation.command.slash.annotations.SlashSubCommand
import xyz.artrinix.aviation.entities.Scaffold

@SlashCommand("channel", "Commands regarding voice channels", guildOnly = true, botPermissions = [Permission.VOICE_CONNECT, Permission.VOICE_SPEAK])
class Channel : Scaffold {
    @SlashSubCommand("Gets the bot to join the voice channel and start listening to the assigned text/voice channel")
    suspend fun join(ctx: SlashContext, @Description("The voice channel to join") channel: VoiceChannel) {
        val reply = ctx.interaction.deferReply()
        try {
            Audio.tryConnect(ctx.guild!!.audioManager, channel)
            reply.setContent("Joined ${channel.asMention}, ready to speak your mind!").await()
        } catch (e: IllegalArgumentException) {
            reply.setContent("I could not join ${channel.asMention}, try again later.").await()
        } catch (e: UnsupportedOperationException) {
            reply.setContent("Something went horribly wrong.").await()
        } catch (e: InsufficientPermissionException) {
            reply.setContent("I don't have permission to join that channel.").await()
        }
    }

    @SlashSubCommand("Makes the bot leave the voice channel and stop listening to the text channel")
    suspend fun leave(ctx: SlashContext) {
        val channel = ctx.guild!!.audioManager.connectedChannel ?: return ctx.sendPrivate("I'm not connected to a voice channel?")

        ctx.guild!!.audioManager.closeAudioConnection()
        ctx.send("Left ${channel.asMention}, no longer transmitting the voices in our head.")
    }
}