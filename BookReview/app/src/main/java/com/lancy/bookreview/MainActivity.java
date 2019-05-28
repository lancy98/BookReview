package com.lancy.bookreview;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity
        implements BookSearchRecyclerAdapter.RecyclerViewSelection,
        NetworkRequest.NetworkResponse, BookList.BookListParsingCallback {

    private FirebaseAuth mAuth;
    private BookSearchRecyclerAdapter mBookSearchRecyclerAdapter;
    private NetworkRequest mNetworkRequest;
    private BookList mBookList;
    private EditText mEditText;
    private ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mProgressBar = findViewById(R.id.progressBar);

        mEditText = findViewById(R.id.editText);
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                loadBooks(v.getText().toString());
                InputMethodManager inputManager = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                return true;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        /*
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
            startActivity(intent);
            finish();
        } else {
            Log.d(RegistrationActivity.TAG, "You have successfully regisered.");
        }
        */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.main_logout_button) {
            FirebaseAuth.getInstance().signOut();
        }

        return true;
    }

    public void loginButtonTapped(View view) {

    }

    public void searchButtonTapped(View view) {
        loadBooks(mEditText.getText().toString());
    }

    private void convertImageToText(Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
        detector.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        String text = firebaseVisionText.getText();
                        String searchText = text.replaceAll(System.lineSeparator(), " ");
                        loadBooks(searchText);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void loadBooks(String searchText) {
        if (searchText == null || searchText.length() == 0) {
            return;
        }

        mProgressBar.setVisibility(View.VISIBLE);
        UserInterfaceHelper.disableUserInteraction(this);

        searchText = searchText.replace(" ", "%20");
        String url = "https://www.goodreads.com/search/index.xml?key=RP1SC8DhEPLIxNC1NwA9g&q=" + searchText;

        mNetworkRequest = new NetworkRequest(this, url, this);
    }

    @Override
    public void selected(Book book) {

    }

    @Override
    public void networkResponse(String url, String responseString, VolleyError error) {
        mBookList = new BookList();
        mBookList.parseXML(responseString, this);
    }

    @Override
    public void parsingCompleted() {
        RecyclerView countryRecyclerView = findViewById(R.id.bookSearchRecyclerView);
        countryRecyclerView.setHasFixedSize(true);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        countryRecyclerView.setLayoutManager(gridLayoutManager);

        mBookSearchRecyclerAdapter = new BookSearchRecyclerAdapter(this, mBookList, this);
        countryRecyclerView.setAdapter(mBookSearchRecyclerAdapter);

        mProgressBar.setVisibility(View.GONE);
        UserInterfaceHelper.enableUserInteraction(this);
    }

    public void onEditorAction(TextView textView, int actionId, KeyEvent event) {

    }
}
