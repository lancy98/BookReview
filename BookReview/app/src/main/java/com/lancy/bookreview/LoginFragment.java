package com.lancy.bookreview;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
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

public class LoginFragment extends Fragment implements View.OnClickListener {

    private TextInputLayout mEmailTextInputLayout;
    private TextInputLayout mPasswordTextInputLayout;
    private Button signInButton;
    private FirebaseAuth mAuth;
    private DatabaseReference userInfoReference;
    private boolean isSignInProgress;
    public LoginHandlerInterface handler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        mAuth = FirebaseAuth.getInstance();

        mEmailTextInputLayout = view.findViewById(R.id.emailTextInputLayout);

        mPasswordTextInputLayout = view.findViewById(R.id.passwordTextInputLayout);
        mPasswordTextInputLayout.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                signIn();
                return true;
            }
        });

        signInButton = view.findViewById(R.id.signInButton);
        signInButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        signIn();
    }

    private void signIn() {
        if (isSignInProgress) {
            return;
        }

        isSignInProgress = true;

        InputMethodManager inputManager =
                (InputMethodManager) getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(handler.getCurrentActivity()
                        .getCurrentFocus()
                        .getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);

        String emailID = mEmailTextInputLayout.getEditText().getText().toString();
        String password = mPasswordTextInputLayout.getEditText().getText().toString();

        signIn(emailID, password);
    }

    private void signIn(String emailID, String password) {
        handler.showProgressView();

        mAuth.signInWithEmailAndPassword(emailID, password)
                .addOnCompleteListener(handler.getCurrentActivity(),
                        new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            updateUserDatabase();
                        } else {
                            handler.hideProgressView();
                            showToast(task.getException().getLocalizedMessage());
                            isSignInProgress = false;
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
                            handler.hideProgressView();
                            handler.signInCompleted(task.isSuccessful());

                            if (task.isSuccessful()) {

                                showToast("Success");
                            } else {
                                showToast(task.getException().getLocalizedMessage());
                            }

                            isSignInProgress = false;
                        }
                    });

                } else {
                    showToast(task.getException().getLocalizedMessage());
                }
            }
        });
    }

    private void showToast(String message) {
        handler.showToast(message);
    }

    public interface LoginHandlerInterface {
        public void showToast(String message);
        public void showProgressView();
        public void hideProgressView();
        public Activity getCurrentActivity();
        public void signInCompleted(boolean success);
    }
}
