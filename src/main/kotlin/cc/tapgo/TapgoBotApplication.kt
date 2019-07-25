package cc.tapgo


import mu.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


private val logger = KotlinLogging.logger {}

@RestController
@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class TapgoBotApplication {
    @GetMapping("/test")
    fun test(): String {
        logger.debug {"BBBBBBBB ======= BBBBBBBB"}
        return "test"
    }
}

fun main(args: Array<String>) {
    runApplication<TapgoBotApplication>(*args)
}

