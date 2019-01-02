package dr.kotliners.kotlinbackend.dao

import dr.kotliners.kotlinbackend.exception.InsufficientFundsException
import dr.kotliners.kotlinbackend.model.Transaction
import dr.kotliners.kotlinbackend.model.TransactionDB
import dr.kotliners.kotlinbackend.model.Transactions
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

class TransactionDao @Inject constructor(private val accountDao: AccountDao) {

    fun storeTransaction(tr: Transaction) {
        transaction {
            addLogger(StdOutSqlLogger)

            val accountDB = accountDao.findById(tr.accountId)
            if (accountDB.amount.add(tr.value) < BigDecimal.ZERO) {
                throw InsufficientFundsException(tr)
            }

            accountDB.amount = accountDB.amount.add(tr.value)

            TransactionDB.new {
                account = accountDB
                value = tr.value
                type = tr.type
                date = DateTime(tr.date)
            }
        }
    }

    fun findByAccountId(accountId: UUID): List<TransactionDB> =
        transaction {
            TransactionDB.find { Transactions.account eq accountId }.sortedByDescending { it.date }
        }
}