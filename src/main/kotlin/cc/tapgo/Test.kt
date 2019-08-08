//package cc.tapgo
//
//import com.google.common.io.ByteStreams
//import com.linecorp.bot.client.LineMessagingClient
//import com.linecorp.bot.client.MessageContentResponse
//import com.linecorp.bot.model.event.MessageEvent
//import com.linecorp.bot.model.event.message.TextMessageContent
//import com.linecorp.bot.model.message.TextMessage
//import com.linecorp.bot.spring.boot.annotation.EventMapping
//import com.linecorp.bot.spring.boot.annotation.LineMessageHandler
//import org.springframework.beans.factory.annotation.Autowired
//
//import java.io.OutputStream
//import java.nio.file.Files
//
///**
// * Created by AlexHe on 2019-08-07.
// * Describe
// */
//@LineMessageHandler
//class Test {
//    @Autowired
//    private val lineMessagingClient: LineMessagingClient? = null
//
//    @EventMapping
//    @Throws(Exception::class)
//    fun handleTextMessageEvent(event: MessageEvent<TextMessageContent>): TextMessage {
//        val message = event.message
//        return TextMessage("兒爾")
//    }
//
//
//}