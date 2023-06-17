package component

import js.core.jso
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import query.QueryError
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.router.useNavigate
import react.useContext
import react.useState
import ru.altmanea.webapp.config.Config
import ru.altmanea.webapp.data.Teacher
import tanstack.query.core.QueryKey
import tanstack.react.query.useMutation
import tanstack.react.query.useQuery
import tanstack.react.query.useQueryClient
import tools.HTTPResult
import tools.fetch
import tools.fetchText
import userInfoContext
import kotlin.js.json

val CDataLoadContainer = FC<Props>("DataLoadContainer") {
    /* Состояние программы (0 - отсутствие данных, 1 - извлечение полезных данных,
        2 - загрузка данных в MongoDB, 3 - наличие данных */
    val (appState, setAppState) = useState(0)

    val userInfo = useContext(userInfoContext) // Данные для аутентификации
    val navigate = useNavigate() // Дополнительный контроль навигации веб-приложения

    val queryClient = useQueryClient() // Для управления запросами на мутацию
    val queryPageKey = arrayOf("DataCheck").unsafeCast<QueryKey>() // Ключ для запроса на сервер
    val queryPageKey2 = arrayOf("Teachers").unsafeCast<QueryKey>() // Ключ для запроса на сервер

    /* Осуществление запроса на проверку наличия данных в MongoDB */
    val query = useQuery<String, QueryError, String, QueryKey>(
        queryKey = queryPageKey,
        queryFn = {
            fetchText(
                "${Config.payloadPath}check",
                jso {
                    headers = json(
                        "Content-Type" to "application/json",
                        "Authorization" to userInfo?.second?.authHeader
                    )
                }
            )
        }
    )

    /* Осуществление запроса на добавление данных в MongoDB */
    val addMutation = useMutation<HTTPResult, Any, List<Teacher>, Any>(
        mutationFn = { teachers: List<Teacher> ->
            fetch(
                Config.payloadPath,
                jso {
                    method = "POST"
                    headers = json(
                        "Content-Type" to "application/json",
                        "Authorization" to userInfo?.second?.authHeader
                    )
                    body = Json.encodeToString(teachers)
                }
            )
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                setAppState(3)
                queryClient.invalidateQueries<Any>(queryPageKey)
                queryClient.invalidateQueries<Any>(queryPageKey2)
            }
        }
    )

    /* Осуществление запроса на удаление данных в MongoDB */
    val deleteMutation = useMutation<HTTPResult, Any, Any, Any>(
        mutationFn = {
            fetch(
                Config.payloadPath,
                jso {
                    method = "DELETE"
                    headers = json(
                        "Authorization" to userInfo?.second?.authHeader
                    )
                }
            )
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                setAppState(0)
                navigate("") // Переход на главную страницу
                queryClient.invalidateQueries<Any>(queryPageKey)
            }
        }
    )

    if(query.isLoading) { div { + "Загрузка..." } }
    else if(query.isError) { div { + "Ошибка!" } }
    else {
        val check: Boolean = Json.decodeFromString(query.data ?: "")

        if(!check && appState == 3) setAppState(0) // В случае отсутствия данных
        if(check && appState == 0) setAppState(3) // В случае наличия данных

        /* Вызов компонента загрузки данных */
        if(appState != 2) {
            CDataLoad {
                this.appState = appState
                this.setAppState = setAppState
                this.addMutation = { addMutation.mutateAsync(it, null); setAppState(2) }
                this.deleteMutation = { deleteMutation.mutateAsync(it, null) }
            }
        }
    }
}