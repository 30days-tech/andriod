package com.thirtydays.pushservice.receiver;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.thirtydays.pushservice.PushManager;
import com.thirtydays.pushservice.constant.PushConstant;
import com.thirtydays.pushservice.entity.PushMessage;
import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;
import com.xiaomi.push.service.XMJobService;

import java.util.List;

/**
 * Created by yanchengmeng on 2016/12/14.
 * 小米推送消息接收器
 */
public class XMMessageReceiver extends PushMessageReceiver {

    private final static String TAG = XMMessageReceiver.class.getSimpleName();


    @Override
    public void onNotificationMessageClicked(Context context, MiPushMessage miPushMessage) {
        Log.e(TAG, "onNotificationMessageClicked");
        PushMessage pushMessage = new PushMessage();
        pushMessage.setExtras(miPushMessage.getExtra());
        pushMessage.setTitle(miPushMessage.getTitle());
        // 传输数据从content取
        pushMessage.setDesc(miPushMessage.getDescription());
        pushMessage.setMsgId(miPushMessage.getMessageId());

        if (PushManager.getInstance().getMessageHandler() != null) {
            PushManager.getInstance().getMessageHandler().setOriginalMessage(miPushMessage.toBundle().toString());
            PushManager.getInstance().getMessageHandler().onNotificationClicked(context, pushMessage);
        }
    }

    /**
     * 推送消息收到后立即触发
     * @param context
     * @param miPushMessage
     */
    @Override
    public void onNotificationMessageArrived(Context context, MiPushMessage miPushMessage) {
        Log.e(TAG, "onNotificationMessageArrived.");
        super.onNotificationMessageArrived(context, miPushMessage);
//        PushMessage pushMessage = new PushMessage();
//        pushMessage.setExtras(miPushMessage.getExtra());
//        pushMessage.setTitle(miPushMessage.getTitle());
//        // 传输数据从content取
//        pushMessage.setDesc(miPushMessage.getDescription());
//        pushMessage.setMsgId(miPushMessage.getMessageId());
//        pushMessage.setNotifyId(miPushMessage.getNotifyId());
//
//        if (PushManager.getInstance().getMessageHandler() != null) {
//            PushManager.getInstance().getMessageHandler().setOriginalMessage(miPushMessage.toBundle().toString());
//            PushManager.getInstance().getMessageHandler().onNotificationClicked(context, pushMessage);
//        }
    }

    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage miPushCommandMessage) {
        String command = miPushCommandMessage.getCommand();
        Log.e(TAG, "onReceiveRegisterResult:" + command);
        List<String> arguments = miPushCommandMessage.getCommandArguments();
        String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
        String cmdArg2 = ((arguments != null && arguments.size() > 1) ? arguments.get(1) : null);
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (miPushCommandMessage.getResultCode() == ErrorCode.SUCCESS) {
                Log.i(TAG, "Regist XIAOMI push token success. token:" + cmdArg1);
                // 通知推送token
                Intent intent = new Intent (PushConstant.PUSH_TOKEN_CHANGED);
                intent.putExtra(PushConstant.PUSH_TOKEN, cmdArg1);
                context.sendBroadcast(intent);
            } else {
                // 注册失败就用友盟推送:小米推送会因为系统时间错误导致注册失败
                Intent intent = new Intent(PushConstant.PUSH_SERVICE_INIT_FAIL_ACTION);
                context.sendBroadcast(intent);
                // TODO 上报bugly
//                CrashReport.postCatchedException(new Exception(miPushCommandMessage.getReason()));
            }
        }
    }

    /**
     * 处理透传消息
     *
     * @param context
     * @param miPushMessage
     */
    @Override
    public void onReceivePassThroughMessage(Context context, MiPushMessage miPushMessage) {
        Log.e(TAG, "onReceivePassThroughMessage-----");
        Log.e(TAG, "miPushMessage:" + miPushMessage.toString());
        // 取自定义消息内容
        PushMessage pushMessage = new PushMessage();
        pushMessage.setMsgId(miPushMessage.getMessageId());
        pushMessage.setDesc(miPushMessage.getDescription());
        pushMessage.setCustom(miPushMessage.getContent());
        pushMessage.setExtras(miPushMessage.getExtra());
        // 处理消息
        if (null != pushMessage && PushManager.getInstance().getMessageHandler() != null) {
            PushManager.getInstance().getMessageHandler().setOriginalMessage(miPushMessage.toBundle().toString());
            PushManager.getInstance().getMessageHandler().onReceiveMessage(context, pushMessage);
        }

        pushMessage.setExtras(miPushMessage.getExtra());
    }

    @Override
    public void onCommandResult(Context context, MiPushCommandMessage miPushCommandMessage) {
        String command = miPushCommandMessage.getCommand();
        Log.e(TAG, "onCommandResult:" + command + ", resultCode:" + miPushCommandMessage.getResultCode()
                + ", reason:" + miPushCommandMessage.getReason());
    }

    @Override
    public void onReceiveMessage(Context context, MiPushMessage miPushMessage) {
        Log.e(TAG, "onReceiveMessage");
        super.onReceiveMessage(context, miPushMessage);
    }


}
