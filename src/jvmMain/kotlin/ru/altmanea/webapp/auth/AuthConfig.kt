package ru.altmanea.webapp.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import ru.altmanea.webapp.data.userList
import ru.altmanea.webapp.data.userRoles

class AuthConfig {
    companion object {
        const val secret = "secret"
        const val issuer = "http://0.0.0.0:8080/"
        const val audience = "http://0.0.0.0:8080/hello"
        const val myRealm = "Access to 'hello'"
    }
}

fun Application.authConfig() {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = AuthConfig.myRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(AuthConfig.secret))
                    .withAudience(AuthConfig.audience)
                    .withIssuer(AuthConfig.issuer)
                    .build()
            )
            validate { credential ->
                userList.find {
                    it.username == credential.payload.getClaim("username").asString()
                }?.let {
                    UserPrincipal(it)
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
    install(Authorization) {
        getRole = { user ->
            val existUser = userList.find { user.username == it.username }
            userRoles.getOrDefault(existUser, emptySet())
        }
    }
}