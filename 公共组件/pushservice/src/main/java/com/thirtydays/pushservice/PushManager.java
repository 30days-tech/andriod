package com.thirtydays.pushservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.huawei.hms.api.ConnectionResult;
import com.huawei.hms.api.HuaweiApiAvailability;
import com.huawei.hms.api.HuaweiApiClient;
import com.huawei.hms.support.api.client.PendingResult;
import com.huawei.hms.support.api.client.ResultCallback;
import com.huawei.hms.support.api.push.HuaweiPush;
import com.huawei.hms.support.api.push.PushException;
import com.huawei.hms.support.api.push.TokenResult;
import com.thirtydays.pushservice.constant.GlobalConfig;
import com.thirtydays.pushservice.constant.PushConstant;
import com.thirtydays.pushservice.entity.PushMessage;
import com.thirtydays.pushservice.handler.AbstractMessageHandler;
import com.thirtydays.pushservice.util.CommonUtil;
import com.thirtydays.pushservice.util.SystemUtil;
import com.umeng.message.IUmengCallback;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.MsgConstant;
import com.umeng.message.PushAgent;
import com.umeng.message.UTrack;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.common.inter.ITagManager;
import com.umeng.message.entity.UMessage;
import com.umeng.message.tag.TagManager;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.mipush.sdk.MiPushClient;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static android.os.Looper.getMainLooper;

/**
 * Created by chenxiaojin on 2016/12/28.
 * 推送管理类
 * <p>
 * Update at 2016.12.28
 * 友盟推送: v3.1.1
 * 华为推送: v2.7.05
 * 小米推送: v3.1.2
 */

public class PushManager {
    private static final String TAG = "PushManager";
    private static final int REQUEST_HMS_RESOLVE_ERROR = 1000;
    private static PushManager pushManager;
    //华为移动服务Client
    private HuaweiApiClient client;
    // 是否调试模式
    private boolean isDebug = false;

    // 消息处理
    private AbstractMessageHandler messageHandler;
    // 当前使用推送类型
    private int pushType;
    // 推送token, 友盟:deviceToken, 华为:token, 小米: regId
    private String pushToken;
    private static Context context;
    // token监听
    private static List<SoftReference<PushTokenListener>> listenerList = new ArrayList<>();

    public static PushManager getInstance() {
        if (null == context) {
            throw new RuntimeException("Please init push manager first.");
        }
        if (pushManager == null) {
            pushManager = new PushManager();
        }
        return pushManager;
    }

    public static void init(Context applicationContext) {
        context = applicationContext.getApplicationContext();
        pushManager = new PushManager();
    }

    public static void init(Context applicationContext, PushTokenListener pushTokenListener) {
        context = applicationContext.getApplicationContext();
        pushManager = new PushManager();
        listenerList.add(new SoftReference<>(pushTokenListener));
    }

