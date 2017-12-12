package com.thirtydays.library.util;

import android.app.Activity;

import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

/**
 * Created by yanchengmeng on 17/8/3.
 */
public class SocialShareUtil {

    public static String QQ = "qq";
    public static String WEIXIN = "weixin";
    public static String CIRCLE = "circle";
    public static String TWITTWE = "twitter";
    public static String FACEBOOK = "facebook";
    public static String SINA = "sina";

    private static SocialShareUtil instance = new SocialShareUtil();

    public static SocialShareUtil getInstance() {
        return instance;
    }

    private SocialShareUtil() {
    }

    public void share(Activity activity, String title, String content, String url, int photoResId, UMShareListener shareListener, String type) {
        share(activity, title, content, url, new UMImage(activity, photoResId), shareListener, type);
    }

    public void share(Activity activity, String title, String content, String url, int photoResId, String type) {
        share(activity, title, content, url, new UMImage(activity, photoResId), type);
    }

    public void share(Activity activity, String title, String content, String url, String photoUrl, UMShareListener shareListener, String type) {
        share(activity, title, content, url, new UMImage(activity, photoUrl), shareListener, type);
    }


    public void share(Activity activity, String title, String content, String url, String photoUrl, String type) {
        share(activity, title, content, url, new UMImage(activity, photoUrl), type);
    }

    private void share(Activity activity, String title, String content, String url, UMImage photo, String type) {
        share(activity, title, content, url, photo, null, type);
    }


    public void postShare(Activity activity, SHARE_MEDIA shareMedia, String title, String content, String url, int photoRes, UMShareListener shareListener) {
        postShare(activity, shareMedia, title, content, url, new UMImage(activity, photoRes), shareListener);
    }

    public void postShare(Activity activity, SHARE_MEDIA shareMedia, String title, String content, String url, String photoUrl, UMShareListener shareListener) {
        postShare(activity, shareMedia, title, content, url, new UMImage(activity, photoUrl), shareListener);
    }

    private void postShare(Activity activity, SHARE_MEDIA shareMedia, String title, String content, String url, UMImage photo, UMShareListener shareListener) {
        ShareAction wxShareAction = new ShareAction(activity);
        UMWeb web = new UMWeb(url);
        web.setTitle(title);//标题
        web.setThumb(photo);  //缩略图
        web.setDescription(content);//描述

        wxShareAction.setPlatform(shareMedia);
        wxShareAction.withMedia(web);
        wxShareAction.setCallback(shareListener);
        wxShareAction.share();
    }

    private void share(Activity activity, String title, String content, String shareLink, UMImage image, UMShareListener umShareListener, String type) {
        UMWeb web = new UMWeb(shareLink);
        web.setTitle(title);//标题
        web.setThumb(image);  //缩略图
        web.setDescription(content);//描述
        ShareAction shareAction = new ShareAction(activity);
        shareAction.withMedia(web);
        shareAction.setCallback(umShareListener);
        if (QQ.equals(type)) {
            shareAction.setPlatform(SHARE_MEDIA.QQ);
        } else if (WEIXIN.equals(type)) {
            shareAction.setPlatform(SHARE_MEDIA.WEIXIN);
        } else if (CIRCLE.equals(type)) {
            shareAction.setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE);
        } else if (SINA.equals(type)) {
            shareAction.setPlatform(SHARE_MEDIA.SINA);
        }
        shareAction.share();
    }

    public void shareImage(Activity activity, String title, String imageUrl, UMShareListener umShareListener, String type) {
        UMImage image = new UMImage(activity, imageUrl);//网络图片
        ShareAction shareAction = new ShareAction(activity);
        shareAction.withText(title);
        shareAction.withMedia(image);
        shareAction.setCallback(umShareListener);
        if (QQ.equals(type)) {
            shareAction.setPlatform(SHARE_MEDIA.QQ);
        } else if (WEIXIN.equals(type)) {
            shareAction.setPlatform(SHARE_MEDIA.WEIXIN);
        } else if (CIRCLE.equals(type)) {
            shareAction.setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE);
        } else if (SINA.equals(type)) {
            shareAction.setPlatform(SHARE_MEDIA.SINA);
        }
        shareAction.share();
    }
}
