package com.lancy.bookreview;

import android.os.Parcel;
import android.os.Parcelable;

public class BookSeller implements Parcelable {
    public String sellerUserID;
    public String sellerName;
    public String bookPrice;
    public String bookName;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.sellerUserID);
        dest.writeString(this.sellerName);
        dest.writeString(this.bookPrice);
        dest.writeString(this.bookName);
    }

    public BookSeller() { }

    public BookSeller(String sellerUserID, String sellerName,
                      String bookPrice, String bookName) {
        this.sellerUserID = sellerUserID;
        this.sellerName = sellerName;
        this.bookPrice = bookPrice;
        this.bookName = bookName;
    }

    protected BookSeller(Parcel in) {
        this.sellerUserID = in.readString();
        this.sellerName = in.readString();
        this.bookPrice = in.readString();
        this.bookName = in.readString();
    }

    public static final Parcelable.Creator<BookSeller> CREATOR = new Parcelable.Creator<BookSeller>() {
        @Override
        public BookSeller createFromParcel(Parcel source) {
            return new BookSeller(source);
        }

        @Override
        public BookSeller[] newArray(int size) {
            return new BookSeller[size];
        }
    };
}
