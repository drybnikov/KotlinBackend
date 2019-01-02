package dr.kotliners.kotlinbackend.dao

import dr.kotliners.kotlinbackend.model.AccountDB
import dr.kotliners.kotlinbackend.model.Accounts
import dr.kotliners.kotlinbackend.model.UserDB
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

class AccountDao @Inject constructor() {

    fun findById(accountId: UUID): AccountDB =
        transaction {
            AccountDB.findById(accountId)
                ?: throw IllegalArgumentException("Account :${accountId.node()} not found")
        }

    fun findByUser(userId: Int): AccountDB {
        return transaction {
            addLogger(StdOutSqlLogger)

            AccountDB.find { Accounts.user eq userId }
                .firstOrNull() ?: AccountDB.new {
                user = UserDB[userId]
                currency = "USD"
                amount = BigDecimal.ZERO
            }
        }
    }
}