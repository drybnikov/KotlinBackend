package dr.kotliners.kotlinbackend.dao

import dr.kotliners.kotlinbackend.model.UserDB
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import javax.inject.Inject

class UserDao @Inject constructor() {

    fun findUserById(id: Int): UserDB? =
        transaction {
            addLogger(StdOutSqlLogger)
            UserDB.findById(id)
        }

    fun allUsers(): List<UserDB> =
        transaction {
            UserDB.all().sortedBy { it.name }
        }
}