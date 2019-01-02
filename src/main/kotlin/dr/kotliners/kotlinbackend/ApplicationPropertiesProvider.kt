package dr.kotliners.kotlinbackend

import java.io.IOException
import java.util.*

class ApplicationPropertiesProvider {
    internal fun readProperties(app: KotlinBackendApp): Properties {
        var result: Properties
        try {
            app::class.java.classLoader.getResourceAsStream("application.properties")
                .apply {
                    result = Properties()
                    result.load(this)
                }
        } catch (e: IOException) {
            //LOG.error("Failed to load properties", e)
            throw RuntimeException(e)
        }

        return result
    }
}