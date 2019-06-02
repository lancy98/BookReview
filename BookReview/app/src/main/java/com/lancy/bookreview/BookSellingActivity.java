package com.lancy.bookreview;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class BookSellingActivity extends ProgressActivity {
    private DatabaseReference mDatabase;
    private Book mBook;
    private TextView mBookNameTextView;
    private TextView mAuthorNameTextView;
    private EditText mPriceEditText;
    private ImageView mBookImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_selling);
        mBook = getIntent().getExtras().getParcelable("book");
        mDatabase = FirebaseDatabase.getInstance().getReference();

        getUIComponentsReferences();
        loadDataIntoUI();
    }

    private void loadDataIntoUI() {
        Picasso.get().load(mBook.mImageLink).into(mBookImageView);
        mBookNameTextView.setText(mBook.mName);
        mAuthorNameTextView.setText(mBook.mAuthorName);
    }

    private void getUIComponentsReferences() {
        mProgressBar = findViewById(R.id.progressBar);
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

        showProgressUI();
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
                    hideProgressUI();
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
                    showToast("Database updated sucess");
                } else {
                    showToast("There was problem with the server");
                }

                hideProgressUI();
                finish();
            }
        });
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
