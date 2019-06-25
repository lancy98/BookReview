package com.lancy.bookreview.userflow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.lancy.bookreview.R;
import com.roger.catloadinglibrary.CatLoadingView;

public class LoginActivity
        extends AppCompatActivity
        implements LoginFragment.LoginHandlerInterface {

    CatLoadingView mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (savedInstanceState == null) {
            LoginFragment fragment = new LoginFragment();
            fragment.handler = this;

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction =
                    fragmentManager.beginTransaction();
            fragmentTransaction.replace(android.R.id.content, fragment);
            fragmentTransaction.commit();
        }

        mView = new CatLoadingView();
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void signInCompleted(boolean success) {
        if (success) {
            finish();
        }
    }

    @Override
    public Activity getCurrentActivity() {
        return this;
    }


    @Override
    public void hideProgressView() {
        mView.dismiss();
    }

    @Override
    public void showProgressView() {
        mView.show(getSupportFragmentManager(), "");
    }

}
