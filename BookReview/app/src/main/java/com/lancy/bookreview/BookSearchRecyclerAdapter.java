package com.lancy.bookreview;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

public class BookSearchRecyclerAdapter
        extends RecyclerView.Adapter<BookSearchRecyclerAdapter.ViewHolder>
        implements Filterable {

    private ArrayList<Book> books;
    private BookList bookList;
    private Context context;
    private RecyclerViewSelection selection;

    private Filter bookFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
//            ArrayList<Country> filteredCountries = new ArrayList<Country>();

//            if (constraint == null || constraint.length() == 0) {
//                filteredCountries.addAll(itemsFull);
//            } else {
//                String filterPattern = constraint.toString().toLowerCase().trim();
//
//                for (Country country: itemsFull) {
//                    if (country.name.toLowerCase().contains(filterPattern)) {
//                        filteredCountries.add(country);
//                    }
//                }
//            }
//
//            FilterResults results = new FilterResults();
//            results.values = filteredCountries;
            ArrayList<String> arrayList = new ArrayList<String>();

            FilterResults results = new FilterResults();
            results.values = arrayList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
//            items.clear();
//            items.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public BookSearchRecyclerAdapter(Context context,
                                     BookList bookList,
                                     BookSearchRecyclerAdapter.RecyclerViewSelection handler) {
        this.bookList = bookList;
        this.books = bookList.mBooks;
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
        View view = inflater.inflate(R.layout.item_book_list, viewGroup, false);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final Book book = books.get(i);

        viewHolder.bookNameTextView.setText(book.mName);

//        try {
//            InputStream ims = context.getAssets().open("imagePlaceholder.png");
//            Drawable drawable = Drawable.createFromStream(ims, null);

//            String imageURL = "https://www.countryflags.io/" + country.identification + "/shiny/64.png";
//            Picasso.get().load(imageURL).placeholder(drawable).into(viewHolder.imageView);
        Log.i("Lancy", book.mImageLink);
            Picasso.get().load(book.mImageLink).into(viewHolder.bookImageView);
//        }
//        catch(IOException ex) {
//            return;
//        }
//
//        viewHolder.view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                selection.selected(country);
//            }
//        });

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

    @Override
    public Filter getFilter() {
        return bookFilter;
    }
}