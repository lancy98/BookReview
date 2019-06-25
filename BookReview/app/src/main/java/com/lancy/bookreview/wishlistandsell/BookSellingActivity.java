package com.lancy.bookreview.wishlistandsell;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lancy.bookreview.R;
import com.lancy.bookreview.model.Book;
import com.roger.catloadinglibrary.CatLoadingView;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class BookSellingActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private Book mBook;
    private TextView mBookNameTextView;
    private TextView mAuthorNameTextView;
    private EditText mPriceEditText;
    private ImageView mBookImageView;
    private CatLoadingView catLoadingView = new CatLoadingView();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_selling);
        mBook = getIntent().getExtras().getParcelable("book");
        mDatabase = FirebaseDatabase.getInstance().getReference();

        catLoadingView.setCanceledOnTouchOutside(false);
        getUIComponentsReferences();
        loadDataIntoUI();
    }

    private void loadDataIntoUI() {
        Picasso.get().load(mBook.mImageLink).into(mBookImageView);
        mBookNameTextView.setText(mBook.mName);
        mAuthorNameTextView.setText("by " + mBook.mAuthorName);
    }

    private void getUIComponentsReferences() {
        mBookNameTextView = findViewById(R.id.bookNameTextView);
        mAuthorNameTextView = findViewById(R.id.authorNameTextView);
        mPriceEditText = findViewById(R.id.priceEditText);
        mBookImageView = findViewById(R.id.bookImageView);
    }

    public void sellButtonTapped(View view) {
        if (!(sellingPrice() != null && sellingPrice().length() > 0)) {
            showToast("Please enter price");
            return;
        }

        catLoadingView.show(getSupportFragmentManager(), "");
        updateSellDatabase();
    }

    private String userID() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private String sellingPrice() {
        return mPriceEditText.getText().toString();
    }

    private void updateSellDatabase() {
        Map sellingInfo = new HashMap();
        sellingInfo.put("bookPrice", sellingPrice());
        sellingInfo.put("bookName", mBook.mName);
        sellingInfo.put("sellerUserID", userID());

        mDatabase.child("sell").child(mBook.mISBN).push()
                .setValue(sellingInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    updateUserDatabase();
                } else {
                    catLoadingView.dismiss();
                    showToast("There was problem with the server");
                }
            }
        });
    }

    private void updateUserDatabase() {
        mDatabase.child("user").child(userID())
                .child("sell").child(mBook.mISBN)
                .setValue(sellingPrice()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    updateBookDatabase();
                } else {
                    showToast("There was problem with the server");
                }
            }
        });
    }

    private void updateBookDatabase() {
        Map bookInformation = new HashMap();
        bookInformation.put("mName", mBook.mName);
        bookInformation.put("mImageLink", mBook.mImageLink);
        bookInformation.put("mAuthorName", mBook.mAuthorName);
        bookInformation.put("mAverageRatings", mBook.mAverageRatings);

        mDatabase.child("book")
                .child(mBook.mISBN)
                .setValue(bookInformation)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                        } else {
                            showToast("There was problem with the server");
                        }

                        catLoadingView.dismiss();
                        finish();
                    }
                });
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
