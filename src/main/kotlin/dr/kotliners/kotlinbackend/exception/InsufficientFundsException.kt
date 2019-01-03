package dr.kotliners.kotlinbackend.exception

import dr.kotliners.kotlinbackend.service.TransferData

class InsufficientFundsException(transfer: TransferData) :
    IllegalStateException("Insufficient funds for transfer: $transfer")