package dr.kotliners.kotlinbackend.dao

import dr.kotliners.kotlinbackend.model.UserDB
import dr.kotliners.kotlinbackend.model.Users
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
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


    fun createTestData() {
        transaction {
            addLogger(StdOutSqlLogger)

            SchemaUtils.create(Users)

            UserDB.new {
                name = "Alice"
                email = "alice@alice.kt"
            }

            UserDB.new {
                name = "Bob"
                email = "bob@bob.kt"
            }

            UserDB.new {
                name = "Carol"
                email = "carol@carol.kt"
            }

            UserDB.new {
                name = "Dave"
                email = "dave@dave.kt"
            }
        }
    }
}