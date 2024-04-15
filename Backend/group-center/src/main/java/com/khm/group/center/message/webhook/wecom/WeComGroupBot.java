package com.khm.group.center.message.webhook.wecom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.alibaba.fastjson2.JSON;

public class WeComGroupBot {

    public static final String WEBHOOK_URL_HEADER =
            "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=";

    private static final OkHttpClient client =
            new OkHttpClient();
    private static final MediaType mediaType =
            MediaType.get("application/json; charset=utf-8");

    public static boolean directSendTextWithUrl(
            String webhookUrlOrKey,
            String messageContent,
            List<String> mentionedIdList,
            List<String> mentionedMobileList
    ) {
        if (webhookUrlOrKey == null || webhookUrlOrKey.trim().isEmpty()) {
            return false;
        }

        webhookUrlOrKey = webhookUrlOrKey.trim();

        if (!webhookUrlOrKey.startsWith(WEBHOOK_URL_HEADER)) {
            webhookUrlOrKey = WEBHOOK_URL_HEADER + webhookUrlOrKey;
        }

        if (mentionedIdList == null) {
            mentionedIdList = new ArrayList<>();
        }

        if (mentionedMobileList == null) {
            mentionedMobileList = new ArrayList<>();
        }

        Map<String, Object> weComMessageMap = new HashMap<>();
        // 添加msgtype
        weComMessageMap.put("msgtype", "text");

        // 创建text内部的HashMap
        Map<String, Object> textMap = new HashMap<>();
        textMap.put("content", messageContent);
        textMap.put("mentioned_list", mentionedIdList);
        textMap.put("mentioned_mobile_list", mentionedMobileList);

        // 将textMap添加到外层的HashMap中
        weComMessageMap.put("text", textMap);

        // Hashmap 序列化为 JSON
        String json = JSON.toJSONString(weComMessageMap);

        RequestBody requestBody = RequestBody.create(json, mediaType);
        Request request = new Request.Builder()
                .url(webhookUrlOrKey)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            if (response.body() != null) {
                System.out.println("[Webhook]WeCom(Text):" + response.body().string());
                return true;
            } else {
                System.out.println("[Webhook]WeCom(Text) response body is null!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getWebhookUrl(String key) {
        if (key == null || key.trim().isEmpty()) {
            return null;
        }
        return WEBHOOK_URL_HEADER + key.trim();
    }

}
