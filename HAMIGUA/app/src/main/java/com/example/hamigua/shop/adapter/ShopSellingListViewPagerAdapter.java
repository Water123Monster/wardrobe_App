package com.example.hamigua.shop.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.hamigua.shop.customer.fragment.CustomerCompletedFragment;
import com.example.hamigua.shop.customer.fragment.CustomerUnconfirmedFragment;
import com.example.hamigua.shop.customer.fragment.CustomerUnratedFragment;
import com.example.hamigua.shop.seller.fragment.SellerCompletedFragment;
import com.example.hamigua.shop.seller.fragment.SellerUnconfirmedFragment;
import com.example.hamigua.shop.seller.fragment.SellerUnratedFragment;

public class ShopSellingListViewPagerAdapter extends FragmentStateAdapter {

    public ShopSellingListViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new SellerUnconfirmedFragment();

            case 1:
                return new SellerUnratedFragment();

            case 2:
                return new SellerCompletedFragment();

            default:
                return new SellerUnconfirmedFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
