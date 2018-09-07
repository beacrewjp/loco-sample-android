package jp.beacrew.locotutorial;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import jp.beacrew.loco.BCLRegion;

public class MyNotification {
    private NotificationManager notificationManager;
    private NotificationChannel notificationChannel;
    private int notfiyCount = 1;
    private Context mContext;

    public MyNotification(Context con) {
        mContext = con;
        notificationManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel("channel_1","locoNotification", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public void regionNotification(BCLRegion bclRegion) {
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.setFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP  // 起動中のアプリがあってもこちらを優先する
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED  // 起動中のアプリがあってもこちらを優先する
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS  // 「最近利用したアプリ」に表示させない
        );

        PendingIntent contentIntent =
                PendingIntent.getActivity(
                        mContext,
                        notfiyCount,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);


        android.app.Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new android.app.Notification.Builder(mContext, "channel_1")
                    .setSmallIcon(R.mipmap.icon)
                    .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.icon))
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle("LocoRegionIn")
                    .setContentText(bclRegion.getName() + "にチェックインしました")
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .build();
        } else {
            notification = new android.app.Notification.Builder(mContext)
                    .setSmallIcon(R.mipmap.icon)
                    .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.icon))
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle("LocoRegionIn")
                    .setContentText(bclRegion.getName() + "にチェックインしました")
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .build();
        }

        // 古い通知を削除
        notificationManager.cancelAll();
        // 通知

        notificationManager.notify(notfiyCount, notification);
        notfiyCount++;
    }

    public void actionNotification(String message) {
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.setFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP  // 起動中のアプリがあってもこちらを優先する
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED  // 起動中のアプリがあってもこちらを優先する
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS  // 「最近利用したアプリ」に表示させない
        );

        PendingIntent contentIntent =
                PendingIntent.getActivity(
                        mContext,
                        notfiyCount,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);


        android.app.Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new android.app.Notification.Builder(mContext, "channel_1")
                    .setSmallIcon(R.mipmap.icon)
                    .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.icon))
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle("LocoAction")
                    .setContentText(message)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .build();
        } else {
            notification = new android.app.Notification.Builder(mContext)
                    .setSmallIcon(R.mipmap.icon)
                    .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.icon))
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle("LocoAction")
                    .setContentText(message)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .build();
        }

        // 古い通知を削除
        notificationManager.cancelAll();
        // 通知

        notificationManager.notify(notfiyCount, notification);
        notfiyCount++;
    }

}
