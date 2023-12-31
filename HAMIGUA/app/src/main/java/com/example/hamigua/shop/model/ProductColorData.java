package com.example.hamigua.shop.model;

import android.net.Uri;

public class ProductColorData {

    public final String product_color;
    public String product_image_url;

    public ProductColorData(String product_color, String product_image_url) {
        this.product_color = product_color;
        this.product_image_url = product_image_url;
    }

    public String getProduct_color() {
        return product_color;
    }

    public String getProduct_image_url() {
        return product_image_url;
    }
}
