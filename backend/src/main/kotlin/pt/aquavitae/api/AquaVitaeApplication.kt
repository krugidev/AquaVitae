package pt.aquavitae.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AquaVitaeApplication

fun main(args: Array<String>) {
    runApplication<AquaVitaeApplication>(*args)
}
