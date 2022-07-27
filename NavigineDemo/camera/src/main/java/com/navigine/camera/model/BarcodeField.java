package com.navigine.camera.model;


import android.os.Parcel;
import android.os.Parcelable;

public class BarcodeField implements Parcelable {

    private String label = "";
    private String value = "";


    protected BarcodeField(Parcel in) {
        label = in.readString();
        value = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(label);
        parcel.writeString(value);
    }

    public static final Creator<BarcodeField> CREATOR = new Creator<BarcodeField>() {
        @Override
        public BarcodeField createFromParcel(Parcel in) {
            return new BarcodeField(in);
        }

        @Override
        public BarcodeField[] newArray(int size) {
            return new BarcodeField[size];
        }
    };
}
