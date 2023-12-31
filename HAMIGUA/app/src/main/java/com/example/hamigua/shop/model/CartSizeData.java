package com.example.hamigua.shop.model;

public class CartSizeData {

    private final String color;
    private final String size;
    public String quantity;
    private final String colorImageUrl;

    public CartSizeData(String color, String size, String quantity, String colorImageUrl) {
        this.color = color;
        this.size = size;
        this.quantity = quantity;
        this.colorImageUrl = colorImageUrl;
    }

    public String getColor() {
        return color;
    }

    public String getSize() {
        return size;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getColorImageUrl() {
        return colorImageUrl;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}
