package dr.kotliners.kotlinbackend.exception

import dr.kotliners.kotlinbackend.service.TransferData

class OptimisticLockException(transfer: TransferData) :
    IllegalStateException("Optimistic lock exception: $transfer")