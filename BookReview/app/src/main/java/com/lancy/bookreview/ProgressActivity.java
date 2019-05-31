package com.lancy.bookreview;

import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class ProgressActivity extends AppCompatActivity {
    protected ProgressBar mProgressBar;

    protected void showProgressUI() {
        mProgressBar.setVisibility(View.VISIBLE);
        UserInterfaceHelper.disableUserInteraction(this);
    }

    protected void hideProgressUI() {
        mProgressBar.setVisibility(View.GONE);
        UserInterfaceHelper.enableUserInteraction(this);
    }
}
