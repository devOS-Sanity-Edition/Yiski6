package one.devos.yiski.commands.slash

import net.dv8tion.jda.api.JDA
import one.devos.yiski.Yiski
import xyz.artrinix.aviation.Aviation
import xyz.artrinix.aviation.command.slash.SlashContext
import xyz.artrinix.aviation.command.slash.annotations.SlashCommand
import xyz.artrinix.aviation.entities.Scaffold

class Version : Scaffold {
    @SlashCommand("version", "Shows the bots version")
    suspend fun version(ctx: SlashContext) {
        val user = ctx.jda.getUserById(299779734932291604)
        ctx.sendPrivateEmbed {
            setThumbnail(ctx.jda.selfUser.effectiveAvatarUrl)
            addField("Yiski 6", Yiski.version, true)
            addField("JDA", JDA::class.java.`package`.implementationVersion, true)
            addField("Aviation", Aviation::class.java.`package`.implementationVersion, true)
            setFooter("Built with love, sweat & tears by ${user?.asTag ?: "Storm"}", user?.effectiveAvatarUrl)
        }
    }
}