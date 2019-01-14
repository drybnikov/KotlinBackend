package dr.kotliners.kotlinbackend

import com.despegar.http.client.HttpResponse
import com.despegar.sparkjava.test.SparkServer
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dr.kotliners.kotlinbackend.controller.DepositRequest
import dr.kotliners.kotlinbackend.controller.LoginRequest
import dr.kotliners.kotlinbackend.controller.TransferRequest
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
    @DisplayName("POST $LOGIN_URL {'userId':'1'}")
    fun getLogin() {
        val sessionId = loginUser(USER_0.id)

        assertNotNull(sessionId)

        LOG.info("POST: $LOGIN_URL -> $sessionId")
    }

    @Test
    @DisplayName("POST $LOGIN_URL {'userId':'unknown'}")
    fun `error on login with unknown user id`() {
        val error: ResponseError = givenPostResponse(LOGIN_URL, "", "null")

        assertNotNull(error)
        assertEquals(error.message, "User not found.")

        LOG.info("POST: $LOGIN_URL -> $error")
    }

    @Test
    @DisplayName("GET $ACCOUNT_URL (user not logged in)")
    fun `error on account info when user not logged in`() {
        val error: ResponseError = givenResponse(ACCOUNT_URL)

        assertNotNull(error)
        assertEquals(error.message, "User not login or session expired.")

        LOG.info("GET: $ACCOUNT_URL -> $error")
    }

    @Test
    @DisplayName("POST $DEPOSIT_URL {'amount':'1000'} (user not logged in)")
    fun `error on deposit when user not logged in`() {
        val request = givenDepositRequest(amount = "1000")

        val error: ResponseError = givenPostResponse(DEPOSIT_URL, request)

        assertNotNull(error)
        assertEquals(error.message, "User not login or session expired.")

        LOG.info("POST: $DEPOSIT_URL -> $error")
    }

    @Test
    @DisplayName("POST $DEPOSIT_URL {unknown:unknown}")
    fun `error on deposit when request contains wrong data`() {
        val sessionId = loginUser(USER_0.id)

        val error: ResponseError = givenPostResponse(DEPOSIT_URL, sessionId, "{unknown:unknown}")

        assertNotNull(error)
        assertEquals(error.message, "Amount:null should be are number value")

        LOG.info("POST: $DEPOSIT_URL -> $error")
    }

    @Test
    @DisplayName("POST $DEPOSIT_URL {'amount':'1000'} (user logged in)")
    fun `deposit 1000 to current user account`() {
        val sessionId = loginUser(USER_0.id)
        val request = givenDepositRequest(amount = "1000")

        val transaction: Transaction = givenPostResponse(DEPOSIT_URL, sessionId, request)

        assertEquals(transaction.value, BigDecimal("1000.00"))

        LOG.info("POST: $DEPOSIT_URL -> $transaction")
    }

    @Test
    @DisplayName("POST $DEPOSIT_URL {'amount':'1000'} (Call twice)")
    fun `error on deposit when call deposit two times`() {
        val sessionId = loginUser(USER_0.id)
        val request = givenDepositRequest(amount = "1000")

        executor.submit {
            val errorTransaction: ResponseError =
                givenPostResponse(DEPOSIT_URL, sessionId, givenDepositRequest(amount = "1000"))
            assertTrue(errorTransaction.message.contains("Optimistic lock exception:"))

            LOG.info("POST: $DEPOSIT_URL -> $errorTransaction")
        }

        val transaction: Transaction = givenPostResponse(DEPOSIT_URL, sessionId, request)

        assertEquals(transaction.value, BigDecimal("1000.00"))

        LOG.info("POST: $DEPOSIT_URL -> $transaction")
    }

    @Test
    @DisplayName("GET $ACCOUNT_URL (before and after deposit)")
    fun `check account balance`() {
        val sessionId = loginUser(USER_1.id)

        var account: Account = givenResponse(ACCOUNT_URL, sessionId)
        assertEquals(account.amount, BigDecimal("0.00"))
        LOG.info("GET: $ACCOUNT_URL -> $account")

        val transaction: Transaction = givenPostResponse(DEPOSIT_URL, sessionId, givenDepositRequest(amount = "1000"))
        assertEquals(transaction.value, BigDecimal("1000.00"))
        LOG.info("POST: $DEPOSIT_URL -> $transaction")

        account = givenResponse(ACCOUNT_URL, sessionId)
        assertEquals(account.amount, BigDecimal("1000.00"))
        LOG.info("GET: $ACCOUNT_URL -> $account")
    }

    @Test
    @DisplayName("POST $TRANSFER_URL {userId:4,amount:555}")
    fun `transfer between accounts`() {
        val transferRequest = givenTransferRequest(userId = "4", amount = "555")
        val sessionId = loginUser(USER_2.id)

        val depositTransaction: Transaction =
            givenPostResponse(DEPOSIT_URL, sessionId, givenDepositRequest(amount = "1000"))
        LOG.info("POST: $DEPOSIT_URL -> $depositTransaction")

        var account: Account = givenResponse(ACCOUNT_URL, sessionId)
        assertEquals(account.amount, BigDecimal("1000.00"))
        LOG.info("GET: $ACCOUNT_URL -> $account")

        val transferTransaction: Transaction = givenPostResponse(TRANSFER_URL, sessionId, transferRequest)
        assertEquals(transferTransaction.value, BigDecimal("-555.00"))
        LOG.info("POST: $TRANSFER_URL $transferRequest -> $transferTransaction")

        account = givenResponse(ACCOUNT_URL, sessionId)
        assertEquals(account.amount, BigDecimal("445.00"))
        LOG.info("GET: $ACCOUNT_URL -> $account")
    }

    @Test
    @DisplayName("POST $TRANSFER_URL {userId:1,amount:1000} (Insufficient funds)")
    fun `error when transfer more than amount`() {
        val transferRequest = givenTransferRequest(userId = "1", amount = "1000")
        val sessionId = loginUser(USER_3.id)

        val account: Account = givenResponse(ACCOUNT_URL, sessionId)
        LOG.info("GET: $ACCOUNT_URL -> $account")

        val transaction: ResponseError = givenPostResponse(TRANSFER_URL, sessionId, transferRequest)
        assertTrue(transaction.message.contains("Insufficient funds"))

        LOG.info("POST: $TRANSFER_URL $transferRequest -> $transaction")
    }

    private fun loginUser(userId: Int): String {
        val body = gson.toJson(LoginRequest(userId = userId.toString()))
        val get = testServer.post(LOGIN_URL, body, false)
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

    private fun givenDepositRequest(amount: String) =
        gson.toJson(DepositRequest(amount = amount))

    private fun givenTransferRequest(userId: String, amount: String) =
        gson.toJson(TransferRequest(userId = userId, amount = amount))

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

        private const val LOGIN_URL = "/login"
        private const val ACCOUNT_URL = "/user/account"
        private const val DEPOSIT_URL = "/user/account/deposit"
        private const val TRANSFER_URL = "/user/account/transfer"
    }
}