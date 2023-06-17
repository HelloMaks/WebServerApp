import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.altmanea.webapp.access.Token
import ru.altmanea.webapp.access.json
import ru.altmanea.webapp.data.userAdmin
import ru.altmanea.webapp.common.Payload
import ru.altmanea.webapp.config.Config
import ru.altmanea.webapp.data.Student
import ru.altmanea.webapp.data.json
import ru.altmanea.webapp.main


class ApplicationTest : StringSpec({
    "Students routes" {
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
            val students = withClue("read") {
                val response = client.get("/students/") {
                    headers { this["Authorization"] = token.authHeader }
                }
                response.status shouldBe HttpStatusCode.OK
                Json.decodeFromString<List<Payload<Student>>>(response.bodyAsText()).apply {
                    size shouldBe 4
                }
            }
            withClue("read id") {
                val sheldon = students.first { it.elem.firstname == "Sheldon" }
                val response = client.get("/students/${sheldon.id}") {
                    headers { this["Authorization"] = token.authHeader }
                }
                response.status shouldBe HttpStatusCode.OK
                Json.decodeFromString<Payload<Student>>(response.bodyAsText()).apply {
                    elem.firstname shouldBe "Sheldon"
                }
            }
            val newStudents = withClue("create") {
                val response = client.post("/students/") {
                    headers { this["Authorization"] = token.authHeader }
                    contentType(ContentType.Application.Json)
                    setBody(Student("Raj", "Koothrappali").json)
                }
                response.status shouldBe HttpStatusCode.Created
                Json.decodeFromString<List<Payload<Student>>>(
                    client.get("/students/"){
                        headers { this["Authorization"] = token.authHeader }
                    }.bodyAsText()
                ).apply {
                    size shouldBe 5
                }
            }
            val emi = withClue("update") {
                val raj = newStudents.first { it.elem.firstname == "Raj" }
                client.put("/students/") {
                    headers { this["Authorization"] = token.authHeader }
                    contentType(ContentType.Application.Json)
                    setBody(
                        Json.encodeToString(
                            Payload(
                                Student("Amy", "Fowler"),
                                raj.id, raj.version)
                        )
                    )
                }.let {
                    it.status shouldBe HttpStatusCode.Created
                }
                Json.decodeFromString<Payload<Student>>(
                    client.get("/students/${raj.id}"){
                        headers { this["Authorization"] = token.authHeader }
                    }.bodyAsText()
                ).apply {
                    elem.firstname shouldBe "Amy"
                }
            }
            withClue("delete") {
                client.delete("/students/${emi.id}"){
                    headers { this["Authorization"] = token.authHeader }
                }
                Json.decodeFromString<List<Payload<Student>>>(
                    client.get("/students/"){
                        headers { this["Authorization"] = token.authHeader }
                    }.bodyAsText()
                ).apply {
                    size shouldBe 4
                }
            }
        }
    }
})

