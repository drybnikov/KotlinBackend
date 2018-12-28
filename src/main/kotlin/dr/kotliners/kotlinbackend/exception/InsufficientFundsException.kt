package dr.kotliners.kotlinbackend.exception

import dr.kotliners.kotlinbackend.model.Transaction

class InsufficientFundsException(transaction: Transaction) :
    IllegalStateException("Insufficient funds for transaction: $transaction")