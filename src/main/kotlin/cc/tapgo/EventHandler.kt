package cc.tapgo


import java.io.IOException
import java.io.UncheckedIOException
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.concurrent.ExecutionException
import java.util.stream.Collectors
import java.util.stream.Stream

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

import com.google.common.io.ByteStreams

import com.linecorp.bot.client.LineMessagingClient
import com.linecorp.bot.client.MessageContentResponse
import com.linecorp.bot.model.Broadcast
import com.linecorp.bot.model.Multicast
import com.linecorp.bot.model.PushMessage
import com.linecorp.bot.model.ReplyMessage
import com.linecorp.bot.model.action.*
import com.linecorp.bot.model.event.Event
import com.linecorp.bot.model.event.FollowEvent
import com.linecorp.bot.model.event.JoinEvent
import com.linecorp.bot.model.event.MessageEvent
import com.linecorp.bot.model.event.PostbackEvent
import com.linecorp.bot.model.event.UnfollowEvent
import com.linecorp.bot.model.event.message.*
import com.linecorp.bot.model.event.source.GroupSource
import com.linecorp.bot.model.event.source.RoomSource
import com.linecorp.bot.model.message.*
import com.linecorp.bot.model.message.imagemap.*
import com.linecorp.bot.model.message.template.ButtonsTemplate
import com.linecorp.bot.model.message.template.CarouselColumn
import com.linecorp.bot.model.message.template.CarouselTemplate
import com.linecorp.bot.model.message.template.ConfirmTemplate
import com.linecorp.bot.model.message.template.ImageCarouselColumn
import com.linecorp.bot.model.message.template.ImageCarouselTemplate
import com.linecorp.bot.spring.boot.annotation.EventMapping
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler
import org.springframework.http.HttpEntity
import java.util.*


/**
 * Created by AlexHe on 2019-08-07.
 * Describe
 */
private val TAPGO_ALEX = "U9b087fb7b7fb48606bca0604d1b6f2a6"
private val TAPGO_MIKE = "U394fc2fbdabd1249d79accb75b7dd621"
private val TAPGO_BOB = "Uf0dc1149924b4c0b4c20c9e307e32e5b"
private val ALEX = "U0051e296713d9446d34a22013834ad81"
private val 郭 = "U1b133f8afc3a1882d3a80c23bf0519e2"
private val DEV_SHION = "U2ff4562feafef7806d120e05b627d910"
@LineMessageHandler
class EventHandler {
    companion object {
        fun createUri(path: String): String {
            return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(path).build()
                .toUriString()
        }

        fun saveContent(ext: String, responseBody: MessageContentResponse): DownloadedContent {
            logger.info("Got content-type: {}", responseBody)
            var tempFile = createTempFile(ext)
            try {
                var outputStream = Files.newOutputStream(tempFile.path)
                ByteStreams.copy(responseBody.stream, outputStream)
                logger.info("Saved {}: {}", ext, tempFile)
                return tempFile
            } catch (e: IOException) {
                throw UncheckedIOException(e)
            }
        }

        fun createTempFile(ext: String): DownloadedContent {
            var fileName = LocalDateTime.now().toString() + '-' + UUID.randomUUID().toString() + '.' + ext
            var tempFile = downloadedContentDir.resolve(fileName)
            tempFile.toFile().deleteOnExit()
            return DownloadedContent(tempFile, createUri("/downloaded/" + tempFile.fileName))
        }
    }

    @Autowired
    private val lineMessagingClient: LineMessagingClient? = null

