package com.khm.group.center.message.webhook.wecom;

import com.khm.group.center.utils.environ.EnvironVariable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class WeComGroupBotTest {

    @Test
    public void WebhookWeComTest() {

        String webhookKey = System.getenv("GROUP_CENTER_WEBHOOK_WEWORK");
        System.out.println("WeCom WebHook Key:" + webhookKey);

        boolean key_is_valid = !(webhookKey == null || webhookKey.isEmpty());

        assert key_is_valid;

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
