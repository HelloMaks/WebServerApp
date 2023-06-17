import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ru.altmanea.webapp.access.Token
import ru.altmanea.webapp.access.json
import ru.altmanea.webapp.data.userAdmin
import ru.altmanea.webapp.data.user
import ru.altmanea.webapp.config.Config
import ru.altmanea.webapp.main

class AuthTest : StringSpec({
    "Authentication routes" {
        testApplication {
            application {
                main()
            }
            val token = Json.decodeFromString<Token>(
                client.post(Config.loginPath) {
                    contentType(ContentType.Application.Json)
                    setBody(user.json)
                }.bodyAsText()
            )
            client.get("hello/all") {
                headers {
                    this["Authorization"] = token.authHeader
                }
            }.let {
                it.status shouldBe HttpStatusCode.OK
            }
        }
    }
    "Authorization routes" {
        testApplication {
            application {
                main()
            }
            val token = Json.decodeFromString<Token>(
                client.post(Config.loginPath) {
                    contentType(ContentType.Application.Json)
                    setBody(userAdmin.json)
                }.bodyAsText()
            )
            client.get("hello/admin")
                .status shouldBe HttpStatusCode.Unauthorized
            client.get("hello/admin") {
                headers {
                    this["Authorization"] = token.authHeader
                }
            }.let {
                it.status shouldBe HttpStatusCode.OK
            }
        }
    }
})