package ru.altmanea.webapp.data

import ru.altmanea.webapp.access.Role
import ru.altmanea.webapp.access.User

val user = User("user", "user")
val userAdmin = User("admin","admin")
val userList = listOf(userAdmin, user)

val roleAdmin = Role("admin")
val roleUser = Role("user")
val roleList = listOf(roleAdmin, roleUser)

val userRoles = mapOf(
    user to setOf(roleUser),
    userAdmin to roleList.toSet()
)