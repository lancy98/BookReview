package com.lancy.bookreview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BookDetailActivity extends ProgressActivity
        implements NetworkRequest.NetworkResponse, BookDetailsXMLParser.BookDetailsXMLParserCompletion {

    private FirebaseAuth mAuth;
    private Button mBottomLeftButton;
    private Button mBottomRightButton;
    private Book mBook;
    private NetworkRequest mNetworkRequest;
    private BookDetailsXMLParser mBookDetailsXMLParser;
    private TextView mBookDescriptionTextView;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        mBottomLeftButton = findViewById(R.id.bottomLeftButton);
        mBottomRightButton = findViewById(R.id.bottomRightButton);
        mProgressBar = findViewById(R.id.progressBar);
        mBookDescriptionTextView = findViewById(R.id.bookDescriptionTextView);

        mAuth = FirebaseAuth.getInstance();
        mBook = getIntent().getExtras().getParcelable("book");

        loadBookInformation();

        mBottomRightButton.setVisibility(View.VISIBLE);

        if (hasUserLoggedIn()) {
            mBottomLeftButton.setText("Wishlist");
            mBottomRightButton.setText("Sell");
        } else {
            mBottomLeftButton.setText("Sign In");
            mBottomRightButton.setText("Create Account");
        }
    }

    public void bottomLeftButtonTapped(View view) {
        if (hasUserLoggedIn()) {
            Intent availableSellersActivity = new Intent(BookDetailActivity.this, AvailableSellersActivity.class);
            availableSellersActivity.putExtra("book", mBook);
            startActivity(availableSellersActivity);
        } else {
            Intent signInIntent = new Intent(BookDetailActivity.this, LoginActivity.class);
            startActivity(signInIntent);
        }
    }

    public void bottomRightButtonTapped(View view) {
        if (hasUserLoggedIn()) {
            Intent bookSellingActivity = new Intent(BookDetailActivity.this, BookSellingActivity.class);
            bookSellingActivity.putExtra("book", mBook);
            startActivity(bookSellingActivity);
        } else {
            Intent registrationIntent = new Intent(BookDetailActivity.this, RegistrationActivity.class);
            startActivity(registrationIntent);
        }
    }

    private void loadBookInformation() {
        showProgressUI();
        String authorName = mBook.mAuthorName.replace(" ", "%20");
        String bookName = mBook.mName.replace(" ", "%20");
        String url = "https://www.goodreads.com/book/title.xml?author="
                + authorName + "&key=RP1SC8DhEPLIxNC1NwA9g&title=" + bookName;

        mNetworkRequest = new NetworkRequest(this, url, this);
    }

    @Override
    public void networkResponse(String url, String responseString, VolleyError error) {
        mBookDetailsXMLParser = new BookDetailsXMLParser(mBook, responseString, this);
    }

    @Override
    public void parsingCompleted(boolean success, String errorString) {
        if (success) {
            mBookDescriptionTextView.setText(mBook.mDescription);
            CheckIfTheUserAlreadySellingThisBook();
        }
        hideProgressUI();
    }

    private boolean hasUserLoggedIn() {
        return (mAuth.getCurrentUser() != null);
    }

    private void CheckIfTheUserAlreadySellingThisBook() {
        if (!hasUserLoggedIn()) {
            return;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabase.child("sell").child(mBook.mISBN).child(userUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                mBottomRightButton.setVisibility((value != null) ? View.GONE: View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
