package com.lancy.bookreview;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SellAndWishlistFragment extends Fragment {

    public enum ScreenType {
        ScreenTypeWishlist,
        ScreenTypeSell
    }

    public ScreenType screenType;
    private BookListControlObject contolObject;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sell_wishlist,
                container, false);
        recyclerView = view.findViewById(R.id.bookListReyclerViewView);
        updateControlObject();
        return view;
    }

    public void configure(ScreenType screenType) {
        this.screenType = screenType;
    }

    private void updateControlObject() {
        switch (screenType) {
            case ScreenTypeSell:
                contolObject = new SellControlObject(getActivity(), recyclerView);
                break;
            case ScreenTypeWishlist:
                contolObject = new WishlistControlObject(getActivity(), recyclerView);
                break;
        }
    }
}
