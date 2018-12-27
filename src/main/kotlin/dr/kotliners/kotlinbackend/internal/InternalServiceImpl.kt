package dr.kotliners.kotlinbackend.internal

import dr.kotliners.kotlinbackend.dao.AccountDao
import dr.kotliners.kotlinbackend.dao.UserDao
import dr.kotliners.kotlinbackend.model.Account
import dr.kotliners.kotlinbackend.model.Transaction
import dr.kotliners.kotlinbackend.model.User
import dr.kotliners.kotlinbackend.service.TransferService
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

class InternalServiceImpl @Inject constructor(
    private val userDao: UserDao,
    private val accountDao: AccountDao,
    private val transferService: TransferService
) : InternalService {

    override fun users(): MutableCollection<User> {
        return userDao.users()
    }

    override fun findUserById(id: Int?): User {
        return userDao.findById(id)
            ?: throw IllegalArgumentException("User not found.")
    }

    override fun userAccount(userId: Int): Account {
        return accountDao.findByUserId(userId)
            ?: accountDao.create(userId = userId, currency = Currency.getInstance("USD"))
    }

    override fun depositMoney(userId: Int, deposit: String?): Transaction =
        transferService.depositMoney(
            account = userAccount(userId),
            depositString = deposit
        )

    override fun transferMoney(sourceUserId: Int, destinationUserId: String?, amount: String?): Transaction {
        if (sourceUserId == destinationUserId?.toIntOrNull())
            throw IllegalArgumentException("Can not transfer to himself.")

        findUserById(destinationUserId?.toIntOrNull()).let {
            return transferService.transferMoney(
                sourceAccount = userAccount(sourceUserId),
                destinationAccount = userAccount(it.id),
                transferString = amount
            )
        }
    }
}