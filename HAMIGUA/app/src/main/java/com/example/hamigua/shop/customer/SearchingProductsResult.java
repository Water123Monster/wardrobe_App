package com.example.hamigua.shop.customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hamigua.LoadingDialog;
import com.example.hamigua.R;
import com.example.hamigua.shop.adapter.ShopProductsRecyclerViewAdapter;
import com.example.hamigua.shop.model.ProductColorData;
import com.example.hamigua.shop.model.ProductData;
import com.example.hamigua.shop.model.ProductSizeData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nex3z.notificationbadge.NotificationBadge;

import java.util.ArrayList;

public class SearchingProductsResult extends AppCompatActivity implements ShopProductsRecyclerViewAdapter.OnItemClickListener {

    ImageView open_cart;

    ImageView searchResult_back;
    EditText productsSearch;
    RecyclerView searchResult_recyclerView;

    TabLayout filterTable;
    ConstraintLayout priceFilter, ratingFilter;
    EditText minimumPrice, maximumPrice;
    TextView priceFilterHelper;
    MaterialButton btnPriceFilter, btnPriceOrderBy;
    MaterialButton btnRatingStar_1, btnRatingStar_2, btnRatingStar_3, btnRatingStar_4, btnRatingStar_5;

    ArrayList<String> searchResult;
    ArrayList<ProductData> productsData; //存放商品資料的動態陣列
    ArrayList<ProductColorData> productColorData;
    ArrayList<ProductSizeData> productSizeData;
    ShopProductsRecyclerViewAdapter productsAdapter; //商品RecyclerView類別物件

    FirebaseFirestore DB;
    FirebaseAuth USER;

    LoadingDialog loadingDialog;

    NotificationBadge cart_badge;
    int cartCount = 0;

    private final int REQUEST_NEWER_FILTER = 0; //依照上傳時間新~舊排序
    private final int REQUEST_PRICE_FILTER = 1; //依照最小值、最大值篩選，不排序
    private final int REQUEST_RATING_FILTER = 2;
    private final int REQUEST_PRICE_ORDER_BY_ASCENDING = 3; //依照價格便宜~昂貴排序
    private final int REQUEST_PRICE_ORDER_BY_DESCENDING = 4; //依照價格昂貴~便宜排序
    private final int REQUEST_PRICE_ORDER_BY_ASCENDING_FILTER = 5; //依照最小值、最大值篩選，依照價格便宜~昂貴排序
    private final int REQUEST_PRICE_ORDER_BY_DESCENDING_FILTER = 6; //依照最小值、最大值篩選，依照價格昂貴~便宜排序

