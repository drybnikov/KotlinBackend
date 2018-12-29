package dr.kotliners.kotlinbackend.dao

import dr.kotliners.kotlinbackend.exception.InsufficientFundsException
import dr.kotliners.kotlinbackend.model.Transaction
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionDao @Inject constructor(private val accountDao: AccountDao) {
    private val transactionsData = ConcurrentHashMap<Long, HashSet<Transaction>>()

    fun Transaction.store() {
        val balance = accountDao.findById(accountId).amount

        if (balance.add(value) < BigDecimal.ZERO) {
            throw InsufficientFundsException(this)
        }
        Thread.sleep(500)//Simulate long operations
        accountDao.updateAmount(accountId, value)
        findByAccountId(accountId).add(this)
    }

    fun findByAccountId(accountId: Long) =
        transactionsData.computeIfAbsent(accountId) { emptySet<Transaction>().toHashSet() }
}