package dr.kotliners.kotlinbackend.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.UUIDTable
import org.joda.time.format.DateTimeFormat
import java.math.BigDecimal
import java.util.*

data class Transaction(
    val id: UUID,
    val value: BigDecimal,
    val type: TransactionType,
    val date: String
)

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

fun TransactionDB.toTransaction() = Transaction(
    id = id.value,
    value = value,
    type = type,
    date = date.toString(DateTimeFormat.fullDateTime())
)