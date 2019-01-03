package dr.kotliners.kotlinbackend.dao

import dr.kotliners.kotlinbackend.exception.InsufficientFundsException
import dr.kotliners.kotlinbackend.model.TransactionDB
import dr.kotliners.kotlinbackend.model.Transactions
import dr.kotliners.kotlinbackend.service.TransferData
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

class TransactionDao @Inject constructor() {

    fun storeTransaction(transferData: TransferData): TransactionDB =
        transaction {
            addLogger(StdOutSqlLogger)
            val accountDb =
                transferData.account ?: throw IllegalArgumentException("Account :${transferData.account?.id} not found")

            if (accountDb.amount.add(transferData.value) < BigDecimal.ZERO) {
                throw InsufficientFundsException(transferData)
            }

            accountDb.amount = accountDb.amount.add(transferData.value)
            TransactionDB.new {
                account = accountDb
                value = transferData.value
                type = transferData.type
                date = DateTime.now(DateTimeZone.UTC)
            }
        }

    fun findByAccountId(accountId: UUID): List<TransactionDB> =
        transaction {
            TransactionDB.find { Transactions.account eq accountId }.sortedByDescending { it.date }
        }
}