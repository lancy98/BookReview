package com.lancy.bookreview;

import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable {
    public String mIdentification;
    public String mName;
    public String mImageLink;
    public String mAverageRatings;
    public String mAuthorName;
    public String mISBN;
    public String mISBN13;
    public String mDescription;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mIdentification);
        dest.writeString(this.mName);
        dest.writeString(this.mImageLink);
        dest.writeString(this.mAverageRatings);
        dest.writeString(this.mAuthorName);
        dest.writeString(this.mISBN);
        dest.writeString(this.mISBN13);
        dest.writeString(this.mDescription);
    }

    public Book() {
    }

    protected Book(Parcel in) {
        this.mIdentification = in.readString();
        this.mName = in.readString();
        this.mImageLink = in.readString();
        this.mAverageRatings = in.readString();
        this.mAuthorName = in.readString();
        this.mISBN = in.readString();
        this.mISBN13 = in.readString();
        this.mDescription = in.readString();
    }

    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel source) {
            return new Book(source);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };
}
