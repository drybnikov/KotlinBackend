package dr.kotliners.kotlinbackend.controller

import com.google.gson.Gson
import spark.ResponseTransformer
import javax.inject.Inject

class JsonResponseTransformer @Inject constructor() : ResponseTransformer {

    private val gson = Gson()

    override fun render(model: Any?): String {
        return gson.toJson(model)
    }
}