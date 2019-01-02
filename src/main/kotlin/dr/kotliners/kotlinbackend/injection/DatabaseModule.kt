package dr.kotliners.kotlinbackend.injection

import dagger.Module
import dagger.Provides
import dr.kotliners.kotlinbackend.ApplicationPropertiesProvider
import dr.kotliners.kotlinbackend.KotlinBackendApp
import org.jetbrains.exposed.sql.Database
import java.util.*
import javax.inject.Singleton

@Module
class DatabaseModule(private val app: KotlinBackendApp) {

    @Provides
    fun provideDatabase(properties: Properties) : Database {
        return Database.connect(properties.getProperty("database.url"), driver = "org.h2.Driver")
    }

    @Provides
    @Singleton
    fun provideProperties(): Properties {
        return ApplicationPropertiesProvider().readProperties(app)
    }
}