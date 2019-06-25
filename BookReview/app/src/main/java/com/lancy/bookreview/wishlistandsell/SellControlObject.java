package com.lancy.bookreview.wishlistandsell;

import android.app.Activity;

import androidx.recyclerview.widget.RecyclerView;

import com.lancy.bookreview.wishlistandsell.BookListControlObject;

import static com.lancy.bookreview.model.ListingScreenType.ListingScreenTypeSell;

public class SellControlObject extends BookListControlObject {

    public SellControlObject(Activity activity,
                             RecyclerView recyclerView) {
        super(activity, recyclerView);
        screenType = ListingScreenTypeSell;
        getTopLevelData("sell");
    }
}
