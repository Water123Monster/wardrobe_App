package com.example.hamigua.shop.customer;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamigua.R;
import com.example.hamigua.shop.adapter.ShopProductColorRecyclerViewAdapter;
import com.example.hamigua.shop.adapter.ShopTryOnRecyclerViewAdapter;
import com.example.hamigua.shop.model.ProductColorData;
import com.example.hamigua.shop.seller.ChooseProductSize;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class TryOnDialog implements ShopTryOnRecyclerViewAdapter.OnItemClickListener{

    Activity activity;
    AlertDialog dialog;
    String productName;
    String productCategory;
    ArrayList<ProductColorData> colorDataExceptCover;

    ShopTryOnRecyclerViewAdapter adapter;

    View view;
    RecyclerView recyclerView;

    FirebaseFirestore DB;
    FirebaseUser USER;

    public TryOnDialog(Activity activity, ArrayList<ProductColorData> colorDataExceptCover, String productName, String productCategory) {
        this.activity = activity;
        this.colorDataExceptCover = colorDataExceptCover;
        this.productName = productName;
        this.productCategory = productCategory;
        init();
    }

    public void start_TryOn_Dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);

        dialog = builder.create();
        dialog.show();
    }

    public void dismiss_Dialog() {
        dialog.dismiss();
    }

    public void init() {
        LayoutInflater inflater = activity.getLayoutInflater();
        view = inflater.inflate(R.layout.custom_shop_tryon_dialog, null);

        recyclerView = view.findViewById(R.id.tryOnRecyclerview);
        adapter = new ShopTryOnRecyclerViewAdapter(activity, colorDataExceptCover, productCategory, TryOnDialog.this);
        GridLayoutManager manager = new GridLayoutManager(activity, 1, GridLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        DB = FirebaseFirestore.getInstance();
        USER = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public void onImageClick(ProductColorData productColorData, String productCategory) {

        //將string型別的圖片輸入資料放進hashmap
        Map<String, Object> productData = new HashMap<>();
        productData.put("clothes_Image_Name", productName);
        productData.put("clothes_Category", productCategory);
        productData.put("accessories_Category", null);
        productData.put("clothes_Image_Hashtag1", "shop");
        productData.put("clothes_Image_Hashtag2", "null");
        productData.put("clothes_Image_Hashtag3", "null");
        productData.put("clothes_Image_URL", productColorData.product_image_url);
        productData.put("set_Data_Time", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()));
        productData.put("index", "10");
        productData.put("favorite_Status", "No");
        productData.put("privacy_Status", "locked");

        productData.put("H", "228");
        productData.put("S", "2");
        productData.put("V", "97");
        productData.put("length", "S");
        productData.put("width", "B");

        DB.collection("user_Information").document(USER.getUid()).collection("Clothes")
                .document(productName)
                .set(productData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(activity, "已將商品 " + productName + " 匯入衣櫃", Toast.LENGTH_SHORT).show();
                        dismiss_Dialog();
                    }
                });
    }
}
