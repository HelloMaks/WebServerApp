package auth

import react.FC
import react.Props
import react.dom.html.ReactHTML.b
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.useState
import ru.altmanea.webapp.access.User
import web.html.InputType

typealias Username = String
typealias Password = String

external interface AuthInProps : Props {
    var signIn: (Username, Password) -> Unit
}

external interface AuthOutProps : Props {
    var user: User
    var signOff: () -> Unit
}

val CAuthIn = FC<AuthInProps>("Auth") { props ->
    var name by useState("")
    var pass by useState("")
    div {
        b { + "Логин: " }
        input {
            type = InputType.text
            value = name
            onChange = { name = it.target.value }
        }
    }
    div {
        b { + "Пароль: " }
        input {
            type = InputType.password
            value = pass
            onChange = { pass = it.target.value }
        }
    }
    div {
        button {
            b { + "Войти" }
            onClick = { props.signIn(name, pass) }
        }
    }
}

val CAuthOut = FC<AuthOutProps>("Auth") { props ->
    div { b { + "Логин: ${props.user.username}" } }
    div {
        button {
            b { + "Выйти" }
            onClick = { props.signOff() }
        }
    }
}