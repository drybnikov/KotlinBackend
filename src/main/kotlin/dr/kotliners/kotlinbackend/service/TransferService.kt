package dr.kotliners.kotlinbackend.service

import dr.kotliners.kotlinbackend.model.Account
import dr.kotliners.kotlinbackend.model.Transaction
import javax.inject.Inject

class TransferService @Inject constructor() {
    fun depositMoney(account: Account, depositString: String?): Transaction {
        val deposit = depositString.toBigDecimalOrThrow()

        synchronized(account) {
            val transaction = Transaction.depositTransaction(account.id, deposit)
            account.transactions.add(transaction)
            account.amount = account.amount.add(deposit)

            return transaction
        }
    }

    fun transferMoney(sourceAccount: Account, destinationAccount: Account, transferString: String?): Transaction {
        val transferValue = transferString.toBigDecimalOrThrow()

        synchronized(sourceAccount) {
            if (sourceAccount.amount < transferValue) throw IllegalArgumentException("Insufficient funds in the account")

            val sourceTransaction =
                Transaction.transferTransaction(destinationAccount.id, transferValue.negate())
            sourceAccount.transactions.add(sourceTransaction)

            destinationAccount.transactions.add(
                Transaction.transferTransaction(sourceAccount.id, transferValue)
            )

            sourceAccount.amount = sourceAccount.amount.subtract(transferValue)
            destinationAccount.amount = destinationAccount.amount.add(transferValue)

            return sourceTransaction
        }
    }

    private fun String?.toBigDecimalOrThrow() =
        this?.toBigDecimalOrNull() ?: throw NumberFormatException("Amount:$this should be are number value")
}