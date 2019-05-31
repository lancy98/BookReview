package com.lancy.bookreview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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
        mDatabase.child("sell").child(mBook.mISBN)
                .child(userID()).setValue(sellingPrice()).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                    fetchUsersInterestedInTheBook();
                } else {
                    hideProgressUI();
                    showToast("There was problem with the server");
                }
            }
        });
    }

    private void fetchUsersInterestedInTheBook() {
        mDatabase.child("wishlist").child(mBook.mISBN).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userKey = snapshot.getKey();
                    updateNotificationDatabase(userKey);
                }
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                hideProgressUI();
                showToast("There was problem with the server");
            }
        });
    }


    private void updateNotificationDatabase(String toUserID) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String price = mPriceEditText.getText().toString();

        HashMap<String, String> data = new HashMap<>();
        data.put("from", userId);
        data.put("to", toUserID);
        data.put("bookName", mBook.mName);
        data.put("price", price);

        mDatabase.child("notifications").push().setValue(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                hideProgressUI();

                if (task.isSuccessful()) {
                    showToast("succesfully notified user");
                } else {
                    showToast("There was problem with the server");
                }
            }
        });
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
