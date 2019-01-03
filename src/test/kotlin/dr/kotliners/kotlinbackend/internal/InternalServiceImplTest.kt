package dr.kotliners.kotlinbackend.internal

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dr.kotliners.kotlinbackend.dao.AccountDao
import dr.kotliners.kotlinbackend.dao.UserDao
import dr.kotliners.kotlinbackend.service.TransferService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class InternalServiceImplTest {

    private val userDao: UserDao = mock()
    private val accountDao: AccountDao = mock()
    private val transferService: TransferService = mock()

    private val service = InternalServiceImpl(userDao, accountDao, transferService)

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun `produce IllegalArgumentException when find user with nul id`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            service.findUserById(null)
        }
    }

    @Test
    fun `produce IllegalArgumentException when can not find user`() {
        whenever(userDao.findUserById(any())).thenReturn(null)

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            service.findUserById(111)
        }
    }
}