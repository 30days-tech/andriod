# 友盟分享模块
整理QQ、QQ空间、微信、微信朋友圈分享

## 使用方法
Android Studio以module形式直接导入

## 使用步骤
- 在AndroidManifest.xml package包下添加wxapi包，新建WXEntryActivity继承WXCallbackActivity
- 在主项目AndroidManifest.xml配置WXEntryActivity等配置信息

        <!-- 友盟分享6.4.5 -->
        <!-- 微信 -->
        <activity
            android:name=".wxapi.WXEntryActivity"
            android:configChanges="keyboardHidden|orientation|screenSize"
            android:exported="true"
            android:screenOrientation="portrait"
            android:theme="@android:style/Theme.Translucent.NoTitleBar" />

        <!-- QQ分享 -->
        <activity
            android:name="com.umeng.qq.tencent.AuthActivity"
            android:launchMode="singleTask"
            android:noHistory="true">
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />

                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                    <!-- 1106350727替换QQ Appkey -->
                <data android:scheme="tencent1106350727" />
            </intent-filter>
        </activity>
        <activity
            android:name="com.umeng.qq.tencent.AssistActivity"
            android:configChanges="orientation|keyboardHidden|screenSize"
            android:screenOrientation="portrait"
            android:theme="@android:style/Theme.Translucent.NoTitleBar" />
            
            
- 在主项目Application初始化第三方平台配置信息

       @Override
       public void onCreate() {
           super.onCreate();
           UMShareAPI.get(this);
           PlatformConfig.setWeixin(GlobalConfig.UMENG_WX_APP_ID, GlobalConfig.UMENG_WX_APP_SECRET);
           PlatformConfig.setQQZone(GlobalConfig.UMENG_QQ_APP_ID, GlobalConfig.UMENG_QQ_APP_KEY);
           com.umeng.socialize.Config.DEBUG = isDev;
           // 打开获取用户信息都需要授权
           Config.isNeedAuth = true;
           UMShareAPI.get(this);
       }
            
- 在使用的activity复写onActivityResult，onDestroy方法，在使用地方直接调用工具类即可

       @Override
       protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
       }
       
       @Override
       protected void onDestroy() {
            super.onDestroy();
            UMShareAPI.get(this).release();
       }

       SocialShareUtil.getInstance().share(getActivity(),
                curComment.getNickname() + "对《" + curComment.getBookName() + "》的想法", curComment.getContent(),                              String.format(Constant.SHARE_IDEA, curComment.getCommentId()), R.drawable.ic_launcher, new UMShareListener() {
                    @Override
                    public void onStart(SHARE_MEDIA share_media) {
                        showLoading("");
                    }

                    @Override
                    public void onResult(SHARE_MEDIA share_media) {
                        hideLoading();
                        shareDialog.dismiss();
                        //200代表分享成功
                        showToast("分享成功");
                    }

                    @Override
                    public void onError(SHARE_MEDIA share_media, Throwable throwable) {
                        hideLoading();
                        showToast("分享失败");
                    }

                    @Override
                    public void onCancel(SHARE_MEDIA share_media) {
                        hideLoading();
                        showToast("取消分享");
                    }
                }, SocialShareUtil.CIRCLE);
       
