package com.lancy.bookreview;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AvailableSellersActivity extends ProgressActivity
        implements SellerRecyclerAdapter.RecyclerViewSelection {
    private RecyclerView sellersRecyclerView;
    private DatabaseReference mDatabase;
    private Book mBook;
    private ArrayList<BookSeller> availableSellers = new ArrayList<>();
    private SellerRecyclerAdapter sellerRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_sellers);
        mProgressBar = findViewById(R.id.progressBar);
        sellersRecyclerView = findViewById(R.id.sellersRecyclerView);
        mBook = getIntent().getExtras().getParcelable("book");
        mDatabase = FirebaseDatabase.getInstance().getReference();

        showProgressUI();
        updateWishlistDatabase();
    }

    private String userID() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void updateWishlistDatabase() {
        mDatabase.child("wishlist").child(mBook.mISBN)
                .child(userID()).setValue("-")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mDatabase.child("sell").child(mBook.mISBN)
                                    .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    // iterate through the sellers.
                                    for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        BookSeller seller = snapshot.getValue(BookSeller.class);
                                        availableSellers.add(seller);
                                    }

                                    updateUserDatabase();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    sellerInformationFetched();
                                }
                            });
                        } else {
                            showToast("There was problem with the server");
                        }
                    }
                });
    }

    private void updateUserDatabase() {
        mDatabase.child("user").child(userID())
                .child("wishlist").child(mBook.mISBN).setValue("-")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                        } else {
                        }
                        sellerInformationFetched();
                    }
                });
    }

    private void sellerInformationFetched() {
        hideProgressUI();
        configureRecyclerView();
    }

    private void configureRecyclerView() {
        sellersRecyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        sellersRecyclerView.setLayoutManager(layoutManager);

        sellerRecyclerAdapter = new SellerRecyclerAdapter(this, availableSellers, this);
        sellersRecyclerView.setAdapter(sellerRecyclerAdapter);
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void selected(BookSeller seller) {
        Intent chatActivityIntent = new Intent(this, ChatActivity.class);

        chatActivityIntent.putExtra("book", mBook);
        chatActivityIntent.putExtra("seller", seller);

        startActivity(chatActivityIntent);
    }
}