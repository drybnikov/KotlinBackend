package dr.kotliners.kotlinbackend.controller

import dr.kotliners.kotlinbackend.ServiceRunner
import dr.kotliners.kotlinbackend.model.ResponseError
import spark.Spark.*
import spark.kotlin.after
import spark.kotlin.get
import javax.inject.Inject

const val USER_ID = "userID"
const val CONTENT_TYPE = "application/json"

class ServiceController {
    @Inject
    lateinit var gateway: RouteServiceGateway
    @Inject
    lateinit var responseTransformer: JsonResponseTransformer

    init {
        ServiceRunner.serviceComponent.inject(this)

        initRoutes()
    }

    private fun initRoutes() {
        path("/users") {
            get("", gateway.routeUsersList(), responseTransformer)
        }

        path("/user") {
            with(gateway) {
                get("", routeUserInfo(), responseTransformer)
                get("/account", routeUserAccountInfo(), responseTransformer)
                get("/account/deposit", routeAccountDeposit(), responseTransformer)
                get("/account/transfer", routeAccountTransfer(), responseTransformer)
            }

            exception(IllegalArgumentException::class.java) { e, _, res ->
                res.status(400)
                res.type(CONTENT_TYPE)
                res.body(responseTransformer.render(ResponseError(e)))
            }
        }

        get("/login", CONTENT_TYPE, gateway.routeLogin())

        after { type(CONTENT_TYPE) }

        get("/") {
            type("text/html")
            """
                Welcome to SPARK website !!! <br/>
                Test API:
                <ul>
                    <li><a href='/users'>List of users</a></li>
                    <li><a href='/login?id=:id'>User login with ID /login?id=:id</a></li>
                    <li><a href='/user'>User details /user</a></li>
                    <li><a href='/user/account'>User Account details /user/account</a></li>
                    <li><a href='/user/account/deposit?amount=:value'>Deposit /user/account/deposit?amount=:value</a></li>
                    <li><a href='/user/account/transfer?to=:userId&amount=:value'>Transfer /user/account/transfer?to=:userId&amount=:value</a></li>
                </ul>
            """.trimIndent()
        }
    }
}