    private BroadcastReceiver pushReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (PushConstant.PUSH_TOKEN_CHANGED.equals(intent.getAction())) {
                String token = intent.getStringExtra(PushConstant.PUSH_TOKEN);
                pushToken = token;
                onTokenChanged(pushType, token);
            } else if (PushConstant.PUSH_SERVICE_INIT_FAIL_ACTION.equalsIgnoreCase(intent.getAction())) {
                // 注册失败， 使用友盟
                initUmengPush();
            }
        }
    };

    private PushManager() {
        registTokenReceiver();
        init(SystemUtil.getPhoneBrand());
    }


    /**
     * 初始化推送, 根据设备类型来确认使用哪一种推送
     *
     * @param deviceType
     */

    private void init(String deviceType) {
        Log.d(TAG, "Init push service, device type is :" + deviceType);
        if (deviceType.equalsIgnoreCase(PushConstant.BRAND_HUAWEI)) {
            initHWPush();
        } else if (deviceType.equalsIgnoreCase(PushConstant.BRAND_XIAOMI)) {
            initXMPush();
        } else {
            // 非中文语言以及谷歌play服务能正常使用, 使用谷歌推送
//            if (!SystemUtil.getLocale().startsWith("ZH") && SystemUtil.isGooglePlayServicesAvailable(context)) {
//                pushType = PushType.GOOGLE;
//                pushToken = FirebaseInstanceId.getInstance().getToken();
////                 谷歌推送自动启动, 无需手动启动
////                FirebaseApp.initializeApp(context);
//            } else {
//                initUmengPush();
//            }
            initUmengPush();
        }
    }


    private void initXMPush() {
        // 注册push服务，注册成功后会向DemoMessageReceiver发送广播
        // 可以从DemoMessageReceiver的onCommandResult方法中MiPushCommandMessage对象参数中获取注册信息
        pushType = PushType.XIAOMI;
        Log.d(TAG, "Start to init XIAOMI push...");
        String processName = SystemUtil.getProcessName();
        if (CommonUtil.isEmpty(processName) || context.getPackageName().equals(processName)) {
            MiPushClient.registerPush(context, GlobalConfig.XMPUSH_APPID, GlobalConfig.XMPUSH_APPKEY);
        }

        // 测试log
        if (isDebug) {
            LoggerInterface newLogger = new LoggerInterface() {

                @Override
                public void setTag(String tag) {
                    // ignore
                    Log.e(TAG, "XM tag:" + tag);
                }

                @Override
                public void log(String content, Throwable t) {
                    Log.d(TAG, content, t);
                }

                @Override
                public void log(String content) {
                    Log.d(TAG, content);
                }
            };
            Logger.setLogger(context, newLogger);
        }
    }

    private void initHWPush() {
        Log.d(TAG, "Start to init HUAWEI push...");
        pushType = PushType.HUAWEI;
//        com.huawei.android.pushagent.api.PushManager.requestToken(context);
        //连接回调以及连接失败监听
        client = new HuaweiApiClient.Builder(context)

                .addApi(HuaweiPush.PUSH_API)

                .addConnectionCallbacks(new HuaweiApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected() {
                        Log.e(TAG, "华为服务连接成功");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })

                .addOnConnectionFailedListener(new HuaweiApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.e(TAG, "华为服务连接失败" + connectionResult.getErrorCode());
                        if (HuaweiApiAvailability.getInstance().isUserResolvableError(connectionResult.getErrorCode())) {

                            final int errorCode = connectionResult.getErrorCode();
                            new Handler(getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    // 此方法必须在主线程调用, xxxxxx.this 为当前界面的activity
                                    Intent intent = new Intent();
                                    intent.setAction("HMS");
                                    intent.putExtra("errorCode", errorCode);
                                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                }
                            });
                        }
                    }
                })

                .build();
        client.connect();
    }

    public void initUmengPush() {
        pushType = PushType.UMENG;
        PushAgent mPushAgent = PushAgent.getInstance(context);
        mPushAgent.setPushCheck(true);
        mPushAgent.setDebugMode(isDebug);
        // 通过服务端控制响铃、呼吸灯、震动
        mPushAgent.setNotificationPlaySound(MsgConstant.NOTIFICATION_PLAY_SERVER); //声音
        mPushAgent.setNotificationPlayLights(MsgConstant.NOTIFICATION_PLAY_SERVER);//呼吸灯
        mPushAgent.setNotificationPlayVibrate(MsgConstant.NOTIFICATION_PLAY_SERVER);//振动
        // 参数number可以设置为0~10之间任意整数。当参数为0时，表示不合并通知。
        mPushAgent.setDisplayNotificationNumber(0);
        //注册推送服务，每次调用register方法都会回调该接口
        Log.e(TAG, "Start to regist umeng token...");
        mPushAgent.register(new IUmengRegisterCallback() {

            @Override
            public void onSuccess(String deviceToken) {
                //注册成功会返回device token
                Log.e(TAG, "Umeng push get deviceToken success, deviceToken:" + deviceToken);
                pushToken = deviceToken;
                onTokenChanged(pushType, pushToken);
            }

            @Override
            public void onFailure(String s, String s1) {
                Log.e(TAG, "Umeng push get deviceToken failed:" + s + s1);
            }
        });

        // 处理通知打开方式
        mPushAgent.setNotificationClickHandler(new UmengNotificationClickHandler() {
            /**
             * 打开应用 方式
             * @param context
             * @param uMessage
             */
            @Override
            public void launchApp(Context context, UMessage uMessage) {
                Log.e(TAG, "lanchApp:" + uMessage.getRaw().toString());
//                String msgType = uMessage.extra.get("msgType");
//                Log.e("dealWithCustomMessage", "msgType" + msgType);
                PushMessage pushMessage = new PushMessage();
                pushMessage.setExtras(uMessage.extra);
                pushMessage.setTitle(uMessage.title);
                pushMessage.setDesc(uMessage.text);
                pushMessage.setMsgId(uMessage.msg_id);
                if (null != pushManager && null != pushManager.getMessageHandler()) {
                    pushManager.getMessageHandler().setOriginalMessage(uMessage.getRaw().toString());
                    pushManager.getMessageHandler().onNotificationClicked(context, pushMessage);
                }

            }

            /**
             * 自定义行为方式
             * @param context
             * @param uMessage
             */
            @Override
            public void dealWithCustomAction(Context context, UMessage uMessage) {
                Log.e(TAG, "dealWithCustomAction");
                PushMessage pushMessage = new PushMessage();
                pushMessage.setExtras(uMessage.extra);
                pushMessage.setTitle(uMessage.title);
                pushMessage.setDesc(uMessage.text);
                pushMessage.setCustom(uMessage.custom);
                pushMessage.setMsgId(uMessage.msg_id);
                if (null != pushManager && null != pushManager.getMessageHandler()) {
                    pushManager.getMessageHandler().setOriginalMessage(uMessage.getRaw().toString());
                    pushManager.getMessageHandler().onNotificationClicked(context, pushMessage);
                }
            }
        });

        UmengMessageHandler messageHandler = new UmengMessageHandler() {
            @Override
            public void dealWithCustomMessage(final Context context, final UMessage msg) {
                new Handler(getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        // 对于自定义消息，PushSDK默认只统计送达。若开发者需要统计点击和忽略，则需手动调用统计方法。
                        boolean isClickOrDismissed = true;
                        if (isClickOrDismissed) {
                            //自定义消息的点击统计
                            UTrack.getInstance(context).trackMsgClick(msg);
                        } else {
                            //自定义消息的忽略统计
                            UTrack.getInstance(context).trackMsgDismissed(msg);
                        }
                        PushMessage pushMessage = new PushMessage();
                        pushMessage.setExtras(msg.extra);
                        pushMessage.setTitle(msg.title);
                        pushMessage.setDesc(msg.text);
                        pushMessage.setCustom(msg.custom);
                        pushMessage.setMsgId(msg.msg_id);
                        if (null != pushManager && null != pushManager.getMessageHandler()) {
                            pushManager.getMessageHandler().setOriginalMessage(msg.getRaw().toString());
                            pushManager.getMessageHandler().onReceiveMessage(context, pushMessage);
                        }
                    }
                });
            }
        };
        mPushAgent.setMessageHandler(messageHandler);

    }

    private void registTokenReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PushConstant.PUSH_TOKEN_CHANGED);
        context.registerReceiver(pushReceiver, intentFilter);
    }

    private void unRegistTokenReceiver() {
        if (null != context) {
            context.unregisterReceiver(pushReceiver);
        }
    }

    /**
     * 设置友盟推送自处理服务类, 通知消息完全自己定义
     *
     * @param clazz
     */
    public void setUmengPushIntentService(Class clazz) {
//        if (pushType.equals(PUSH_TYPE_UMENG)) {
//            PushAgent.getInstance(context).setPushIntentServiceClass(clazz);
//        }
    }


    /**
     * 设置推送别名, 华为没有别名
     *
     * @param aliasType umeng别名名称
     * @param alias     别名
     */
    public void setPushAlias(String aliasType, String alias) {
        Log.d(TAG, String.format("Set push alias: %s, push type: %s", alias, pushType));
        switch (pushType) {
            case PushType.HUAWEI:
                // TODO 目前是标签，可能得修改
//                setHWPushTag(context, aliasType, alias);
                break;
            case PushType.XIAOMI:
                setXMPushAlias(context, alias);
                break;
            case PushType.UMENG:
                setUmengPushAlias(context, aliasType, alias);
                break;
        }
    }


    /**
     * 设置标签, 小米不支持标签，小米使用订阅代替标签
     *
     * @param tag 标签名
     */
    public void setPushTag(String tag) {
        Log.d(tag, String.format("Set push tag: %s, push type: %s", tag, pushType));
        switch (pushType) {
            case PushType.HUAWEI:
//                setHWPushTag(context, PushConstant.DEFAUT_TAG_NAME, tag);
                break;
            case PushType.XIAOMI:
                setXMPushSubscribe(context, tag);
                break;
            case PushType.UMENG:
                setUmengPushTag(context, tag);
                break;
        }
    }

    /**
     * 设置标签，带标签名、标签值
     *
     * @param tagName
     * @param tagValue
     */
    public void setPushTag(String tagName, String tagValue) {
        Log.d(TAG, String.format("Set push tag: %s, push type: %s", tagValue, pushType));
        switch (pushType) {
            case PushType.HUAWEI:
//                setHWPushTag(context, tagName, tagValue);
                break;
            case PushType.XIAOMI:
                setXMPushSubscribe(context, tagValue);
                break;
            case PushType.UMENG:
                setUmengPushTag(context, tagValue);
                break;
        }
    }


    /**
     * 友盟推送：上报别名
     *
     * @param context
     * @param alias
     */
    public void setUmengPushAlias(final Context context, final String aliasType, final String alias) {
        PushAgent.getInstance(context).addExclusiveAlias(alias, aliasType, new UTrack.ICallBack() {
            @Override
            public void onMessage(boolean isSuccess, String message) {
                Log.e("onMessage", "上报成功" + isSuccess + message);
            }
        });
    }


    /**
     * 友盟推送：上报标签
     *
     * @param context
     * @param tag
     */
    public void setUmengPushTag(Context context, String tag) {
        PushAgent.getInstance(context).getTagManager().add(new TagManager.TCallBack() {
            @Override
            public void onMessage(boolean b, ITagManager.Result result) {

            }
        }, tag);
    }


    /**
     * 小米推送：上报别名
     *
     * @param context
     * @param alias
     */
    public void setXMPushAlias(Context context, String alias) {
        /**
         * context 	Android平台上app的上下文，建议传入当前app的application context
         * alias 	为指定用户设置别名
         * category 扩展参数，暂时没有用途，直接填null
         */
        MiPushClient.setAlias(context, alias, null);
    }

    /**
     * 小米推送：订阅主题 类似友盟别名，根据用户订阅的不同主题，开发者可以根据订阅的主题实现分组群发
     *
     * @param context
     * @param topic
     */
    public void setXMPushSubscribe(final Context context, final String topic) {
        /**
         * context 	Android平台上app的上下文，建议传入当前app的application context
         * topic 	主题
         * category 扩展参数，暂时没有用途，直接填null
         */
        Log.e(TAG, "set xiaomi subscribe:" + topic);
        new Thread(new Runnable() {
            @Override
            public void run() {
                MiPushClient.subscribe(context, topic, null);
            }
        }).start();
    }


    /**
     * 华为推送：上报标签
     *
     * @param context
     * @param tagName
     * @param tagValue
     */
    public void setHWPushTag(Context context, String tagName, String tagValue) {
        String key = tagName;
        String value = tagValue;
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(key, value);
        try {
            HuaweiPush.HuaweiPushApi.setTags(client, map);
        } catch (PushException e) {
            Log.e(TAG, e.toString());
        }
    }

