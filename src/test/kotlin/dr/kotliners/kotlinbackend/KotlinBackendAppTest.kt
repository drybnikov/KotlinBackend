package dr.kotliners.kotlinbackend

import com.despegar.http.client.HttpResponse
import com.despegar.sparkjava.test.SparkServer
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dr.kotliners.kotlinbackend.model.User
import dr.kotliners.kotlinbackend.service.TransferService
import junit.framework.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import spark.servlet.SparkApplication

class TestSparkApplication : SparkApplication {
    override fun init() {
        main(arrayOf())
    }
}

@DisplayName("API Integration")
internal class KotlinBackendAppTest {
    private val LOG = LoggerFactory.getLogger(KotlinBackendAppTest::class.java.simpleName)
    private var app = KotlinBackendApp().run()

    private lateinit var testServer: SparkServer<TestSparkApplication>
    private val gson = Gson()

    @BeforeEach
    fun setUp() {
        testServer = SparkServer(TestSparkApplication::class.java, 4567)
    }

    @Test
    @DisplayName("GET /users")
    fun getUsers() {
        val get = testServer.get("/users", false)
        val httpResponse = testServer.execute(get)

        val result: List<User> = httpResponse.fromJson()
        println(result)

        assertEquals(httpResponse.code(), 200)
        assertEquals(result.size, 4)
        assertEquals(result[0], User(name = "Alice", email = "alice@alice.kt", id = 0))

        LOG.info("GET: {}?pan={} -> {}", "/users", 0, result[0])
    }

    private inline fun <reified T> HttpResponse.fromJson(): T =
        gson.fromJson(String(body()), object : TypeToken<T>() {}.type)
}