package com.lancy.bookreview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roger.catloadinglibrary.CatLoadingView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class BookDetailActivity extends AppCompatActivity
        implements NetworkRequest.NetworkResponse, BookDetailsXMLParser.BookDetailsXMLParserCompletion {

    private FirebaseAuth mAuth;
    private Button wishlistButton;
    private Button sellButton;
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

        sellButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (hasUserLoggedIn()) {
            wishlistButton.setText("Wishlist");
            sellButton.setText("Sell");
        } else {
            wishlistButton.setText("Sign In");
            sellButton.setText("Create Account");
        }
    }

    private void getUIComponentsReferences() {
        wishlistButton = findViewById(R.id.wishlistButton);
        sellButton = findViewById(R.id.sellButton);
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

    public void wishlistButtonTapped(View view) {
        if (hasUserLoggedIn()) {
            checkIfTheUserHasAlreadySellingTheBook();
        } else {
            Intent signInIntent = new Intent(this, LoginActivity.class);
            startActivity(signInIntent);
        }
    }

    public void sellButtonTapped(View view) {
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
            checkIfTheUserHasAddedTheBookToWishlist();
        } else {
            catLoadingView.dismiss();
        }
    }

    private boolean hasUserLoggedIn() {
        return (mAuth.getCurrentUser() != null);
    }

    private void checkIfTheUserHasAddedTheBookToWishlist() {
        if (!hasUserLoggedIn()) {
            catLoadingView.dismiss();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabase.child("wishlist")
                .child(mBook.mISBN)
                .child(userUID)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot == null) {
                    catLoadingView.dismiss();
                    return;
                }

                String value = dataSnapshot.getValue(String.class);
                if (value != null && value.length() > 0) {
                    wishlistButton.setText("Check Available Sellers");
                    sellButton.setVisibility(View.GONE);
                }

                catLoadingView.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void checkIfTheUserHasAlreadySellingTheBook() {
        catLoadingView.show(getSupportFragmentManager(), "");

        mDatabase = FirebaseDatabase.getInstance().getReference();
        final String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabase.child("sell")
                .child(mBook.mISBN)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot == null) {
                            catLoadingView.dismiss();
                            return;
                        } else if (dataSnapshot.hasChildren() == false) {
                            catLoadingView.dismiss();
                            return;
                        }

                        boolean isUserASeller = false;

                        for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                            BookSeller seller = childSnapshot.getValue(BookSeller.class);

                            if (seller.sellerUserID.equals(userUID)) {
                                isUserASeller = true;
                                break;
                            }
                        }

                        catLoadingView.dismiss();

                        if (isUserASeller) {
                            showUserAlreadySellingThisBookPopup();
                        } else {
                            showAvailableSellersActivity();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    private void showUserAlreadySellingThisBookPopup() {
        // Show the notification alert.
        new SweetAlertDialog(this)
                .setTitleText("Sorry")
                .setContentText("You are already selling this book. Hence cannot be added to wishlist")
                .setConfirmText("OK")
                .show();
    }

    private void showAvailableSellersActivity() {
        Intent availableSellersActivity =
                new Intent(BookDetailActivity.this,
                        AvailableSellersActivity.class);
        availableSellersActivity.putExtra("book", mBook);
        startActivity(availableSellersActivity);
    }
}
