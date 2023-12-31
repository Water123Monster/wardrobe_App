package com.example.hamigua.shop.model;

//實作Parcelable介面可將此類別物件序列化，透過intent傳到其他activity
//詳細教學在 https://ithelp.ithome.com.tw/articles/10188862 裡面有說明Parcelable介面的細節

import android.os.Parcel;
import android.os.Parcelable;

public class ChatUserData implements Parcelable {


    public String userID, userName, imageUrl, email;


    public ChatUserData(String userID, String userName, String imageUrl, String email) {

        this.userID = userID;
        this.userName = userName;
        this.imageUrl = imageUrl;
        this.email = email;
    }

    //Parcelable介面的建構方法
    protected ChatUserData(Parcel in) {
        userID = in.readString();
        userName = in.readString();
        imageUrl = in.readString();
        email = in.readString();
    }

    //Parcelable介面的Creator方法
    public static final Creator<ChatUserData> CREATOR = new Creator<ChatUserData>() {
        @Override
        public ChatUserData createFromParcel(Parcel in) {
            return new ChatUserData(in);
        }

        @Override
        public ChatUserData[] newArray(int size) {
            return new ChatUserData[size];
        }
    };

    public String getUserID() {
        return userID;
    }

    public String getUserName() {
        return userName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userID);
        dest.writeString(userName);
        dest.writeString(imageUrl);
        dest.writeString(email);
    }
}
