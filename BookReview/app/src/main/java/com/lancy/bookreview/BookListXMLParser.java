package com.lancy.bookreview;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

public class BookListXMLParser {

    private BookListXMLParserCompletion mCompletionHandlerObject;

    public BookListXMLParser(String xmlString, BookListXMLParserCompletion handler) {

        if (!checkIfXMLStringIsValid(xmlString, handler)) {
            return;
        }

        this.mCompletionHandlerObject = handler;

        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(xmlString));
            startParsing(parser);
        } catch (XmlPullParserException e) {
            Log.e("BookListXMLParser", e.getLocalizedMessage());
            mCompletionHandlerObject.parsingCompleted(null, e.getLocalizedMessage());
        } catch (IOException e) {
            Log.e("BookListXMLParser", e.getLocalizedMessage());
            mCompletionHandlerObject.parsingCompleted(null, e.getLocalizedMessage());
        }
    }

    private boolean checkIfXMLStringIsValid(String xmlString, BookListXMLParserCompletion handler) {
        if (xmlString == null) {
            handler.parsingCompleted(null, "Could not fetch data from internet");
            return false;
        } else if (xmlString.length() == 0) {
            handler.parsingCompleted(null, "Unfortunately server has not returned any data");
            return false;
        }

        return true;
    }

    private void startParsing(XmlPullParser parser)
            throws IOException, XmlPullParserException {

        ArrayList<Book> books = new ArrayList<>();
        Book currentBook = null;

        while (parser.next() != XmlPullParser.END_DOCUMENT) {

            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            if ("work".equals(parser.getName())) {
                currentBook = new Book();
                books.add(currentBook);
            } else if (currentBook != null) {
                parseForBookInfo(parser, currentBook);
            }
        }

        mCompletionHandlerObject.parsingCompleted(books, null);
    }

    public void parseForBookInfo(XmlPullParser parser, Book currentBook)
            throws IOException, XmlPullParserException {

        String tagName = parser.getName();

        switch (tagName) {
            case "title":
                currentBook.mName = parser.nextText();
                break;
            case "image_url":
                currentBook.mImageLink = parser.nextText();
                break;
            case "average_rating":
                currentBook.mAverageRatings = parser.nextText();
                break;
            case "name":
                currentBook.mAuthorName = parser.nextText();
                break;
            case "id":
                currentBook.mIdentification = parser.nextText();
                break;
        }

    }

    public interface BookListXMLParserCompletion {
        public void parsingCompleted(ArrayList<Book> books, String errorString);
    }
}
