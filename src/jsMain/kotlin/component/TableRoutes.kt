package component

import js.core.jso
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import query.QueryError
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.span
import react.router.dom.Link
import react.useContext
import ru.altmanea.webapp.common.PayloadId
import ru.altmanea.webapp.config.Config
import ru.altmanea.webapp.data.FullName
import tanstack.query.core.QueryKey
import tanstack.react.query.useMutation
import tanstack.react.query.useQuery
import tanstack.react.query.useQueryClient
import tools.HTTPResult
import tools.fetch
import tools.fetchText
import userInfoContext
import kotlin.js.json

val CTableRoutes = FC<Props>("TableRoutes") {
    val userInfo = useContext(userInfoContext) // Данные для аутентификации

    val queryClient = useQueryClient() // Для управления запросами на мутацию
    val queryPageKey = arrayOf("Teachers").unsafeCast<QueryKey>() // Ключ для запроса на сервер

    /* Осуществление запроса на предоставление информации, необходимой для вывода ссылок */
    val query = useQuery<String, QueryError, String, QueryKey>(
        queryKey = queryPageKey,
        queryFn = {
            fetchText("${Config.payloadPath}links",
                jso {
                    headers = json(
                        "Content-Type" to "application/json",
                        "Authorization" to userInfo?.third?.authHeader
                    )
                }
            )
        }
    )

    /* Осуществление запроса на удаление преподавателя из базы данных по ID */
    val deleteMutation = useMutation<HTTPResult, Any, PayloadId, Any>(
        mutationFn = { id: PayloadId ->
            fetch(
                "${Config.payloadPath}$id",
                jso {
                    method = "DELETE"
                    headers = json(
                        "Authorization" to userInfo?.third?.authHeader
                    )
                }
            )
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>(queryPageKey)
            }
        }
    )

    if(query.isLoading) { div { + "Загрузка..." } }
    else if(query.isError) { div { + "Ошибка!" } }
    else {
        val teachers: Map<PayloadId, FullName> =
            Json.decodeFromString(query.data ?: "") // Соотношение ID преподавателя с ФИО

        /* Вывод ссылок на таблицу со всеми преподавателями и на таблицы каждого преподавателя */
        h3 { + "Результат импорта данных:" }
        div {
            Link { + "Общая таблица"; to = "table/common" }
        }

        teachers.forEach { (id, fullname) ->
            div {
                Link { + fullname; to = "table/$id" }
                if(userInfo?.second!! && teachers.size != 1) {
                    span {
                        + " ✂ "
                        onClick = { deleteMutation.mutateAsync(id, null) }
                    }
                }
            }
        }
    }
}