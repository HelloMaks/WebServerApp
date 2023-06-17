package ru.altmanea.webapp.auth

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.util.*
import ru.altmanea.webapp.access.Role
import ru.altmanea.webapp.access.User

class AuthorizationConfig(
    var getRole: (User) -> Set<Role> = { emptySet() }
)

class Authorization(internal var config: AuthorizationConfig) {
    companion object : BaseApplicationPlugin<Application, AuthorizationConfig, Authorization> {
        override val key: AttributeKey<Authorization> = AttributeKey("AuthorizationHolder")
        override fun install(pipeline: Application, configure: AuthorizationConfig.() -> Unit) =
            Authorization(AuthorizationConfig().apply(configure))
    }
}

class RouteAuthorizationConfig {
    var allowedRoles: () -> Set<Role> = { emptySet() }
}

val RouteAuthorization: RouteScopedPlugin<RouteAuthorizationConfig> = createRouteScopedPlugin(
    "RouteAuthorization",
    ::RouteAuthorizationConfig
) {
    val holderConfig = application.plugin(Authorization).config
    val allowedRoles = pluginConfig.allowedRoles
    val getRole = holderConfig.getRole
    on(AuthenticationChecked) {
        val principal = it.authentication.principal<UserPrincipal>()
            ?: throw PrincipalError
        if (allowedRoles().intersect(getRole(principal.user)).isEmpty())
            throw AccessDenied
    }
}

fun Route.authorization(
    roles: () -> Set<Role>,
    build: Route.() -> Unit
): Route {
    val name = roles().joinToString { it.name }
    val authenticatedRoute = createChild(AuthorizationRouteSelector(name))
    authenticatedRoute.install(RouteAuthorization) {
        this.allowedRoles = roles
    }
    authenticatedRoute.build()
    return authenticatedRoute
}


class AuthorizationRouteSelector(val name: String) : RouteSelector() {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        return RouteSelectorEvaluation.Transparent
    }

    override fun toString(): String = "(authorize $name )"
}

inline fun Route.authorization(
    roles: Set<Role>,
    noinline build: Route.() -> Unit
): Route = authorization(
    { roles },
    build
)
