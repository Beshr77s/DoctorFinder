package com.elmobarmij.chatting.FireBase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.elmobarmij.chatting.R;
import com.elmobarmij.chatting.activities.ChatActivity;
import com.elmobarmij.chatting.models.User;
import com.elmobarmij.chatting.utiles.Constants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.i("TOP",token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        User user = new User();
        user.id = message.getData().get(Constants.KEY_USER_ID);
        user.name = message.getData().get(Constants.KEY_NAME);
        user.token = message.getData().get(Constants.KEY_FCM_TOKEN);
        user.id = message.getData().get(Constants.KEY_USER_ID);

        int notificationId = new Random().nextInt();
        String channelId = "chat_message";
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constants.KEY_USER,user);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this,channelId);
        builder.setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(user.name)
                .setContentText(message.getData().get(Constants.KEY_MESSAGE))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(
                  message.getData().get(Constants.KEY_MESSAGE)
                ));
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
         builder.setContentIntent(pendingIntent)
         .setAutoCancel(true);
         if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
             CharSequence sequence = "Chat Message";
             String channelDescription = "This notification is used for chat message notification";
             int impedance = NotificationManager.IMPORTANCE_DEFAULT;
             NotificationChannel channel = new NotificationChannel(channelId,sequence,impedance);
             channel.setDescription(channelDescription);
             NotificationManager notificationManager = getSystemService(NotificationManager.class);
             notificationManager.createNotificationChannel(channel);
         }
        NotificationManagerCompat compat =  NotificationManagerCompat.from(this);
         compat.notify(notificationId,builder.build());

    }
}
