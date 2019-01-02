package dr.kotliners.kotlinbackend

import dr.kotliners.kotlinbackend.controller.ServiceController
import dr.kotliners.kotlinbackend.injection.ServiceComponent
import org.slf4j.LoggerFactory

class KotlinBackendApp {

    init {
        serviceComponent = ServiceComponent.createComponent(this)
    }

    fun run() {
        println("Spark Service running...")

        ServiceController()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(KotlinBackendApp::class.java)

        //platformStatic allow access it from java code
        @JvmStatic
        lateinit var serviceComponent: ServiceComponent
    }
}

fun main(args: Array<String>) {
    KotlinBackendApp().run()
}
