package dr.kotliners.kotlinbackend.dao

import dr.kotliners.kotlinbackend.model.User
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDao @Inject constructor() {
    private val data by lazy {
        hashMapOf(
            0 to User(name = "Alice", email = "alice@alice.kt", id = 0),
            1 to User(name = "Bob", email = "bob@bob.kt", id = 1),
            2 to User(name = "Carol", email = "carol@carol.kt", id = 2),
            3 to User(name = "Dave", email = "dave@dave.kt", id = 3)
        )
    }

    private var lastId: AtomicInteger = AtomicInteger(data.size - 1)

    fun users() = data.values.toList()

    fun save(name: String, email: String) {
        val id = lastId.incrementAndGet()
        data.put(id, User(name = name, email = email, id = id))
    }

    fun findById(id: Int?): User? {
        return data[id]
    }

    fun findByEmail(email: String): User? {
        return data.values.find { it.email == email }
    }

    fun update(id: Int, name: String, email: String) {
        data.put(id, User(name = name, email = email, id = id))
    }

    fun delete(id: Int) {
        data.remove(id)
    }
}