package com.lancy.bookreview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SellerRecyclerAdapter
        extends RecyclerView.Adapter<SellerRecyclerAdapter.ViewHolder> {

    private ArrayList<Seller> sellers;
    private Context context;
    private RecyclerViewSelection selection;

    public SellerRecyclerAdapter(Context context,
                                 ArrayList<Seller> sellers,
                                 SellerRecyclerAdapter.RecyclerViewSelection handler) {
        this.sellers = sellers;
        this.context = context;
        selection = handler;
    }

    @Override
    public int getItemCount() {
        if (sellers == null) {
            return 0;
        }

        return sellers.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_seller_list, viewGroup, false);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final Seller seller = sellers.get(i);

        viewHolder.sellerUsernameTextView.setText(seller.name);
        viewHolder.sellingPricingTextView.setText("Selling Price: " + seller.sellingPrice);

        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selection.selected(seller);
            }
        });

    }

    // View holder for recycler view.
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView sellerUsernameTextView;
        public TextView sellingPricingTextView;
        public ImageView sellerImageView;
        public View view;

        public ViewHolder(View itemView) {
            super(itemView);

            sellerUsernameTextView = itemView.findViewById(R.id.usernameTextView);
            sellingPricingTextView = itemView.findViewById(R.id.priceTextView);
            sellerImageView = itemView.findViewById(R.id.userIconImageView);

            view = itemView;
        }
    }

    public interface RecyclerViewSelection {
        public void selected(Seller seller);
    }
}