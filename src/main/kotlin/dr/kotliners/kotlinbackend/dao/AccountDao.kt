package dr.kotliners.kotlinbackend.dao

import dr.kotliners.kotlinbackend.model.Account
import dr.kotliners.kotlinbackend.model.Transaction
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class AccountDao @Inject constructor() {
    private val data = ConcurrentHashMap<Long, Account>()

    fun create(userId: Int, currency: Currency): Account {
        val id = UUID.randomUUID().mostSignificantBits
        val account = Account(
            id = id,
            currency = currency,
            userId = userId,
            amount = 0.0,
            transactions = hashSetOf()
        )
        data[id] = account

        return account
    }

    fun depositMoney(accountId: Long, depositString: String?): Transaction {
        val deposit = depositString.toDoubleOrThrow()

        data[accountId]?.let {
            synchronized(it) {
                val transaction = Transaction.depositTransaction(it.id, deposit)
                it.transactions.add(transaction)
                it.amount += deposit

                return transaction
            }
        } ?: throw IllegalArgumentException("Account with id:$accountId not found")
    }

    fun transferMoney(sourceAccountId: Long, destinationAccountId: Long, transferString: String?): Transaction {
        val transferValue = transferString.toDoubleOrThrow()
        val sourceAccount = findById(sourceAccountId)
        val destinationAccount = findById(destinationAccountId)

        synchronized(sourceAccount) {
            if (sourceAccount.amount < transferValue) throw IllegalArgumentException("Insufficient funds in the account")
            val sourceTransaction =
                Transaction.transferTransaction(destinationAccount.id, "-$transferString".toDouble())
            sourceAccount.transactions.add(sourceTransaction)

            destinationAccount.transactions.add(
                Transaction.transferTransaction(sourceAccount.id, transferValue)
            )

            sourceAccount.amount -= transferValue
            destinationAccount.amount += transferValue

            return sourceTransaction
        }
    }

    fun findByUserId(userId: Int): Account? {
        return data.values.find { it.userId == userId }
    }

    private fun findById(accountId: Long) =
        data[accountId] ?: throw IllegalArgumentException("Account with id:$accountId not found")

    private fun String?.toDoubleOrThrow() =
        this?.toDoubleOrNull() ?: throw NumberFormatException("Amount:$this should be are number value")
}