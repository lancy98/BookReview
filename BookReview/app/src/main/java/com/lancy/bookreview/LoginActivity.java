package com.lancy.bookreview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout mEmailTextInputLayout;
    private TextInputLayout mPasswordTextInputLayout;
    private FirebaseAuth mAuth;
    private DatabaseReference userInfoReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mEmailTextInputLayout = findViewById(R.id.emailTextInputLayout);
        mPasswordTextInputLayout = findViewById(R.id.passwordTextInputLayout);
    }

    public void signInButtonTapped(View view) {
        String emailID = mEmailTextInputLayout.getEditText().getText().toString();
        String password = mPasswordTextInputLayout.getEditText().getText().toString();

        signIn(emailID, password);
    }

    private void signIn(String emailID, String password) {
        mAuth.signInWithEmailAndPassword(emailID, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            updateUserDatabase();
                        } else {
                            showToast(task.getException().getLocalizedMessage());
                        }
                    }
                });
    }

    private void updateUserDatabase() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user =  FirebaseAuth.getInstance().getCurrentUser();
                    String token = task.getResult().getToken();

                    userInfoReference = FirebaseDatabase.getInstance()
                            .getReference().child("user").child(user.getUid());
                    userInfoReference.child("token").setValue(token).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                showToast("Success");
                                finish();
                            } else {
                                showToast(task.getException().getLocalizedMessage());
                            }
                        }
                    });

                } else {
                    showToast(task.getException().getLocalizedMessage());
                }
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