//    /**
//     * 华为推送：上报位置信息:华为推送会根据位置信息推送
//     */
//    public void setHWPushFeature(Context context) {
//        com.huawei.android.pushagent.api.PushManager.enableFeature(context, com.huawei.android.pushagent.api.PushManager.PushFeature.LOCATION_BASED_MESSAGE, true);
//    }


    /**
     * 删除别名
     *
     * @param alias
     */
    public void deleteAlias(String aliasType, String alias) {
        switch (pushType) {
            case PushType.HUAWEI:
                // 华为没有别名
                deleteHWTag(context);
                break;
            case PushType.XIAOMI:
                deleteXMAlias(context, alias);
                break;
            case PushType.UMENG:
                deleteUmengAlias(context, aliasType, alias);
                break;
        }
    }

    /**
     * 删除别名
     *
     * @param tag
     */
    public void deleteTag(String tagKey, String tag) {
        switch (pushType) {
            case PushType.HUAWEI:
                deleteHWTag(context);
                break;
            case PushType.XIAOMI:
                deleteXMSubscribe(context, tag);
                break;
            case PushType.UMENG:
                deleteUmengTag(context);
                break;
        }
    }

    /**
     * 友盟推送：删除之前添加的别名
     *
     * @param context
     * @param alias
     */
    public void deleteUmengAlias(Context context, String aliasType, String alias) {
        PushAgent.getInstance(context).removeAlias(alias, aliasType, new UTrack.ICallBack() {
            @Override
            public void onMessage(boolean b, String s) {

            }
        });
    }

    /**
     * 友盟推送：删除之前添加的所有标签
     *
     * @param context
     */
    public void deleteUmengTag(Context context) {
        PushAgent.getInstance(context).getTagManager().reset(new TagManager.TCallBack() {
            @Override
            public void onMessage(boolean isSuccess, ITagManager.Result result) {
                Log.e("SettingsActivity", "isSuccess" + isSuccess);
            }
        });
    }

    /**
     * 小米推送：删除之前的别名
     *
     * @param context
     * @param alias
     */
    public void deleteXMAlias(Context context, String alias) {
        /**
         * context 	Android平台上app的上下文，建议传入当前app的application context
         * alias 	别名
         * category 扩展参数，暂时没有用途，直接填null
         */
        MiPushClient.unsetAlias(context, alias, null);
    }

    /**
     * 小米推送：删除之前的订阅主题
     *
     * @param context
     * @param topic
     */
    public void deleteXMSubscribe(Context context, String topic) {
        /**
         * context 	Android平台上app的上下文，建议传入当前app的application context
         * topic 	主题
         * category 扩展参数，暂时没有用途，直接填null
         */
        MiPushClient.unsubscribe(context, topic, null);
    }

    /**
     * 华为推送：删除之前添加的标签
     * TODO 删除标签是否影响其他设备
     *
     * @param context
     */
    public void deleteHWTag(Context context) {
        String delKeyStr = PushConstant.CUSTOMALIASTYPETOKEN;
        List<String> list = new ArrayList<String>();
        list.add(delKeyStr);
        try {
            HuaweiPush.HuaweiPushApi.deleteTags(client, list);
        } catch (PushException e) {
            Log.e(TAG, e.toString());
        }
    }

