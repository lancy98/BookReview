package com.lancy.bookreview;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
import com.roger.catloadinglibrary.CatLoadingView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class BookDetailActivity extends AppCompatActivity
        implements NetworkRequest.NetworkResponse, BookDetailsXMLParser.BookDetailsXMLParserCompletion {

    private FirebaseAuth mAuth;
    private Button mBottomLeftButton;
    private Button mBottomRightButton;
    private Book mBook;
    private TextView mBookNameTextView;
    private TextView mAuthorNameTextView;
    private ImageView mBookImageView;
    private ArrayList<ImageView> starImageViews = new ArrayList<>();

    private NetworkRequest mNetworkRequest;
    private BookDetailsXMLParser mBookDetailsXMLParser;
    private TextView mBookDescriptionTextView;
    private DatabaseReference mDatabase;
    private CatLoadingView catLoadingView = new CatLoadingView();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        catLoadingView.setCanceledOnTouchOutside(false);
        getUIComponentsReferences();

        mAuth = FirebaseAuth.getInstance();
        mBook = getIntent().getExtras().getParcelable("book");

        loadBookInformation();

        mBottomRightButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (hasUserLoggedIn()) {
            mBottomLeftButton.setText("Wishlist");
            mBottomRightButton.setText("Sell");
        } else {
            mBottomLeftButton.setText("Sign In");
            mBottomRightButton.setText("Create Account");
        }
    }

    private void getUIComponentsReferences() {
        mBottomLeftButton = findViewById(R.id.bottomLeftButton);
        mBottomRightButton = findViewById(R.id.bottomRightButton);
        mBookNameTextView = findViewById(R.id.bookNameTextView);
        mAuthorNameTextView = findViewById(R.id.authorNameTextView);
        mBookImageView = findViewById(R.id.bookImageView);

        starImageViews.add((ImageView) findViewById(R.id.firstStarImageView));
        starImageViews.add((ImageView) findViewById(R.id.secondStarImageView));
        starImageViews.add((ImageView) findViewById(R.id.thirdStarImageView));
        starImageViews.add((ImageView) findViewById(R.id.fourthStarImageView));
        starImageViews.add((ImageView) findViewById(R.id.fifthStarImageView));
    }

    private void updateDataIntoUIComponents() {
        Picasso.get().load(mBook.mImageLink).into(mBookImageView);
        mBookNameTextView.setText(mBook.mName);
        mAuthorNameTextView.setText("by " + mBook.mAuthorName);

       double ratings = Double.parseDouble(mBook.mAverageRatings);
       int baseStars = (int) Math.floor(ratings);
       ratings = Math.ceil(Math.floor(ratings / 0.5) * 0.5) - baseStars;

       for (int i = 0; i < baseStars; i++) {
           starImageViews.get(i).setImageResource(R.drawable.ic_star);
       }

       if (ratings == 1) {
           starImageViews.get(baseStars).setImageResource(R.drawable.ic_star_half);
       }
    }

    public void descriptionButtonClicked(View view) {
        Intent BookDescriptionActivityIntent =
                new Intent(this, BookDescriptionActivity.class);
        BookDescriptionActivityIntent.putExtra("book", mBook);
        startActivity(BookDescriptionActivityIntent);
    }

    public void reviewsButtonClicked(View view) {
        Intent ReviewsActivityIntent =
                new Intent(this, ReviewsActivity.class);
        ReviewsActivityIntent.putExtra("book", mBook);
        startActivity(ReviewsActivityIntent);
    }

    public void bottomLeftButtonTapped(View view) {
        if (hasUserLoggedIn()) {
            Intent availableSellersActivity =
                    new Intent(this, AvailableSellersActivity.class);
            availableSellersActivity.putExtra("book", mBook);
            startActivity(availableSellersActivity);
        } else {
            Intent signInIntent = new Intent(this, LoginActivity.class);
            startActivity(signInIntent);
        }
    }

    public void bottomRightButtonTapped(View view) {
        if (hasUserLoggedIn()) {
            Intent bookSellingActivity =
                    new Intent(this, BookSellingActivity.class);
            bookSellingActivity.putExtra("book", mBook);
            startActivity(bookSellingActivity);
        } else {
            Intent registrationIntent =
                    new Intent(this,
                    RegistrationActivity.class);
            startActivity(registrationIntent);
        }
    }

    private void loadBookInformation() {
        catLoadingView.show(getSupportFragmentManager(), "");
        String authorName = mBook.mAuthorName.replace(" ", "%20");
        String bookName = mBook.mName.replace(" ", "%20");
        String url = "https://www.goodreads.com/book/title.xml?author="
                + authorName + "&key=RP1SC8DhEPLIxNC1NwA9g&title=" + bookName;

        mNetworkRequest = new NetworkRequest(this, url, this);
    }

    @Override
    public void networkResponse(String url, String responseString, VolleyError error) {
        mBookDetailsXMLParser = new BookDetailsXMLParser(mBook,
                responseString, this);
    }

    @Override
    public void parsingCompleted(boolean success, String errorString) {
        if (success) {
            updateDataIntoUIComponents();
            CheckIfTheUserAlreadySellingThisBook();
        }
        catLoadingView.dismiss();
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
        mDatabase.child("sell")
                .child(mBook.mISBN)
                .child(userUID)
                .addValueEventListener(new ValueEventListener() {
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
