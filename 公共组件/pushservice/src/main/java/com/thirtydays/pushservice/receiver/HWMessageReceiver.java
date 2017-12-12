package com.thirtydays.pushservice.receiver;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.huawei.hms.support.api.push.PushReceiver;
import com.thirtydays.pushservice.PushManager;
import com.thirtydays.pushservice.constant.PushConstant;
import com.thirtydays.pushservice.entity.PushMessage;
import com.thirtydays.pushservice.util.CommonUtil;
import com.thirtydays.pushservice.util.JsonUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by yanchengmeng on 2016/12/14.
 * 华为推送消息接收器
 */
public class HWMessageReceiver extends PushReceiver {

    private final static String TAG = HWMessageReceiver.class.getSimpleName();

    @Override
    public void onToken(Context context, String token, Bundle extras) {
        Log.i(TAG, "Get HUAWEI push token success. token:" + token);
        // 设置token
        Intent intent = new Intent (PushConstant.PUSH_TOKEN_CHANGED);
        intent.putExtra(PushConstant.PUSH_TOKEN, token);
        context.sendBroadcast(intent);
    }


    /**
     * 处理透传消息
     *
     * @param context
     * @param msg
     * @param bundle
     * @return
     */
    @Override
    public boolean onPushMsg(Context context, byte[] msg, Bundle bundle) {
        Log.d(TAG, "[onPushMsg] Receive custom message.");
        try {
            String content = new String(msg, "UTF-8");
            Log.e(TAG, "content:" + content);
            Log.e(TAG, "content length:" + content.length());
            PushMessage pushMessage = JsonUtil.json2obj(content, PushMessage.class);
            pushMessage.setCustom(content);
            // 处理消息
            if (null != pushMessage && PushManager.getInstance().getMessageHandler() != null) {
                PushManager.getInstance().getMessageHandler().setOriginalMessage(content);
                PushManager.getInstance().getMessageHandler().onReceiveMessage(context, pushMessage);
            }
            Log.e(TAG, content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 这个方法如果需要回调，必须添加自定义内容, 否则不会回调
     *
     * @param context
     * @param event
     * @param extras
     */
    @Override
    public void onEvent(Context context, Event event, Bundle extras) {
        Log.e(TAG, "onEvent:" + extras.toString());
        Toast.makeText(context, "onEvent(Context context, Event event, Bundle extras)", Toast.LENGTH_SHORT).show();
        // 点击通知栏通知或点击通知栏通知里面按钮
        if (Event.NOTIFICATION_OPENED.equals(event) || Event.NOTIFICATION_CLICK_BTN.equals(event)) {
            int notifyId = extras.getInt(BOUND_KEY.pushNotifyId, 0);
            if (0 != notifyId) {
                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(notifyId);
            }
            String content = "收到通知附加消息： " + extras.getString(BOUND_KEY.pushMsgKey);
            Log.e(TAG, content);
        }
        PushMessage pushMessage = new PushMessage();
        String msgTypeStr = extras.getString(BOUND_KEY.pushMsgKey);
        Log.e(TAG, "msgTypeStr" + msgTypeStr);
        HashMap<String, String> extrasMsg = new HashMap<String, String>();
        List<Map> mapList = JsonUtil.json2list(msgTypeStr, Map.class);
        if (!CommonUtil.isEmpty(mapList)) {
            for (Map map : mapList) {
                Iterator entries = map.entrySet().iterator();
                while (entries.hasNext()) {
                    Map.Entry entry = (Map.Entry) entries.next();
                    Log.e(TAG, "(String)entry.getKey()" + (String) entry.getKey() + "(String)entry.getValue()" + (String) entry.getValue());
                    extrasMsg.put((String) entry.getKey(), (String) entry.getValue());
                }
            }
            pushMessage.setExtras(extrasMsg);
            if (PushManager.getInstance().getMessageHandler() != null) {
                PushManager.getInstance().getMessageHandler().setOriginalMessage(msgTypeStr);
                PushManager.getInstance().getMessageHandler().onNotificationClicked(context, pushMessage);
            }
        }
        //super.onEvent(context,event,extras);
    }

    @Override
    public void onPushMsg(Context context, byte[] bytes, String s) {
        Log.e(TAG, "onPushMsg string");

        super.onPushMsg(context, bytes, s);
        Toast.makeText(context, "onPushMsg(Context context, byte[] bytes, String s)", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPushState(Context context, boolean b) {
        Log.e(TAG, "onPushState");
        super.onPushState(context, b);
        Toast.makeText(context, " onPushState(Context context, boolean b)", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onToken(Context context, String s) {
        Log.e(TAG, "onToken:" + s);
        super.onToken(context, s);
        Toast.makeText(context, "onToken(Context context, String s)", Toast.LENGTH_SHORT).show();
    }
}
