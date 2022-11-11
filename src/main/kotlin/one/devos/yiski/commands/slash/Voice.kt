package one.devos.yiski.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.interactions.components.StringSelectMenu
import dev.minn.jda.ktx.interactions.components.option
import dev.minn.jda.ktx.messages.editMessage
import dev.minn.jda.ktx.messages.into
import dev.minn.jda.ktx.messages.reply_
import kotlinx.coroutines.withTimeoutOrNull
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import one.devos.yiski.tables.MembersSetting
import one.devos.yiski.tiktok.Voices
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import xyz.artrinix.aviation.command.slash.SlashContext
import xyz.artrinix.aviation.command.slash.annotations.SlashCommand
import xyz.artrinix.aviation.command.slash.annotations.SlashSubCommand
import xyz.artrinix.aviation.entities.Scaffold
import java.time.Instant
import kotlin.time.Duration.Companion.minutes


@SlashCommand("voice", "Manage voice settings for yourself", guildOnly = true)
class Voice : Scaffold {

    @SlashSubCommand("Enable TTS for your messages")
    suspend fun enable(ctx: SlashContext) {
        newSuspendedTransaction {
            val settings = MembersSetting.findById(ctx.author.idLong) ?: MembersSetting.new(ctx.author.idLong) {}
            settings.enabled = true
        }

        ctx.sendPrivate("Schizophrenia has been enabled.")
    }

    @SlashSubCommand("Disable TTS for your messages")
    suspend fun disable(ctx: SlashContext) {
        newSuspendedTransaction {
            val settings = MembersSetting.findById(ctx.author.idLong) ?: MembersSetting.new(ctx.author.idLong) {}
            settings.enabled = false
        }

        ctx.sendPrivate("Schizophrenia has been disabled.")
    }

    @SlashSubCommand("Select a voice from the list of options")
    suspend fun select(ctx: SlashContext) {
        val now = Instant.now()

        val reply = ctx.interaction.reply_(
            "Select a voice.",
            components = StringSelectMenu("$now:voice:select", "No voice selected", 1..1) {
                Voices.values().forEach { option(it.desc, it.code, emoji = Emoji.fromUnicode("\uD83D\uDD39"), default = false) }
            }.into(),
            ephemeral = true,
        ).await()

        withTimeoutOrNull(1.minutes) {
            ctx.jda.listener<StringSelectInteractionEvent>(timeout = 1.minutes) { event ->
                if (event.selectMenu.id != "$now:voice:select") return@listener

                val selectedOption = Voices.values().find { it.code == event.selectedOptions.first().value } ?: return@listener

                newSuspendedTransaction {
                    val settings = MembersSetting.findById(ctx.author.idLong) ?: MembersSetting.new(ctx.author.idLong) {}
                    settings.voiceCode = selectedOption.code
                }

                reply.editMessage(content = "You have selected **${selectedOption.desc}**.", components = emptyList()).queue()
            }
        } ?: reply.editMessage(content = "Timed out.", components = emptyList()).queue()
    }
}