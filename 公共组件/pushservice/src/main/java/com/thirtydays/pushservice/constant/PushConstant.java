package com.thirtydays.pushservice.constant;

/**
 * Created by chenxiaojin on 2017/8/11.
 */

public interface PushConstant {
    String CUSTOMALIASTYPETOKEN = "CustomAliasTypeToken";   // 推送别名类型
    String DEFAUT_TAG_NAME = "PROVINCE"; // 默认标签名称, 用于华为, 默认为省份
    // 小米手机
    String BRAND_XIAOMI = "XIAOMI";
    // 华为手机
    String BRAND_HUAWEI = "HUAWEI";
    // 魅族手机
    String BRAND_MEIZU = "MEIZU";
    // 其他手机
    String BRAND_OTHER = "OTHER";
    // 推送token
    String PUSH_TOKEN = "pushToken";
    // 推送token变更
    String PUSH_TOKEN_CHANGED = "push.token.changed.action";
    // 推送服务注册失败
    String PUSH_SERVICE_INIT_FAIL_ACTION = "push.service.init.failed.action";



}