    @EventMapping
    @Throws(Exception::class)
    fun handleTextMessageEvent(event: MessageEvent<TextMessageContent>) {
        val message = event.getMessage()
        handleTextContent(event.replyToken, event, message)
    }
    @EventMapping
    fun handleStickerMessageEvent(event: MessageEvent<StickerMessageContent>) {
        handleSticker(event.replyToken, event.message)
    }
    @EventMapping
    fun handleLocationMessageEvent(event: MessageEvent<LocationMessageContent>) {
        val locationMessage = event.message
        var locationMessageTitle: String
        locationMessageTitle = if (locationMessage.title == null){
            "NULL"
        } else {
            locationMessage.title
        }
        reply(
            event.replyToken, LocationMessage(
                locationMessageTitle,
                locationMessage.address,
                locationMessage.latitude,
                locationMessage.longitude
            )
        )
    }
    @EventMapping
    @Throws(IOException::class)
    fun handleImageMessageEvent(event: MessageEvent<ImageMessageContent>) {
        // You need to install ImageMagick
        handleHeavyContent(
            event.replyToken,
            event.message.id
        ) { responseBody ->
            val provider = event.message.contentProvider
            val jpg: DownloadedContent
            val previewImg: DownloadedContent
            if (provider.isExternal) {
                jpg = DownloadedContent(null, provider.originalContentUrl)
                previewImg = DownloadedContent(null, provider.previewImageUrl)
            } else {
                //TODO: // You need to install ImageMagick
                jpg = saveContent("jpg", responseBody)
//                previewImg = createTempFile("jpg")
//                system(
//                    "convert",
//                    "-resize", "240x",
//                    jpg.path.toString(),
//                    previewImg.path.toString()
//                )
            }
            reply(
                event.replyToken,
                ImageMessage(jpg.uri, jpg.uri)
            )
        }
    }
    @EventMapping
    @Throws(IOException::class)
    fun handleAudioMessageEvent(event: MessageEvent<AudioMessageContent>) {
        handleHeavyContent(
            event.replyToken,
            event.message.id
        ) {
            var provider = event.message.contentProvider
            var mp4: DownloadedContent
            mp4 = if (provider.isExternal) {
                DownloadedContent(null, provider.originalContentUrl)
            } else {
                saveContent("mp4", it)
            }
            reply(event.replyToken, AudioMessage(mp4.uri, 6000))
        }
    }
    @EventMapping
    @Throws(IOException::class)
    fun handleVideoMessageEvent(event: MessageEvent<VideoMessageContent>) {
        // You need to install ffmpeg and ImageMagick.
        handleHeavyContent(
            event.replyToken,
            event.message.id
        ) { responseBody ->
            val provider = event.message.contentProvider
            val mp4: DownloadedContent
            val previewImg: DownloadedContent
            if (provider.isExternal) {
                mp4 = DownloadedContent(null, provider.originalContentUrl)
                previewImg = DownloadedContent(null, provider.previewImageUrl)
            } else {
                mp4 = saveContent("mp4", responseBody)
//                previewImg = createTempFile("jpg")
//                system(
//                    "convert",
//                    (mp4.path!! + "[0]").toString(),
//                    previewImg.path.toString()
//                )
            }
            reply(
                event.replyToken,
                VideoMessage(mp4.uri, mp4.uri)
            )
        }
    }
    @EventMapping
    fun handleFileMessageEvent(event: MessageEvent<FileMessageContent>) {
        this.reply(
            event.replyToken,
            TextMessage(
                String.format(
                    "Received '%s'(%d bytes)",
                    event.message.fileName,
                    event.message.fileSize
                )
            )
        )
    }
    @EventMapping
    fun handleUnfollowEvent(event: UnfollowEvent) {
        logger.info("unfollowed this bot: {}", event)
    }
    @EventMapping
    fun handleFollowEvent(event: FollowEvent) {
        val replyToken = event.replyToken
        this.replyText(replyToken, "Got followed event")
    }
    @EventMapping
    fun handleJoinEvent(event: JoinEvent) {
        val replyToken = event.replyToken
        this.replyText(replyToken, "Joined " + event.source)
    }
    @EventMapping
    fun handlePostbackEvent(event:PostbackEvent) {
        val replyToken = event.replyToken
        var eventPostbackContentParams: String = if (event.postbackContent.params == null) {
            "NULL"
        } else {
            event.postbackContent.params.toString()
        }
        this.replyText(replyToken,
            "Got postback data " + event.postbackContent.data + ", param " + eventPostbackContentParams
        )
    }

    private fun reply(replyToken: String, message: Message) {
        reply(replyToken, Collections.singletonList(message))
    }

