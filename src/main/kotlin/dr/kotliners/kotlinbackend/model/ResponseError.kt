package dr.kotliners.kotlinbackend.model

data class ResponseError(val message: String) {
    constructor(exception: Exception) : this(exception.message.orEmpty())
}