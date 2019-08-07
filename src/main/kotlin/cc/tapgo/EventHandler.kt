package cc.tapgo


import java.io.IOException
import java.io.OutputStream
import java.io.UncheckedIOException
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.concurrent.ExecutionException
import java.util.function.Consumer
import java.util.stream.Collectors
import java.util.stream.Stream

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

import com.google.common.io.ByteStreams

import com.linecorp.bot.client.LineMessagingClient
import com.linecorp.bot.client.MessageContentResponse
import com.linecorp.bot.model.ReplyMessage
import com.linecorp.bot.model.action.*
import com.linecorp.bot.model.event.BeaconEvent
import com.linecorp.bot.model.event.Event
import com.linecorp.bot.model.event.FollowEvent
import com.linecorp.bot.model.event.JoinEvent
import com.linecorp.bot.model.event.MemberJoinedEvent
import com.linecorp.bot.model.event.MemberLeftEvent
import com.linecorp.bot.model.event.MessageEvent
import com.linecorp.bot.model.event.PostbackEvent
import com.linecorp.bot.model.event.UnfollowEvent
import com.linecorp.bot.model.event.message.AudioMessageContent
import com.linecorp.bot.model.event.message.ContentProvider
import com.linecorp.bot.model.event.message.FileMessageContent
import com.linecorp.bot.model.event.message.ImageMessageContent
import com.linecorp.bot.model.event.message.LocationMessageContent
import com.linecorp.bot.model.event.message.StickerMessageContent
import com.linecorp.bot.model.event.message.TextMessageContent
import com.linecorp.bot.model.event.message.VideoMessageContent
import com.linecorp.bot.model.event.source.GroupSource
import com.linecorp.bot.model.event.source.RoomSource
import com.linecorp.bot.model.event.source.Source
import com.linecorp.bot.model.message.AudioMessage
import com.linecorp.bot.model.message.ImageMessage
import com.linecorp.bot.model.message.ImagemapMessage
import com.linecorp.bot.model.message.LocationMessage
import com.linecorp.bot.model.message.Message
import com.linecorp.bot.model.message.StickerMessage
import com.linecorp.bot.model.message.TemplateMessage
import com.linecorp.bot.model.message.TextMessage
import com.linecorp.bot.model.message.VideoMessage
import com.linecorp.bot.model.message.imagemap.*
import com.linecorp.bot.model.message.template.ButtonsTemplate
import com.linecorp.bot.model.message.template.CarouselColumn
import com.linecorp.bot.model.message.template.CarouselTemplate
import com.linecorp.bot.model.message.template.ConfirmTemplate
import com.linecorp.bot.model.message.template.ImageCarouselColumn
import com.linecorp.bot.model.message.template.ImageCarouselTemplate
import com.linecorp.bot.model.response.BotApiResponse
import com.linecorp.bot.spring.boot.annotation.EventMapping
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler
import java.util.*


/**
 * Created by AlexHe on 2019-08-07.
 * Describe
 */

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
    //    @EventMapping
