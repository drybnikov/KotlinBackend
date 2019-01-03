package dr.kotliners.kotlinbackend.dao

import dr.kotliners.kotlinbackend.model.AccountDB
import dr.kotliners.kotlinbackend.model.Accounts
import dr.kotliners.kotlinbackend.model.UserDB
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import javax.inject.Inject

class AccountDao @Inject constructor() {

    fun findByUser(userId: Int): AccountDB {
        return transaction {
            AccountDB.find { Accounts.user eq userId }
                .firstOrNull() ?: AccountDB.new {
                user = UserDB[userId]
                currency = "USD"
                amount = BigDecimal.ZERO
            }
        }
    }
}