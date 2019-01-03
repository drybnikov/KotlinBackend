package dr.kotliners.kotlinbackend.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

data class User(
    val id: Int,
    val name: String,
    val email: String
)

object Users : IntIdTable() {
    val name = varchar("name", 50)
    val email = varchar("email", 50)
}

class UserDB(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserDB>(Users)

    var name by Users.name
    var email by Users.email
}

fun UserDB.toJsonUser() = User(
    id = id.value,
    name = name,
    email = email
)