package com.example.hamigua.shop.model;

public class ProductRatingData {

    public String productID;
    public String productName;
    public String productImageUrl;
    public String selectColor;
    public String selectSize;
    public String selectQuantity;
    public String productPrice;
    public String buyerName;
    public String buyerID;
    public String ratingValue;

    public ProductRatingData(String productID, String productName, String productImageUrl,
                             String selectColor, String selectSize, String selectQuantity,
                             String productPrice, String buyerName, String buyerID, String ratingValue) {
        this.productID = productID;
        this.productName = productName;
        this.productImageUrl = productImageUrl;
        this.selectColor = selectColor;
        this.selectSize = selectSize;
        this.selectQuantity = selectQuantity;
        this.productPrice = productPrice;
        this.buyerName = buyerName;
        this.buyerID = buyerID;
        this.ratingValue = ratingValue;
    }
}
