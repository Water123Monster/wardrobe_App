package com.example.hamigua.shop.seller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.hamigua.R;
import com.example.hamigua.shop.adapter.ShopProductsRecyclerViewAdapter;
import com.example.hamigua.shop.customer.Shop;
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

import java.util.ArrayList;

public class SellerStore extends AppCompatActivity implements ShopProductsRecyclerViewAdapter.OnItemClickListener{

    TextView sellerName;
    ImageView sellerImage;
    RecyclerView testRecyclerview;

    ArrayList<ProductData> productsData; //存放商品資料的動態陣列
    ArrayList<ProductColorData> productColorData;
    ArrayList<ProductSizeData> productSizeData;
    ShopProductsRecyclerViewAdapter productsAdapter; //商品RecyclerView類別物件

    FirebaseFirestore DB;
    FirebaseAuth USER;

    String sellerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_seller_store);

        sellerName = findViewById(R.id.sellerName);
        sellerImage = findViewById(R.id.sellerImage);
        testRecyclerview = findViewById(R.id.testRecyclerview);

        init();
        get_Intent_Data();
        set_UI_Data();
        getProduct();
    }

    protected void init() {
        //取得firestore與authentication的連接
        DB = FirebaseFirestore.getInstance();
        USER = FirebaseAuth.getInstance();
        productsData = new ArrayList<>();
    }

    protected void get_Intent_Data() {
        Intent intent = getIntent();
        sellerID = intent.getStringExtra("OnClickedSeller");
    }

    protected void set_UI_Data() {

        DB.collection("user_Information").document(sellerID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()) {
                                //設定賣家名稱
                                sellerName.setText(document.getString("userName"));
                                //設定賣家大頭照
                                Glide.with(SellerStore.this).load(document.getString("userImage")).into(sellerImage);
                            }
                        }
                    }
                });

    }

    //從firestore讀取已上架的商品，放進recyclerview
    protected void getProduct() {
        //設定要讀取的資料路徑
        DB.collection("products_Information")
                .whereEqualTo("seller_ID", sellerID)
                .get()
                //如果讀取資料成功
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        //讀取商品屬性，並傳進'Product_Data'類別的物件'product_data'
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){

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

                                ProductData product_data = new ProductData(
                                        document.getId(),
                                        document.get("product_Name").toString(),
                                        document.get("product_Detail").toString(),
                                        document.get("product_Size").toString(),
                                        document.get("product_Price").toString(),
                                        document.get("product_Quantity").toString(),
                                        document.get("product_Category").toString(),
                                        document.get("accessories_Category").toString(),
                                        document.get("seller_Name").toString(),
                                        document.get("seller_ID").toString(),
                                        document.get("product_Image_URL").toString(),
                                        document.get("product_status").toString(),
                                        document.get("set_Data_Time").toString(),
                                        productColorData,
                                        productSizeData);
                                productsData.add(product_data); //將物件'product_data'加到'Product_Data'型別的arraylist
                            }

                            //如果'Product_Data'型別的arraylist的長度為0
                            if(productsData.size() == 0) {
                                Toast.makeText(SellerStore.this, "還沒有任何一項商品喔", Toast.LENGTH_SHORT).show();
                            }
                            productsAdapter = new ShopProductsRecyclerViewAdapter(SellerStore.this, productsData, SellerStore.this); //將陣列傳到商品RecyclerView類別的建構方法

                            //用GridLayoutManager指定RecyclerView中每列的顯示方式：縱向、2個物件為一列
                            GridLayoutManager gridLayoutManager = new GridLayoutManager(SellerStore.this, 2, GridLayoutManager.VERTICAL, false);
                            testRecyclerview.setLayoutManager(gridLayoutManager);
                            testRecyclerview.setAdapter(productsAdapter);
                        }
                    }
                });
    }

    @Override
    public void onItemClick(ProductData productData, ArrayList<ProductColorData> colorData, ArrayList<ProductSizeData> sizeData) {

    }
}