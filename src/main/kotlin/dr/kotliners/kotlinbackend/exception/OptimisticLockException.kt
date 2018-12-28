package dr.kotliners.kotlinbackend.exception

import dr.kotliners.kotlinbackend.model.Transaction

class OptimisticLockException(transaction: Transaction) :
    IllegalStateException("Optimistic lock exception: $transaction")