//    /**
//     * 关闭位置信息的周期上报
//     */
//    public void deleteHWPushFeature(Context context) {
//        com.huawei.android.pushagent.api.PushManager.enableFeature(context, com.huawei.android.pushagent.api.PushManager.PushFeature.LOCATION_BASED_MESSAGE, false);
//    }

    public int getPushType() {
        return pushType;
    }

    public void setPushType(int pushType) {
        this.pushType = pushType;
    }

    public AbstractMessageHandler getMessageHandler() {
        return messageHandler;
    }

    public void setMessageHandler(AbstractMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    public String getPushToken() {
        if (pushType == PushType.HUAWEI && TextUtils.isEmpty(pushToken)) {
//            com.huawei.android.pushagent.api.PushManager.requestToken(context);
            if (!client.isConnected()) {
                Log.e(TAG, "获取token失败，原因：HuaweiApiClient未连接");
                client.connect();
            }


            Log.i(TAG, "异步接口获取push token");

            PendingResult<TokenResult> tokenResult = HuaweiPush.HuaweiPushApi.getToken(client);

            tokenResult.setResultCallback(new ResultCallback<TokenResult>() {

                @Override

                public void onResult(TokenResult result) {
                    if (result != null && result.getTokenRes() != null) {
                        pushToken = result.getTokenRes().getToken();
                    }
                }
            });
        }
        return pushToken;
    }

    public void disablePush() {
        switch (pushType) {
            case PushType.XIAOMI:
                MiPushClient.unregisterPush(context);
                break;
            case PushType.HUAWEI:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "unregist huawei push:" + pushToken);
                        try {
                            if (!TextUtils.isEmpty(pushToken)) {
                                HuaweiPush.HuaweiPushApi.deleteToken(client, pushToken);
                            }
                        } catch (Throwable e) {
                            Log.e(TAG, "unregist huawei push falied:" + e.getMessage(), e);
                        }
                    }
                }).start();
                break;
            case PushType.UMENG:
                PushAgent.getInstance(context).disable(new IUmengCallback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure(String s, String s1) {

                    }
                });
                break;
        }
    }

    public void enablePush() {
        switch (pushType) {
            case PushType.XIAOMI:
                MiPushClient.registerPush(context, GlobalConfig.XMPUSH_APPID, GlobalConfig.XMPUSH_APPKEY);
                break;
            case PushType.HUAWEI:
                client.connect();
//                com.huawei.android.pushagent.api.PushManager.requestToken(context);
                break;
            case PushType.UMENG:
                PushAgent.getInstance(context).enable(new IUmengCallback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure(String s, String s1) {

                    }
                });
                break;
        }
    }

    public boolean isDebug() {
        return isDebug;
    }

    public void setDebug(boolean debug) {
        isDebug = debug;
    }

    public interface PushTokenListener {
        void onPushTokenChanged(int pushType, String token);
    }

    public void addPushTokenListener(PushTokenListener pushTokenListener) {
        listenerList.add(new SoftReference<>(pushTokenListener));
    }

    private void onTokenChanged(int pushType, String token) {
        Iterator<SoftReference<PushTokenListener>> iterator = listenerList.iterator();
        while (iterator.hasNext()) {
            Log.e(TAG, "Start to process token changed...");
            // 取出回调接口
            SoftReference<PushTokenListener> callback = iterator.next();
            if (null != callback && null != callback.get()) {
                callback.get().onPushTokenChanged(pushType, token);
            } else {
                // 回调如果为空, 说明已经被回收, 直接删除
                Log.e(TAG, "process token callback is null");
                iterator.remove();
            }
        }
    }

