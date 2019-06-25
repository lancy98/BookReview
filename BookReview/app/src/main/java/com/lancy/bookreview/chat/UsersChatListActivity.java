package com.lancy.bookreview.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lancy.bookreview.R;
import com.lancy.bookreview.model.Book;
import com.lancy.bookreview.model.BookSeller;
import com.lancy.bookreview.model.ListingScreenType;
import com.lancy.bookreview.model.User;
import com.roger.catloadinglibrary.CatLoadingView;

import java.util.ArrayList;

public class UsersChatListActivity
        extends AppCompatActivity
        implements UsersListRecyclerAdapter.RecyclerViewSelection {

    private Book book;
    private ListingScreenType screenType;
    private String databaseKey;
    private User user;
    private DatabaseReference mDatabase;
    private RecyclerView recyclerView;
    private UsersListRecyclerAdapter adapter;
    private ArrayList<User> users = new ArrayList<>();
    private CatLoadingView catLoadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_chat_list);

        catLoadingView = new CatLoadingView();
        catLoadingView.show(getSupportFragmentManager(), "");
        catLoadingView.setCanceledOnTouchOutside(false);

        recyclerView = findViewById(R.id.chatListRecyclerView);
        book = getIntent().getExtras().getParcelable("book");
        screenType = (ListingScreenType) getIntent()
                .getExtras().getSerializable("screenType");

        if (screenType == ListingScreenType.ListingScreenTypeWishlist) {
            databaseKey = "sell";
        } else {
            databaseKey = "wishlist";
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(databaseKey)
                .child(book.mISBN)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (screenType == ListingScreenType.ListingScreenTypeWishlist) {
                    handleSellSnapshot(dataSnapshot);
                } else {
                    handleWishlistSnapshot(dataSnapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void handleSellSnapshot(DataSnapshot dataSnapshot) {
        ArrayList<String> userIDS = new ArrayList<>();

        for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
            BookSeller seller = childSnapshot.getValue(BookSeller.class);
            userIDS.add(seller.sellerUserID);
        }

        findUsersInformation(userIDS);
    }

    private void loadData() {
        if (catLoadingView != null) {
            catLoadingView.dismiss();
            catLoadingView = null;
        }

        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new UsersListRecyclerAdapter(this, users, this);
        recyclerView.setAdapter(adapter);
    }

    private String userID() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void handleWishlistSnapshot(DataSnapshot dataSnapshot) {
        String bookID = dataSnapshot.getKey();
        mDatabase.child("chat")
                .child(bookID)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ArrayList<String> prospectBuyersUserIDS = new ArrayList<>();

                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    String key = childSnapshot.getKey();
                    if (key.contains(userID())) {
                        String[] parts = key.split("_");
                        String part1 = parts[0];
                        String part2 = parts[1];

                        String sellerUserID = "";
                        if (part1.compareTo(userID()) == 0) {
                            sellerUserID = part2;
                        } else if (part2.compareTo(userID()) == 0) {
                            sellerUserID = part1;
                        }

                        if (sellerUserID.length() > 0) {
                            prospectBuyersUserIDS.add(sellerUserID);
                        }
                    }
                }

                findUsersInformation(prospectBuyersUserIDS);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void findUsersInformation(final ArrayList<String> userIDS) {
        users.clear();

        mDatabase.child("user").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childDataSnapshot: dataSnapshot.getChildren()) {
                    String UserUUID = childDataSnapshot.getKey();

                    if (userIDS.contains(UserUUID)) {
                        User user = (User) childDataSnapshot.getValue(User.class);
                        user.userID = UserUUID;
                        users.add(user);
                    }

                    loadData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void selected(User user) {
        Intent chatIntent = new Intent(UsersChatListActivity.this, ChatActivity.class);
        chatIntent.putExtra("book", book);
        chatIntent.putExtra("userID", user.userID);
        chatIntent.putExtra("username", user.username);
        startActivity(chatIntent);
    }
}
