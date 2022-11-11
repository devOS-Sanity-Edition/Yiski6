package one.devos.yiski.tables

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object LinkedChannels : LongIdTable("linked_channels") {
    val textChannel = long("text_channel")
    val voiceChannel = long("voice_channel")
}

class LinkedChannel(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<LinkedChannel>(LinkedChannels)

    var textChannel by LinkedChannels.textChannel
    var voiceChannel by LinkedChannels.voiceChannel
}