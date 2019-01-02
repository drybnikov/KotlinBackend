package dr.kotliners.kotlinbackend.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

data class User(
    val id: Int,
    val name: String,
    val email: String
) {
    companion object {
        fun fromUserDB(userDb: UserDB) = User(
            id = userDb.id.value,
            name = userDb.name,
            email = userDb.email
        )
    }
}

object Users : IntIdTable() {
    val name = varchar("name", 50)
    val email = varchar("email", 50)
}

class UserDB(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserDB>(Users)

    var name by Users.name
    var email by Users.email
}