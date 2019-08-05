package cc.tapgo


import com.linecorp.bot.model.event.Event
import com.linecorp.bot.model.event.MessageEvent
import com.linecorp.bot.model.event.message.TextMessageContent
import com.linecorp.bot.model.message.Message
import com.linecorp.bot.model.message.TextMessage
import com.linecorp.bot.spring.boot.annotation.EventMapping
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.*


private val logger = KotlinLogging.logger {}

@RestController
@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
@LineMessageHandler
class TapgoBotApplication {
    @EventMapping
    fun handleTextMessageEvent(event: MessageEvent<TextMessageContent>): Message {
        println("event: $event")
        val originalMessageText = event.message.text
        return TextMessage(originalMessageText)
    }

    @EventMapping
    fun handleDefaultMessageEvent(event: Event) {
        println("event: $event")
    }
    /*@PostMapping("/api/line")
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
    }*/
}

fun main(args: Array<String>) {
    runApplication<TapgoBotApplication>(*args)
}


