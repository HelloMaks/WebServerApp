package ru.altmanea.webapp.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class Discipline(val name: String, val groups: Array<Group>)

val Discipline.json
    get() = Json.encodeToString(this)