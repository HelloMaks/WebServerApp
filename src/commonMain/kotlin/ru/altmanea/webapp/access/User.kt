package ru.altmanea.webapp.access

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
open class User(
    val username: String,
    val password: String
)

val User.json
    get() = Json.encodeToString(this)