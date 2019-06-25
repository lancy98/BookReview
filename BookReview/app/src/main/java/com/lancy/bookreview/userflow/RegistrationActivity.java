package com.lancy.bookreview.userflow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.lancy.bookreview.R;
import com.roger.catloadinglibrary.CatLoadingView;

import java.util.HashMap;

public class RegistrationActivity extends AppCompatActivity {

    public static final String TAG = "LancyBookReview";
    private TextInputLayout emailTextInputLayout;
    private TextInputLayout passwordTextInputLayout;
    private TextInputLayout regionTextInputLayout;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private CatLoadingView catLoadingView = new CatLoadingView();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        catLoadingView.setCanceledOnTouchOutside(false);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        emailTextInputLayout = findViewById(R.id.emailTextInputLayout);
        passwordTextInputLayout = findViewById(R.id.passwordTextInputLayout);
        regionTextInputLayout = findViewById(R.id.regionTextInputLayout);
    }

    public void createAccountButtonTapped(View view) {
        final String email = emailTextInputLayout.getEditText().getText().toString();
        String password = passwordTextInputLayout.getEditText().getText().toString();
        final String region = regionTextInputLayout.getEditText().getText().toString();

        int index = email.indexOf('@');
        String username = email.substring(0, index);
        username = username.substring(0, 1).toUpperCase() + username.substring(1);

        final String user = username;

        catLoadingView.show(getSupportFragmentManager(), "");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseInstanceId.getInstance().getInstanceId()
                                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                @Override
                                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                    if (!task.isSuccessful()) {
                                        Log.w(TAG, "getInstanceId failed", task.getException());
                                        catLoadingView.dismiss();
                                        return;
                                    }

                                    // Get new Instance ID token
                                    String token = task.getResult().getToken();

                                    HashMap<String, Object> result = new HashMap<>();
                                    result.put("username", user);
                                    result.put("email", email);
                                    result.put("region", region);
                                    result.put("token", token);

                                    mDatabase.child("user")
                                            .child(mAuth.getCurrentUser().getUid())
                                            .setValue(result).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            catLoadingView.dismiss();
                                            if (task.isSuccessful()) {

                                                Intent intent = new Intent("User Data Changed");
                                                LocalBroadcastManager
                                                        .getInstance(RegistrationActivity.this)
                                                        .sendBroadcast(intent);

                                                finish();
                                            } else {

                                            }
                                        }
                                    });
                                }
                            });
                        } else {
                            catLoadingView.dismiss();
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegistrationActivity.this, task.getException().getLocalizedMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }
}
