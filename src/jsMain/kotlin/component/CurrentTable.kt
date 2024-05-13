package component

import js.core.get
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
import react.router.useNavigate
import react.router.useParams
import react.useContext
import ru.altmanea.webapp.common.PayloadId
import ru.altmanea.webapp.config.Config
import ru.altmanea.webapp.data.Teacher
import tanstack.query.core.QueryKey
import tanstack.react.query.useQuery
import tools.fetchText
import userInfoContext
import kotlin.js.json

val CCurrentTable = FC<Props>("CurrentTable") {
    val id: PayloadId = useParams()["id"]!! // ID полезной нагрузки
    val navigate = useNavigate() // Дополнительный контроль навигации веб-приложения
    val userInfo = useContext(userInfoContext) // Данные для аутентификации
    val queryPageKey = arrayOf("DataTable", id).unsafeCast<QueryKey>() // Ключ для запроса на сервер
    /* Осуществление запроса на предоставление информации о конкретном преподавателе, его дисциплинах и группах */
    val query = useQuery<String, QueryError, String, QueryKey>(
        queryKey = queryPageKey,
        queryFn = {
            fetchText("${Config.payloadPath}table/$id",
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
        val teacher: Teacher? =
            try {
                Json.decodeFromString(query.data ?: "")
            } catch (e: Throwable) { null }
        teacher?.let {
            /* Вывод таблицы конкретного преподавателя */
            h3 { + "Преподаватель: ${it.fullname}" }
            table {
                border = 3 // Задание толщины границ таблицы
                th { + "Дисциплины" }; th { + "Группы" } // Заголовки таблицы
                it.disciplines.forEach { discipline ->
                    tr {
                        td { + discipline.name; align = TdAlign.center }
                        td { + discipline.groups.joinToString(", ") { it.name }; align = TdAlign.center }
                    }
                }
            }
        } ?: navigate(-1) // Возвращение на предыдущую страницу
    }
}