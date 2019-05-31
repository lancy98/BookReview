package com.lancy.bookreview;

import android.util.Log;

import androidx.annotation.NonNull;

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

    }

    @Override
    public void onDeletedMessages() {

    }
}
