///**
// * Copyright 2016 Google Inc. All Rights Reserved.
// * <p>
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * <p>
// * http://www.apache.org/licenses/LICENSE-2.0
// * <p>
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.thirtydays.pushservice.google.service;
//
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.content.Context;
//import android.content.Intent;
//import android.media.RingtoneManager;
//import android.net.Uri;
//import android.support.v4.app.NotificationCompat;
//import android.util.Log;
//
//import com.google.firebase.messaging.FirebaseMessagingService;
//import com.google.firebase.messaging.RemoteMessage;
//import com.thirtydays.pushservice.PushManager;
//import com.thirtydays.pushservice.entity.PushMessage;
//
////import com.firebase.jobdispatcher.Constraint;
////import com.firebase.jobdispatcher.FirebaseJobDispatcher;
////import com.firebase.jobdispatcher.GooglePlayDriver;
////import com.firebase.jobdispatcher.Job;
//
///**
// * google消息处理服务
// */
//public class FCMMessageService extends FirebaseMessagingService {
//
//    private static final String TAG = "FCMMessageService";
//
//    /**
//     * Called when message is received.
//     *
//     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
//     */
//    // [START receive_message]
//    @Override
//    public void onMessageReceived(RemoteMessage remoteMessage) {
//        // [START_EXCLUDE]
//        // There are two types of messages data messages and notification messages. Data messages are handled
//        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
//        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
//        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
//        // When the user taps on the notification they are returned to the app. Messages containing both notification
//        // and data payloads are treated as notification messages. The Firebase console always sends notification
//        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
//        // [END_EXCLUDE]
//        /**
//         * 推送有两种类型消息， 通知和透传， 当有notification时就是通知消息， 当只有data时， 就是透传消息
//         * 同时有notification和data， 也算通知消息。
//         * 通知消息：app在前台时， 通知栏不显示通知消息， 信息直接到onMessageReceived方法处理
//         *          app在后台时， 通知栏显示消息， 点击后会将推送消息传递到intent给到打开的activity， 不走onMessageReceived方法
//         * 透传消息：无论app在前台还是后台， 都走onMessageReceived方法。
//         * 注意：google推送仅在app不被完全杀死的情况下可以收到推送， 如果设置-app-强制停止的按钮不点击， 说明app已完全被杀死， 此时不能收到推送
//         */
////        {
////            "notification": {
////                    "title": "Portugal vs. Denmark",
////                    "body": "5 to 1",
////                    "sound": "default",
////                    "click_action": "OPEN_ACTIVITY_1"
////            },
////            "data": {
////                    "score": "5x1",
////                    "time": "15:10"
////            },
////            "priority": "high",
////            "to": "xxxx"
////        }
//
//        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
//        Log.d(TAG, "From: " + remoteMessage.getFrom());
//        PushMessage pushMessage = new PushMessage();
//        // notification不为空， 就是通知消息, 否则就为透传消息
//        RemoteMessage.Notification notification = remoteMessage.getNotification();
//        if (null != notification) {
//            Log.d(TAG, "Receive notification message.");
//            pushMessage.setTitle(remoteMessage.getNotification().getTitle());
//            // 传输数据从content取
//            pushMessage.setDesc(remoteMessage.getNotification().getBody());
//            pushMessage.setExtras(remoteMessage.getData());
//            if (null != PushManager.getInstance().getMessageHandler()) {
//                PushManager.getInstance().getMessageHandler().onNotificationClicked(this, pushMessage);
//            }
//        } else {
//            pushMessage.setExtras(remoteMessage.getData());
//            if (null != PushManager.getInstance().getMessageHandler()) {
//                PushManager.getInstance().getMessageHandler().onReceiveMessage(this, pushMessage);
//            }
//        }
//
////        // Check if message contains a data payload.
////        if (remoteMessage.getData().size() > 0) {
////            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
////
////            if (/* Check if data needs to be processed by long running job */ true) {
////                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
////                scheduleJob();
////            } else {
////                // Handle message within 10 seconds
////                handleNow();
////            }
////
////        }
////
////        // Check if message contains a notification payload.
////        if (remoteMessage.getNotification() != null) {
////            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
////        }
//
//        // Also if you intend on generating your own notifications as a result of a received FCM
//        // message, here is where that should be initiated. See sendNotification method below.
//    }
//    // [END receive_message]
//
//    /**
//     * Schedule a job using FirebaseJobDispatcher.
//     */
//    private void scheduleJob() {
//        // [START dispatch_job]
////        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
////        Job myJob = dispatcher.newJobBuilder()
////                .setService(MyJobService.class)
////                .setTag("my-job-tag")
////                .build();
////        dispatcher.schedule(myJob);
//        // [END dispatch_job]
//    }
//
//
//    /**
//     * Handle time allotted to BroadcastReceivers.
//     */
//    private void handleNow() {
//        Log.d(TAG, "Short lived task is done.");
//    }
//
//    /**
//     * Create and show a simple notification containing the received FCM message.
//     *
//     * @param messageBody FCM message body received.
//     */
////    private void sendNotification(String messageBody) {
////        Intent intent = new Intent(this, MainActivity.class);
////        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
////        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
////                PendingIntent.FLAG_ONE_SHOT);
////
////        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
////        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
////                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
////                .setContentTitle("FCM Message")
////                .setContentText(messageBody)
////                .setAutoCancel(true)
////                .setSound(defaultSoundUri)
////                .setContentIntent(pendingIntent);
////
////        NotificationManager notificationManager =
////                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
////
////        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
////    }
//}
