package one.devos.yiski

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.generics.getChannel
import dev.minn.jda.ktx.jdabuilder.default
import dev.minn.jda.ktx.jdabuilder.intents
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import one.devos.yiski.audio.Audio
import one.devos.yiski.tables.LinkedChannel
import one.devos.yiski.tables.LinkedChannels
import one.devos.yiski.tables.MembersSetting
import one.devos.yiski.tables.MembersSettings
import one.devos.yiski.tiktok.TikTok
import one.devos.yiski.tiktok.Voices
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.artrinix.aviation.Aviation
import xyz.artrinix.aviation.AviationBuilder
import xyz.artrinix.aviation.events.AviationExceptionEvent
import xyz.artrinix.aviation.events.CommandFailedEvent
import xyz.artrinix.aviation.internal.utils.on
import xyz.artrinix.aviation.ratelimit.DefaultRateLimitStrategy

object Yiski {
    val version = this::class.java.`package`.implementationVersion ?: "DEV"
    val config = Config.loadConfig()

    private lateinit var jda: JDA
    lateinit var aviation: Aviation

    val logger: Logger = LoggerFactory.getLogger(Yiski::class.java)
    val database = Database.connect(config.database.url, config.database.driver, databaseConfig = DatabaseConfig { useNestedTransactions = true })
    val tiktok = TikTok()

    @JvmStatic
    fun main(args: Array<String>): Unit = runBlocking {
        logger.info("Starting DevOS: Satanic Edition - Yiski6 ($version) Schizophrenia: THE VOICES IN MY HEAD")

        transaction {
            addLogger(Slf4jSqlDebugLogger)

            logger.info("Connected to the database")

            SchemaUtils.createMissingTablesAndColumns(
                LinkedChannels,
                MembersSettings,
            )
        }

        jda = default(config.bot.token, enableCoroutines = true) {
            intents += listOf(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES)
            setActivity(Activity.of(Activity.ActivityType.valueOf(config.bot.activity.uppercase()), config.bot.status))
        }

        aviation = AviationBuilder()
            .apply {
                ratelimitProvider = DefaultRateLimitStrategy()
                doTyping = true
                developers.addAll(config.bot.developers.toTypedArray())
                registerDefaultParsers()
            }
            .build()
            .apply {
                slashCommands.register("one.devos.yiski.commands.slash")
            }

        aviation.syncCommands(jda)
        listenAviationEvents()

        jda.addEventListener(aviation)

        jda.listener<ReadyEvent> {
            logger.info("Ready")
        }

        jda.listener<MessageReceivedEvent> { event ->
            if (event.author.isBot || !event.isFromGuild) return@listener
            if (event.guild.selfMember.voiceState == null || !event.guild.selfMember.voiceState!!.inAudioChannel()) return@listener

            if (!event.member!!.voiceState!!.inAudioChannel()) return@listener
            if (event.member!!.voiceState!!.channel!!.idLong != event.guild.selfMember.voiceState!!.channel!!.idLong) return@listener

            newSuspendedTransaction {
                return@newSuspendedTransaction LinkedChannel.find {
                    (LinkedChannels.textChannel eq event.channel.idLong) and (LinkedChannels.voiceChannel eq event.guild.selfMember.voiceState!!.channel!!.idLong)
                }.firstOrNull()
            } ?: return@listener

            val voice = newSuspendedTransaction {
                MembersSetting.findById(event.author.idLong)?.let { db ->
                    if (!db.enabled) return@newSuspendedTransaction null
                    Voices.values().find {
                        it.code == db.voiceCode
                    }
                } ?: if (config.bot.enabledByDefault)
                    Voices.DEFAULT
                else
                    return@newSuspendedTransaction null
            } ?: return@listener

            Audio.executeTikTok(event.message.contentDisplay, voice, event.guild)
        }

        jda.listener<GuildVoiceUpdateEvent> { event ->
            if (config.bot.leaveVoiceChannelAutomatically == false) return@listener
            if (event.oldValue == null) return@listener
            if (event.newValue?.idLong == event.oldValue?.idLong) return@listener

            val voiceChannel = event.oldValue!!

            if (voiceChannel.members.contains(event.guild.selfMember)) {
                if (voiceChannel.members.none { it.idLong != jda.selfUser.idLong }) {
                    event.guild.audioManager.closeAudioConnection()

                    val textChannelId =
                        newSuspendedTransaction {
                            return@newSuspendedTransaction LinkedChannel
                                .find(LinkedChannels.voiceChannel eq voiceChannel.idLong)
                                .firstOrNull()
                        }
                        ?.textChannel
                        ?: return@listener

                    event.guild.getChannel<TextChannel>(textChannelId)
                        ?.sendMessage("I have left ${voiceChannel.asMention} because it got lonely.")
                        ?.await()
                        ?: return@listener
                }
            }
        }
    }

    private fun listenAviationEvents() {
        aviation.on<AviationExceptionEvent> {
            logger.error("Aviation threw an exception", this.error)
        }

        aviation.on<CommandFailedEvent> {
            logger.error("[Command Execution] A command has failed. ", this.error)
        }
    }
}