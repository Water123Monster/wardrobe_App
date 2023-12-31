package com.example.hamigua.shop.seller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.hamigua.R;
import com.example.hamigua.shop.customer.RatingProducts;
import com.example.hamigua.shop.customer.adapter.RatingProductsAdapter;
import com.example.hamigua.shop.model.OrderData;
import com.example.hamigua.shop.model.ProductRatingData;
import com.example.hamigua.shop.seller.adapter.ShowProductsRateAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ShowProductsRate extends AppCompatActivity {

    RecyclerView rating_recyclerview;

    ShowProductsRateAdapter adapter;
    ArrayList<ProductRatingData> arrProductRatingData;
    OrderData orderData;

    FirebaseFirestore DB;

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
        setContentView(R.layout.activity_shop_show_products_rate);

        //設置標題列ActionBar的左上角返回鍵
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        rating_recyclerview = findViewById(R.id.rating_recyclerview);

        init();
        get_Intent_Data();
        set_Products_Data();
    }

    protected void init() {
        arrProductRatingData = new ArrayList<>();
        DB = FirebaseFirestore.getInstance();

        adapter = new ShowProductsRateAdapter(arrProductRatingData, ShowProductsRate.this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(ShowProductsRate.this, 1, GridLayoutManager.VERTICAL, false);
        rating_recyclerview.setLayoutManager(gridLayoutManager);
        rating_recyclerview.setAdapter(adapter);
    }

    protected void get_Intent_Data() {
        Intent intent = getIntent();
        orderData = intent.getParcelableExtra("OrderData");
    }

    protected void set_Products_Data() {
        //處理訂單內商品ID
        String[] productIdArr = orderData.stringProductID.split(";");
        //處理訂單內商品名稱
        String[] productNameArr = orderData.stringProductName.split(";");
        //處理訂單內商品圖片url
        String[] productImageUrlArr = orderData.stringProductImageUrl.split(";");
        //處理訂單內商品所選顏色
        String[] selectColorArr = orderData.stringSelectColor.split(";");
        //處理訂單內商品所選尺寸
        String[] selectSizeArr = orderData.stringSelectSize.split(";");
        //處理訂單內商品所選數量
        String[] selectQuantityArr = orderData.stringSelectQuantity.split(";");
        //處理訂單內商品價格
        String[] productPriceArr = orderData.stringProductPrice.split(";");

        for(int i = 0; i < productIdArr.length; i++) {
            int finalI = i;
            DB.collection("shop_Products_Rating")
                    .whereEqualTo("productID", productIdArr[i])
                    .whereEqualTo("buyerID", orderData.buyerID)
                    .whereEqualTo("selectColor", selectColorArr[i])
                    .whereEqualTo("selectSize", selectSizeArr[i])
                    .whereEqualTo("selectQuantity", selectQuantityArr[i])
                    .orderBy("ratingValue", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful() && task.getResult() != null) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    ProductRatingData data = new ProductRatingData(
                                            productIdArr[finalI], productNameArr[finalI], productImageUrlArr[finalI], selectColorArr[finalI],
                                            selectSizeArr[finalI], selectQuantityArr[finalI], productPriceArr[finalI], orderData.buyerName,
                                            orderData.buyerID, document.getString("ratingValue"));
                                    arrProductRatingData.add(data);
                                }
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
        }
    }
}