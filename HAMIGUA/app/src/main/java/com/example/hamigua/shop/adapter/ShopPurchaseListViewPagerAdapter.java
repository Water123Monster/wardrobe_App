package com.example.hamigua.shop.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.hamigua.shop.customer.fragment.CustomerCompletedFragment;
import com.example.hamigua.shop.customer.fragment.CustomerUnconfirmedFragment;
import com.example.hamigua.shop.customer.fragment.CustomerUnratedFragment;

public class ShopPurchaseListViewPagerAdapter extends FragmentStateAdapter {

    public ShopPurchaseListViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new CustomerUnconfirmedFragment();

            case 1:
                return new CustomerUnratedFragment();

            case 2:
                return new CustomerCompletedFragment();

            default:
                return new CustomerUnconfirmedFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
