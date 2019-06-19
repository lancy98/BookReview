package com.lancy.bookreview;

import android.app.Activity;

import androidx.recyclerview.widget.RecyclerView;

import static com.lancy.bookreview.ListingScreenType.ListingScreenTypeSell;

public class SellControlObject extends BookListControlObject {

    public SellControlObject(Activity activity,
                             RecyclerView recyclerView) {
        super(activity, recyclerView);
        screenType = ListingScreenTypeSell;
        getTopLevelData("sell");
    }
}
