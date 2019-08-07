package cc.tapgo


/**
 * Created by AlexHe on 2019-08-07.
 * Describe
 */

import java.util.Arrays
import java.util.function.Supplier

import com.linecorp.bot.model.action.CameraAction
import com.linecorp.bot.model.action.CameraRollAction
import com.linecorp.bot.model.action.LocationAction
import com.linecorp.bot.model.action.MessageAction
import com.linecorp.bot.model.action.PostbackAction
import com.linecorp.bot.model.message.Message
import com.linecorp.bot.model.message.TextMessage
import com.linecorp.bot.model.message.quickreply.QuickReply
import com.linecorp.bot.model.message.quickreply.QuickReplyItem

class MessageWithQuickReplySupplier : Supplier<Message> {
    override fun get(): Message {
        val items = Arrays.asList(
            QuickReplyItem.builder()
                .action(MessageAction("MessageAction", "MessageAction"))
                .build(),
            QuickReplyItem.builder()
                .action(CameraAction.withLabel("CameraAction"))
                .build(),
            QuickReplyItem.builder()
                .action(CameraRollAction.withLabel("CemeraRollAction"))
                .build(),
            QuickReplyItem.builder()
                .action(LocationAction.withLabel("Location"))
                .build(),
            QuickReplyItem.builder()
                .action(
                    PostbackAction.builder()
                        .label("PostbackAction")
                        .text("PostbackAction clicked")
                        .data("{PostbackAction: true}")
                        .build()
                )
                .build()
        )

        val quickReply = QuickReply.items(items)

        return TextMessage
            .builder()
            .text("Message with QuickReply")
            .quickReply(quickReply)
            .build()
    }
}