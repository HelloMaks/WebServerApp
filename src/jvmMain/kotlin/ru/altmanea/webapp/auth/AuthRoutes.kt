package ru.altmanea.webapp.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.altmanea.webapp.access.Role
import ru.altmanea.webapp.access.User
import ru.altmanea.webapp.auth.AuthConfig.Companion.audience
import ru.altmanea.webapp.auth.AuthConfig.Companion.issuer
import ru.altmanea.webapp.auth.AuthConfig.Companion.secret
import ru.altmanea.webapp.config.Config
import ru.altmanea.webapp.data.roleAdmin
import ru.altmanea.webapp.data.userList
import ru.altmanea.webapp.data.userRoles
import java.util.*

fun Route.authRoutes() {
    post(Config.loginPath) {
        val user = call.receive<User>()
        val localUser = userList.find { it.username == user.username }
        if (localUser?.password != user.password)
            return@post call.respondText("Неверный пароль", status = HttpStatusCode.Unauthorized)
        val token = JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("username", user.username)
            .withExpiresAt(Date(System.currentTimeMillis() + 1200000))
            .sign(Algorithm.HMAC256(secret))
        val admin: Boolean = userRoles.getOrDefault(localUser, emptySet()).any { it == roleAdmin }
        call.respond(Pair(admin, hashMapOf("token" to token)))
    }
    route("hello") {
        authenticate("auth-jwt") {
            // authenticate test
            get("all") {
                val principal = call.principal<UserPrincipal>()
                call.respondText("Приветствую, ${principal?.user?.username}! ")
            }
            // authorize test
            authorization(setOf(roleAdmin)) {
                get("admin") {
                    call.respond("Приветствую, Админ!")
                }
            }
        }
    }
}
