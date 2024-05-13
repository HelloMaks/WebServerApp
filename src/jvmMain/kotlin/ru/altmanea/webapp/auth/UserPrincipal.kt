package ru.altmanea.webapp.auth

import io.ktor.server.auth.*
import ru.altmanea.webapp.access.User

class UserPrincipal(val user: User): Principal