package dr.kotliners.kotlinbackend.model

import java.math.BigDecimal
import java.util.*

data class Transaction(
    val id: UUID,
    val accountId: UUID,
    val value: BigDecimal,
    val type: TransactionType,
    val date: Long
) {
    companion object {
        fun transactionByType(accountId: UUID, value: BigDecimal, type: TransactionType): Transaction {
            val transactionId = UUID.randomUUID()
            return Transaction(
                id = transactionId,
                accountId = accountId,
                date = System.currentTimeMillis(),
                type = type,
                value = value
            )
        }
    }
}

enum class TransactionType {
    TRANSFER,
    WITHDRAWAL,
    DEPOSIT
}