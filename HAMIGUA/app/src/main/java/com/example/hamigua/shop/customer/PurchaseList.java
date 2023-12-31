package com.example.hamigua.shop.customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.hamigua.R;
import com.example.hamigua.shop.adapter.ShopPurchaseListViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class PurchaseList extends AppCompatActivity {

    TabLayout fragmentTable;
    ViewPager2 fragmentViewpager2;
    ShopPurchaseListViewPagerAdapter viewPagerAdapter;

    //標題列ActionBar的左上角返回鍵監聽事件
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_purchase_list);

        //設置標題列ActionBar的左上角返回鍵
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        fragmentTable = findViewById(R.id.fragmentTable);
        fragmentViewpager2 = findViewById(R.id.fragmentViewpager2);

        viewPagerAdapter = new ShopPurchaseListViewPagerAdapter(this);
        fragmentViewpager2.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(fragmentTable, fragmentViewpager2, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("待確認");
                    break;

                case 1:
                    tab.setText("未評價");
                    break;

                case 2:
                    tab.setText("已完成");
                    break;
            }
        }).attach();
    }
}