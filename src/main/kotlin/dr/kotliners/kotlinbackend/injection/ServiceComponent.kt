package dr.kotliners.kotlinbackend.injection

import dagger.Component
import dr.kotliners.kotlinbackend.KotlinBackendApp
import dr.kotliners.kotlinbackend.controller.ServiceController
import javax.inject.Singleton

@Singleton
@Component(modules = [ServiceModule::class, DatabaseModule::class])
interface ServiceComponent {

    fun inject(serviceRunner: ServiceController)

    companion object {
        internal fun createComponent(app: KotlinBackendApp): ServiceComponent =
            DaggerServiceComponent.builder()
                .databaseModule(DatabaseModule(app))
                .build()
    }
}