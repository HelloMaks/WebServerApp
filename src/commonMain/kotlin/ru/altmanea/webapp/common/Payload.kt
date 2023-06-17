package ru.altmanea.webapp.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.altmanea.webapp.data.Teacher

typealias PayloadId = String // ID элемента

@Serializable
class Payload(
    val id: PayloadId,
    val teacher: Teacher
)

val Payload.json
    get() = Json.encodeToString(this)