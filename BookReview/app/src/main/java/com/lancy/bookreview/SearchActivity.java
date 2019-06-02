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
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import com.google.firebase.auth.FirebaseAuth;

import static android.app.Activity.RESULT_OK;

public class SearchActivity extends Fragment
        implements BookSearchRecyclerAdapter.RecyclerViewSelection,
        NetworkRequest.NetworkResponse, BookList.BookListParsingCallback {

    private BookSearchRecyclerAdapter mBookSearchRecyclerAdapter;
    private RecyclerView countryRecyclerView;

    private NetworkRequest mNetworkRequest;
    private BookList mBookList;
    private EditText mEditText;
    private static final int Image_Capture_Code = 1001;
    protected ProgressBar mProgressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_search, container, false);

        mProgressBar = view.findViewById(R.id.progressBar);
        countryRecyclerView = view.findViewById(R.id.bookSearchRecyclerView);
        mEditText = view.findViewById(R.id.editText);
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                loadBooks(v.getText().toString());
                InputMethodManager inputManager = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                return true;
            }
        });

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.main_logout_button) {
            FirebaseAuth.getInstance().signOut();
        }

        return true;
    }

    public void cameraButtonTapped(View view) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, Image_Capture_Code);
    }

    public void searchButtonTapped(View view) {
        loadBooks(mEditText.getText().toString());
    }

    private void convertImageToTextAndLoadBooks(Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
        detector.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        String text = firebaseVisionText.getText();
                        String searchText = text.replaceAll(System.lineSeparator(), " ");
                        loadBooks(searchText);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void loadBooks(String searchText) {
        if (searchText == null || searchText.length() == 0) {
            return;
        }

        showProgressUI();

        searchText = searchText.replace(" ", "%20");
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

        hideProgressUI();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Image_Capture_Code) {
            if (resultCode == RESULT_OK) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                convertImageToTextAndLoadBooks(bitmap);
            }
        }
    }

    protected void showProgressUI() {
        mProgressBar.setVisibility(View.VISIBLE);
        UserInterfaceHelper.disableUserInteraction(getActivity());
    }

    protected void hideProgressUI() {
        mProgressBar.setVisibility(View.GONE);
        UserInterfaceHelper.enableUserInteraction(getActivity());
    }
}