//    private void checkGooglePlayConnect() {
//        GoogleApiClient mGoogleApiClient2 = new GoogleApiClient.Builder(context)
//                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
//                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
//                    @Override
//                    public void onConnected(@Nullable Bundle arg0) {
//
////                            statusCallBack(isAvailableModuleContext, true, "GooglePlayServicesUtil service is available. onConnected");
//                    }
//
//                    @Override
//                    public void onConnectionSuspended(int arg0) {
//                    }
//
//                })
//                .addOnConnectionFailedListener(
//                        new GoogleApiClient.OnConnectionFailedListener() {
//                            @Override
//                            public void onConnectionFailed(@NonNull ConnectionResult arg0) {
////                                    statusCallBack(isAvailableModuleContext, false, "GooglePlayServicesUtil service is NOT available. OnConnectionFailedListener");
//                            }
//                        }).build();
//        if (mGoogleApiClient2 != null) {
//            mGoogleApiClient2.connect();
//        }
//
//    }

    /**
     * 返回推送类型描述
     *
     * @return
     */
    public String getPushTypeDesc() {
        switch (pushType) {
            case PushType.XIAOMI:
                return "XIAOMI";
            case PushType.HUAWEI:
                return "HUAWEI";
            case PushType.GOOGLE:
                return "GOOGLE";
            default:
                return "ANDROID";
        }
    }

    public interface PushType {
        int UMENG = 0;  // 友盟
        int HUAWEI = 1; // 小米
        int XIAOMI = 2; //华为
        int GOOGLE = 3; // 谷歌
    }
}
