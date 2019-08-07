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
import java.nio.file.Files


val logger = KotlinLogging.logger {}
val downloadedContentDir = Files.createTempDirectory("line-bot")!!

@LineMessageHandler
@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class TapgoBotApplication

fun main(args: Array<String>) {
    runApplication<TapgoBotApplication>(*args)
}