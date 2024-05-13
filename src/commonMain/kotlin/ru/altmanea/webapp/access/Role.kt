package ru.altmanea.webapp.access

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class Role(val name: String)

val Role.json
    get() = Json.encodeToString(this)