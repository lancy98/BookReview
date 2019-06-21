package com.lancy.bookreview;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FCMInstanceIDService extends FirebaseMessagingService {

    private DatabaseReference userInfoReference;
    private static String kChannelId = "Default";

    @Override
    public void onNewToken(String token) {
        // User not logged in? don't do anything
        FirebaseUser user =  FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
           return;
        }

        // logged in user
        userInfoReference = FirebaseDatabase.getInstance()
                .getReference().child("user").child(user.getUid());
        userInfoReference.child("token").setValue(token).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.i("Lancy", "token successfully updated from Messaging service");
                } else {
                    Log.i("Lancy", "failed to update token from Messaging service");
                }
            }
        });
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d("msg", "onMessageReceived: " + remoteMessage.getData().get("message"));

        createLocalNotification(remoteMessage);
        broadcastMessage(remoteMessage);
    }

    @Override
    public void onDeletedMessages() {

    }

    //Mark: Private methods

    private void broadcastMessage(RemoteMessage remoteMessage) {
        Intent intent = new Intent("Firebase Notification");
        intent.putExtra("title", remoteMessage.getNotification().getTitle());
        intent.putExtra("body", remoteMessage.getNotification().getBody());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void createLocalNotification(RemoteMessage remoteMessage) {
        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        handleVersionO(manager);
        manager.notify(0, createBuilder(remoteMessage).build());
    }

    private void handleVersionO(NotificationManager manager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(kChannelId,
                            "Default channel",
                            NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
    }

    private PendingIntent pendingIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT);
        return pendingIntent;
    }

    private NotificationCompat.Builder createBuilder(RemoteMessage remoteMessage) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, kChannelId)
                        .setSmallIcon(R.drawable.ic_book)
                        .setContentTitle(remoteMessage.getNotification().getTitle())
                        .setContentText(remoteMessage.getNotification().getBody())
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent());
        return builder;
    }
}
