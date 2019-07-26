package cc.tapgo


import mu.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.*


private val logger = KotlinLogging.logger {}

@RestController
@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class TapgoBotApplication {
    @PostMapping("/api/line")
    fun testPost(@RequestHeader allHeaders:Map<String,String>, @RequestBody body:String): String {
        for ((key, value) in allHeaders) {
            logger.info {"[Header] $key: $value" }
        }
        logger.info {"BODY: $body" }
        return "test"
    }

    @GetMapping("/api/line")
    fun testGet(@RequestHeader allHeaders:Map<String,String>, @RequestParam  allParams:Map<String,String>):String{
        for ((key, value) in allHeaders) {
            logger.info {"[Header] $key: $value" }
        }
        for ((key, value) in allParams) {
            logger.info {"[Parameter] $key: $value" }
        }
        return "test"
    }
}

fun main(args: Array<String>) {
    runApplication<TapgoBotApplication>(*args)
}


