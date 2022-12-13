package one.devos.yiski.tables

import one.devos.yiski.Yiski
import one.devos.yiski.tiktok.Voices
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object MembersSettings : LongIdTable("member_settings") {
    val voiceCode = varchar("voice", 70).default(Voices.DEFAULT.code)
    val enabled = bool("enabled").default(Yiski.config.bot.enabledByDefault)
    val blacklisted = bool("blacklisted").default(false)
}

class MembersSetting(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<MembersSetting>(MembersSettings)

    var voiceCode by MembersSettings.voiceCode
    var enabled by MembersSettings.enabled
    var blacklisted by MembersSettings.blacklisted
}