package com.example.little_share.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.little_share.R;
import com.example.little_share.ui.volunteer.activity_volunteer_main;

public class LittleShareNotification {
    private static final String CHANNEL_ID = "little_share_channel";
    private Context context;
    private MediaPlayer mediaPlayer;

    public LittleShareNotification(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Tạo custom sound URI
            Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.little_share_sound);

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Little Share Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setSound(soundUri, audioAttributes);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 250, 250, 250});

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public void showNotificationWithSound(String title, String message) {
        // Tạo intent
        Intent intent = new Intent(context, activity_volunteer_main.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Custom sound cho notification
        Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.little_share_sound);

        // Tạo notification với đầy đủ thông tin
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(soundUri)
                .setVibrate(new long[]{0, 250, 250, 250});

        // Hiển thị notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            int notificationId = (int) System.currentTimeMillis();
            manager.notify(notificationId, builder.build());
            android.util.Log.d("LittleShare", "Notification displayed with ID: " + notificationId);
        }

        // Play custom sound (backup nếu notification sound không work)
        playCustomSound();
    }

    private void playCustomSound() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            mediaPlayer = MediaPlayer.create(context, R.raw.little_share_sound);

            if (mediaPlayer != null) {
                mediaPlayer.setOnCompletionListener(mp -> {
                    mp.release();
                    mediaPlayer = null;
                });

                mediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // Method static để test dễ dàng
    public static void testNotification(Context context) {
        LittleShareNotification notification = new LittleShareNotification(context);
        notification.showNotificationWithSound("Little Share", "Chào mừng bạn đến với Little Share! 🎉");
    }
}