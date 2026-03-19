package dev.havir.hellouniverse

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class HellouniverseApplication

fun main(args: Array<String>) {
    runApplication<HellouniverseApplication>(*args)
}
