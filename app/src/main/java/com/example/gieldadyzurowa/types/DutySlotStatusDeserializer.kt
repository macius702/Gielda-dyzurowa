package com.example.gieldadyzurowa.types

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type as ReflectType


class DutySlotStatusDeserializer : JsonDeserializer<DutySlotStatus> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: ReflectType?,
        context: JsonDeserializationContext?
    ): DutySlotStatus {
        val status = json?.asString
        return DutySlotStatus.from(
            status ?: throw JsonParseException("Null or invalid status value")
        )
    }
}
