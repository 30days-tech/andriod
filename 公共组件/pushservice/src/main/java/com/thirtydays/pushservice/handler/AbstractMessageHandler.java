package com.thirtydays.pushservice.handler;

import android.content.Context;

import com.thirtydays.pushservice.entity.PushMessage;


/**
 * Created by chenxiaojin on 2016/12/29.
 * 消息处理抽象类, 处理通知消息和自定位消息
 */

public abstract class AbstractMessageHandler {
    // 接受到的原始消息, 友盟:UMessage, 华为:String, 小米:MiPushMessage
    private String originalMessage;

    public String getOriginalMessage() {
        return originalMessage;
    }

    public void setOriginalMessage(String originalMessage) {
        this.originalMessage = originalMessage;
    }

    /**
     * 点击通知栏消息时触发
     *
     * @param context
     * @param pushMessage
     */
    public abstract void onNotificationClicked(Context context, PushMessage pushMessage);

    /**
     * 收到自定义消息触发
     *
     * @param context
     * @param pushMessage
     */
    public abstract void onReceiveMessage(Context context, PushMessage pushMessage);
}
