package com.navigine.navigine.demo.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class VenueIconObj implements Parcelable {

    private int    imageDrawable = -1;
    private String categoryName  = null;
    private boolean isActivated  = false;


    public VenueIconObj(int imageDrawable, String categoryName) {
        this.imageDrawable = imageDrawable;
        this.categoryName  = categoryName;
    }

    protected VenueIconObj(Parcel in) {
        imageDrawable = in.readInt();
        categoryName  = in.readString();
        isActivated   = in.readByte() != 0;
    }

    public int getImageDrawable() {
        return imageDrawable;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public boolean isActivated() {
        return isActivated;
    }

    public void setActivated(boolean activated) {
        isActivated = activated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VenueIconObj venueIconObj = (VenueIconObj) o;
        return categoryName.equals(venueIconObj.categoryName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryName);
    }

    @Override
    public String toString() {
        return "VenueIconObj{" +
                "categoryName='" + categoryName + '\'' +
                ", isActivated=" + isActivated +
                '}';
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(imageDrawable);
        dest.writeString(categoryName);
        dest.writeByte((byte) (isActivated ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VenueIconObj> CREATOR = new Creator<VenueIconObj>() {
        @Override
        public VenueIconObj createFromParcel(Parcel in) {
            return new VenueIconObj(in);
        }

        @Override
        public VenueIconObj[] newArray(int size) {
            return new VenueIconObj[size];
        }
    };

}
