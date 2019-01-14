package dr.kotliners.kotlinbackend.controller

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import spark.ResponseTransformer
import javax.inject.Inject

class JsonResponseTransformer @Inject constructor() : ResponseTransformer {

    val gson = Gson()

    override fun render(model: Any?): String {
        return gson.toJson(model)
    }

    inline fun <reified T> fromJson(json: String): T =
        gson.fromJson(json, object : TypeToken<T>() {}.type)
}