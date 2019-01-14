package dr.kotliners.kotlinbackend

import com.despegar.http.client.HttpResponse
import com.despegar.sparkjava.test.SparkServer
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dr.kotliners.kotlinbackend.controller.DepositRequest
import dr.kotliners.kotlinbackend.model.Account
import dr.kotliners.kotlinbackend.model.ResponseError
import dr.kotliners.kotlinbackend.model.Transaction
import dr.kotliners.kotlinbackend.model.User
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.slf4j.LoggerFactory
import spark.Spark.stop
import spark.servlet.SparkApplication
import java.math.BigDecimal
import java.util.concurrent.Executors

class TestSparkApplication : SparkApplication {
    override fun init() {
        main(arrayOf())
    }
}

@DisplayName("API Integration")
internal class KotlinBackendAppTest {
    private val LOG = LoggerFactory.getLogger(KotlinBackendAppTest::class.java.simpleName)
    private val gson = Gson()

    @BeforeEach
    fun setUp() {

    }

    @Test
    @DisplayName("GET /users")
    fun getUsers() {
        val url = "/users"
        val get = testServer.get(url, false)
        val httpResponse = testServer.execute(get)

        val result: List<User> = httpResponse.fromJson()

        assertEquals(httpResponse.code(), 200)
        assertEquals(result.size, 4)
        assertEquals(result[0], USER_0)

        LOG.info("GET: $url -> ${result[0]}")
    }

    @Test
    @DisplayName("POST /login?id=:id")
    fun getLogin() {
        val url = "/login?id=1"
        val sessionId = loginUser(USER_0.id)

        assertNotNull(sessionId)

        LOG.info("POST: $url -> $sessionId")
    }

    @Test
    @DisplayName("POST /login?id=unknown")
    fun `error on login with unknown user id`() {
        val url = "/login?id=unknown"

        val error: ResponseError = givenPostResponse(url)

        assertNotNull(error)
        assertEquals(error.message, "User not found.")

        LOG.info("POST: $url -> $error")
    }

    @Test
    @DisplayName("GET /user/account (user not logged in)")
    fun `error on account info when user not logged in`() {
        val url = "/user/account"

        val error: ResponseError = givenResponse(url)

        assertNotNull(error)
        assertEquals(error.message, "User not login or session expired.")

        LOG.info("GET: $url -> $error")
    }

    @Test
    @DisplayName("POST /user/account/deposit {'amount':'1000'} (user not logged in)")
    fun `error on deposit when user not logged in`() {

        val error: ResponseError = givenPostResponse(DEPOSIT_URL, givenDepostRequest(amount = "1000"))

        assertNotNull(error)
        assertEquals(error.message, "User not login or session expired.")

        LOG.info("POST: $DEPOSIT_URL -> $error")
    }

    @Test
    @DisplayName("POST /user/account/deposit {'amount':'1000'} (user logged in)")
    fun `deposit 1000 to current user account`() {
        val url = "/user/account/deposit"
        val sessionId = loginUser(USER_0.id)

        val transaction: Transaction = givenPostResponse(url, sessionId, givenDepostRequest(amount = "1000"))

        assertEquals(transaction.value, BigDecimal("1000.00"))

        LOG.info("POST: $url -> $transaction")
    }

    @Test
    @DisplayName("POST /user/account/deposit {'amount':'1000'} (Call twice)")
    fun `error on deposit when call deposit two times`() {
        val sessionId = loginUser(USER_0.id)

        executor.submit {
            val errorTransaction: ResponseError =
                givenPostResponse(DEPOSIT_URL, sessionId, givenDepostRequest(amount = "1000"))
            assertTrue(errorTransaction.message.contains("Optimistic lock exception:"))

            LOG.info("POST: $DEPOSIT_URL -> $errorTransaction")
        }

        val transaction: Transaction = givenPostResponse(DEPOSIT_URL, sessionId, givenDepostRequest(amount = "1000"))

        assertEquals(transaction.value, BigDecimal("1000.00"))

        LOG.info("POST: $DEPOSIT_URL -> $transaction")
    }

