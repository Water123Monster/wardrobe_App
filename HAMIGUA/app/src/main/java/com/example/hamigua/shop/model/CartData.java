package com.example.hamigua.shop.model;

import java.util.ArrayList;

public class CartData {

    public String productID;
    public String productName;
    public String selectColor;
    public String selectSize;
    public String selectQuantity;
    public String productPrice;
    public String sellerName;
    public String sellerID;
    public String productImageURL;
    public String setDataTime;
    public String isChecked;
    public ArrayList<ProductColorData> colorData;
    public ArrayList<ProductSizeData> sizeData;

    public CartData(String productID, String productName, String selectColor, String selectSize, String selectQuantity,
                       String productPrice, String sellerName, String sellerID, String productImageURL, String setDataTime, String isChecked,
                    ArrayList<ProductColorData> colorData, ArrayList<ProductSizeData> sizeData) {

        this.productID = productID;
        this.productName = productName;
        this.selectColor = selectColor;
        this.selectSize = selectSize;
        this.selectQuantity = selectQuantity;
        this.productPrice = productPrice;
        this.sellerName = sellerName;
        this.sellerID = sellerID;
        this.productImageURL = productImageURL;
        this.setDataTime = setDataTime;
        this.isChecked = isChecked;
        this.colorData = colorData;
        this.sizeData = sizeData;
    }

    public String getProductID() {
        return productID;
    }

    public String getProductName() {
        return productName;
    }

    public String getSelectColor() {
        return selectColor;
    }

    public String getSelectSize() {
        return selectSize;
    }

    public String getSelectQuantity() {
        return selectQuantity;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public String getSellerName() {
        return sellerName;
    }

    public String getSellerID() {
        return sellerID;
    }

    public String getProductImageURL() {
        return productImageURL;
    }

    public String getSetDataTime() {
        return setDataTime;
    }

    public String getIsChecked() {
        return isChecked;
    }

    public ArrayList<ProductColorData> getColorData() {
        return colorData;
    }

    public ArrayList<ProductSizeData> getSizeData() {
        return sizeData;
    }

    public void setSelectQuantity(String selectQuantity) {
        this.selectQuantity = selectQuantity;
    }

    public void setIsChecked(String isChecked) {
        this.isChecked = isChecked;
    }
}
