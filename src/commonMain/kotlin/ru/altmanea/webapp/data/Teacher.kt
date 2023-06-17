package ru.altmanea.webapp.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

typealias FullName = String // ФИО преподавателя

@Serializable
class Teacher(
    val firstname: String,
    val surname: String,
    val patronymic: String?,
    val disciplines: Array<Discipline>
){
    fun fullname() =
        patronymic?.let {
            "$surname $firstname $it"
        } ?: "$surname $firstname"
}

val Teacher.json
    get() = Json.encodeToString(this)