    private fun reply(replyToken: String, messages: kotlin.collections.List<Message>) {
        try {
            var apiResponse = lineMessagingClient!!.
                replyMessage(ReplyMessage(replyToken, messages))!!.get()
            logger.info("Sent messages: {}", apiResponse)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        } catch (e: ExecutionException) {
            throw RuntimeException(e)
        }
    }
    private fun replyText(replyToken: String, message: String) {
        var message = message
        if (replyToken.isEmpty()) {
            throw IllegalArgumentException("replyToken must not be empty")
        }
        if (message.length > 1000) {
            message = message.substring(0, 1000 - 2) + "……"
        }
        this.reply(replyToken, TextMessage(message))
    }
    private fun push(userId: String, message: Message) {
        push(userId, Collections.singletonList(message))
    }
    private fun push(userId: String, messages: kotlin.collections.List<Message>) {
        try {
            var apiResponse = lineMessagingClient!!.
                pushMessage(PushMessage(userId, messages))!!.get()
            logger.info("Push messages: {}", apiResponse)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        } catch (e: ExecutionException) {
            throw RuntimeException(e)
        }
    }
    private fun pushText(userId: String, message: String) {
        var message = message
        if (userId.isEmpty()) {
            throw IllegalArgumentException("userId must not be empty")
        }
        if (message.length > 1000) {
            message = message.substring(0, 1000 - 2) + "……"
        }
        this.push(userId, TextMessage(message))
    }

    private fun multicast(usersId: MutableSet<String>, message: Message) {
        multicast(usersId, Collections.singletonList(message))
    }
    private fun multicast(usersId: MutableSet<String>, messages: kotlin.collections.List<Message>) {
        try {
            var apiResponse = lineMessagingClient!!.
                multicast(Multicast(usersId, messages))!!.get()
            logger.info("Multicast messages: {}", apiResponse)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        } catch (e: ExecutionException) {
            throw RuntimeException(e)
        }
    }
    private fun multicastText(usersId: MutableSet<String>, message: String) {
        var message = message
        if (usersId.isEmpty()) {
            throw IllegalArgumentException("usersId must not be empty")
        }
        if (message.length > 1000) {
            message = message.substring(0, 1000 - 2) + "……"
        }
        this.multicast(usersId, TextMessage(message))
    }
    private fun broadcast(message: Message, notificationDisabled: Boolean) {
        broadcast(Collections.singletonList(message), notificationDisabled)
    }
    private fun broadcast(messages: kotlin.collections.List<Message>, notificationDisabled: Boolean) {
        try {
            var apiResponse = lineMessagingClient!!.
                broadcast(Broadcast(messages, notificationDisabled))!!.get()
            logger.info("Broadcast messages: {}", apiResponse)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        } catch (e: ExecutionException) {
            throw RuntimeException(e)
        }
    }
    private fun broadcastText(message: String, notificationDisabled: Boolean) {
        var message = message
        if (message.length > 1000) {
            message = message.substring(0, 1000 - 2) + "……"
        }
        this.broadcast(TextMessage(message), notificationDisabled)
    }


