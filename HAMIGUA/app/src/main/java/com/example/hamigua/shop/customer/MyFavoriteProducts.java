package com.example.hamigua.shop.customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.hamigua.R;
import com.example.hamigua.shop.adapter.ShopMyFavoriteRecyclerViewAdapter;
import com.example.hamigua.shop.model.ProductColorData;
import com.example.hamigua.shop.model.ProductData;
import com.example.hamigua.shop.model.ProductSizeData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nex3z.notificationbadge.NotificationBadge;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MyFavoriteProducts extends AppCompatActivity implements ShopMyFavoriteRecyclerViewAdapter.OnItemClickListener {

    ImageView open_cart;

    ImageView MyFavoriteProducts_back;
    RecyclerView MyFavoriteProducts_recyclerView;

    ArrayList<ProductData> productsData; //存放商品資料的動態陣列
    ArrayList<ProductColorData> productColorData;
    ArrayList<ProductSizeData> productSizeData;
    ShopMyFavoriteRecyclerViewAdapter myFavoriteAdapter; //商品RecyclerView類別物件
    ArrayList<String> productsID;

    FirebaseFirestore DB;
    FirebaseAuth USER;

    NotificationBadge cart_badge;
    int cartCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_my_favorite_products);

        MyFavoriteProducts_back = findViewById(R.id.checkout_back);
        MyFavoriteProducts_back.setOnClickListener(backListener);
        MyFavoriteProducts_recyclerView = findViewById(R.id.MyFavoriteProducts_recyclerView);
        open_cart = findViewById(R.id.open_cart);
        open_cart.setOnClickListener(cartListener);
        cart_badge = findViewById(R.id.cart_badge);

        init();
        get_DB_Cart_Count();
        get_DB_Searching_Result();
    }

    protected void init() {
        productsData = new ArrayList<>();
        productsID = new ArrayList<>();
        DB = FirebaseFirestore.getInstance();
        USER = FirebaseAuth.getInstance();

        myFavoriteAdapter = new ShopMyFavoriteRecyclerViewAdapter(
                MyFavoriteProducts.this,
                productsData,
                MyFavoriteProducts.this,
                MyFavoriteProducts.this); //將陣列傳到商品RecyclerView類別的建構方法
        //用GridLayoutManager指定RecyclerView中每列的顯示方式：縱向、2個物件為一列
        GridLayoutManager gridLayoutManager = new GridLayoutManager(
                MyFavoriteProducts.this, 2, GridLayoutManager.VERTICAL, false);
        MyFavoriteProducts_recyclerView.setLayoutManager(gridLayoutManager);
        MyFavoriteProducts_recyclerView.setAdapter(myFavoriteAdapter);
    }

    //開啟電商購物車
    protected View.OnClickListener cartListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(MyFavoriteProducts.this, Cart.class);
            startActivity(intent);
        }
    };

    protected void get_DB_Cart_Count() {
        DB.collection("user_Information").document(USER.getCurrentUser().getUid()).collection("Shop_Cart")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                cartCount += 1;
                            }
                            cart_badge.setNumber(cartCount);
                        }
                    }
                });
    }

    protected void get_DB_Searching_Result() {

        DB.collection("user_Information").document(USER.getCurrentUser().getUid())
                .collection("Shop_MyFavorite_Products")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && !task.getResult().isEmpty()) {
                            int resultCount = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                resultCount += 1;
                                DB.collection("products_Information").document(document.getId())
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @SuppressLint("NotifyDataSetChanged")
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                DocumentSnapshot documentSnapshot = task.getResult();
                                                productColorData = new ArrayList<>();
                                                productSizeData = new ArrayList<>();

                                                //處理顏色資料
                                                String colorResult = documentSnapshot.getString("colorString");
                                                String[] colorArr1 = colorResult.split(";");
                                                for (String colorStr : colorArr1) {
                                                    String[] colorArr2 = colorStr.split(",");
                                                    ProductColorData colorData = new ProductColorData(colorArr2[0], colorArr2[1]);
                                                    productColorData.add(colorData);
                                                }
                                                //處理數量資料
                                                String quantityResult = documentSnapshot.getString("quantityString");
                                                String[] quantityArr1 = quantityResult.split(";");
                                                for (String quantityStr : quantityArr1) {
                                                    String[] quantityArr2 = quantityStr.split(",");
                                                    ProductSizeData sizeData = new ProductSizeData(quantityArr2[0], quantityArr2[1], quantityArr2[2]);
                                                    productSizeData.add(sizeData);
                                                }

                                                ProductData product_data = new ProductData(
                                                        documentSnapshot.getId(),
                                                        documentSnapshot.getString("product_Name"),
                                                        documentSnapshot.getString("product_Detail"),
                                                        documentSnapshot.getString("product_Size"),
                                                        documentSnapshot.getString("product_Price"),
                                                        documentSnapshot.getString("product_Quantity"),
                                                        documentSnapshot.getString("product_Category"),
                                                        documentSnapshot.getString("accessories_Category"),
                                                        documentSnapshot.getString("seller_Name"),
                                                        documentSnapshot.getString("seller_ID"),
                                                        documentSnapshot.getString("product_Image_URL"),
                                                        documentSnapshot.getString("product_status"),
                                                        documentSnapshot.getString("set_Data_Time"),
                                                        productColorData,
                                                        productSizeData);

                                                productsData.add(product_data); //將物件'product_data'加到'Product_Data'型別的arraylist
                                                myFavoriteAdapter.notifyDataSetChanged();
                                            }
                                        });
                            }
                            myFavoriteAdapter.set_FavoriteState_Array(resultCount);
                        }
                    }
                });
    }

    protected View.OnClickListener backListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    @Override
    public void onItemClick(ProductData productData, ArrayList<ProductColorData> colorData, ArrayList<ProductSizeData> sizeData) {

        //處理顏色資料
        StringBuilder colorResult = new StringBuilder();
        for (ProductColorData data : colorData) {
            colorResult.append(data.product_color).append(",").append(data.product_image_url).append(";");
        }
        //處理數量資料
        StringBuilder quantityResult = new StringBuilder();
        for (ProductSizeData data : sizeData) {
            quantityResult.append(data.product_color).append(",").append(data.product_size).append(",").append(data.product_quantity).append(";");
        }

        Intent intent = new Intent(MyFavoriteProducts.this, SingleProductView.class);
        intent.putExtra("ProductData", productData);
        intent.putExtra("colorString", colorResult.toString());
        intent.putExtra("quantityString", quantityResult.toString());
        startActivity(intent);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onFavoriteClick(ProductData product_data) {
        DB.collection("user_Information").document(USER.getCurrentUser().getUid()).collection("Shop_MyFavorite_Products")
                .whereEqualTo("productID", product_data.product_ID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @SuppressLint("SimpleDateFormat")
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && !task.getResult().isEmpty()) {
                            DB.collection("user_Information").document(USER.getCurrentUser().getUid())
                                    .collection("Shop_MyFavorite_Products").document(product_data.product_ID)
                                    .delete();
                            Toast.makeText(MyFavoriteProducts.this, "已將 " + product_data.product_Name + " 從收藏商品移除", Toast.LENGTH_SHORT).show();
                        } else {
                            Map<String, Object> favoriteData = new HashMap<>();
                            favoriteData.put("productID", product_data.product_ID);
                            favoriteData.put("productName", product_data.product_Name);
                            favoriteData.put("setDataTime", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()));

                            DB.collection("user_Information").document(USER.getCurrentUser().getUid())
                                    .collection("Shop_MyFavorite_Products").document(product_data.product_ID).set(favoriteData);
                            Toast.makeText(MyFavoriteProducts.this, "成功將 " + product_data.product_Name + " 加入收藏商品", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }
}