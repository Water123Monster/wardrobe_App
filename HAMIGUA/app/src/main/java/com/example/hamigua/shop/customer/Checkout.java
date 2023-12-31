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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hamigua.LoadingDialog;
import com.example.hamigua.R;
import com.example.hamigua.shop.adapter.ShopCartRecyclerViewAdapter;
import com.example.hamigua.shop.adapter.ShopOrderRecyclerViewAdapter;
import com.example.hamigua.shop.model.CartData;
import com.example.hamigua.shop.model.ProductColorData;
import com.example.hamigua.shop.model.ProductSizeData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Checkout extends AppCompatActivity {

    TextView checkout_total_price;
    MaterialButton btn_checkout;

    ShopOrderRecyclerViewAdapter checkoutAdapter;
    RecyclerView orderRecyclerView;
    ArrayList<CartData> orderData;
    ArrayList<ProductColorData> productColorData;
    ArrayList<ProductSizeData> productSizeData;

    FirebaseFirestore DB;
    FirebaseAuth USER;

    LoadingDialog loadingDialog;

    int orderTotalPrice = 0;
    int orderTotalCount = 0;
    int totalPrice = 0;
    String tempSellerID = null;
    String tempSellerName = null;

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
        setContentView(R.layout.activity_shop_checkout);

        //設置標題列ActionBar的左上角返回鍵
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        orderRecyclerView = findViewById(R.id.orderRecyclerView);
        checkout_total_price = findViewById(R.id.checkout_total_price);
        btn_checkout = findViewById(R.id.btn_checkout);
        btn_checkout.setOnClickListener(checkoutListener);

        init();
        get_DB_Order_Data();
    }

    protected void init() {
        orderData = new ArrayList<>();
        DB = FirebaseFirestore.getInstance();
        USER = FirebaseAuth.getInstance();
        loadingDialog = new LoadingDialog(Checkout.this);
    }

    protected View.OnClickListener checkoutListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            loadingDialog.start_Loading_Dialog();
            create_DB_Order();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadingDialog.dismiss_Dialog();
                    Toast.makeText(Checkout.this, "下單成功，哈密瓜團隊感謝您的購買", Toast.LENGTH_SHORT).show();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(Checkout.this, Shop.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); //將其他activity的生命週期結束
                            startActivity(intent);

                            //activity的生命週期教學 https://codertw.com/android-%E9%96%8B%E7%99%BC/333374/
                        }
                    }, 1500);
                }
            }, 2000);
        }
    };

    protected void get_DB_Order_Data() {

        DB.collection("user_Information").document(USER.getCurrentUser().getUid()).collection("Shop_Cart")
                .whereEqualTo("isChecked", "true")
                .orderBy("sellerID")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && !task.getResult().isEmpty()) {
                            int documentCount = task.getResult().size();
                            int tempCount = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //處理訂單商品數量、訂單總價資料
                                if(orderTotalCount != 0 && !Objects.equals(tempSellerID, document.getString("sellerID"))) {
                                    CartData data = new CartData(null, null, null, null,
                                            String.valueOf(orderTotalCount), String.valueOf(orderTotalPrice), null, null,
                                            null, null, "false", null, null);
                                    orderData.add(data);
                                    totalPrice += orderTotalPrice;
                                    orderTotalCount = 0;
                                    orderTotalPrice = 0;
                                }

                                //處理賣家名稱資料
                                if(tempSellerID == null || !Objects.equals(tempSellerID, document.getString("sellerID"))) {
                                    tempSellerID = document.getString("sellerID");
                                    CartData data = new CartData(null, null, null, null,
                                            null, null, document.getString("sellerName"), document.getString("sellerID"),
                                            null, null, "false", null, null);
                                    orderData.add(data);
                                }

                                productColorData = new ArrayList<>();
                                productSizeData = new ArrayList<>();

                                //處理顏色資料
                                String colorResult = document.getString("colorString");
                                String[] colorArr1 = colorResult.split(";");
                                for (String colorStr : colorArr1) {
                                    String[] colorArr2 = colorStr.split(",");
                                    ProductColorData colorData = new ProductColorData(colorArr2[0], colorArr2[1]);
                                    productColorData.add(colorData);
                                }
                                //處理數量資料
                                String quantityResult = document.getString("quantityString");
                                String[] quantityArr1 = quantityResult.split(";");
                                for (String quantityStr : quantityArr1) {
                                    String[] quantityArr2 = quantityStr.split(",");
                                    ProductSizeData sizeData = new ProductSizeData(quantityArr2[0], quantityArr2[1], quantityArr2[2]);
                                    productSizeData.add(sizeData);
                                }

                                //處理訂單商品數量、訂單總價
                                orderTotalCount += 1;
                                int productPrice = Integer.parseInt(document.getString("productPrice"));
                                int selectQuantity = Integer.parseInt(document.getString("selectQuantity"));
                                orderTotalPrice += productPrice * selectQuantity;

                                CartData data = new CartData(
                                        document.getString("productID"),
                                        document.getString("productName"),
                                        document.getString("selectColor"),
                                        document.getString("selectSize"),
                                        document.getString("selectQuantity"),
                                        document.getString("productPrice"),
                                        document.getString("sellerName"),
                                        document.getString("sellerID"),
                                        document.getString("productImageURL"),
                                        document.getString("setDataTime"),
                                        document.getString("isChecked"),
                                        productColorData,
                                        productSizeData);

                                orderData.add(data);

                                tempCount += 1;
                                if(tempCount == documentCount) {
                                    CartData cartData = new CartData(null, null, null, null,
                                            String.valueOf(orderTotalCount), String.valueOf(orderTotalPrice), null, null,
                                            null, null, "false", null, null);
                                    orderData.add(cartData);
                                    totalPrice += orderTotalPrice;
                                    orderTotalCount = 0;
                                    orderTotalPrice = 0;
                                }
                            }
                            //將陣列傳到商品RecyclerView類別的建構方法
                            checkoutAdapter = new ShopOrderRecyclerViewAdapter(Checkout.this, orderData);
                            //用GridLayoutManager指定RecyclerView中每列的顯示方式：縱向、1個物件為一列
                            GridLayoutManager gridLayoutManager = new GridLayoutManager(Checkout.this, 1, GridLayoutManager.VERTICAL, false);
                            orderRecyclerView.setLayoutManager(gridLayoutManager);
                            orderRecyclerView.setAdapter(checkoutAdapter);

                            checkout_total_price.setText(String.valueOf(totalPrice));
                        }
                    }
                });
    }

    protected void create_DB_Order() {

        StringBuilder stringProductID = new StringBuilder();
        StringBuilder stringProductName = new StringBuilder();
        StringBuilder stringSelectColor = new StringBuilder();
        StringBuilder stringSelectSize = new StringBuilder();
        StringBuilder stringSelectQuantity = new StringBuilder();
        StringBuilder stringProductPrice = new StringBuilder();
        StringBuilder stringProductImageURL = new StringBuilder();

        DB.collection("user_Information").document(USER.getCurrentUser().getUid())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful() && task.getResult() != null) {
                                    DocumentSnapshot userDocument = task.getResult();

                                    DB.collection("user_Information").document(USER.getCurrentUser().getUid()).collection("Shop_Cart")
                                            .whereEqualTo("isChecked", "true")
                                            .orderBy("sellerID")
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @SuppressLint("SimpleDateFormat")
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if(task.isSuccessful() && !task.getResult().isEmpty()) {
                                                        int documentCount = task.getResult().size();
                                                        int tempCount = 0;
                                                        for (QueryDocumentSnapshot document : task.getResult()) {

                                                            //處理賣家資料
                                                            if(orderTotalCount != 0 && !Objects.equals(tempSellerID, document.getString("sellerID"))) {
                                                                //找到下一個賣家
                                                                Map<String, Object> orderDatas = new HashMap<>();
                                                                orderDatas.put("productIdString", stringProductID.toString());
                                                                orderDatas.put("productNameString", stringProductName.toString());
                                                                orderDatas.put("selectColorString", stringSelectColor.toString());
                                                                orderDatas.put("selectSizeString", stringSelectSize.toString());
                                                                orderDatas.put("selectQuantityString", stringSelectQuantity.toString());
                                                                orderDatas.put("productPriceString", stringProductPrice.toString());
                                                                orderDatas.put("productImageUrlString", stringProductImageURL.toString());
                                                                orderDatas.put("sellerName", tempSellerName);
                                                                orderDatas.put("sellerID", tempSellerID);
                                                                orderDatas.put("buyerID", USER.getCurrentUser().getUid());
                                                                orderDatas.put("buyerName", userDocument.getString("userName"));
                                                                orderDatas.put("orderPrice", String.valueOf(orderTotalPrice));
                                                                orderDatas.put("orderCount", String.valueOf(orderTotalCount));
                                                                orderDatas.put("orderStatus", "unconfirmed");
                                                                orderDatas.put("setDataTime", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()));

                                                                DB.collection("shop_Order_Information")
                                                                        .add(orderDatas);

                                                                orderTotalPrice = 0;
                                                                orderTotalCount = 0;
                                                                stringProductID.delete(0, stringProductID.length());
                                                                stringProductName.delete(0, stringProductName.length());
                                                                stringSelectColor.delete(0, stringSelectColor.length());
                                                                stringSelectSize.delete(0, stringSelectSize.length());
                                                                stringSelectQuantity.delete(0, stringSelectQuantity.length());
                                                                stringProductPrice.delete(0, stringProductPrice.length());
                                                                stringProductImageURL.delete(0, stringProductImageURL.length());
                                                            }

                                                            tempSellerID = document.getString("sellerID");
                                                            tempSellerName = document.getString("sellerName");
                                                            Log.d("seller", document.getString("sellerName"));

                                                            productColorData = new ArrayList<>();
                                                            //處理顏色資料
                                                            String colorResult = document.getString("colorString");
                                                            String[] colorArr1 = colorResult.split(";");
                                                            for (String colorStr : colorArr1) {
                                                                String[] colorArr2 = colorStr.split(",");
                                                                ProductColorData colorData = new ProductColorData(colorArr2[0], colorArr2[1]);
                                                                productColorData.add(colorData);
                                                            }

                                                            stringProductID.append(document.getString("productID")).append(";");
                                                            stringProductName.append(document.getString("productName")).append(";");
                                                            stringSelectColor.append(document.getString("selectColor")).append(";");
                                                            stringSelectSize.append(document.getString("selectSize")).append(";");
                                                            stringSelectQuantity.append(document.getString("selectQuantity")).append(";");
                                                            stringProductPrice.append(document.getString("productPrice")).append(";");
                                                            for (ProductColorData colorData : productColorData) {
                                                                if(Objects.equals(document.getString("selectColor"), colorData.product_color))
                                                                    stringProductImageURL.append(colorData.product_image_url).append(";");
                                                            }

                                                            //處理訂單商品數量、訂單總價
                                                            orderTotalCount += 1;
                                                            int productPrice = Integer.parseInt(document.getString("productPrice"));
                                                            int selectQuantity = Integer.parseInt(document.getString("selectQuantity"));
                                                            orderTotalPrice += productPrice * selectQuantity;


                                                            tempCount += 1;
                                                            if(tempCount == documentCount) {
                                                                //找到最後一個賣家
                                                                Map<String, Object> orderDatas = new HashMap<>();
                                                                orderDatas.put("productIdString", stringProductID.toString());
                                                                orderDatas.put("productNameString", stringProductName.toString());
                                                                orderDatas.put("selectColorString", stringSelectColor.toString());
                                                                orderDatas.put("selectSizeString", stringSelectSize.toString());
                                                                orderDatas.put("selectQuantityString", stringSelectQuantity.toString());
                                                                orderDatas.put("productPriceString", stringProductPrice.toString());
                                                                orderDatas.put("productImageUrlString", stringProductImageURL.toString());
                                                                orderDatas.put("sellerName", document.getString("sellerName"));
                                                                orderDatas.put("sellerID", document.getString("sellerID"));
                                                                orderDatas.put("buyerID", USER.getCurrentUser().getUid());
                                                                orderDatas.put("buyerName", userDocument.getString("userName"));
                                                                orderDatas.put("orderPrice", String.valueOf(orderTotalPrice));
                                                                orderDatas.put("orderCount", String.valueOf(orderTotalCount));
                                                                orderDatas.put("orderStatus", "unconfirmed");
                                                                orderDatas.put("setDataTime", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()));

                                                                DB.collection("shop_Order_Information")
                                                                        .add(orderDatas);

                                                                orderTotalPrice = 0;
                                                                orderTotalCount = 0;
                                                                stringProductID.delete(0, stringProductID.length());
                                                                stringProductName.delete(0, stringProductName.length());
                                                                stringSelectColor.delete(0, stringSelectColor.length());
                                                                stringSelectSize.delete(0, stringSelectSize.length());
                                                                stringSelectQuantity.delete(0, stringSelectQuantity.length());
                                                                stringProductPrice.delete(0, stringProductPrice.length());
                                                                stringProductImageURL.delete(0, stringProductImageURL.length());

                                                            }
                                                        }
                                                    }
                                                }
                                            });
                                }
                                delete_DB_Cart_Products();
                            }
                        });
    }

    protected void delete_DB_Cart_Products() {
        DB.collection("user_Information").document(USER.getCurrentUser().getUid()).collection("Shop_Cart")
                .whereEqualTo("isChecked", "true")
                .orderBy("sellerID")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && task.getResult() != null) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                DB.collection("user_Information").document(USER.getCurrentUser().getUid()).collection("Shop_Cart")
                                        .document(document.getId())
                                        .delete();
                            }
                        }
                    }
                });
    }
}