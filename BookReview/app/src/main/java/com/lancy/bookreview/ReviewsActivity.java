package com.lancy.bookreview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;

import com.android.volley.VolleyError;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReviewsActivity extends AppCompatActivity {

    private Book book;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        book = getIntent().getExtras().getParcelable("book");
    }

    @Override
    protected void onStart() {
        super.onStart();

        WebView bookReviewsWebView = findViewById(R.id.descriptionWebView);

        // Change the width the height in the html to fit the screen.
        String displayString = findMatchAndReplace(book.mReviews,
                "width:", ";", "100%");
        displayString = findMatchAndReplace(displayString,
                "width=", "\" " , "100%");
        displayString = findMatchAndReplace(displayString,
                "height=", "\" " , "100%");

        bookReviewsWebView.loadData(displayString,
                "text/html; charset=utf-8", "utf-8");
    }

    private String findMatchAndReplace(String mainString, String leftString, String rightString,
                                     String replaceString) {
        Matcher myMatcher =
                Pattern.compile(Pattern.quote(leftString) + "(.*?)" + Pattern.quote(rightString))
                .matcher(mainString);

        while (myMatcher.find()) {
            String result = myMatcher.group(1).trim();
            mainString = mainString.replace(result, replaceString);
        }

        return mainString;
    }
}
