package com.example.hamigua.shop.model;

import android.os.Parcel;
import android.os.Parcelable;

//實作Parcelable介面可將此類別物件序列化，透過intent傳到其他activity
//詳細教學在 https://ithelp.ithome.com.tw/articles/10188862 裡面有說明Parcelable介面的細節

public class OrderData implements Parcelable {

    public String stringProductID;
    public String stringProductName;
    public String stringProductImageUrl;
    public String stringSelectColor;
    public String stringSelectSize;
    public String stringSelectQuantity;
    public String stringProductPrice;
    public String orderID;
    public String orderCount;
    public String orderPrice;
    public String sellerName;
    public String sellerID;
    public String buyerName;
    public String buyerID;

    public OrderData(String stringProductID, String stringProductName, String stringProductImageUrl,
                     String stringSelectColor, String stringSelectSize, String stringSelectQuantity,
                     String stringProductPrice, String orderID, String orderCount, String orderPrice,
                     String sellerName, String sellerID, String buyerName, String buyerID) {
        this.stringProductID = stringProductID;
        this.stringProductName = stringProductName;
        this.stringProductImageUrl = stringProductImageUrl;
        this.stringSelectColor = stringSelectColor;
        this.stringSelectSize = stringSelectSize;
        this.stringSelectQuantity = stringSelectQuantity;
        this.stringProductPrice = stringProductPrice;
        this.orderID = orderID;
        this.orderCount = orderCount;
        this.orderPrice = orderPrice;
        this.sellerName = sellerName;
        this.sellerID = sellerID;
        this.buyerName = buyerName;
        this.buyerID = buyerID;

    }

    protected OrderData(Parcel in) {
        stringProductID = in.readString();
        stringProductName = in.readString();
        stringProductImageUrl = in.readString();
        stringSelectColor = in.readString();
        stringSelectSize = in.readString();
        stringSelectQuantity = in.readString();
        stringProductPrice = in.readString();
        orderID = in.readString();
        orderCount = in.readString();
        orderPrice = in.readString();
        sellerName = in.readString();
        sellerID = in.readString();
        buyerName = in.readString();
        buyerID = in.readString();
    }

    public static final Creator<OrderData> CREATOR = new Creator<OrderData>() {
        @Override
        public OrderData createFromParcel(Parcel in) {
            return new OrderData(in);
        }

        @Override
        public OrderData[] newArray(int size) {
            return new OrderData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(stringProductID);
        dest.writeString(stringProductName);
        dest.writeString(stringProductImageUrl);
        dest.writeString(stringSelectColor);
        dest.writeString(stringSelectSize);
        dest.writeString(stringSelectQuantity);
        dest.writeString(stringProductPrice);
        dest.writeString(orderID);
        dest.writeString(orderCount);
        dest.writeString(orderPrice);
        dest.writeString(sellerName);
        dest.writeString(sellerID);
        dest.writeString(buyerName);
        dest.writeString(buyerID);
    }
}
