package dr.kotliners.kotlinbackend

class KotlinBackendApp {

    fun run() {
        println("Spark Service running...")

        ServiceRunner().run()
    }
}

fun main(args: Array<String>) {
    KotlinBackendApp().run()
}
