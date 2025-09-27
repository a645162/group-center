package com.khm.group.center.config

import com.khm.group.center.message.webhook.lark.LarkGroupBot
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class LarkBotConfigTest {

    @Autowired
    private lateinit var larkGroupBot: LarkGroupBot

    @Test
    fun testLarkGroupBotBean() {
        println("LarkGroupBot bean created: ${larkGroupBot.isValid()}")
    }
}
