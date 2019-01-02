package dr.kotliners.kotlinbackend.internal

import dr.kotliners.kotlinbackend.dao.AccountDao
import dr.kotliners.kotlinbackend.dao.UserDao
import dr.kotliners.kotlinbackend.model.Account
import dr.kotliners.kotlinbackend.model.Transaction
import dr.kotliners.kotlinbackend.model.User
import dr.kotliners.kotlinbackend.service.TransferService
import java.util.*
import javax.inject.Inject

class InternalServiceImpl @Inject constructor(
    private val userDao: UserDao,
    private val accountDao: AccountDao,
    private val transferService: TransferService
) : InternalService {

    override fun users() =
        userDao.allUsers().map { User.fromUserDB(it) }

    override fun findUserById(id: Int?): User {
        if (id == null) throw IllegalArgumentException("User not found.")
        val user = userDao.findUserById(id)
            ?: throw IllegalArgumentException("User not found.")

        return User.fromUserDB(user)
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

    override fun depositMoney(userId: Int, deposit: String?): Transaction =
        transferService.depositMoney(
            userId = userId,
            depositString = deposit
        )

    override fun transferMoney(sourceUserId: Int, destinationUserId: String?, amount: String?): Transaction {
        if (sourceUserId == destinationUserId?.toIntOrNull())
            throw IllegalArgumentException("Can not transfer to himself.")
        findUserById(destinationUserId?.toIntOrNull()).let {
            return transferService.transferMoney(
                sourceUserId = sourceUserId,
                destinationUserId = it.id,
                transferString = amount
            )
        }
    }
}