package it.unibo.serviceaccessgui

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ServiceAccessGuiApp

fun main(args: Array<String>) {
    runApplication<ServiceAccessGuiApp>(*args)
}
