package dr.kotliners.kotlinbackend.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.UUIDTable
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

object Transactions : UUIDTable() {
    val account = reference("account", Accounts)
    val value = decimal("value", 18, 2)
    val type = enumeration("type", TransactionType::class)
    val date = datetime("date")
}

class TransactionDB(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TransactionDB>(Transactions)

    var account by AccountDB referencedOn Transactions.account
    var value by Transactions.value
    var type by Transactions.type
    var date by Transactions.date
}