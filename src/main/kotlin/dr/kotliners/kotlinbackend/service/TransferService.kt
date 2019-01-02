package dr.kotliners.kotlinbackend.service

import dr.kotliners.kotlinbackend.dao.AccountDao
import dr.kotliners.kotlinbackend.dao.TransactionDao
import dr.kotliners.kotlinbackend.exception.OptimisticLockException
import dr.kotliners.kotlinbackend.model.AccountDB
import dr.kotliners.kotlinbackend.model.Transaction
import dr.kotliners.kotlinbackend.model.TransactionType
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.locks.StampedLock
import javax.inject.Inject

private const val POOL_SIZE = 4

class TransferService @Inject constructor(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao
) {
    private val LOG = LoggerFactory.getLogger(TransferService::class.java)

    private val transferLocks = ConcurrentHashMap<Int, StampedLock>(POOL_SIZE * 2)
    private val receivers = Executors.newFixedThreadPool(POOL_SIZE)

    fun depositMoney(userId: Int, depositString: String?): Transaction {
        val deposit = depositString.toBigDecimalOrThrow()

        return updateAmount(userId, deposit, TransactionType.DEPOSIT)
    }

    fun transferMoney(sourceUserId: Int, destinationUserId: Int, transferString: String?): Transaction {
        val transferValue = transferString.toBigDecimalOrThrow()

        val sendTransaction = updateAmount(sourceUserId, transferValue.negate(), TransactionType.TRANSFER)
        receivers.submit {
            updateAmount(destinationUserId, transferValue, TransactionType.TRANSFER)
        }
        return sendTransaction
    }

    fun transactionHistory(accountId: UUID): List<Transaction> {
        return transactionDao.findByAccountId(accountId)
            .map { Transaction(
                id = it.id.value,
                accountId = accountId,
                value = it.value,
                type = it.type,
                date = it.date.millis
            ) }
    }

    private fun updateAmount(userId: Int, value: BigDecimal, type: TransactionType): Transaction {
        val lock = transferLocks.computeIfAbsent(userId) { StampedLock() }
        var stamp = lock.tryOptimisticRead()
        try {
            val account = findAccount(userId)

            val transaction = Transaction.transactionByType(account.id.value, value, type)
            LOG.info(
                "${transaction.type}:$value store. Current amount:${account.amount}, id:${transaction.id}. Optimistic Lock Valid:${lock.validate(
                    stamp
                )}"
            )
            stamp = storeTransaction(lock, stamp, transaction)

            LOG.info(
                "${transaction.type}:$value done. Current amount:${account.amount}, id:${transaction.id}. Optimistic Lock Valid:${lock.validate(
                    stamp
                )}"
            )

            return transaction
        } finally {
            if (lock.tryConvertToWriteLock(stamp) != 0L) {
                transferLocks.remove(userId)
            }
        }
    }

    private fun findAccount(userId: Int): AccountDB {
        return accountDao.findByUser(userId)
    }

    private fun storeTransaction(lock: StampedLock, readStamp: Long, transaction: Transaction): Long {
        var stamp = lock.tryConvertToWriteLock(readStamp)
        if (stamp == 0L) {
            throw OptimisticLockException(transaction)
        } else {
            try {
                transactionDao.storeTransaction(transaction)
            } finally {
                stamp = lock.tryConvertToOptimisticRead(stamp)
            }
        }
        return stamp
    }

    private fun String?.toBigDecimalOrThrow() =
        this?.toBigDecimalOrNull() ?: throw NumberFormatException("Amount:$this should be are number value")
}