//    @Throws(IOException::class)
//    fun handleImageMessageEvent(event: MessageEvent<ImageMessageContent>) {
//        // You need to install ImageMagick
//        handleHeavyContent(
//            event.replyToken,
//            event.message.id
//        ) { responseBody ->
//            val provider = event.message.contentProvider
//            val jpg: DownloadedContent
//            val previewImg: DownloadedContent
//            if (provider.isExternal) {
//                jpg = DownloadedContent(null, provider.originalContentUrl)
//                previewImg = DownloadedContent(null, provider.previewImageUrl)
//            } else {
//                jpg = saveContent("jpg", responseBody)
//                previewImg = createTempFile("jpg")
//                system(
//                    "convert",
//                    "-resize", "240x",
//                    jpg.path.toString(),
//                    previewImg.path.toString()
//                )
//            }
//            reply(
//                event.replyToken,
//                ImageMessage(jpg.getUri(), previewImg.getUri())
//            )
//        }
//    }
//    @EventMapping
//    @Throws(IOException::class)
//    fun handleAudioMessageEvent(event: MessageEvent<AudioMessageContent>) {
//        handleHeavyContent(
//            event.replyToken,
//            event.message.id
//        ) { responseBody ->
//            val provider = event.message.contentProvider
//            val mp4: DownloadedContent
//            if (provider.isExternal) {
//                mp4 = DownloadedContent(null, provider.originalContentUrl)
//            } else {
//                mp4 = saveContent("mp4", responseBody)
//            }
//            reply(event.replyToken, AudioMessage(mp4.getUri(), 100))
//        }
//    }
//    @EventMapping
//    @Throws(IOException::class)
//    fun handleVideoMessageEvent(event: MessageEvent<VideoMessageContent>) {
//        // You need to install ffmpeg and ImageMagick.
//        handleHeavyContent(
//            event.replyToken,
//            event.message.id
//        ) { responseBody ->
//            val provider = event.message.contentProvider
//            val mp4: DownloadedContent
//            val previewImg: DownloadedContent
//            if (provider.isExternal) {
//                mp4 = DownloadedContent(null, provider.originalContentUrl)
//                previewImg = DownloadedContent(null, provider.previewImageUrl)
//            } else {
//                mp4 = saveContent("mp4", responseBody)
//                previewImg = createTempFile("jpg")
//                system(
//                    "convert",
//                    mp4.path + "[0]",
//                    previewImg.path.toString()
//                )
//            }
//            reply(
//                event.replyToken,
//                VideoMessage(mp4.getUri(), previewImg.uri)
//            )
//        }
//    }
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

    private fun handleHeavyContent(
        replyToken: String, messageId: String,
        messageConsumer: Consumer<MessageContentResponse>
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

        messageConsumer.accept(response)
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
                    "Do it?",
                    MessageAction("Yes", "Yes!"),
                    MessageAction("No", "No!")
                )
                val templateMessage = TemplateMessage("Confirm alt text", confirmTemplate)
                this.reply(replyToken, templateMessage)
            }
            "buttons" -> {
                val imageUrl = createUri("/static/buttons/1040.jpg")
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
                            "hello こんにちは"
                        ),
                        PostbackAction(
                            "言 hello2",
                            "hello こんにちは",
                            "hello こんにちは"
                        ),
                        MessageAction(
                            "Say message",
                            "Rice=米"
                        )
                    )
                )
                val templateMessage = TemplateMessage("Button alt text", buttonsTemplate)
                this.reply(replyToken, templateMessage)
            }
            "carousel" -> {
                val imageUrl = createUri("/static/buttons/1040.jpg")
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
                                    "hello こんにちは"
                                )
                            )
                        ),
                        CarouselColumn(
                            imageUrl, "hoge", "fuga", Arrays.asList<Action>(
                                PostbackAction(
                                    "言 hello2",
                                    "hello こんにちは",
                                    "hello こんにちは"
                                ),
                                PostbackAction(
                                    "言 hello2",
                                    "hello こんにちは",
                                    "hello こんにちは"
                                ),
                                MessageAction(
                                    "Say message",
                                    "Rice=米"
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
                val imageUrl = createUri("/static/buttons/1040.jpg")
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
                                "Rice=米"
                            )
                        ),
                        ImageCarouselColumn(
                            imageUrl,
                            PostbackAction(
                                "言 hello2",
                                "hello こんにちは",
                                "hello こんにちは"
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
            "imagemap" -> this.reply(
                replyToken, ImagemapMessage(
                    createUri("/static/rich"),
                    "This is alt text",
                    ImagemapBaseSize(1040, 1040),
                    Arrays.asList<ImagemapAction>(
                        URIImagemapAction(
                            "https://store.line.me/family/manga/en",
                            ImagemapArea(
                                0, 0, 520, 520
                            )
                        ),
                        URIImagemapAction(
                            "https://store.line.me/family/music/en",
                            ImagemapArea(
                                520, 0, 520, 520
                            )
                        ),
                        URIImagemapAction(
                            "https://store.line.me/family/play/en",
                            ImagemapArea(
                                0, 520, 520, 520
                            )
                        ),
                        MessageImagemapAction(
                            "URANAI!",
                            ImagemapArea(
                                520, 520, 520, 520
                            )
                        )
                    )
                )
            )
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
        val processBuilder = ProcessBuilder(*args)
        try {
            val start = processBuilder.start()
            val i = start.waitFor()
            logger.info("result: {} =>  {}", Arrays.toString(args), i)
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        } catch (e: InterruptedException) {
            logger.info("Interrupted", e)
            Thread.currentThread().interrupt()
        }
    }

    data class DownloadedContent (var path: Path, var uri: String)
}