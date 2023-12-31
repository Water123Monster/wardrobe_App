package com.example.hamigua.wardrobe.model;

import android.os.Parcel;
import android.os.Parcelable;

//實作Parcelable介面可將此類別物件序列化，透過intent傳到其他activity
//詳細教學在 https://ithelp.ithome.com.tw/articles/10188862 裡面有說明Parcelable介面的細節

public class ClothData implements Parcelable {

    private String imageUrl;
    private String imageName;
    private String imageCategory;
    private String imageHashtag1;
    private String imageHashtag2;
    private String imageHashtag3;
    private String favoriteStatus;
    private String privateStatus;

    public ClothData(String imageUrl, String imageName, String imageCategory, String imageHashtag1,
                     String imageHashtag2, String imageHashtag3, String favoriteStatus, String privateStatus) {
        this.imageUrl = imageUrl;
        this.imageName = imageName;
        this.imageCategory = imageCategory;
        this.imageHashtag1 = imageHashtag1;
        this.imageHashtag2 = imageHashtag2;
        this.imageHashtag3 = imageHashtag3;
        this.favoriteStatus = favoriteStatus;
        this.privateStatus = privateStatus;
    }

    //Parcelable介面的建構方法
    protected ClothData(Parcel in) {
        imageUrl = in.readString();
        imageName = in.readString();
        imageCategory = in.readString();
        imageHashtag1 = in.readString();
        imageHashtag2 = in.readString();
        imageHashtag3 = in.readString();
        favoriteStatus = in.readString();
        privateStatus = in.readString();
    }

    //Parcelable介面的Creator方法
    public static final Creator<ClothData> CREATOR = new Creator<ClothData>() {
        @Override
        public ClothData createFromParcel(Parcel in) {
            return new ClothData(in);
        }

        @Override
        public ClothData[] newArray(int size) {
            return new ClothData[size];
        }
    };

    public String getImageUrl() {
        return imageUrl;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImageCategory() {
        return imageCategory;
    }

    public String getImageHashtag1() {
        return imageHashtag1;
    }

    public String getImageHashtag2() {
        return imageHashtag2;
    }

    public String getImageHashtag3() {
        return imageHashtag3;
    }

    public String getFavoriteStatus() {
        return favoriteStatus;
    }

    public String getPrivateStatus() {
        return privateStatus;
    }

    //Parcelable介面的方法
    @Override
    public int describeContents() {
        return 0;
    }

    //Parcelable介面的方法
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imageUrl);
        dest.writeString(imageName);
        dest.writeString(imageCategory);
        dest.writeString(imageHashtag1);
        dest.writeString(imageHashtag2);
        dest.writeString(imageHashtag3);
        dest.writeString(favoriteStatus);
        dest.writeString(privateStatus);
    }
}
