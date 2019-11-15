package com.sesyme.sesyme.data;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.sesyme.sesyme.AnswersActivity;
import com.sesyme.sesyme.DashboardActivity;
import com.sesyme.sesyme.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static com.sesyme.sesyme.SesymeApp.CHANNEL_ANSWERS;
import static com.sesyme.sesyme.SesymeApp.CHANNEL_COMMENTS;
import static com.sesyme.sesyme.SesymeApp.CHANNEL_LIKE;
import static com.sesyme.sesyme.SesymeApp.CHANNEL_MENTION;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FirebaseMessagingServce";
    private NotificationManagerCompat managerCompat;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        String notificationTitle, notificationBody;

        managerCompat = NotificationManagerCompat.from(this);
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            notificationTitle = remoteMessage.getNotification().getTitle();
            notificationBody = remoteMessage.getNotification().getBody();
            // Also if you intend on generating your own notifications as a result of a received FCM
            // message, here is where that should be initiated. See sendNotification method below.

            if (notificationBody != null) {
                if (notificationBody.toLowerCase().contains("like")) {
                    sendLikeNotification(notificationBody, notificationTitle);
                } else if (notificationBody.toLowerCase().contains("mention")) {
                    sendMentionNotification(notificationBody, notificationTitle);
                } else if (notificationBody.toLowerCase().contains("comment")) {
                    sendCommentNotification(notificationBody, notificationTitle);
                } else if (notificationBody.toLowerCase().contains("answer")) {
                    sendAnswerNotification(notificationBody, notificationTitle);
                } else {
                    sendDefaultNotification(notificationBody, notificationTitle);
                }
            }

            sendNotification(notificationTitle, notificationBody);
        }else {
            sendNotification("New notification", "You may have new notifications");
        }
    }

    private void sendNotification(String notificationTitle, String notificationBody) {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)   //Automatically delete the notification
                .setSmallIcon(R.mipmap.ic_launcher) //Notification icon
                .setContentIntent(pendingIntent)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setSound(defaultSoundUri);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (notificationBody.toLowerCase().contains("like")) {
                notificationBuilder.setPriority(NotificationManager.IMPORTANCE_DEFAULT);
            } else if (notificationBody.toLowerCase().contains("mention")) {
                notificationBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH);
            } else if (notificationBody.toLowerCase().contains("comment")) {
                notificationBuilder.setPriority(NotificationManager.IMPORTANCE_DEFAULT);
            } else if (notificationBody.toLowerCase().contains("answer")) {
                notificationBuilder.setPriority(NotificationManager.IMPORTANCE_MAX);
            } else {
                notificationBuilder.setPriority(NotificationManager.IMPORTANCE_DEFAULT);
                sendDefaultNotification(notificationBody, notificationTitle);
            }
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(0, notificationBuilder.build());
        }
    }

    private void sendAnswerNotification(String notificationBody, String notificationTitle) {
        Intent intent = new Intent(this, AnswersActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ANSWERS)
                .setSmallIcon(R.mipmap.ic_launcher) //Notification icon
                .setContentIntent(pendingIntent)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .build();
        managerCompat.notify(1, notification);
    }

    private void sendCommentNotification(String notificationBody, String notificationTitle) {
        Intent intent = new Intent(this, AnswersActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_COMMENTS)
                .setSmallIcon(R.mipmap.ic_launcher) //Notification icon
                .setContentIntent(pendingIntent)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .build();
        managerCompat.notify(2, notification);
    }

    private void sendMentionNotification(String notificationBody, String notificationTitle) {
        Intent intent = new Intent(this, AnswersActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 2, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_MENTION)
                .setSmallIcon(R.mipmap.ic_launcher) //Notification icon
                .setContentIntent(pendingIntent)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .build();
        managerCompat.notify(3, notification);
    }

    private void sendLikeNotification(String notificationBody, String notificationTitle) {
        Intent intent = new Intent(this, AnswersActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 3, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_LIKE)
                .setSmallIcon(R.mipmap.ic_launcher) //Notification icon
                .setContentIntent(pendingIntent)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .build();
        managerCompat.notify(4, notification);
    }

    private void sendDefaultNotification(String notificationBody, String notificationTitle) {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 4, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_LIKE)
                .setSmallIcon(R.mipmap.ic_launcher) //Notification icon
                .setContentIntent(pendingIntent)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .build();
        managerCompat.notify(5, notification);
    }
}
