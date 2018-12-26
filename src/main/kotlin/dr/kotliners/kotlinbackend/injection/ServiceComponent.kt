package dr.kotliners.kotlinbackend.injection

import dagger.Component
import dr.kotliners.kotlinbackend.controller.ServiceController
import javax.inject.Singleton

@Singleton
@Component(modules = [ServiceModule::class])
interface ServiceComponent {

    fun inject(serviceRunner: ServiceController)
}