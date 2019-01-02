package dr.kotliners.kotlinbackend.internal

import dr.kotliners.kotlinbackend.model.Account
import dr.kotliners.kotlinbackend.model.Transaction
import dr.kotliners.kotlinbackend.model.User
import dr.kotliners.kotlinbackend.model.UserDB
import org.jetbrains.exposed.sql.SizedIterable

interface InternalService {
    fun users(): List<User>

    fun findUserById(id: Int?): User

    fun userAccount(userId: Int): Account

    fun depositMoney(userId: Int, deposit: String?): Transaction

    fun transferMoney(sourceUserId: Int, destinationUserId: String?, amount: String?): Transaction
}