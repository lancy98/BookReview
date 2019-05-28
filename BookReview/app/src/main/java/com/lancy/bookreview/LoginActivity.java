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

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout mEmailTextInputLayout;
    private TextInputLayout mPasswordTextInputLayout;
    private FirebaseAuth mAuth;

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
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(RegistrationActivity.TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(RegistrationActivity.TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, task.getException().getLocalizedMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }
}
