package cc.tapgo


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
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import java.nio.file.Files
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.Instant
import java.time.LocalDateTime
import java.time.Clock


val logger = KotlinLogging.logger {}
val downloadedContentDir = Files.createTempDirectory("line-bot")!!
var btn1Count = 0
var btn1Echo = ""
var btn2Count = 0
var btn2Echo = ""
var btn3Count = 0
var btn3Echo = ""
val currentPointPlus8 = LocalDateTime.now(Clock.system(ZoneId.of("+8")))

@RestController
@LineMessageHandler
@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class TapgoBotApplication {
//    @PostMapping("/api/line")
//    fun testPost(@RequestHeader allHeaders:Map<String,String>, @RequestBody body:String): String {
//        for ((key, value) in allHeaders) {
//            logger.info {"[Header] $key: $value" }
//        }
//        logger.info {"BODY: $body" }
//        return "test"
//    }

    @GetMapping("/api/line")
    fun testGet(@RequestHeader allHeaders:Map<String,String>, @RequestParam  allParams:Map<String,String>): ModelAndView {
        var echo = "Error"
        for ((key, value) in allHeaders) {
            logger.info {"[Header] $key: $value" }
        }
        for ((key, value) in allParams) {
            logger.info {"[Parameter] $key: $value" }
            if (("buttonId") == key && ("1") == value) {
                btn1Count ++
                btn1Echo += "${allParams["sourceId"].toString()}\n"
                echo = "https://store.line.me/family/manga/en"
            }
            if (("buttonId") == key && ("2") == value) {
                btn2Count ++
                btn2Echo += "${allParams["sourceId"].toString()}\n"
                echo = "https://store.line.me/family/music/en"
            }
            if (("buttonId") == key && ("3") == value) {
                btn3Count ++
                btn3Echo += "${allParams["sourceId"].toString()}\n"
                echo = "https://store.line.me/family/play/en"
            }
        }
        return ModelAndView(RedirectView(echo))
    }
}

fun main(args: Array<String>) {
    runApplication<TapgoBotApplication>(*args)
}