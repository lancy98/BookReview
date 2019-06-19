package com.lancy.bookreview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class BookSellWishlistRecyclerAdaptor
        extends RecyclerView.Adapter<BookSellWishlistRecyclerAdaptor.ViewHolder> {

    private ArrayList<Book> books;
    private Context context;
    private RecyclerViewSelection selection;

    public BookSellWishlistRecyclerAdaptor(Context context,
                                           ArrayList<Book> books,
                                           RecyclerViewSelection handler) {
        this.books = books;
        this.context = context;
        selection = handler;
    }

    @Override
    public int getItemCount() {
        if (books == null) {
            return 0;
        }

        return books.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_sell_wishlist_list, viewGroup, false);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final Book book = books.get(i);

        viewHolder.bookNameTextView.setText(book.mName);
        Picasso.get().load(book.mImageLink).into(viewHolder.bookImageView);

        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selection.selected(book);
            }
        });
    }

    // View holder for recycler view.
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView bookNameTextView;
        public ImageView bookImageView;
        public View view;

        public ViewHolder(View itemView) {
            super(itemView);

            bookNameTextView = itemView.findViewById(R.id.bookNameTextView);
            bookImageView = itemView.findViewById(R.id.bookImageView);
            view = itemView;
        }
    }

    public interface RecyclerViewSelection {
        public void selected(Book book);
    }
}
