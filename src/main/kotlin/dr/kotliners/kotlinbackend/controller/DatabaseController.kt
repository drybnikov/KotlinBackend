package dr.kotliners.kotlinbackend.controller

import dr.kotliners.kotlinbackend.model.Accounts
import dr.kotliners.kotlinbackend.model.UserDB
import dr.kotliners.kotlinbackend.model.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import javax.inject.Inject

class DatabaseController @Inject constructor(
    private val properties: Properties
) {
    fun initDB() {
        Database.connect(properties.getProperty("database.url"), driver = "org.h2.Driver")

        createTestData()
    }

    private fun createTestData() {
        transaction {
            addLogger(StdOutSqlLogger)

            SchemaUtils.create(Users, Accounts)

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