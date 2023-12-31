package com.example.hamigua.shop.seller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.hamigua.R;
import com.example.hamigua.shop.adapter.ShopProductQuantityRecyclerViewAdapter;
import com.example.hamigua.shop.model.ProductSizeData;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;

public class ChooseProductQuantity extends AppCompatActivity {

    private RecyclerView sizeRecyclerView; //輸入個別規格數量的RecyclerView
    private ArrayList<ProductSizeData> sizeData; //商品個別規格的資料模型物件
    private ShopProductQuantityRecyclerViewAdapter sizeAdapter; //個別規格數量Adapter

    private ArrayList<String> productColor, productSize, productUrl, productQuantity; //取得的規格參數

    private MaterialButton saveProductSize; //儲存規格資料button

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
        setContentView(R.layout.activity_seller_choose_product_quantity);

        //設置標題列ActionBar的左上角返回鍵
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        sizeRecyclerView = findViewById(R.id.sizeRecyclerView);

        saveProductSize = findViewById(R.id.saveProductSize);
        saveProductSize.setOnClickListener(saveListener);

        init();
        get_IntentData();
        set_SizeData();
        set_SizeAdapter();
    }

    protected void init() {
        sizeData = new ArrayList<>();
        productQuantity = new ArrayList<>();
    }

    //儲存規格資料button的點擊事件
    protected View.OnClickListener saveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String[] arrQuantity = sizeAdapter.getQuantity(); //透過Adapter裡的方法取的所有數量的回傳(string陣列)
            //如果有找到陣列內有null，即結束方法
            for (String quantity : arrQuantity) {
                if (quantity == null) {
                    Toast.makeText(ChooseProductQuantity.this, "商品數量尚未輸入完整\n請輸入後再儲存資料", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            set_Data_BackTo_AddNewProducts(arrQuantity); //將資料傳回activity，AddNewProducts
        }
    };

    //設定個別規格數量Adapter
    protected void set_SizeAdapter() {
        sizeAdapter = new ShopProductQuantityRecyclerViewAdapter(ChooseProductQuantity.this, sizeData);
        GridLayoutManager manager = new GridLayoutManager(ChooseProductQuantity.this, 1, GridLayoutManager.VERTICAL, false);
        sizeRecyclerView.setLayoutManager(manager);
        sizeRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        sizeRecyclerView.setAdapter(sizeAdapter);
    }

    //將資料傳回activity，新增商品(AddNewProducts)
    protected void set_Data_BackTo_AddNewProducts(String[] arrQuantity) {
        productQuantity.addAll(Arrays.asList(arrQuantity));
        Intent intent = new Intent();
        intent.setClass(ChooseProductQuantity.this, AddNewProducts.class);
        intent.putExtra("productColor", productColor); //放入傳入參數(顏色arraylist)
        intent.putExtra("productSize", productSize); //放入傳入參數(尺寸arraylist)
        intent.putExtra("productQuantity", productQuantity); //放入傳入參數(規格數量arraylist)
        intent.putExtra("productUrl", productUrl); //放入傳入參數(顏色照片arraylist)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); //將其他activity的生命週期結束
        startActivity(intent);

        //activity的生命週期教學 https://codertw.com/android-%E9%96%8B%E7%99%BC/333374/
    }

    //設定好從前一個activity傳來的參數資料，整合成物件
    protected void set_SizeData() {
        for(int i = 0; i < productColor.size(); i++) {
            for(int j = 0; j < productSize.size(); j++) {
                ProductSizeData data = new ProductSizeData(
                        productColor.get(i), productSize.get(j), null);
                sizeData.add(data);
            }
        }
    }

    //接收從前一個activity(ChooseProductSize)傳來的資料參數
    protected void get_IntentData() {
        Intent intent = getIntent();
        productColor = intent.getStringArrayListExtra("productColor"); //參數(顏色arraylist)
        productSize = intent.getStringArrayListExtra("productSize"); //參數(尺寸arraylist)
        productUrl = intent.getStringArrayListExtra("productUrl"); //參數(顏色照片arraylist)
    }
}