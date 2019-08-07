package cc.tapgo


/**
 * Created by AlexHe on 2019-08-07.
 * Describe
 */

import java.util.Arrays.asList

import java.util.function.Supplier

import com.linecorp.bot.model.action.URIAction
import com.linecorp.bot.model.message.FlexMessage
import com.linecorp.bot.model.message.flex.component.*
import com.linecorp.bot.model.message.flex.component.Button.ButtonHeight
import com.linecorp.bot.model.message.flex.component.Button.ButtonStyle
import com.linecorp.bot.model.message.flex.component.Image.ImageAspectMode
import com.linecorp.bot.model.message.flex.component.Image.ImageAspectRatio
import com.linecorp.bot.model.message.flex.component.Image.ImageSize
import com.linecorp.bot.model.message.flex.component.Text.TextWeight
import com.linecorp.bot.model.message.flex.container.Bubble
import com.linecorp.bot.model.message.flex.unit.FlexFontSize
import com.linecorp.bot.model.message.flex.unit.FlexLayout
import com.linecorp.bot.model.message.flex.unit.FlexMarginSize

class ExampleFlexMessageSupplier : Supplier<FlexMessage> {
    override fun get(): FlexMessage {
        val heroBlock = Image.builder()
            .url("https://example.com/cafe.jpg")
            .size(ImageSize.FULL_WIDTH)
            .aspectRatio(ImageAspectRatio.R20TO13)
            .aspectMode(ImageAspectMode.Cover)
            .action(URIAction("label", "http://example.com", null))
            .build()

        val bodyBlock = createBodyBlock()
        val footerBlock = createFooterBlock()
        val bubble = Bubble.builder()
            .hero(heroBlock)
            .body(bodyBlock)
            .footer(footerBlock)
            .build()

        return FlexMessage("ALT", bubble)
    }

    private fun createFooterBlock(): Box {
        val spacer = Spacer.builder().size(FlexMarginSize.SM).build()
        val callAction = Button
            .builder()
            .style(ButtonStyle.LINK)
            .height(ButtonHeight.SMALL)
            .action(URIAction("CALL", "tel:000000", null))
            .build()
        val separator = Separator.builder().build()
        val websiteAction = Button.builder()
            .style(ButtonStyle.LINK)
            .height(ButtonHeight.SMALL)
            .action(URIAction("WEBSITE", "https://example.com", null))
            .build()

        return Box.builder()
            .layout(FlexLayout.VERTICAL)
            .spacing(FlexMarginSize.SM)
            .contents(asList<FlexComponent>(spacer, callAction, separator, websiteAction))
            .build()
    }

    private fun createBodyBlock(): Box {
        val title = Text.builder()
            .text("Brown Cafe")
            .weight(TextWeight.BOLD)
            .size(FlexFontSize.XL)
            .build()

        val review = createReviewBox()

        val info = createInfoBox()

        return Box.builder()
            .layout(FlexLayout.VERTICAL)
            .contents(asList<FlexComponent>(title, review, info))
            .build()
    }

    private fun createInfoBox(): Box {
        val place = Box
            .builder()
            .layout(FlexLayout.BASELINE)
            .spacing(FlexMarginSize.SM)
            .contents(
                asList<FlexComponent>(
                    Text.builder()
                        .text("Place")
                        .color("#aaaaaa")
                        .size(FlexFontSize.SM)
                        .flex(1)
                        .build(),
                    Text.builder()
                        .text("Shinjuku, Tokyo")
                        .wrap(true)
                        .color("#666666")
                        .size(FlexFontSize.SM)
                        .flex(5)
                        .build()
                )
            )
            .build()
        val time = Box.builder()
            .layout(FlexLayout.BASELINE)
            .spacing(FlexMarginSize.SM)
            .contents(
                asList<FlexComponent>(
                    Text.builder()
                        .text("Time")
                        .color("#aaaaaa")
                        .size(FlexFontSize.SM)
                        .flex(1)
                        .build(),
                    Text.builder()
                        .text("10:00 - 23:00")
                        .wrap(true)
                        .color("#666666")
                        .size(FlexFontSize.SM)
                        .flex(5)
                        .build()
                )
            )
            .build()

        return Box.builder()
            .layout(FlexLayout.VERTICAL)
            .margin(FlexMarginSize.LG)
            .spacing(FlexMarginSize.SM)
            .contents(asList<FlexComponent>(place, time))
            .build()
    }

    private fun createReviewBox(): Box {
        val goldStar = Icon.builder().size(FlexFontSize.SM).url("https://example.com/gold_star.png").build()
        val grayStar = Icon.builder().size(FlexFontSize.SM).url("https://example.com/gray_star.png").build()
        val point = Text.builder()
            .text("4.0")
            .size(FlexFontSize.SM)
            .color("#999999")
            .margin(FlexMarginSize.MD)
            .flex(0)
            .build()

        return Box.builder()
            .layout(FlexLayout.BASELINE)
            .margin(FlexMarginSize.MD)
            .contents(asList<FlexComponent>(goldStar, goldStar, goldStar, goldStar, grayStar, point))
            .build()
    }
}