package dr.kotliners.kotlinbackend.dao

import dr.kotliners.kotlinbackend.model.Account
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

@Singleton
class AccountDao @Inject constructor() {
    private val accountData = ConcurrentHashMap<Long, Account>()

    fun create(userId: Int, currency: Currency): Account {
        val id = UUID.randomUUID().mostSignificantBits
        val account = Account(
            id = id,
            currency = currency,
            userId = userId,
            amount = BigDecimal.ZERO,
            transactions = ArrayList()
        )
        accountData[id] = account

        return account
    }

    fun findByUserId(userId: Int): Account? {
        return accountData.values.find { it.userId == userId }
    }

    fun findById(accountId: Long): Account =
        accountData[accountId] ?: throw IllegalArgumentException("Account :$accountId not found")

    fun updateAmount(accountId: Long, value: BigDecimal) {
        val account = findById(accountId)
        account.amount = account.amount.add(value)
    }
}