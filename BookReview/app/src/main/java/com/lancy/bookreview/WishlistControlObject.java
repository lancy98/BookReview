package com.lancy.bookreview;


import android.app.Activity;

import androidx.recyclerview.widget.RecyclerView;

import static com.lancy.bookreview.ListingScreenType.ListingScreenTypeWishlist;

public class WishlistControlObject extends BookListControlObject {

    public WishlistControlObject(Activity activity, RecyclerView recyclerView) {
        super(activity, recyclerView);
        screenType = ListingScreenTypeWishlist;
        getTopLevelData("wishlist");
    }


}
