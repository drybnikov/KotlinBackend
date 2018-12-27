package dr.kotliners.kotlinbackend.dao

import dr.kotliners.kotlinbackend.model.Account
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class AccountDao @Inject constructor() {
    private val accountData = ConcurrentHashMap<Long, Account>()

    fun create(userId: Int, currency: Currency): Account {
        val id = UUID.randomUUID().mostSignificantBits
        val account = Account(
            id = id,
            currency = currency,
            userId = userId,
            amount = BigDecimal.ZERO,
            transactions = hashSetOf()
        )
        accountData[id] = account

        return account
    }

    fun findByUserId(userId: Int): Account? {
        return accountData.values.find { it.userId == userId }
    }
}