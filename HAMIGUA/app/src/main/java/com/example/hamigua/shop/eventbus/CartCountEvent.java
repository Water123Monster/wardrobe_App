package com.example.hamigua.shop.eventbus;

//EventBus教學：https://ithelp.ithome.com.tw/articles/10188117
public class CartCountEvent {

    public int cartCount;

    public CartCountEvent(int cartCount) {
        this.cartCount = cartCount;
    }

    public int getCartCount() {
        return cartCount;
    }
}
