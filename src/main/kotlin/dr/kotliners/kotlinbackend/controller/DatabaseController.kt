package dr.kotliners.kotlinbackend.controller

import dr.kotliners.kotlinbackend.dao.UserDao
import org.jetbrains.exposed.sql.Database
import java.util.*
import javax.inject.Inject

class DatabaseController @Inject constructor(
    private val properties: Properties,
    private val userDao: UserDao
) {
    fun initDB() {
        Database.connect(properties.getProperty("database.url"), driver = "org.h2.Driver")

        userDao.createTestData()
    }
}