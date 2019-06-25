package com.lancy.bookreview.wishlistandsell;


import android.app.Activity;

import androidx.recyclerview.widget.RecyclerView;

import com.lancy.bookreview.wishlistandsell.BookListControlObject;

import static com.lancy.bookreview.model.ListingScreenType.ListingScreenTypeWishlist;

public class WishlistControlObject extends BookListControlObject {

    public WishlistControlObject(Activity activity, RecyclerView recyclerView) {
        super(activity, recyclerView);
        screenType = ListingScreenTypeWishlist;
        getTopLevelData("wishlist");
    }


}
