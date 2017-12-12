package com.thirtydays.pushservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by yanchengmeng on 2016/12/14.
 * 推送消息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PushMessage implements Serializable {

    private String msgId;
    // 标题
    private String title;
    // 标题下描述
    private String desc;
    // 自定义消息-自定义内容
    private String custom;
    // 自定义参数
    private Map<String, String> extras;
    // 通知消息id, 用于手动清楚推送ID
    private int notifyId = -1;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public Map<String, String> getExtras() {
        return extras;
    }

    public void setExtras(Map<String, String> extras) {
        this.extras = extras;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCustom() {
        return custom;
    }

    public void setCustom(String custom) {
        this.custom = custom;
    }

    public int getNotifyId() {
        return notifyId;
    }

    public void setNotifyId(int notifyId) {
        this.notifyId = notifyId;
    }
}
