package com.khm.group.center.message

import com.khm.group.center.message.webhook.lark.LarkGroupBot
import org.junit.jupiter.api.Test

class LarkTest {
    @Test
    fun testLarkGroupBot() {
        val larkGroupBotObj = LarkGroupBot(
            "",
            ""
        )

        larkGroupBotObj.sendText(
            "Hello, World!" +
                    LarkGroupBot.getAtUserHtml("")
        )
    }
}

