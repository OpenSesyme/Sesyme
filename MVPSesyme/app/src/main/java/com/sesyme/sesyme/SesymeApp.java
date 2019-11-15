package com.sesyme.sesyme;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class SesymeApp extends Application {

    public static final String CHANNEL_COMMENTS = "Comments";
    public static final String CHANNEL_ANSWERS = "Answers";
    public static final String CHANNEL_MENTION = "Mention";
    public static final String CHANNEL_LIKE = "Like";
    public static final String CHANNEL_DEFAULT = "Default";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel commentsChannel = new NotificationChannel(
                    CHANNEL_COMMENTS, "Comment on your Question", NotificationManager.IMPORTANCE_HIGH
            );

            NotificationChannel answersChannel = new NotificationChannel(
                    CHANNEL_ANSWERS, "Answer on your Question", NotificationManager.IMPORTANCE_HIGH
            );

            NotificationChannel mentionChannel = new NotificationChannel(
                    CHANNEL_MENTION, "Your name was Mentioned", NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationChannel likeChannel = new NotificationChannel(
                    CHANNEL_LIKE, "Your post got a like", NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationChannel defaultChannel = new NotificationChannel(
                    CHANNEL_LIKE, "Sesyme Notification", NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            assert manager != null;
            manager.createNotificationChannel(commentsChannel);
            manager.createNotificationChannel(answersChannel);
            manager.createNotificationChannel(mentionChannel);
            manager.createNotificationChannel(likeChannel);
            manager.createNotificationChannel(defaultChannel);
        }
    }
}
