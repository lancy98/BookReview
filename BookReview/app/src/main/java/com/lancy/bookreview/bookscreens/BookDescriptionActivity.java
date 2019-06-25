package com.lancy.bookreview.bookscreens;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.lancy.bookreview.R;
import com.lancy.bookreview.model.Book;

public class BookDescriptionActivity extends AppCompatActivity {

    private Book book;
    private TextView bookNameTextView;
    private TextView bookAuthorNameTextView;
    private TextView descriptionTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_description);

        book = getIntent().getExtras().getParcelable("book");

        bookNameTextView = findViewById(R.id.bookNameTextView);
        bookAuthorNameTextView = findViewById(R.id.bookAuthorTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);

        bookNameTextView.setText(book.mName);
        bookAuthorNameTextView.setText("by " + book.mAuthorName);

        descriptionTextView.setText(Html.fromHtml(book.mDescription));

        descriptionTextView.setMovementMethod(new ScrollingMovementMethod());
    }
}
