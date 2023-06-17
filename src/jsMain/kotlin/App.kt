import auth.authProvider
import component.*
import react.*
import react.dom.client.createRoot
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3
import react.router.Route
import react.router.Routes
import react.router.dom.HashRouter
import react.router.dom.Link
import ru.altmanea.webapp.access.Token
import ru.altmanea.webapp.access.User
import ru.altmanea.webapp.config.Config
import tanstack.query.core.QueryClient
import tanstack.react.query.QueryClientProvider
import web.dom.document

typealias UserInfo = Pair<User, Token>?
val userInfoContext = createContext<UserInfo>(null)

fun main() {
    val container = document.getElementById("root")!!
    createRoot(container).render(app.create())
}

val app = FC<Props>("App") {
    HashRouter {
        authProvider {
            QueryClientProvider {
                /* Ссылки по умолчанию */
                h3 { + "Навигация в приложении:" }
                div {
                    Link { + "Главная страница"; to = "" }
                }
                div {
                    Link { + "Преподаватели"; to = Config.payloadPath }
                }

                /* Пути приложения */
                Routes {
                    /* Пути к таблицам с данными */
                    Route {
                        path = Config.payloadPath
                        element = CTableRoutes.create { }
                    }
                    Route {
                        path = "${Config.payloadPath}table/common"
                        element = CCommonTable.create { }
                    }
                    Route {
                        path = "${Config.payloadPath}table/:id"
                        element = CCurrentTable.create { }
                    }
                }

                CDataLoadContainer { }
                client = QueryClient()
            }
        }
    }
}