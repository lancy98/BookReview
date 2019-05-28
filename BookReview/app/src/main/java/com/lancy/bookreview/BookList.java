package com.lancy.bookreview;

import java.util.ArrayList;

public class BookList implements BookListXMLParser.BookListXMLParserCompletion {
    public ArrayList<Book> mBooks = new ArrayList<>();
    private BookListXMLParser mBookListXMLParser;
    private BookListParsingCallback mCallback;

    public void parseXML(String xmlString, BookListParsingCallback callbackHandler) {
        mCallback = callbackHandler;
        constructBookListFromXMLString(xmlString);
    }


    public void constructBookListFromXMLString(String xmlString) {
        mBookListXMLParser = new BookListXMLParser(xmlString, this);
    }

    @Override
    public void parsingCompleted(ArrayList<Book> books, String errorString) {
        mBooks = books;
        mCallback.parsingCompleted();
        mCallback = null;
        mBookListXMLParser = null;
    }

    public interface BookListParsingCallback {
        public void parsingCompleted();
    }
}
