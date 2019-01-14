package dr.kotliners.kotlinbackend.internal

import dr.kotliners.kotlinbackend.controller.DepositRequest
import dr.kotliners.kotlinbackend.controller.TransferRequest
import dr.kotliners.kotlinbackend.dao.AccountDao
import dr.kotliners.kotlinbackend.dao.UserDao
import dr.kotliners.kotlinbackend.model.*
import dr.kotliners.kotlinbackend.service.TransferService
import java.util.*
import javax.inject.Inject

class InternalServiceImpl @Inject constructor(
    private val userDao: UserDao,
    private val accountDao: AccountDao,
    private val transferService: TransferService
) : InternalService {

    override fun users() =
        userDao.allUsers().map(UserDB::toJsonUser)

    override fun findUserById(id: Int?): User {
        if (id == null) throw IllegalArgumentException("User not found.")
        val user = userDao.findUserById(id)
            ?: throw IllegalArgumentException("User not found.")

        return user.toJsonUser()
    }

    override fun userAccount(userId: Int): Account {
        val accountDB = accountDao.findByUser(userId)

        return Account(
            id = accountDB.id.value,
            amount = accountDB.amount,
            userId = userId,
            currency = Currency.getInstance(accountDB.currency),
            transactions = transferService.transactionHistory(accountDB.id.value)
        )
    }

    override fun depositMoney(userId: Int, deposit: DepositRequest?): Transaction =
        transferService.depositMoney(
            userId = userId,
            depositString = deposit?.amount
        )

    override fun transferMoney(sourceUserId: Int, request: TransferRequest?): Transaction {
        if (sourceUserId == request?.userId?.toIntOrNull())
            throw IllegalArgumentException("Can not transfer to himself.")

        findUserById(request?.userId?.toIntOrNull()).let {
            return transferService.transferMoney(
                sourceUserId = sourceUserId,
                destinationUserId = it.id,
                transferString = request?.amount
            )
        }
    }
}