package com.khm.group.center.message.webhook.wecom;

import com.khm.group.center.utils.environ.EnvironVariable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class WeComGroupBotTest {

    @Test
    public void WebhookWeComTest() {

        String webhookKey = System.getenv("WEBHOOK_WEWORK");
        if (webhookKey == null || webhookKey.isEmpty()) {
            return;
        }

        boolean ret = WeComGroupBot.directSendTextWithUrl(
                webhookKey,
                "Test Passed!",
                new ArrayList<>() {{
                    add("khm");
                }},
                null
        );

        assert ret;
    }

}