    private fun handleHeavyContent(
        replyToken: String, messageId: String,
        messageConsumer: (MessageContentResponse) -> Unit
    ) {
        val response: MessageContentResponse
        try {
            response = lineMessagingClient?.getMessageContent(messageId)
                ?.get()!!
        } catch (e: InterruptedException) {
            reply(replyToken, TextMessage("Cannot get image: " + e.message))
            throw RuntimeException(e)
        } catch (e: ExecutionException) {
            reply(replyToken, TextMessage("Cannot get image: " + e.message))
            throw RuntimeException(e)
        }

        messageConsumer(response)
    }
    private fun handleSticker(replyToken: String, content: StickerMessageContent) {
        reply(
            replyToken, StickerMessage(
                content.packageId, content.stickerId
            )
        )
    }
    @Throws(Exception::class)
    private fun handleTextContent(replyToken: String, event: Event, content: TextMessageContent) {
        val text = content.text

        logger.info("Got text message from replyToken:{}: text:{}", replyToken, text)
        when (text) {
            "profile" -> {
                logger.info(
                    "Invoking 'profile' command: source:{}",
                    event.source
                )
                val userId = event.source.userId
                if (userId != null) {
                    if (event.source is GroupSource) {
                        lineMessagingClient
                            ?.getGroupMemberProfile((event.source as GroupSource).groupId, userId)
                            ?.whenComplete { profile, throwable ->
                                if (throwable != null) {
                                    this.replyText(replyToken, throwable.message!!)
                                    return@whenComplete
                                }
                                this.reply(
                                    replyToken,
                                    Arrays.asList(
                                        TextMessage("(from group)"),
                                        TextMessage("Display name: " + profile.displayName),
                                        TextMessage("Status message: " + profile.statusMessage),
                                        ImageMessage(profile.pictureUrl, profile.pictureUrl),
                                        TextMessage("User ID: " + profile.userId)
                                    )
                                )
                            }
                    } else {
                        lineMessagingClient
                            ?.getProfile(userId)
                            ?.whenComplete { profile, throwable ->
                                if (throwable != null) {
                                    this.replyText(replyToken, throwable.message!!)
                                    return@whenComplete
                                }
                                this.reply(
                                    replyToken,
                                    Arrays.asList(
                                        TextMessage("Display name: " + profile.displayName),
                                        TextMessage("Status message: " + profile.statusMessage),
                                        ImageMessage(profile.pictureUrl, profile.pictureUrl),
                                        TextMessage("User ID: " + profile.userId)
                                    )
                                )

                            }
                    }
                } else {
                    this.replyText(replyToken, "Bot can't use profile API without user ID")
                }
            }
            "bye" -> {
                val source = event.source
                if (source is GroupSource) {
                    this.replyText(replyToken, "Leaving group")
                    lineMessagingClient?.leaveGroup(source.groupId)?.get()
                } else if (source is RoomSource) {
                    this.replyText(replyToken, "Leaving room")
                    lineMessagingClient?.leaveRoom(source.roomId)?.get()
                } else {
                    this.replyText(replyToken, "Bot can't leave from 1:1 chat")
                }
            }
            "confirm" -> {
                val confirmTemplate = ConfirmTemplate(
                    "確認?",
                    MessageAction("Yes", "Yes!"),
                    MessageAction("No", "No!")
                )
                val templateMessage = TemplateMessage("Confirm alt text", confirmTemplate)
                this.reply(replyToken, templateMessage)
            }
            "buttons" -> {
                val imageUrl = createUri("/static/rich/1040")
                val buttonsTemplate = ButtonsTemplate(
                    imageUrl,
                    "My button sample",
                    "Hello, my button",
                    Arrays.asList<Action>(
                        URIAction(
                            "Go to line.me",
                            "https://line.me", null
                        ),
                        PostbackAction(
                            "Say hello1",
                            "hello~"
                        ),
                        PostbackAction(
                            "説 hello2",
                            "hello こんにちは",
                            "你好"
                        ),
                        MessageAction(
                            "Say message",
                            "Hi"
                        )
                    )
                )
                val templateMessage = TemplateMessage("Button alt text", buttonsTemplate)
                this.reply(replyToken, templateMessage)
            }
            "carousel" -> {
                val imageUrl = createUri("/static/rich/1040")
                val carouselTemplate = CarouselTemplate(
                    Arrays.asList(
                        CarouselColumn(
                            imageUrl, "hoge", "fuga", Arrays.asList<Action>(
                                URIAction(
                                    "Go to line.me",
                                    "https://line.me", null
                                ),
                                URIAction(
                                    "Go to line.me",
                                    "https://line.me", null
                                ),
                                PostbackAction(
                                    "Say hello1",
                                    "hello~"
                                )
                            )
                        ),
                        CarouselColumn(
                            imageUrl, "hoge", "fuga", Arrays.asList<Action>(
                                PostbackAction(
                                    "説 hello2",
                                    "hello こんにちは",
                                    "你好"
                                ),
                                PostbackAction(
                                    "説 hello2",
                                    "hello こんにちは",
                                    "你好"
                                ),
                                MessageAction(
                                    "Say message",
                                    "Hi"
                                )
                            )
                        ),
                        CarouselColumn(
                            imageUrl, "Datetime Picker",
                            "Please select a date, time or datetime", Arrays.asList<Action>(
                                DatetimePickerAction(
                                    "Datetime",
                                    "action=sel",
                                    "datetime",
                                    "2017-06-18T06:15",
                                    "2100-12-31T23:59",
                                    "1900-01-01T00:00"
                                ),
                                DatetimePickerAction(
                                    "Date",
                                    "action=sel&only=date",
                                    "date",
                                    "2017-06-18",
                                    "2100-12-31",
                                    "1900-01-01"
                                ),
                                DatetimePickerAction(
                                    "Time",
                                    "action=sel&only=time",
                                    "time",
                                    "06:15",
                                    "23:59",
                                    "00:00"
                                )
                            )
                        )
                    )
                )
                val templateMessage = TemplateMessage("Carousel alt text", carouselTemplate)
                this.reply(replyToken, templateMessage)
            }
            "image_carousel" -> {
                val imageUrl = createUri("/static/rich/1040")
                val imageCarouselTemplate = ImageCarouselTemplate(
                    Arrays.asList(
                        ImageCarouselColumn(
                            imageUrl,
                            URIAction(
                                "Goto line.me",
                                "https://line.me", null
                            )
                        ),
                        ImageCarouselColumn(
                            imageUrl,
                            MessageAction(
                                "Say message",
                                "Hi"
                            )
                        ),
                        ImageCarouselColumn(
                            imageUrl,
                            PostbackAction(
                                "説 hello2",
                                "hello こんにちは",
                                "你  好"
                            )
                        )
                    )
                )
                val templateMessage = TemplateMessage(
                    "ImageCarousel alt text",
                    imageCarouselTemplate
                )
                this.reply(replyToken, templateMessage)
            }
            "imagemap" -> {
                var sourceId = "NULL"
                sourceId = if (event.source is GroupSource) {
                    (event.source as GroupSource).groupId.toString()
                } else if (event.source is RoomSource) {
                    (event.source as RoomSource).roomId.toString()
                } else {
                    event.source.userId.toString()
                }
                this.reply(
                    replyToken, ImagemapMessage(
                        "https://i.imgur.com/YJ4BzB4.jpg",
                        "This is alt text",
                        ImagemapBaseSize(1040, 1040),
                        Arrays.asList<ImagemapAction>(
                            URIImagemapAction(
                                "https://tapgo.cc:8553/api/line?buttonId=1&sourceId=$sourceId",
                                ImagemapArea(
                                    0, 0, 520, 520
                                )
                            ),
                            URIImagemapAction(
                                "https://tapgo.cc:8553/api/line?buttonId=2&sourceId=$sourceId",
                                ImagemapArea(
                                    520, 0, 520, 520
                                )
                            ),
                            URIImagemapAction(
                                "https://tapgo.cc:8553/api/line?buttonId=3&sourceId=$sourceId",
                                ImagemapArea(
                                    0, 520, 520, 520
                                )
                            ),
                            MessageImagemapAction(
                                "你點了右下角",
                                ImagemapArea(
                                    520, 520, 520, 520
                                )
                            )
                        )
                    )
                )
//                    replyToken, ImagemapMessage
//                        .builder()
//                        .baseUrl(createUri("/static/rich"))
//                        .altText("This is alt text")
//                        .baseSize(ImagemapBaseSize(1040, 1040))
//                        .actions(
//                            Arrays.asList<ImagemapAction>(
//                                URIImagemapAction(
//                                    "https://store.line.me/family/manga/en",
//                                    ImagemapArea(
//                                        0, 0, 520, 520
//                                    )
//                                ),
//                                URIImagemapAction(
//                                    "https://store.line.me/family/music/en",
//                                    ImagemapArea(
//                                        520, 0, 520, 520
//                                    )
//                                ),
//                                URIImagemapAction(
//                                    "https://store.line.me/family/play/en",
//                                    ImagemapArea(
//                                        0, 520, 520, 520
//                                    )
//                                ),
//                                MessageImagemapAction(
//                                    "URANAI!",
//                                    ImagemapArea(
//                                        520, 520, 520, 520
//                                    )
//                                )
//                            )
//                        ).build()
//                )
            }
            "clicked_count" -> {
                this.replyText(
                    replyToken,
                    "----點擊率---- \n" +
                            "manga: $btn1Count\n" +
                            "$btn1Echo\n" +
                            "music: $btn2Count\n" +
                            "$btn2Echo\n" +
                            "play: $btn3Count\n" +
                            "$btn3Echo\n" +
                            "---伺服器開機時間---\n" +
                            currentPointPlus8
                )
            }
            "imagemap_video" -> this.reply(
                replyToken, ImagemapMessage
                    .builder()
                    .baseUrl(createUri("/static/imagemap_video"))
                    .altText("This is an imagemap with video")
                    .baseSize(ImagemapBaseSize(722, 1040))
                    .video(
                        ImagemapVideo.builder()
                            .originalContentUrl(
                                URI.create(
                                    createUri("/static/imagemap_video/originalContent.mp4")
                                )
                            )
                            .previewImageUrl(
                                URI.create(
                                    createUri("/static/imagemap_video/previewImage.jpg")
                                )
                            )
                            .area(ImagemapArea(40, 46, 952, 536))
                            .externalLink(
                                ImagemapExternalLink(
                                    URI.create("https://example.com/see_more.html"),
                                    "See More"
                                )
                            )
                            .build()
                    )
                    .actions(
                        Stream.of(
                            MessageImagemapAction(
                                "NIXIE CLOCK",
                                ImagemapArea(260, 600, 450, 86))
                        ).collect(Collectors.toList()) as kotlin.collections.List<ImagemapAction>?
                    ).build()
            )
            "flex" -> this.reply(replyToken, ExampleFlexMessageSupplier().get())
            "quickreply" -> this.reply(replyToken, MessageWithQuickReplySupplier().get())
            "help" -> this.replyText(replyToken, "profile\n" +
                    "bye\n" +
                    "confirm\n" +
                    "buttons\n" +
                    "carousel\n" +
                    "image_carousel\n" +
                    "imagemap\n" +
                    "imagemap_video\n" +
                    "flex\n" +
                    "push\n" +
                    "multicast\n" +
                    "broadcast\n" +
                    "quickreply\n" +
                    "Get_the_target_limit_for_additional_messages\n" +
                    "Get_number_of_messages_sent_this_month\n\n" +
                    "```------Tapgo_Bot_Api進度------```\n" +
                    "https://gist.github.com/azrael8576/86b661864ac1d62940763945711db769")
            "push" -> this.pushText(TAPGO_ALEX, "來自Push api")
            "multicast" -> {
                var usersId: MutableSet<String> = mutableSetOf()
                usersId.add(TAPGO_ALEX)
                usersId.add(ALEX)
                usersId.add(郭)
                usersId.add(DEV_SHION)
                this.multicastText(usersId, "來自Multicast api to : TAPGO_ALEX, ALEX, 郭, DEV_SHION")
            }
            "broadcast" -> this.broadcastText("Test message. broadcast api", false)
            "Get_the_target_limit_for_additional_messages" -> this.replyText(replyToken,
                "你的目標訊息配額為: " +
                        lineMessagingClient!!.messageQuota.get().value.toString()
            )
            "Get_number_of_messages_sent_this_month" -> this.replyText(replyToken,
                "你已經發送的數量: " +
                        lineMessagingClient!!.messageQuotaConsumption.get().totalUsage.toString()
            )
//            "Get_number_of_sent_reply_messages" -> this.replyText(replyToken,
//                "已傳送reply api總數 from 2019/08/12: " +
//                        lineMessagingClient!!.getNumberOfSentReplyMessages("20190812").
//            )
//            "Get_number_of_sent_push_messages" -> this.replyText(replyToken,
//                "已傳送push api總數 from 2019/08/01: " +
//                        lineMessagingClient!!.getNumberOfSentPushMessages("20190812").get().success.toString()
//            )
//            "Get_number_of_sent_multicast_messages" -> this.replyText(replyToken,
//                "已傳送multicast api總數 from 2019/08/01: " +
//                        lineMessagingClient!!.getNumberOfSentMulticastMessages("20190812").get().success.toString()
//            )
//            "Get_number_of_sent_broadcast_messages" -> this.replyText(replyToken,
//                "已傳送broadcast api總數 from 2019/08/01: " +
//                        lineMessagingClient!!.getNumberOfSentBroadcastMessages("20190812").get().success.toString()
//            )

//            else -> {
//                logger.info("Returns echo message {}: {}", replyToken, text)
//                this.replyText(
//                    replyToken,
//                    text
//                )
//            }
        }
    }

    private fun system(vararg args: String) {
        var processBuilder = ProcessBuilder(*args)
        try {
            var start = processBuilder.start()
            var i = start.waitFor()
            logger.info("result: {} =>  {}", Arrays.toString(args), i)
        } catch (e: IOException) {
            logger.info("IOException", e)
            throw UncheckedIOException(e)
        } catch (e: InterruptedException) {
            logger.info("Interrupted", e)
            Thread.currentThread().interrupt()
        }
    }

    data class DownloadedContent (var path: Path?, var uri: String)
}