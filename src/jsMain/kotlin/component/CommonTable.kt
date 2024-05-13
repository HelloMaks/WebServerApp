package component

import js.core.jso
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import query.QueryError
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.th
import react.dom.html.ReactHTML.tr
import react.dom.html.TdAlign
import react.useContext
import ru.altmanea.webapp.config.Config
import ru.altmanea.webapp.data.Teacher
import tanstack.query.core.QueryKey
import tanstack.react.query.useQuery
import tools.fetchText
import userInfoContext
import kotlin.js.json

val CCommonTable = FC<Props>("CommonTable") {
    val userInfo = useContext(userInfoContext) // Данные для аутентификации
    val queryPageKey = arrayOf("DataTable", "common").unsafeCast<QueryKey>() // Ключ для запроса на сервер
    /* Осуществление запроса на предоставление информации о преподавателях, их дисциплинах и группах */
    val query = useQuery<String, QueryError, String, QueryKey>(
        queryKey = queryPageKey,
        queryFn = {
            fetchText("${Config.payloadPath}table/common",
                jso {
                    headers = json(
                        "Content-Type" to "application/json",
                        "Authorization" to userInfo?.third?.authHeader
                    )
                }
            )
        }
    )
    if(query.isLoading) { div { + "Загрузка..." } }
    else if(query.isError) { div { + "Ошибка!" } }
    else {
        val teachers: Array<Teacher> = Json.decodeFromString(query.data ?: "")
        /* Вывод общей таблицы, со всеми преподавателями */
        h3 { + "Таблица со всеми преподавателями:" }
        table {
            border = 3 // Задание толщины границ таблицы
            th { + "Преподаватели" }; th { + "Дисциплины" }; th { + "Группы" } // Заголовки таблицы
            teachers.forEach { teacher ->
                teacher.disciplines.forEachIndexed { index, discipline ->
                    tr {
                        if(index == 0) {
                            td {
                                + teacher.fullname
                                align = TdAlign.center // Выравнивание по центру
                                rowSpan = teacher.disciplines.size // Объединение ячеек
                            }
                        }
                        td { + discipline.name; align = TdAlign.center }
                        td { + discipline.groups.joinToString(", ") { it.name }; align = TdAlign.center }
                    }
                }
            }
        }
    }
}