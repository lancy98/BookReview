package com.lancy.bookreview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.VolleyError;

import java.lang.ref.WeakReference;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SearchFragment extends Fragment
        implements BookSearchRecyclerAdapter.RecyclerViewSelection,
        NetworkRequest.NetworkResponse, BookList.BookListParsingCallback {

    private BookSearchRecyclerAdapter mBookSearchRecyclerAdapter;
    private RecyclerView countryRecyclerView;

    private NetworkRequest mNetworkRequest;
    private BookList mBookList;
    private EditText mEditText;
    public SearchFragmentHandlerInterface handler;
    private TextRecognizer textRecognizer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_search, container, false);

        countryRecyclerView = view.findViewById(R.id.bookSearchRecyclerView);
        mEditText = view.findViewById(R.id.editText);
        ImageButton searchImageButton = view.findViewById(R.id.searchImageButton);
        searchImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadBooks(mEditText.getText().toString());
            }
        });

        ImageButton cameraImageButton = view.findViewById(R.id.cameraImageButton);
        cameraImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (handler != null) {
                    handler.showCamera();
                }
            }
        });

        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null &&
                        event.getAction() != KeyEvent.ACTION_DOWN) {
                    return false;
                }

                ((MainActivity) getActivity()).showProgressUI();
                loadBooks(v.getText().toString());
                InputMethodManager inputManager =
                        (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getActivity()
                                .getCurrentFocus()
                                .getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                return true;
            }
        });

        return view;
    }


    public void convertImageToTextAndLoadBooks(Bitmap bitmap) {
        ((MainActivity) getActivity()).showProgressUI();

        final WeakReference<SearchFragment> searchFragmentWeakReference = new WeakReference<>(this);

        textRecognizer = new TextRecognizer(getActivity(), bitmap, new TextRecognizer.Completion() {
            @Override
            public void recognizedText(boolean success, String text) {
                searchFragmentWeakReference.get().handleTextRecognition(success, text);
            }
        });
    }

    private void handleTextRecognition(boolean success, String searchText) {
        if (success) {
            mEditText.setText(searchText.toLowerCase());
            loadBooks(searchText);
        } else {
            new SweetAlertDialog(getActivity())
                    .setTitleText("Oops..")
                    .setContentText("Something went wrong at the server end")
                    .setConfirmText("OK")
                    .show();
            ((MainActivity) getActivity()).hideProgressUI();
        }
    }


    private void loadBooks(String searchText) {
        if (searchText == null || searchText.length() == 0) {
            ((MainActivity) getActivity()).hideProgressUI();

            new SweetAlertDialog(getActivity())
                    .setTitleText("Oops..")
                    .setContentText("Unfortunately there is no text to search")
                    .setConfirmText("OK")
                    .show();

            return;
        }

        searchText = searchText.replace(" ", "+");
        String url = "https://www.goodreads.com/search/index.xml?key=RP1SC8DhEPLIxNC1NwA9g&q=" + searchText;

        mNetworkRequest = new NetworkRequest(getActivity(), url, this);
    }

    @Override
    public void selected(Book book) {
        Intent bookDetailIntent = new Intent(getActivity(), BookDetailActivity.class);
        bookDetailIntent.putExtra("book", book);
        startActivity(bookDetailIntent);
    }

    @Override
    public void networkResponse(String url, String responseString, VolleyError error) {
        mBookList = new BookList();
        mBookList.parseBooksXML(responseString, this);
    }

    @Override
    public void parsingCompleted() {
        countryRecyclerView.setHasFixedSize(true);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        countryRecyclerView.setLayoutManager(gridLayoutManager);

        mBookSearchRecyclerAdapter = new BookSearchRecyclerAdapter(getActivity(), mBookList, this);
        countryRecyclerView.setAdapter(mBookSearchRecyclerAdapter);

        ((MainActivity) getActivity()).hideProgressUI();
    }

    public interface SearchFragmentHandlerInterface {
        public void showCamera();
    }
}
