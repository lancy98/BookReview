package com.lancy.bookreview;


import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;

public class BookDetailsXMLParser {
    private BookDetailsXMLParserCompletion mCallbackObject;

    public BookDetailsXMLParser(Book book, String xmlString,
                                BookDetailsXMLParserCompletion handler) {
        this.mCallbackObject = handler;

        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(xmlString));
            startParsing(book, parser);
        } catch (XmlPullParserException e) {
            Log.e("BookListXMLParser", e.getLocalizedMessage());
            mCallbackObject.parsingCompleted(false, e.getLocalizedMessage());
        } catch (IOException e) {
            Log.e("BookListXMLParser", e.getLocalizedMessage());
            mCallbackObject.parsingCompleted(false, e.getLocalizedMessage());
        }
    }

    private void startParsing(Book book, XmlPullParser parser)
            throws IOException, XmlPullParserException  {

        while (parser.next() != XmlPullParser.END_DOCUMENT) {

            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String tagName = parser.getName();

            switch(tagName) {
                case "isbn":
                    book.mISBN = parser.nextText();
                    break;
                case "isbn13":
                    book.mISBN = parser.nextText();
                    break;
                case "description":
                    book.mDescription = parser.nextText();
                    break;
            }
        }

        mCallbackObject.parsingCompleted(true, null);
    }

    public interface BookDetailsXMLParserCompletion {
        public void parsingCompleted(boolean success, String errorString);
    }
}
