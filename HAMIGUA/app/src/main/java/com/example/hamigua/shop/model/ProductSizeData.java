package com.example.hamigua.shop.model;

public class ProductSizeData {

    public final String product_color;
    public final String product_size;
    public String product_quantity;

    public ProductSizeData(String product_color, String product_size, String product_quantity) {
        this.product_color = product_color;
        this.product_size = product_size;
        this.product_quantity = product_quantity;
    }

    public String getProduct_color() {
        return product_color;
    }

    public String getProduct_size() {
        return product_size;
    }

    public String getProduct_quantity() {
        return product_quantity;
    }

    public void setProduct_quantity(String product_quantity) {
        this.product_quantity = product_quantity;
    }
}
