package dr.kotliners.kotlinbackend.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.UUIDTable
import java.math.BigDecimal
import java.util.*

data class Account(
    val id: UUID,
    val userId: Int,
    val currency: Currency,
    var amount: BigDecimal,
    val transactions: List<Transaction>
) {
    companion object {
        fun fromDB(accountDB: AccountDB) =
            Account(
                id = accountDB.id.value,
                amount = accountDB.amount,
                userId = accountDB.user.id.value,
                currency = Currency.getInstance(accountDB.currency),
                transactions = ArrayList()
            )
    }
}

object Accounts : UUIDTable() {
    val user = reference("user", Users)
    val currency = varchar("currency", 5)
    val amount = decimal("amount", 18, 2)
    val transactions = reference("transactions", Transactions).nullable()
}

class AccountDB(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<AccountDB>(Accounts)

    var user by UserDB referencedOn Accounts.user
    var currency by Accounts.currency
    var amount by Accounts.amount
    var transactions by TransactionDB via Transactions
}