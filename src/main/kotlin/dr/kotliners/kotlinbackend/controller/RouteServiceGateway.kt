package dr.kotliners.kotlinbackend.controller

import dr.kotliners.kotlinbackend.internal.InternalService
import spark.Route
import spark.Session
import spark.kotlin.RouteHandler
import javax.inject.Inject

class RouteServiceGateway @Inject constructor(
    private val service: InternalService,
    private val transformer: JsonResponseTransformer
) {
    internal fun routeUsersList() = Route { _, _ ->
        service.users()
    }

    internal fun routeUserInfo() = Route { req, _ ->
        val userId = req.session().getUserId()
        service.findUserById(userId)
    }

    internal fun routeUserAccountInfo() = Route { req, _ ->
        service.userAccount(req.session().getUserId())
    }

    internal fun routeAccountDeposit() = Route { req, _ ->
        val userId = req.session().getUserId()
        service.depositMoney(
            userId = userId,
            deposit = transformer.fromJson(req.body())
        )
    }

    internal fun routeAccountTransfer() = Route { req, _ ->
        val userId = req.session().getUserId()
        service.transferMoney(
            sourceUserId = userId,
            destinationUserId = req.queryParams("to"),
            amount = req.queryParams("amount")
        )
    }

    internal fun routeLogin(): RouteHandler.() -> Any {
        return {
            val loginRequest: LoginRequest = transformer.fromJson(request.body())
            val user = service.findUserById(loginRequest.userId?.toIntOrNull())

            session().attribute(USER_ID, user.id)
            "Hello ${user.name}."
        }
    }

    private fun Session.getUserId(): Int {
        return attribute<Int>(USER_ID) ?: throw IllegalArgumentException("User not login or session expired.")
    }
}

data class DepositRequest(val amount: String)

data class LoginRequest(val userId: String?)

data class TransferRequest(val userId: String, val amount: String)