    @Test
    @DisplayName("GET /user/account (before and after deposit)")
    fun `check account balance`() {
        val accountUrl = "/user/account"
        val sessionId = loginUser(USER_1.id)

        var account: Account = givenResponse(accountUrl, sessionId)
        assertEquals(account.amount, BigDecimal("0.00"))
        LOG.info("GET: $accountUrl -> $account")

        val transaction: Transaction = givenPostResponse(DEPOSIT_URL, sessionId, givenDepostRequest(amount = "1000"))
        assertEquals(transaction.value, BigDecimal("1000.00"))
        LOG.info("POST: $DEPOSIT_URL -> $transaction")

        account = givenResponse(accountUrl, sessionId)
        assertEquals(account.amount, BigDecimal("1000.00"))
        LOG.info("GET: $accountUrl -> $account")
    }

    @Test
    @DisplayName("POST /user/account/transfer?to=:id&amount=:value")
    fun `transfer between accounts`() {
        val transferUrl = "/user/account/transfer?to=4&amount=555"
        val accountUrl = "/user/account"
        val sessionId = loginUser(USER_2.id)

        val depositTransaction: Transaction =
            givenPostResponse(DEPOSIT_URL, sessionId, givenDepostRequest(amount = "1000"))
        LOG.info("POST: $DEPOSIT_URL -> $depositTransaction")

        var account: Account = givenResponse(accountUrl, sessionId)
        assertEquals(account.amount, BigDecimal("1000.00"))
        LOG.info("GET: $accountUrl -> $account")

        val transferTransaction: Transaction = givenPostResponse(transferUrl, sessionId)
        assertEquals(transferTransaction.value, BigDecimal("-555.00"))
        LOG.info("POST: $transferUrl -> $transferTransaction")

        account = givenResponse(accountUrl, sessionId)
        assertEquals(account.amount, BigDecimal("445.00"))
        LOG.info("GET: $accountUrl -> $account")
    }

    @Test
    @DisplayName("POST /user/account/transfer?to=:id&amount=:value (Insufficient funds)")
    fun `error when transfer more than amount`() {
        val transferUrl = "/user/account/transfer?to=1&amount=1000"
        val accountUrl = "/user/account"
        val sessionId = loginUser(USER_3.id)

        var account: Account = givenResponse(accountUrl, sessionId)
        LOG.info("GET: $accountUrl -> $account")

        val transaction: ResponseError = givenPostResponse(transferUrl, sessionId)
        assertTrue(transaction.message.contains("Insufficient funds"))

        LOG.info("POST: $transferUrl -> $transaction")
    }

    private fun loginUser(userId: Int): String {
        val get = testServer.post("/login?id=$userId", "", false)
        val httpResponse = testServer.execute(get)

        return httpResponse.headers()["Set-Cookie"]?.get(0) ?: ""
    }

    private inline fun <reified T> givenPostResponse(url: String, sessionId: String = "", body: String = ""): T {
        val get = testServer.post(url, body, false).apply {
            addHeader("Cookie", sessionId)
        }
        val httpResponse = testServer.execute(get)
        println(String(httpResponse.body()))

        return httpResponse.fromJson()
    }

    private inline fun <reified T> givenResponse(url: String, sessionId: String = ""): T {
        val get = testServer.get(url, false).apply {
            addHeader("Cookie", sessionId)
        }
        val httpResponse = testServer.execute(get)
        println(String(httpResponse.body()))

        return httpResponse.fromJson()
    }

    private inline fun <reified T> HttpResponse.fromJson(): T =
        gson.fromJson(String(body()), object : TypeToken<T>() {}.type)

    private fun givenDepostRequest(amount: String?) =
        gson.toJson(DepositRequest(amount = amount))

    companion object {
        lateinit var testServer: SparkServer<TestSparkApplication>
        private var executor = Executors.newFixedThreadPool(2)

        @BeforeAll
        @JvmStatic
        fun setupAll() {
            KotlinBackendApp().run()

            testServer = SparkServer(TestSparkApplication::class.java, 4567)
        }

        @AfterAll
        @JvmStatic
        fun cleanupAll() {
            executor.shutdown()
            stop()
        }

        private val USER_0 = User(name = "Alice", email = "alice@alice.kt", id = 1)
        private val USER_1 = User(name = "Bob", email = "bob@bob.kt", id = 2)
        private val USER_2 = User(name = "Carol", email = "carol@carol.kt", id = 3)
        private val USER_3 = User(name = "Dave", email = "dave@dave.kt", id = 4)

        private val DEPOSIT_URL = "/user/account/deposit"
    }
}