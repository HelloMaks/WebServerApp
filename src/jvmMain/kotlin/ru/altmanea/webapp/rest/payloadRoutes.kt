package ru.altmanea.webapp.rest

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.litote.kmongo.*
import ru.altmanea.webapp.auth.authorization
import ru.altmanea.webapp.common.Payload
import ru.altmanea.webapp.common.PayloadId
import ru.altmanea.webapp.config.Config
import ru.altmanea.webapp.data.*
import ru.altmanea.webapp.repo.parsingData

fun Route.payloadRoutes() {
    route(Config.payloadPath) {
        authenticate("auth-jwt") {
            authorization(setOf(roleAdmin)) {
                /* Запрос на загрузку документов в базу данных */
                post {
                    val payloads: List<Payload> =
                        call.receive<List<Teacher>>().map { teacher ->
                            Payload(
                                id = newId<Payload>().toString(),
                                teacher = teacher
                            )
                        } // Добавление элементам метаданных

                    if(parsingData.find().toList().isEmpty()) {
                        parsingData.insertMany(payloads) // Добавление документов в MongoDB
                        call.respondText("Documents in MongoDB correctly added", status = HttpStatusCode.OK)
                    }
                    else {
                        call.respondText("MongoDB has already been updated", status = HttpStatusCode.BadRequest)
                    }
                }

                /* Запрос на удаление документов из базы данных */
                delete {
                    if(parsingData.find().toList().isNotEmpty()) {
                        parsingData.deleteMany() // Удаление всех данных из MongoDB
                        call.respondText("Documents from MongoDB correctly deleted", status = HttpStatusCode.OK)
                    }
                    else {
                        call.respondText("Documents in MongoDB have already been deleted",
                            status = HttpStatusCode.BadRequest)
                    }
                }

                /* Запрос на удаление конкретного преподавателя из базы данных */
                delete("{id}") {
                    val id = call.parameters["id"] ?: return@delete call.respondText(
                        "Missing or malformed ID", status = HttpStatusCode.BadRequest)

                    if(parsingData.findOne(Payload::id eq id) != null) {
                        parsingData.deleteOne(Payload::id eq id) // Удаление конкретного документа из MongoDB
                        call.respondText("The document from MongoDB correctly deleted", status = HttpStatusCode.OK)
                    }
                    else {
                        call.respondText("The document from MongoDB has already been deleted",
                            status = HttpStatusCode.BadRequest)
                    }
                }
            }
            authorization(roleList.toSet()) {
                /* Запрос на проверку наличия информации в базе данных */
                get("check") {
                    val check = parsingData.find().toList().isNotEmpty() // Проверка на наличие данных
                    call.respond(check)
                }

                /* Запрос на предоставление информации для вывода ссылок */
                get("links") {
                    val mapData: Map<PayloadId, FullName> =
                        parsingData.find().toList().associate { payload ->
                            payload.id to payload.teacher.fullname()
                        } // Соотношение ID преподавателей с их ФИО

                    call.respond(mapData)
                }

                /* Запрос на предоставление информации для общей таблицы */
                get("table/common") {
                    val payloadTeachers: List<Payload> =
                        parsingData.find().toList() // Изъятие всех документов из MongoDB

                    call.respond(payloadTeachers.map { it.teacher })
                }

                /* Запрос на предоставление информации для таблицы конкретного преподавателя */
                get("table/{id}") {
                    val id = call.parameters["id"] ?: return@get call.respondText(
                        "Missing or malformed ID", status = HttpStatusCode.BadRequest)

                    val payload: Payload? =
                        parsingData.findOne(Payload::id eq id) // Изъятие нужного документа из MongoDB

                    payload?.let {
                        call.respond(it.teacher)
                    } ?: call.respondText("An element`s ID is not found", status = HttpStatusCode.NotFound)
                }
            }
        }
    }
}