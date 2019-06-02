package com.lancy.bookreview;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.google.firebase.auth.FirebaseAuth;

public class UserInterfaceHelper {

    public static void disableUserInteraction(Activity activity) {
        activity.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        );
    }

    public static void enableUserInteraction(Activity activity) {
        activity.getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        );
    }

    public static void hideKeyboard(View view, Activity activity) {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static String userID() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}