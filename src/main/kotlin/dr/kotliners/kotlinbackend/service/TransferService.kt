package dr.kotliners.kotlinbackend.service

import dr.kotliners.kotlinbackend.dao.AccountDao
import dr.kotliners.kotlinbackend.dao.TransactionDao
import dr.kotliners.kotlinbackend.exception.OptimisticLockException
import dr.kotliners.kotlinbackend.model.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.locks.StampedLock
import javax.inject.Inject

class TransferService @Inject constructor(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    properties: Properties
) {
    private val LOG = LoggerFactory.getLogger(TransferService::class.java)

    private val poolSize = properties.getProperty("transfer.receivers").toInt()
    private val transferLocks = ConcurrentHashMap<Int, StampedLock>(poolSize * 2)
    private val receivers = Executors.newFixedThreadPool(poolSize)

    fun depositMoney(userId: Int, depositString: String?): Transaction {
        val deposit = depositString.toBigDecimalOrThrow()

        return updateAmount(
            TransferData(
                userId = userId,
                value = deposit,
                type = TransactionType.DEPOSIT
            )
        )
    }

    fun transferMoney(sourceUserId: Int, destinationUserId: Int, transferString: String?): Transaction {
        val transferValue = transferString.toBigDecimalOrThrow()

        return updateAmount(
            TransferData(
                userId = sourceUserId,
                value = transferValue.negate()
            )
        ).also {
            receivers.submit {
                updateAmount(
                    TransferData(
                        userId = destinationUserId,
                        value = transferValue
                    )
                )
            }
        }
    }

    fun transactionHistory(accountId: UUID): List<Transaction> {
        return transactionDao.findByAccountId(accountId)
            .map { it.toTransaction() }
    }

    private fun updateAmount(transferData: TransferData): Transaction {
        val lock = transferLocks.computeIfAbsent(transferData.userId) { StampedLock() }
        transferData.readStamp = lock.tryOptimisticRead()
        try {
            return transaction {
                transferData.account = findAccount(transferData.userId)

                LOG.info(
                    "${transferData.type}:${transferData.value} store. Current amount:${transferData.account?.amount}. Optimistic Lock Valid:${lock.validate(
                        transferData.readStamp
                    )}"
                )
                storeTransaction(lock, transferData)
            }.toTransaction()
        } finally {
            if (lock.tryConvertToWriteLock(transferData.readStamp) != 0L) {
                transferLocks.remove(transferData.userId)
            }
        }
    }

    private fun findAccount(userId: Int): AccountDB {
        return accountDao.findByUser(userId)
    }

    private fun storeTransaction(lock: StampedLock, transferData: TransferData): TransactionDB {
        val stamp = lock.tryConvertToWriteLock(transferData.readStamp)
        if (stamp == 0L) {
            throw OptimisticLockException(transferData)
        } else {
            try {
                return transactionDao.storeTransaction(transferData).apply {
                    LOG.info(
                        "$type:$value done. Current amount:${account.amount}, id:$id. Optimistic Lock Valid:${lock.validate(
                            stamp
                        )}"
                    )
                }
            } finally {
                transferData.readStamp = lock.tryConvertToOptimisticRead(stamp)
            }
        }
    }

    private fun String?.toBigDecimalOrThrow() =
        this?.toBigDecimalOrNull() ?: throw NumberFormatException("Amount:$this should be are number value")
}

data class TransferData(
    val userId: Int,
    var account: AccountDB? = null,
    val value: BigDecimal,
    val type: TransactionType = TransactionType.TRANSFER,
    var readStamp: Long = 0
)