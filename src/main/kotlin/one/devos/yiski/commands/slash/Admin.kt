package one.devos.yiski.commands.slash

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import one.devos.yiski.Audio
import one.devos.yiski.tables.LinkedChannel
import one.devos.yiski.tables.LinkedChannels
import one.devos.yiski.tables.MembersSetting
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import xyz.artrinix.aviation.command.slash.SlashContext
import xyz.artrinix.aviation.command.slash.annotations.Description
import xyz.artrinix.aviation.command.slash.annotations.SlashCommand
import xyz.artrinix.aviation.command.slash.annotations.SlashSubCommand
import xyz.artrinix.aviation.command.slash.annotations.SubCommandHolder
import xyz.artrinix.aviation.entities.Scaffold

@SlashCommand("admin", "Manage settings for the bot", guildOnly = true, developerOnly = true, hidden = true)
class Admin : Scaffold {
    @SubCommandHolder("channel", "Channel management commands")
    class Channel {
        @SlashSubCommand("Binds a voice channel to a text channel")
        suspend fun bind(ctx: SlashContext, @Description("Voice channel to playback messages to") voice_channel: VoiceChannel, @Description("Text channel to read messages from") text_channel: TextChannel) {
            newSuspendedTransaction {
                LinkedChannel.new {
                    textChannel = text_channel.idLong
                    voiceChannel = voice_channel.idLong
                }
            }

            ctx.sendPrivate("Bound ${voice_channel.asMention} to ${text_channel.asMention}")
        }

        @SlashSubCommand("Unbinds a text channel")
        suspend fun unbind(ctx: SlashContext, @Description("Voice channel to unbind") voice_channel: VoiceChannel) {
            return newSuspendedTransaction {
                val binding = LinkedChannel.find {
                    LinkedChannels.voiceChannel eq voice_channel.idLong
                }.firstOrNull() ?: return@newSuspendedTransaction ctx.sendPrivate("There was no channel binding to remove.")



                val textChannel = ctx.guild!!.getTextChannelById(binding.textChannel)?.asMention ?: "UNKNOWN CHANNEL"
                val voiceChannel = ctx.guild!!.getVoiceChannelById(binding.voiceChannel)?.asMention ?: "UNKNOWN CHANNEL"
                binding.delete()

                return@newSuspendedTransaction ctx.sendPrivate("Unbound $textChannel from $voiceChannel")
            }
        }
    }

    @SubCommandHolder("user", "User management commands")
    class User {
        @SlashSubCommand("Blacklist a user from using the function")
        suspend fun blacklist(ctx: SlashContext, user: Member) {
            newSuspendedTransaction {
                val settings = MembersSetting.findById(user.idLong) ?: MembersSetting.new(user.idLong) {}
                settings.blacklisted = true
            }

            ctx.sendPrivate("${user.asMention} has been blacklisted.")
        }

        @SlashSubCommand("Un-blacklist a user from using the function")
        suspend fun unblacklist(ctx: SlashContext, user: Member) {
            newSuspendedTransaction {
                val settings = MembersSetting.findById(user.idLong) ?: MembersSetting.new(user.idLong) {}
                settings.blacklisted = false
            }

            ctx.sendPrivate("${user.asMention} has been un-blacklisted.")
        }
    }


    @SubCommandHolder("queue", "Voice queue management commands")
    class Queue {
        @SlashSubCommand("Insert something to the queue")
        suspend fun insert(ctx: SlashContext, item: String) {
            val (_, schedule) = Audio.players[ctx.guild!!.idLong] ?: return ctx.sendPrivate("No voice instance has been initiated, I probably need to play a message first.")
            Audio.loadItem(item, schedule, false)

            ctx.sendPrivate("Inserted `$item` to the queue")
        }

        @SlashSubCommand("Override the currently playing item")
        suspend fun override(ctx: SlashContext, item: String) {
            val (_, schedule) = Audio.players[ctx.guild!!.idLong] ?: return ctx.sendPrivate("No voice instance has been initiated, I probably need to play a message first.")
            Audio.loadItem(item, schedule, true)

            ctx.sendPrivate("Replaced the current item with `$item`")
        }

        @SlashSubCommand("Skips the current item")
        suspend fun skip(ctx: SlashContext) {
            val (player) = Audio.players[ctx.guild!!.idLong] ?: return ctx.sendPrivate("No voice instance has been initiated, I probably need to play a message first.")
            player.playTrack(null)

            ctx.sendPrivate("Skipped the current item")
        }

        @SlashSubCommand("Purge the queue (good for if it got spammed)")
        suspend fun clear(ctx: SlashContext) {
            val (player, schedule) = Audio.players[ctx.guild!!.idLong] ?: return ctx.sendPrivate("No player is setup, have the bot be in vc first.")
            schedule.queue.clear()
            player.stopTrack()

            ctx.send("The queue has been purged.")
        }
    }
}