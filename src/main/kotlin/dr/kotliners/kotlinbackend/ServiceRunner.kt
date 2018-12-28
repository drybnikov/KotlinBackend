package dr.kotliners.kotlinbackend

import dr.kotliners.kotlinbackend.controller.ServiceController
import dr.kotliners.kotlinbackend.injection.DaggerServiceComponent
import dr.kotliners.kotlinbackend.injection.ServiceComponent
import org.slf4j.LoggerFactory

class ServiceRunner {
    init {
        serviceComponent = DaggerServiceComponent.builder()
            .build()
    }

    fun run() {
        initControllers()
    }

    private fun initControllers() {
        ServiceController()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ServiceRunner::class.java)

        //platformStatic allow access it from java code
        @JvmStatic
        lateinit var serviceComponent: ServiceComponent
    }
}