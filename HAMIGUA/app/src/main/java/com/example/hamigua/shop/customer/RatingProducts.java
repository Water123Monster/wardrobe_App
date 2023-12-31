package com.example.hamigua.shop.customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.hamigua.R;
import com.example.hamigua.shop.customer.adapter.RatingProductsAdapter;
import com.example.hamigua.shop.customer.fragment.CustomerUnratedFragment;
import com.example.hamigua.shop.model.OrderData;
import com.example.hamigua.shop.model.ProductRatingData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RatingProducts extends AppCompatActivity implements RatingProductsAdapter.OnItemClickListener{

    RecyclerView rating_recyclerview;
    MaterialButton btn_save_rating;

    RatingProductsAdapter adapter;
    ArrayList<ProductRatingData> arrProductRatingData;
    OrderData orderData;

    FirebaseFirestore DB;

    int[] ratingValues;

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
        setContentView(R.layout.activity_shop_rating_products);

        //設置標題列ActionBar的左上角返回鍵
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        rating_recyclerview = findViewById(R.id.rating_recyclerview);
        btn_save_rating = findViewById(R.id.btn_save_rating);
        btn_save_rating.setOnClickListener(saveListener);

        init();
        get_Intent_Data();
        set_Products_Data();
        set_RecyclerView_Adapter();
    }

    protected void init() {
        arrProductRatingData = new ArrayList<>();
        DB = FirebaseFirestore.getInstance();
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

        for (int i = 0; i < productIdArr.length; i++) {
            ProductRatingData data = new ProductRatingData(
                    productIdArr[i], productNameArr[i], productImageUrlArr[i], selectColorArr[i],
                    selectSizeArr[i], selectQuantityArr[i], productPriceArr[i], orderData.buyerName,
                    orderData.buyerID, "0");
            arrProductRatingData.add(data);
        }

        ratingValues = new int[productIdArr.length];
    }

    protected void set_RecyclerView_Adapter() {
        adapter = new RatingProductsAdapter(arrProductRatingData, RatingProducts.this,
                RatingProducts.this, RatingProducts.this, RatingProducts.this,
                RatingProducts.this, RatingProducts.this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(RatingProducts.this, 1, GridLayoutManager.VERTICAL, false);
        rating_recyclerview.setLayoutManager(gridLayoutManager);
        rating_recyclerview.setAdapter(adapter);
    }

    protected View.OnClickListener saveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            for (ProductRatingData data : arrProductRatingData) {
                if(data.ratingValue.equals("0")) {
                    Toast.makeText(RatingProducts.this, "有商品尚未給予評價，請完成後再儲存", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            //Toast.makeText(RatingProducts.this, "儲存評價", Toast.LENGTH_SHORT).show();
            save_Rating_To_DB();
        }
    };

    protected void save_Rating_To_DB() {
        for (ProductRatingData data : arrProductRatingData) {
            Map<String, Object> ratingData = new HashMap<>();
            ratingData.put("productID", data.productID);
            ratingData.put("productName", data.productName);
            ratingData.put("selectColor", data.selectColor);
            ratingData.put("selectSize", data.selectSize);
            ratingData.put("selectQuantity", data.selectQuantity);
            ratingData.put("productPrice", data.productPrice);
            ratingData.put("buyerID", data.buyerID);
            ratingData.put("buyerName", data.buyerName);
            ratingData.put("ratingValue", data.ratingValue);
            ratingData.put("set_Data_Time", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()));

            DB.collection("shop_Products_Rating")
                    .add(ratingData)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            Toast.makeText(RatingProducts.this, "儲存評價成功，已更新訂單狀態", Toast.LENGTH_SHORT).show();

                            Map<String, Object> updateData = new HashMap<>();
                            updateData.put("orderStatus", "completed");
                            DB.collection("shop_Order_Information").document(orderData.orderID)
                                    .update(updateData);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    finish();
                                }
                            }, 1000);
                        }
                    });
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onStar1Click(int position, ProductRatingData productRatingData) {
        arrProductRatingData.get(position).ratingValue = "1";
        adapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onStar2Click(int position, ProductRatingData productRatingData) {
        arrProductRatingData.get(position).ratingValue = "2";
        adapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onStar3Click(int position, ProductRatingData productRatingData) {
        arrProductRatingData.get(position).ratingValue = "3";
        adapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onStar4Click(int position, ProductRatingData productRatingData) {
        arrProductRatingData.get(position).ratingValue = "4";
        adapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onStar5Click(int position, ProductRatingData productRatingData) {
        arrProductRatingData.get(position).ratingValue = "5";
        adapter.notifyDataSetChanged();
    }
}