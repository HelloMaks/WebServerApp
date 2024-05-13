package ru.altmanea.webapp.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

typealias FullName = String // ФИО преподавателя

@Serializable
class Teacher(val fullname: String, val disciplines: Array<Discipline>)

val Teacher.json
    get() = Json.encodeToString(this)