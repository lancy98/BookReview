package com.lancy.bookreview;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import java.util.Currency;

public class LoginActivity
        extends AppCompatActivity
        implements  LoginFragment.LoginHandlerInterface {

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
    }

    @Override
    public void showToast(String message) {

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

    }

    @Override
    public void showProgressView() {

    }

}
