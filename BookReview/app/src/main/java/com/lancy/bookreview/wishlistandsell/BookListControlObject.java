package com.lancy.bookreview.wishlistandsell;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lancy.bookreview.chat.UsersChatListActivity;
import com.lancy.bookreview.model.Book;
import com.lancy.bookreview.model.ListingScreenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BookListControlObject
        implements BookSellWishlistRecyclerAdaptor.RecyclerViewSelection {
    public DatabaseReference mDatabase = FirebaseDatabase
            .getInstance().getReference();
    private Activity activity;
    private RecyclerView recyclerView;
    private BookSellWishlistRecyclerAdaptor adaptor;
    private ArrayList<Book> books = new ArrayList<>();
    public ListingScreenType screenType;

    public BookListControlObject(Activity activity,
                                 RecyclerView recyclerView) {
        this.activity = activity;
        this.recyclerView = recyclerView;
    }


    public void getTopLevelData(String input) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }

        String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabase.child("user")
                .child(userUID)
                .child(input)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() == null) {
                            return;
                        }

                        Map<String, Object> object =
                                (HashMap<String,Object>) dataSnapshot.getValue();

                        Set<String> isbnSet = object.keySet();
                        fetchBooksWithISBNSet(isbnSet);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
    }

    public void fetchBooksWithISBNSet(final Set<String> bookISBNs) {
        mDatabase.child("book")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (books.size() > 0) {
                            return;
                        }

                        for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                            if (bookISBNs.contains(childSnapshot.getKey())) {
                                Book book = childSnapshot.getValue(Book.class);
                                book.mISBN = childSnapshot.getKey();
                                books.add(book);
                            }
                        }

                        configureRecyclerView();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
    }

    private void configureRecyclerView() {
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);

        adaptor = new BookSellWishlistRecyclerAdaptor(activity, books, this);
        recyclerView.setAdapter(adaptor);
    }

    @Override
    public void selected(Book book) {
        Intent userListIntent = new Intent(activity, UsersChatListActivity.class);
        userListIntent.putExtra("book", book);
        userListIntent.putExtra("screenType", screenType);
        activity.startActivity(userListIntent);
    }
}