    final int PRICE_ORDER_BY_ASCENDING = 10;
    final int PRICE_ORDER_BY_DESCENDING = 11;
    int priceOrderByStatus = PRICE_ORDER_BY_DESCENDING;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_searching_products_result);

        searchResult_back = findViewById(R.id.searchResult_back);
        searchResult_back.setOnClickListener(backListener);
        productsSearch = findViewById(R.id.checkoutTitle);
        searchResult_recyclerView = findViewById(R.id.searchResult_recyclerView);

        priceFilter = findViewById(R.id.priceFilter);
        ratingFilter = findViewById(R.id.ratingFilter);
        minimumPrice = findViewById(R.id.minimumPrice);
        maximumPrice = findViewById(R.id.maximumPrice);
        priceFilterHelper = findViewById(R.id.priceFilterHelper);
        btnPriceFilter = findViewById(R.id.btnPriceFilter);
        btnPriceFilter.setOnClickListener(priceFilterListener);
        btnPriceOrderBy = findViewById(R.id.btnPriceOrderBy);
        btnPriceOrderBy.setOnClickListener(priceOrderByListener);
        btnRatingStar_1 = findViewById(R.id.btnRatingStar_1);
        btnRatingStar_2 = findViewById(R.id.ratingStar_2);
        btnRatingStar_3 = findViewById(R.id.ratingStar_3);
        btnRatingStar_4 = findViewById(R.id.ratingStar_4);
        btnRatingStar_5 = findViewById(R.id.ratingStar_5);
        open_cart = findViewById(R.id.open_cart);
        open_cart.setOnClickListener(cartListener);
        cart_badge = findViewById(R.id.cart_badge);

        init();
        get_Search_Result();
        get_DB_Searching_Result(REQUEST_NEWER_FILTER);
        get_DB_Cart_Count();

        filterTable = findViewById(R.id.fragmentTable);
        filterTable.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition() == 0) {
                    set_PriceFilter_Gone();
                    set_RatingFilter_Gone();
                    minimumPrice.setText("");
                    maximumPrice.setText("");
                    get_DB_Searching_Result(REQUEST_NEWER_FILTER);
                } else if(tab.getPosition() == 1) {
                    set_PriceFilter_Visible();
                    set_RatingFilter_Gone();
                } else if(tab.getPosition() == 2) {
                    set_RatingFilter_Visible();
                    set_PriceFilter_Gone();
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    protected void init() {
        searchResult = new ArrayList<>();
        DB = FirebaseFirestore.getInstance();
        USER = FirebaseAuth.getInstance();
        loadingDialog = new LoadingDialog(SearchingProductsResult.this);
    }

    protected void get_Search_Result() {
        Intent intent = getIntent();
        searchResult = intent.getStringArrayListExtra("searchResult");
        productsSearch.setText(intent.getStringExtra("searchString"));
    }

    protected void get_DB_Searching_Result(int REQUEST_CODE) {

        OnCompleteListener<QuerySnapshot> listener = new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    productsData = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
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
                                document.getString("product_Name"),
                                document.getString("product_Detail"),
                                document.getString("product_Size"),
                                document.getString("product_Price"),
                                document.getString("product_Quantity"),
                                document.getString("product_Category"),
                                document.getString("accessories_Category"),
                                document.getString("seller_Name"),
                                document.getString("seller_ID"),
                                document.getString("product_Image_URL"),
                                document.getString("product_status"),
                                document.getString("set_Data_Time"),
                                productColorData,
                                productSizeData);

                        productsData.add(product_data); //將物件'product_data'加到'Product_Data'型別的arraylist
                    }
                    productsAdapter = new ShopProductsRecyclerViewAdapter(
                            SearchingProductsResult.this,
                            productsData,
                            SearchingProductsResult.this); //將陣列傳到商品RecyclerView類別的建構方法
                    //用GridLayoutManager指定RecyclerView中每列的顯示方式：縱向、2個物件為一列
                    GridLayoutManager gridLayoutManager = new GridLayoutManager(
                            SearchingProductsResult.this, 2, GridLayoutManager.VERTICAL, false);
                    searchResult_recyclerView.setLayoutManager(gridLayoutManager);
                    searchResult_recyclerView.setAdapter(productsAdapter);
                }
            }
        };

        if(REQUEST_CODE == REQUEST_NEWER_FILTER) {
            DB.collection("products_Information")
                    .whereEqualTo("searching_State", "true")
                    .orderBy("set_Data_Time", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(listener);
        } else if(REQUEST_CODE == REQUEST_PRICE_FILTER) {
            DB.collection("products_Information")
                    .whereEqualTo("searching_State", "true")
                    .whereGreaterThanOrEqualTo("product_Price", minimumPrice.getText().toString())
                    .whereLessThanOrEqualTo("product_Price", maximumPrice.getText().toString())
                    .get()
                    .addOnCompleteListener(listener);
        } else if(REQUEST_CODE == REQUEST_RATING_FILTER) {

        } else if(REQUEST_CODE == REQUEST_PRICE_ORDER_BY_ASCENDING) {
            DB.collection("products_Information")
                    .whereEqualTo("searching_State", "true")
                    .orderBy("product_Price", Query.Direction.ASCENDING)
                    .get()
                    .addOnCompleteListener(listener);
        } else if(REQUEST_CODE == REQUEST_PRICE_ORDER_BY_DESCENDING) {
            DB.collection("products_Information")
                    .whereEqualTo("searching_State", "true")
                    .orderBy("product_Price", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(listener);
        } else if(REQUEST_CODE == REQUEST_PRICE_ORDER_BY_ASCENDING_FILTER) {
            DB.collection("products_Information")
                    .whereEqualTo("searching_State", "true")
                    .whereGreaterThanOrEqualTo("product_Price", minimumPrice.getText().toString())
                    .whereLessThanOrEqualTo("product_Price", maximumPrice.getText().toString())
                    .orderBy("product_Price", Query.Direction.ASCENDING)
                    .get()
                    .addOnCompleteListener(listener);
        } else if(REQUEST_CODE == REQUEST_PRICE_ORDER_BY_DESCENDING_FILTER) {
            DB.collection("products_Information")
                    .whereEqualTo("searching_State", "true")
                    .whereGreaterThanOrEqualTo("product_Price", minimumPrice.getText().toString())
                    .whereLessThanOrEqualTo("product_Price", maximumPrice.getText().toString())
                    .orderBy("product_Price", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(listener);
        }
    }

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

    protected View.OnClickListener priceFilterListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(minimumPrice.getText().toString().equals("") || maximumPrice.getText().toString().equals("")) {
                Toast.makeText(SearchingProductsResult.this, "請輸入最小值與最大值再篩選", Toast.LENGTH_SHORT).show();
            } else {

                InputMethodManager manager = ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE));
                if (manager != null) {
                    manager.hideSoftInputFromWindow(SearchingProductsResult.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS); //關閉鍵盤
                }

                minimumPrice.clearFocus();
                maximumPrice.clearFocus();
                loadingDialog.start_Loading_Dialog();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        get_DB_Searching_Result(REQUEST_PRICE_FILTER);
                        loadingDialog.dismiss_Dialog();
                    }
                }, 500);
            }
        }
    };

    protected View.OnClickListener priceOrderByListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(priceOrderByStatus == PRICE_ORDER_BY_DESCENDING) {
                priceOrderByStatus = PRICE_ORDER_BY_ASCENDING;
                btnPriceOrderBy.setText("由高至低");

                if(minimumPrice.getText().toString().isEmpty() || maximumPrice.getText().toString().isEmpty()) {
                    get_DB_Searching_Result(REQUEST_PRICE_ORDER_BY_ASCENDING);
                } else {
                    get_DB_Searching_Result(REQUEST_PRICE_ORDER_BY_ASCENDING_FILTER);
                }

            } else {
                priceOrderByStatus = PRICE_ORDER_BY_DESCENDING;
                btnPriceOrderBy.setText("由低至高");

                if(minimumPrice.getText().toString().isEmpty() || maximumPrice.getText().toString().isEmpty()) {
                    get_DB_Searching_Result(REQUEST_PRICE_ORDER_BY_DESCENDING);
                } else {
                    get_DB_Searching_Result(REQUEST_PRICE_ORDER_BY_DESCENDING_FILTER);
                }
            }
        }
    };

    protected void set_DB_Searching_State_No() {
        for (String documentID : searchResult) {
            DB.collection("products_Information").document(documentID)
                    .update("searching_State", "false");
        }
    }

    protected View.OnClickListener backListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            set_DB_Searching_State_No();
            finish();
        }
    };

    //開啟電商購物車
    protected View.OnClickListener cartListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(SearchingProductsResult.this, Cart.class);
            startActivity(intent);
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        set_DB_Searching_State_No();
    }


    protected void set_PriceFilter_Visible() {
        priceFilter.setVisibility(View.VISIBLE);
        minimumPrice.setVisibility(View.VISIBLE);
        maximumPrice.setVisibility(View.VISIBLE);
        priceFilterHelper.setVisibility(View.VISIBLE);
        btnPriceFilter.setVisibility(View.VISIBLE);
    }

    protected void set_PriceFilter_Gone() {
        minimumPrice.setVisibility(View.GONE);
        maximumPrice.setVisibility(View.GONE);
        priceFilterHelper.setVisibility(View.GONE);
        btnPriceFilter.setVisibility(View.GONE);
        priceFilter.setVisibility(View.GONE);
    }

    protected void set_RatingFilter_Visible() {
        ratingFilter.setVisibility(View.VISIBLE);
        btnRatingStar_1.setVisibility(View.VISIBLE);
        btnRatingStar_2.setVisibility(View.VISIBLE);
        btnRatingStar_3.setVisibility(View.VISIBLE);
        btnRatingStar_4.setVisibility(View.VISIBLE);
        btnRatingStar_5.setVisibility(View.VISIBLE);
    }

    protected void set_RatingFilter_Gone() {
        btnRatingStar_1.setVisibility(View.GONE);
        btnRatingStar_2.setVisibility(View.GONE);
        btnRatingStar_3.setVisibility(View.GONE);
        btnRatingStar_4.setVisibility(View.GONE);
        btnRatingStar_5.setVisibility(View.GONE);
        ratingFilter.setVisibility(View.GONE);
    }

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

        Intent intent = new Intent(SearchingProductsResult.this, SingleProductView.class);
        intent.putExtra("ProductData", productData);
        intent.putExtra("colorString", colorResult.toString());
        intent.putExtra("quantityString", quantityResult.toString());
        startActivity(intent);
    }
}