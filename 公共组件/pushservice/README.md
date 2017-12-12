# 推送模块
整理友盟推送、小米推送、华为移动服务推送组件，小米手机用小米推送，华为手机用华为推送，其他手机用友盟推送

## 使用方法
Android Studio以module形式直接导入

## 使用步骤

### 修改配置信息
  - 修改GlobalConfig.java中小米推送appid，appkey
  
  - 在主项目AndroidManifest.xml添加友盟appkey、appsecret
  ``
      <meta-data
            android:name="UMENG_APPKEY"
            android:value="59e89ee807fe6506df000a43" />
        <meta-data
            android:name="UMENG_CHANNEL"
            android:value="${UMENG_CHANNEL_VALUE}" />

        <!-- 友盟推送 v3.0.5 -07d5da4d5dfd380a5bc68a149d1ef408-->
        <meta-data
            android:name="UMENG_MESSAGE_SECRET"
            android:value="85c4004df97727d2dfcbb1c7c07a92e0" />
    ``
    
  - 在主项目AndroidManifest.xml添加华为appid和其他配置信息
       ``
        <meta-data
            android:name="com.huawei.hms.client.appid"
            android:value="100117231"></meta-data>
        <activity
            android:name="com.huawei.hms.activity.BridgeActivity"
            android:configChanges="orientation|locale|screenSize|layoutDirection|fontScale"
            android:excludeFromRecents="true"
            android:exported="false"
            android:hardwareAccelerated="true"
            android:theme="@android:style/Theme.Translucent">
            <meta-data
                android:name="hwc-theme"
                android:value="androidhwext:style/Theme.Emui.Translucent" />
        </activity>

        <!--com.thirtydays.library”用实际的应用包名替换-->
        <provider
            android:name="com.huawei.hms.update.provider.UpdateProvider"
            android:authorities="com.thirtydays.library.hms.update.provider"
            android:exported="false"
            android:grantUriPermissions="true" />
            
     ``
 - 新增PushMessageHandler消息处理类继承AbstractMessageHandler
 - 在项目Application类中初始化推送
      ``
        try {
            PushManager.init(this, new PushManager.PushTokenListener() {
                @Override
                public void onPushTokenChanged(int pushType, String token) {
                    Log.e(TAG, "[onPushTokenChanged] pushType:" + pushType + ", token:" + token);
                }
            });
            PushManager.getInstance().setDebug(isDev);
            PushManager.getInstance().setMessageHandler(new PushMessageHandler());
        } catch (Throwable e) {
            Log.i(TAG, "Init push failed. " + e.getMessage(), e);
        }
      ``
   - 
