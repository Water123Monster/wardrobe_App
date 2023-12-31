package com.example.hamigua.shop.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

//實作Parcelable介面可將此類別物件序列化，透過intent傳到其他activity
//詳細教學在 https://ithelp.ithome.com.tw/articles/10188862 裡面有說明Parcelable介面的細節

public class ProductData implements Parcelable {

    public final String product_ID;
    public final String product_Name;
    public final String product_Detail;
    public final String product_Size;
    public final String product_Price;
    public final String product_Quantity;
    public final String product_Category;
    public final String accessories_Category;
    public final String seller_Name;
    public final String seller_ID;
    public final String product_Image_URL;
    public final String product_status;
    public final String set_Data_Time;
    public ArrayList<ProductColorData> colorData;
    public ArrayList<ProductSizeData> sizeData;

    public ProductData(String product_ID, String product_Name, String product_Detail, String product_Size, String product_Price,
                       String product_Quantity, String product_Category, String accessories_Category,
                       String seller_Name, String seller_ID, String product_Image_URL, String product_status,
                       String set_Data_Time, ArrayList<ProductColorData> colorData, ArrayList<ProductSizeData> sizeData) {

        this.product_ID = product_ID;
        this.product_Name = product_Name;
        this.product_Detail = product_Detail;
        this.product_Size = product_Size;
        this.product_Price = product_Price;
        this.product_Quantity = product_Quantity;
        this.product_Category = product_Category;
        this.accessories_Category = accessories_Category;
        this.seller_Name = seller_Name;
        this.seller_ID = seller_ID;
        this.product_Image_URL = product_Image_URL;
        this.product_status = product_status;
        this.set_Data_Time = set_Data_Time;
        this.colorData = colorData;
        this.sizeData = sizeData;
    }

    protected ProductData(Parcel in) {
        product_ID = in.readString();
        product_Name = in.readString();
        product_Detail = in.readString();
        product_Size = in.readString();
        product_Price = in.readString();
        product_Quantity = in.readString();
        product_Category = in.readString();
        accessories_Category = in.readString();
        seller_Name = in.readString();
        seller_ID = in.readString();
        product_Image_URL = in.readString();
        product_status = in.readString();
        set_Data_Time = in.readString();
    }

    public static final Creator<ProductData> CREATOR = new Creator<ProductData>() {
        @Override
        public ProductData createFromParcel(Parcel in) {
            return new ProductData(in);
        }

        @Override
        public ProductData[] newArray(int size) {
            return new ProductData[size];
        }
    };

    public String getProduct_ID() {
        return product_ID;
    }

    public String getProduct_Name() {
        return product_Name;
    }

    public String getProduct_Detail() {
        return product_Detail;
    }

    public String getProduct_Size() {
        return product_Size;
    }

    public String getProduct_Price() {
        return product_Price;
    }

    public String getProduct_Quantity() {
        return product_Quantity;
    }

    public String getProduct_Category() {
        return product_Category;
    }

    public String getAccessories_Category() {
        return accessories_Category;
    }

    public String getSeller_Name() {
        return seller_Name;
    }

    public String getSeller_ID() {
        return seller_ID;
    }

    public String getProduct_Image_URL() {
        return product_Image_URL;
    }

    public String getProduct_status() {
        return product_status;
    }

    public String getSet_Data_Time() {
        return set_Data_Time;
    }

    public ArrayList<ProductColorData> getColorData() {
        return colorData;
    }

    public ArrayList<ProductSizeData> getSizeData() {
        return sizeData;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(product_ID);
        dest.writeString(product_Name);
        dest.writeString(product_Detail);
        dest.writeString(product_Size);
        dest.writeString(product_Price);
        dest.writeString(product_Quantity);
        dest.writeString(product_Category);
        dest.writeString(accessories_Category);
        dest.writeString(seller_Name);
        dest.writeString(seller_ID);
        dest.writeString(product_Image_URL);
        dest.writeString(product_status);
        dest.writeString(set_Data_Time);
    }
}
