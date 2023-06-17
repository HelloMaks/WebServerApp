package auth

import js.core.jso
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import react.FC
import react.Props
import ru.altmanea.webapp.access.Role
import ru.altmanea.webapp.access.Token
import ru.altmanea.webapp.access.User
import ru.altmanea.webapp.access.json
import ru.altmanea.webapp.config.Config
import tools.fetch
import kotlin.js.json

external interface AuthContainerProps : Props {
    var user: User?
    var signIn: (Pair<User, Token>) -> Unit
    var signOff: () -> Unit
}

val CAuthContainer = FC<AuthContainerProps>("AuthContainer") { props ->
    val _user = props.user
    if (_user != null) {
        CAuthOut {
            user = _user
            signOff = props.signOff
        }
    } else {
        CAuthIn {
            signIn = { name: Username, pass: Password ->
                val user = User(name, pass)
                fetch(
                    Config.loginPath,
                    jso {
                        method = "POST"
                        headers = json(
                            "Content-Type" to "application/json"
                        )
                        body = user.json
                    }
                )
                    .then { it.text() }
                    .then { props.signIn(Pair(user, Json.decodeFromString<Token>(it))) }
            }
        }
    }
}