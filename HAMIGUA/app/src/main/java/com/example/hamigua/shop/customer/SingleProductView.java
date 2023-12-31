package com.example.hamigua.shop.customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.hamigua.R;
import com.example.hamigua.SetContainedButtonStyle;
import com.example.hamigua.SetOutlinedButtonStyle;
import com.example.hamigua.shop.adapter.ShopSingleProductChooseColorAdapter;
import com.example.hamigua.shop.adapter.ShopSingleProductRecommendProductsAdapter;
import com.example.hamigua.shop.adapter.ShopSingleProductViewPagerAdapter;
import com.example.hamigua.shop.adapter.ShopSingleProductViewPagerTintAdapter;
import com.example.hamigua.shop.adapter.ShopSingleProductSellerOthersProductsAdapter;
import com.example.hamigua.shop.chat.ChatScreen;
import com.example.hamigua.shop.eventbus.CartCountEvent;
import com.example.hamigua.shop.model.CartData;
import com.example.hamigua.shop.model.ChatUserData;
import com.example.hamigua.shop.model.ProductColorData;
import com.example.hamigua.shop.model.ProductData;
import com.example.hamigua.shop.model.ProductSizeData;
import com.example.hamigua.shop.recommendAlgorithm.jaccard.JaccardSimilarity;
import com.example.hamigua.shop.seller.SellerStore;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nex3z.notificationbadge.NotificationBadge;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

//ViewPager2教學：https://codertw.com/%E7%A8%8B%E5%BC%8F%E8%AA%9E%E8%A8%80/712473/

public class SingleProductView extends AppCompatActivity
        implements ShopSingleProductViewPagerTintAdapter.OnItemClickListener,
        ShopSingleProductSellerOthersProductsAdapter.OnItemClickListener,
        ShopSingleProductRecommendProductsAdapter.OnItemClickListener {

    private FirebaseFirestore DB;
    private FirebaseAuth USER;

    private ViewPager2 imageViewPager2;
    private ImageView open_cart, singleView_back, singleView_favorite, sellerImage, singleView_dressing;
    private TextView colorImageName, colorImageIndex;
    private TextView productName, productPrice, chooseSizeTint;
    private TextView sellerName, sellerProductsCount, productDetail;
    private ConstraintLayout chat_with_seller, add_product_to_cart, chooseSize, viewPagerTint;
    private MaterialButton directly_buy_product, getSellerStore;
    private NotificationBadge cartBadge;
    private RecyclerView colorRecyclerView, viewPagerTintRecyclerView, sellerOthersProductsRecyclerView;
    private RecyclerView recommendProductsRecyclerView;
    private TextView rating_grade, sold_count;
    private ImageView ratingStar_1, ratingStar_2, ratingStar_3, ratingStar_4, ratingStar_5;
    private ImageView[] ratingStar;

    private BottomSheetDialog selectSizeDialog;

    ProductData productData;
    String colorString, quantityString;

    private ArrayList<ProductColorData> colorData;
    private ArrayList<ProductSizeData> sizeData;
    private ArrayList<ProductColorData> colorDataExceptCover;
    private ArrayList<ProductData> othersProductsData;
    private ArrayList<ProductColorData> othersProductColorData;
    private ArrayList<ProductSizeData> othersProductSizeData;

    ShopSingleProductViewPagerAdapter viewPagerAdapter;
    ShopSingleProductViewPagerTintAdapter viewPagerTintAdapter;
    ShopSingleProductChooseColorAdapter chooseColorAdapter;
    ShopSingleProductSellerOthersProductsAdapter othersProductsAdapter;
    ShopSingleProductRecommendProductsAdapter recommendProductsAdapter;

    private SetContainedButtonStyle containedButtonStyle;
    private SetOutlinedButtonStyle outlinedButtonStyle;

    private int cartCount = 0;
    private int productsCount = 0;

    //商品評價、售出數量
    int soldCount = 0;
    int ratingSum = 0;
    double ratingAvg;

    private final int REQUEST_ADD_TO_CART = 1; //加入購物車
    private final int REQUEST_DIRECTLY_BUY = 2; //直接購買
    private final int REQUEST_CHOOSE_SIZE = 3; //選擇商品選項

    //演算法用參數
    String tempBuyerID = null;
    ArrayList<String> productIDs;
    HashMap<String, ArrayList<String>> purchaseList = new HashMap<>();
    ArrayList<ProductData> recommendProductsData;
    ArrayList<ProductColorData> recommendProductColorData;
    ArrayList<ProductSizeData> recommendProductSizeData;

    //試衣選擇顏色dialog
    TryOnDialog tryOnDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_single_product_view);

        imageViewPager2 = findViewById(R.id.imageViewPager2);
        imageViewPager2.registerOnPageChangeCallback(viewPagerCallback);
        open_cart = findViewById(R.id.open_cart);
        open_cart.setOnClickListener(openCartListener);
        singleView_back = findViewById(R.id.searchResult_back);
        singleView_back.setOnClickListener(closeActivityListener);
        singleView_favorite = findViewById(R.id.singleView_favorite);
        singleView_favorite.setOnClickListener(favoriteListener);
        cartBadge = findViewById(R.id.cart_badge);
        colorImageName = findViewById(R.id.colorImageName);
        colorImageIndex = findViewById(R.id.colorImageIndex);

        productName = findViewById(R.id.singleView_product_name);
        productPrice = findViewById(R.id.singleView_product_price);
        chooseSizeTint = findViewById(R.id.choose_size_tint);

        chooseSize = findViewById(R.id.third_layout);
        chooseSize.setOnClickListener(chooseSizeListener);
        chat_with_seller = findViewById(R.id.chat_with_seller);
        chat_with_seller.setOnClickListener(chatListener);
        add_product_to_cart = findViewById(R.id.add_product_to_cart);
        add_product_to_cart.setOnClickListener(cartListener);
        directly_buy_product = findViewById(R.id.directly_buy_product);
        directly_buy_product.setOnClickListener(directlyBuyListener);

        colorRecyclerView = findViewById(R.id.choose_size_recyclerView);
        viewPagerTintRecyclerView = findViewById(R.id.viewPagerTintRecyclerView);
        viewPagerTint = findViewById(R.id.viewPagerTint);

        sellerImage = findViewById(R.id.sellerImage);
        sellerName = findViewById(R.id.sellerName);
        sellerProductsCount = findViewById(R.id.sellerProductsCount);
        sellerOthersProductsRecyclerView = findViewById(R.id.sellerOthersProductsRecyclerView);

        recommendProductsRecyclerView = findViewById(R.id.recommendProductsRecyclerView);

        productDetail = findViewById(R.id.productDetail);

        singleView_dressing = findViewById(R.id.singleView_dressing);
        singleView_dressing.setOnClickListener(tryOnListener);

        getSellerStore = findViewById(R.id.getSellerStore);
        getSellerStore.setOnClickListener(sellerStoreListener);

        rating_grade = findViewById(R.id.rating_grade);
        sold_count = findViewById(R.id.sold_count);
        ratingStar_1 = findViewById(R.id.ratingStar_1);
        ratingStar_2 = findViewById(R.id.ratingStar_2);
        ratingStar_3 = findViewById(R.id.ratingStar_3);
        ratingStar_4 = findViewById(R.id.ratingStar_4);
        ratingStar_5 = findViewById(R.id.ratingStar_5);
        ratingStar = new ImageView[] {ratingStar_1, ratingStar_2, ratingStar_3, ratingStar_4, ratingStar_5};

        init();
        get_Intent_Data();
        set_Data_Usable();
        set_Data_On_UI_Item();
        set_ViewPager_Adapter();
        set_ViewPagerTint_Adapter();
        set_ChooseColor_Adapter();
        get_DB_Cart_Count();
        check_DB_Favorite_Product();
        get_DB_Seller_Others_Products();
        check_DB_PurchaseList();
    }

    //EventBus教學：https://ithelp.ithome.com.tw/articles/10188117
    @Override
    protected void onStart() {
        super.onStart();
        // 在此Activity註冊啟用EventBus
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 在Activity取消註冊停用EventBus，讓Subscribe停止接收
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onCartCountEvent(CartCountEvent event) {
        // 收到CartCountEvent時要做的事寫在這裡
        cartBadge.setNumber(event.getCartCount());
    }

    protected void init() {
        colorData = new ArrayList<>();
        sizeData = new ArrayList<>();
        colorDataExceptCover = new ArrayList<>();
        othersProductsData = new ArrayList<>();
        recommendProductsData = new ArrayList<>();
        DB = FirebaseFirestore.getInstance();
        USER = FirebaseAuth.getInstance();
        selectSizeDialog = new BottomSheetDialog(this);
    }

    protected void get_Intent_Data() {
        Intent intent = getIntent();
        productData = intent.getParcelableExtra("ProductData");
        colorString = intent.getStringExtra("colorString");
        quantityString = intent.getStringExtra("quantityString");
    }

    protected void set_Data_Usable() {
        colorData.add(new ProductColorData(null, productData.product_Image_URL)); //先將商品封面加入arraylist
        //處理顏色資料
        String[] colorArr1 = colorString.split(";");
        for (String colorStr : colorArr1) {
            String[] colorArr2 = colorStr.split(",");
            ProductColorData data = new ProductColorData(colorArr2[0], colorArr2[1]);
            colorData.add(data);
            colorDataExceptCover.add(data);
        }

        //處理數量資料
        String[] quantityArr1 = quantityString.split(";");
        for (String quantityStr : quantityArr1) {
            String[] quantityArr2 = quantityStr.split(",");
            ProductSizeData data = new ProductSizeData(quantityArr2[0], quantityArr2[1], quantityArr2[2]);
            sizeData.add(data);
        }
    }

    @SuppressLint("SetTextI18n")
    protected void set_Data_On_UI_Item() {
        //設定商品封面index
        String indexResult = 1 + "/" + colorData.size();
        colorImageIndex.setText(indexResult);

        //設定商品名稱
        productName.setText(productData.product_Name);

        //設定商品金額
        productPrice.setText("$" + productData.product_Price);

        //設定選擇商品選項提示
        chooseSizeTint.setText("選擇商品選項 (" + (colorData.size() - 1) + " 顏色, " + sizeData.size() / (colorData.size() - 1) + " 尺寸)");

        //設定賣家名稱/賣場名稱
        sellerName.setText(productData.seller_Name);

        //設定賣家大頭照
        DB.collection("user_Information").document(productData.seller_ID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            Glide.with(SingleProductView.this).load(document.getString("userImage")).into(sellerImage);
                        }
                    }
                });

        //設定賣家/賣場已上架商品數量
        DB.collection("products_Information")
                .whereEqualTo("seller_ID", productData.seller_ID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && task.getResult() != null) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                productsCount += 1;
                            }
                            sellerProductsCount.setText(String.valueOf(productsCount));
                        }
                    }
                });

        //設定商品詳細資訊
        String[] arrDetail = productData.product_Detail.split("~");
        StringBuilder detailResult = new StringBuilder();
        for (String s : arrDetail) {
            if(s.equals(""))
                detailResult.append("\n");
            else {
                detailResult.append(s);
                detailResult.append("\n");
            }
        }
        productDetail.setText(detailResult.toString());

        //設定商品評價、售出數量
        DB.collection("shop_Products_Rating")
                .whereEqualTo("productID", productData.product_ID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && task.getResult() != null) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                soldCount += 1;
                                ratingSum += Integer.parseInt(Objects.requireNonNull(document.getString("ratingValue")));
                            }
                            DecimalFormat df = new DecimalFormat("###.0");
                            ratingAvg = (double) ratingSum / soldCount;

                            if(soldCount == 0) {
                                ratingAvg = 0;
                                rating_grade.setText(String.valueOf(0.0));
                            } else {
                                rating_grade.setText(df.format(ratingAvg));
                            }
                            sold_count.setText(String.valueOf(soldCount));

                            if(ratingAvg > 0 && ratingAvg < 1) {
                                double[] logic = {0.5, 0, 0, 0, 0};
                                set_RatingStar_On(logic, ratingStar);
                            }  else if(ratingAvg == 1) {
                                double[] logic = {1, 0, 0, 0, 0};
                                set_RatingStar_On(logic, ratingStar);
                            } else if(ratingAvg > 1 && ratingAvg < 2) {
                                double[] logic = {1, 0.5, 0, 0, 0};
                                set_RatingStar_On(logic, ratingStar);
                            } else if(ratingAvg == 2) {
                                double[] logic = {1, 1, 0, 0, 0};
                                set_RatingStar_On(logic, ratingStar);
                            } else if(ratingAvg > 2 && ratingAvg < 3) {
                                double[] logic = {1, 1, 0.5, 0, 0};
                                set_RatingStar_On(logic, ratingStar);
                            } else if(ratingAvg == 3) {
                                double[] logic = {1, 1, 1, 0, 0};
                                set_RatingStar_On(logic, ratingStar);
                            } else if(ratingAvg > 3 && ratingAvg < 4) {
                                double[] logic = {1, 1, 1, 0.5, 0};
                                set_RatingStar_On(logic, ratingStar);
                            } else if(ratingAvg == 4) {
                                double[] logic = {1, 1, 1, 1, 0};
                                set_RatingStar_On(logic, ratingStar);
                            } else if(ratingAvg > 4 && ratingAvg < 5) {
                                double[] logic = {1, 1, 1, 1, 0.5};
                                set_RatingStar_On(logic, ratingStar);
                            } else if(ratingAvg == 5) {
                                double[] logic = {1, 1, 1, 1, 1};
                                set_RatingStar_On(logic, ratingStar);
                            } else {
                                double[] logic = {0, 0, 0, 0, 0};
                                set_RatingStar_On(logic, ratingStar);
                            }
                        }
                    }
                });
    }

    protected void set_RatingStar_On(double[] logic, ImageView[] star) {
        for (int i = 0; i < 5; i++) {
            if(logic[i] == 0)
                star[i].setBackgroundResource(R.drawable.ic_round_star_border_24);
            else if(logic[i] == 0.5)
                star[i].setBackgroundResource(R.drawable.ic_round_star_half_24);
            else if(logic[i] == 1)
                star[i].setBackgroundResource(R.drawable.ic_round_star_24);
        }
    }

    protected void set_ViewPager_Adapter() {
        viewPagerAdapter = new ShopSingleProductViewPagerAdapter(SingleProductView.this, colorData);
        imageViewPager2.setAdapter(viewPagerAdapter);
        imageViewPager2.setClipToPadding(false);
        imageViewPager2.setClipChildren(false);
        imageViewPager2.setOffscreenPageLimit(2);
        imageViewPager2.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    protected void set_ViewPagerTint_Adapter() {
        viewPagerTintAdapter = new ShopSingleProductViewPagerTintAdapter(SingleProductView.this, colorDataExceptCover, SingleProductView.this);
        GridLayoutManager manager = new GridLayoutManager(SingleProductView.this, 1, GridLayoutManager.HORIZONTAL, false);
        viewPagerTintRecyclerView.setNestedScrollingEnabled(false);
        viewPagerTintRecyclerView.setLayoutManager(manager);
        viewPagerTintRecyclerView.setAdapter(viewPagerTintAdapter);
    }

    protected void set_ChooseColor_Adapter() {
        chooseColorAdapter = new ShopSingleProductChooseColorAdapter(SingleProductView.this, colorDataExceptCover);
        GridLayoutManager manager = new GridLayoutManager(SingleProductView.this, 1, GridLayoutManager.HORIZONTAL, false);
        colorRecyclerView.setNestedScrollingEnabled(false);
        colorRecyclerView.setLayoutManager(manager);
        colorRecyclerView.setAdapter(chooseColorAdapter);
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
                            cartBadge.setNumber(cartCount);
                        }
                    }
                });
    }

    protected void check_DB_Favorite_Product() {
        DB.collection("user_Information").document(USER.getCurrentUser().getUid()).collection("Shop_MyFavorite_Products")
                .whereEqualTo("productID", productData.product_ID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && !task.getResult().isEmpty())
                            singleView_favorite.setBackgroundResource(R.drawable.ic_baseline_favorite_24);
                        else
                            singleView_favorite.setBackgroundResource(R.drawable.ic_baseline_favorite_border_24);
                    }
                });
    }

    protected void get_DB_Seller_Others_Products() {
        DB.collection("products_Information")
                .whereEqualTo("seller_ID", productData.seller_ID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && task.getResult() != null) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                othersProductColorData = new ArrayList<>();
                                othersProductSizeData = new ArrayList<>();

                                //處理顏色資料
                                String colorResult = document.getString("colorString");
                                String[] colorArr1 = colorResult.split(";");
                                for (String colorStr : colorArr1) {
                                    String[] colorArr2 = colorStr.split(",");
                                    ProductColorData colorData = new ProductColorData(colorArr2[0], colorArr2[1]);
                                    othersProductColorData.add(colorData);
                                }
                                //處理數量資料
                                String quantityResult = document.getString("quantityString");
                                String[] quantityArr1 = quantityResult.split(";");
                                for (String quantityStr : quantityArr1) {
                                    String[] quantityArr2 = quantityStr.split(",");
                                    ProductSizeData sizeData = new ProductSizeData(quantityArr2[0], quantityArr2[1], quantityArr2[2]);
                                    othersProductSizeData.add(sizeData);
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
                                        othersProductColorData,
                                        othersProductSizeData);
                                othersProductsData.add(product_data); //將物件'product_data'加到'Product_Data'型別的arraylist
                            }
                            //如果'Product_Data'型別的arraylist的長度為0
                            if(othersProductsData.size() == 0) {
                                Toast.makeText(SingleProductView.this, "還沒有任何一項商品喔", Toast.LENGTH_SHORT).show();
                            }
                            othersProductsAdapter = new ShopSingleProductSellerOthersProductsAdapter(
                                    SingleProductView.this, othersProductsData, SingleProductView.this); //將陣列傳到商品RecyclerView類別的建構方法

                            //用GridLayoutManager指定RecyclerView中每列的顯示方式：縱向、2個物件為一列
                            GridLayoutManager gridLayoutManager = new GridLayoutManager(
                                    SingleProductView.this, 1, GridLayoutManager.HORIZONTAL, false);
                            sellerOthersProductsRecyclerView.setLayoutManager(gridLayoutManager);
                            sellerOthersProductsRecyclerView.setAdapter(othersProductsAdapter);
                        }
                    }
                });
    }

    protected void check_DB_PurchaseList() {
        DB.collection("shop_Products_Rating")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int temp = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.getString("buyerID").equals(USER.getCurrentUser().getUid())) {
                                    temp += 1;
                                }
                            }
                            if (temp == 0) {
                                recommendProductsRecyclerView.setVisibility(View.GONE);
                            } else {
                                get_DB_Recommend_Products();
                            }
                        }
                    }
                });
    }

    //推薦商品演算法
    protected void get_DB_Recommend_Products() {

        DB.collection("shop_Products_Rating")
                .orderBy("buyerID")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && task.getResult() != null) {
                            int documentCount = task.getResult().size();
                            int tempCount = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(tempCount == 0) { //第一筆購買紀錄
                                    tempBuyerID = document.getString("buyerID");
                                    productIDs = new ArrayList<>();
                                } else if(!tempBuyerID.equals(document.getString("buyerID"))) { //找到不同買家的購買紀錄
                                    productIDs = delete_Arraylist_Duplicate(productIDs); //刪除一使用者購買紀錄中重複的商品ID
                                    purchaseList.put(tempBuyerID, productIDs); //儲存前一位買家的購買紀錄
                                    tempBuyerID = document.getString("buyerID");
                                    productIDs = new ArrayList<>();
                                }
                                productIDs.add(document.getString("productID"));

                                tempCount += 1;
                                if(tempCount == documentCount) { //儲存最後一位買家的購買紀錄
                                    productIDs = delete_Arraylist_Duplicate(productIDs); //刪除一使用者購買紀錄中重複的商品ID
                                    purchaseList.put(tempBuyerID, productIDs);
                                }
                            }
                            JaccardSimilarity jaccard = new JaccardSimilarity(purchaseList);
                            jaccard.get_Recommend_Data();

                            //從firebase依照推薦數值做排序讀取資料，推薦數值為0的略過
                            DB.collection("products_Information")
                                    .orderBy("recommendValue", Query.Direction.ASCENDING)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @SuppressLint("NotifyDataSetChanged")
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if(task.isSuccessful() && task.getResult() != null) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    if(document.getString("recommendValue").equals("0"))
                                                        continue;
                                                    recommendProductColorData = new ArrayList<>();
                                                    recommendProductSizeData = new ArrayList<>();

                                                    //處理顏色資料
                                                    String colorResult = document.getString("colorString");
                                                    String[] colorArr1 = colorResult.split(";");
                                                    for (String colorStr : colorArr1) {
                                                        String[] colorArr2 = colorStr.split(",");
                                                        ProductColorData colorData = new ProductColorData(colorArr2[0], colorArr2[1]);
                                                        recommendProductColorData.add(colorData);
                                                    }
                                                    //處理數量資料
                                                    String quantityResult = document.getString("quantityString");
                                                    String[] quantityArr1 = quantityResult.split(";");
                                                    for (String quantityStr : quantityArr1) {
                                                        String[] quantityArr2 = quantityStr.split(",");
                                                        ProductSizeData sizeData = new ProductSizeData(quantityArr2[0], quantityArr2[1], quantityArr2[2]);
                                                        recommendProductSizeData.add(sizeData);
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
                                                            recommendProductColorData,
                                                            recommendProductSizeData);
                                                    recommendProductsData.add(product_data);
                                                }
                                                recommendProductsAdapter = new ShopSingleProductRecommendProductsAdapter(
                                                        SingleProductView.this, recommendProductsData, SingleProductView.this); //將陣列傳到商品RecyclerView類別的建構方法

                                                //用GridLayoutManager指定RecyclerView中每列的顯示方式：縱向、2個物件為一列
                                                GridLayoutManager gridLayoutManager = new GridLayoutManager(
                                                        SingleProductView.this, 2, GridLayoutManager.VERTICAL, false);
                                                recommendProductsRecyclerView.setLayoutManager(gridLayoutManager);
                                                recommendProductsRecyclerView.setAdapter(recommendProductsAdapter);
                                                recommendProductsRecyclerView.setNestedScrollingEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    //刪除一使用者購買紀錄中重複的商品ID
    protected ArrayList<String> delete_Arraylist_Duplicate(ArrayList<String> arrayList) {
        LinkedHashSet<String> hashSet = new LinkedHashSet<>(arrayList);
        return new ArrayList<>(hashSet);
    }

    protected View.OnClickListener closeActivityListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    protected View.OnClickListener openCartListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(SingleProductView.this, Cart.class);
            startActivity(intent);
        }
    };

    protected View.OnClickListener favoriteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DB.collection("user_Information").document(USER.getCurrentUser().getUid()).collection("Shop_MyFavorite_Products")
                    .whereEqualTo("productID", productData.product_ID)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @SuppressLint("SimpleDateFormat")
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful() && !task.getResult().isEmpty()) {
                                DB.collection("user_Information").document(USER.getCurrentUser().getUid())
                                        .collection("Shop_MyFavorite_Products").document(productData.product_ID)
                                        .delete();
                                singleView_favorite.setBackgroundResource(R.drawable.ic_baseline_favorite_border_24);
                                Toast.makeText(SingleProductView.this, "已將 " + productData.product_Name + " 從收藏商品移除", Toast.LENGTH_SHORT).show();
                            } else {
                                Map<String, Object> favoriteData = new HashMap<>();
                                favoriteData.put("productID", productData.product_ID);
                                favoriteData.put("productName", productData.product_Name);
                                favoriteData.put("setDataTime", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()));

                                DB.collection("user_Information").document(USER.getCurrentUser().getUid())
                                        .collection("Shop_MyFavorite_Products").document(productData.product_ID).set(favoriteData);
                                singleView_favorite.setBackgroundResource(R.drawable.ic_baseline_favorite_24);
                                Toast.makeText(SingleProductView.this, "成功將 " + productData.product_Name + " 加入收藏商品", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }
    };

    protected View.OnClickListener chatListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ChatUserData user = new ChatUserData(productData.seller_ID, productData.seller_Name, null, null);
            Intent intent = new Intent(SingleProductView.this, ChatScreen.class);
            intent.putExtra("OnClickedUser", user);
            startActivity(intent);
        }
    };

    protected View.OnClickListener sellerStoreListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(SingleProductView.this, SellerStore.class);
            intent.putExtra("OnClickedSeller", productData.seller_ID);
            startActivity(intent);
        }
    };

    protected View.OnClickListener tryOnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            tryOnDialog = new TryOnDialog(SingleProductView.this, colorDataExceptCover, productData.product_Name, productData.product_Category);
            tryOnDialog.start_TryOn_Dialog();
        }
    };

    protected View.OnClickListener chooseSizeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            create_Bottom_Dialog(REQUEST_CHOOSE_SIZE);
            selectSizeDialog.show();
        }
    };

    protected View.OnClickListener cartListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            create_Bottom_Dialog(REQUEST_ADD_TO_CART);
            selectSizeDialog.show();
        }
    };

    protected View.OnClickListener directlyBuyListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            create_Bottom_Dialog(REQUEST_DIRECTLY_BUY);
            selectSizeDialog.show();
        }
    };

    protected ViewPager2.OnPageChangeCallback viewPagerCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            //更改顏色提示，照片位置
            if(position == 0) {
                viewPagerTintRecyclerView.setVisibility(View.GONE);
                viewPagerTint.setVisibility(View.GONE);
                colorImageName.setVisibility(View.GONE);
            }
            else {
                for(int i = 0; i < colorDataExceptCover.size(); i++) {
                    viewPagerTintAdapter.set_ImageView_Stroke_Unselect(i);
                }
                viewPagerTintAdapter.set_ImageView_Stroke_Select(position - 1);

                viewPagerTintRecyclerView.setVisibility(View.VISIBLE);
                viewPagerTint.setVisibility(View.VISIBLE);
                colorImageName.setVisibility(View.VISIBLE);
                colorImageName.setText(colorData.get(position).product_color);
            }
            String indexResult = (position + 1) + "/" + colorData.size();
            colorImageIndex.setText(indexResult);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            super.onPageScrollStateChanged(state);
        }
    };

    @SuppressLint("SetTextI18n")
    protected void create_Bottom_Dialog(int REQUEST_STATE) {
            View view = null;
            if(REQUEST_STATE == REQUEST_CHOOSE_SIZE)
                view = getLayoutInflater().inflate(R.layout.select_product_size_bottom_dialog_another, null, false); //設定對話窗(dialog)的layout樣式
            else if(REQUEST_STATE == REQUEST_ADD_TO_CART || REQUEST_STATE == REQUEST_DIRECTLY_BUY)
                view = getLayoutInflater().inflate(R.layout.select_product_size_bottom_dialog, null, false); //設定對話窗(dialog)的layout樣式

        ArrayList<ProductColorData> tempColorData = new ArrayList<>();
        //處理顏色資料
        String[] colorArr1 = colorString.split(";");
        for (String colorStr : colorArr1) {
            String[] colorArr2 = colorStr.split(",");
            ProductColorData data = new ProductColorData(colorArr2[0], colorArr2[1]);
            tempColorData.add(data);
        }

        //暫時存放的購物車model，用於儲存商品規格前的UI顯示
        CartData tempData = new CartData(productData.product_ID, productData.product_Name, "", "", "1",
                productData.product_Price, productData.seller_Name, productData.seller_ID, productData.product_Image_URL,
                "", "", tempColorData, sizeData);

        //設定對話窗(dialog)的layout的物件id
        ImageView dialogFinish = view.findViewById(R.id.dialogFinish); //關閉對話窗imageView
        ImageView select_product_size_imageview = view.findViewById(R.id.select_product_size_imageview);
        ImageView select_product_quantity_minus = view.findViewById(R.id.select_product_quantity_minus);
        ImageView select_product_quantity_plus = view.findViewById(R.id.select_product_quantity_plus);
        TextView select_product_size_price = view.findViewById(R.id.select_product_size_price);
        TextView product_size_quantity = view.findViewById(R.id.rating_product_price);
        TextView cant_add_quantity_helper = view.findViewById(R.id.cant_add_quantity_helper);
        ChipGroup select_product_color_chipGroup = view.findViewById(R.id.select_product_color_chipGroup);
        ChipGroup select_product_size_chipGroup = view.findViewById(R.id.select_product_size_chipGroup);
        EditText select_product_quantity = view.findViewById(R.id.select_product_quantity);
        MaterialButton btn_select_product_save = view.findViewById(R.id.btn_select_product_save);
        MaterialButton btn_product_add_to_cart = view.findViewById(R.id.btn_product_add_to_cart);

        containedButtonStyle = new SetContainedButtonStyle(btn_select_product_save, SingleProductView.this);
        outlinedButtonStyle = new SetOutlinedButtonStyle(btn_product_add_to_cart);

        Glide.with(SingleProductView.this).load(productData.product_Image_URL).into(select_product_size_imageview);

        //設定顏色規格chip
        int colorChipIdIndex = 0;
        for (ProductColorData data : tempColorData) {
            LayoutInflater inflater = LayoutInflater.from(this);
            Chip chip = (Chip)inflater.inflate(R.layout.product_color_chip_item, null, false); //設定自訂chip樣式
            chip.setText(data.product_color); //設定chip文字
            chip.setCheckable(true); //設定chip可勾選
            chip.setClickable(true); //設定chip點擊
            chip.setId(colorChipIdIndex);
            select_product_color_chipGroup.addView(chip); //新增chip至顏色ChipGroup
            colorChipIdIndex += 1;
        }

        //設定顏色規格chipGroup的點擊事件，設置為單選模式(singleSelection = true)
        ArrayList<ProductColorData> finalTempColorData = tempColorData;
        select_product_color_chipGroup.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                if(checkedIds.size() != 0) {
                    int select = checkedIds.get(0);
                    Chip chip = (Chip) group.getChildAt(select);
                    for (ProductColorData data : finalTempColorData) {
                        if(data.product_color.contentEquals(chip.getText())) {
                            Glide.with(SingleProductView.this).load(data.product_image_url).into(select_product_size_imageview);
                            tempData.selectColor = (String) chip.getText();
                        }
                    }
                    for (ProductSizeData data : sizeData) {
                        if(data.product_color.contentEquals(chip.getText()) && data.product_size.equals(tempData.selectSize))
                            product_size_quantity.setText("商品數量：" + data.product_quantity);
                    }
                    if(!tempData.selectSize.equals("")) {
                        containedButtonStyle.setButtonClickable(); //設定button可使用樣式
                        if(REQUEST_STATE == REQUEST_CHOOSE_SIZE)
                            outlinedButtonStyle.setButtonClickable();
                    }

                } else {
                    containedButtonStyle.setButtonUnClickable(); //設定button無法使用樣式
                    if(REQUEST_STATE == REQUEST_CHOOSE_SIZE)
                        outlinedButtonStyle.setButtonUnClickable();
                    Glide.with(SingleProductView.this).load(productData.product_Image_URL).into(select_product_size_imageview);
                    tempData.selectColor = "";
                    product_size_quantity.setText("商品數量：");
                }
                select_product_quantity.setText("1");
                tempData.selectQuantity = "1";
                cant_add_quantity_helper.setVisibility(View.GONE);

            }
        });

        //設定尺寸規格chip
        int sizeIndex = 0;
        int sizeChipIdIndex = 0;
        for (ProductSizeData data : sizeData) {
            if(sizeIndex < sizeData.size() / tempColorData.size()) {
                LayoutInflater inflater = LayoutInflater.from(this);
                Chip chip = (Chip)inflater.inflate(R.layout.product_size_chip_item, null, false); //設定自訂chip樣式
                chip.setText(data.product_size); //設定chip文字
                chip.setCheckable(true); //設定chip可勾選
                chip.setClickable(true); //設定chip點擊
                chip.setId(sizeChipIdIndex);
                select_product_size_chipGroup.addView(chip); //新增chip至顏色ChipGroup
                sizeChipIdIndex += 1;
            }
            sizeIndex += 1;
        }

        //設定尺寸規格chipGroup的點擊事件，設置為單選模式(singleSelection = true)
        select_product_size_chipGroup.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                if(checkedIds.size() != 0) {
                    int select = checkedIds.get(0);
                    Chip chip = (Chip) group.getChildAt(select);
                    for (ProductSizeData data : sizeData) {
                        if(data.product_size.contentEquals(chip.getText()))
                            tempData.selectSize = (String) chip.getText();
                        if(data.product_color.equals(tempData.selectColor) && data.product_size.contentEquals(chip.getText())) {
                            product_size_quantity.setText("商品數量：" + data.product_quantity);
                            tempData.selectSize = (String) chip.getText();
                        }
                    }
                    if(!tempData.selectColor.equals("")) {
                        containedButtonStyle.setButtonClickable(); //設定button可使用樣式
                        if(REQUEST_STATE == REQUEST_CHOOSE_SIZE)
                            outlinedButtonStyle.setButtonClickable();
                    }
                    select_product_quantity.setText("1");
                    tempData.selectQuantity = "1";
                    cant_add_quantity_helper.setVisibility(View.GONE);
                } else {
                    containedButtonStyle.setButtonUnClickable(); //設定button無法使用樣式
                    if(REQUEST_STATE == REQUEST_CHOOSE_SIZE)
                        outlinedButtonStyle.setButtonUnClickable();
                    tempData.selectSize = "";
                    product_size_quantity.setText("商品數量：");
                    select_product_quantity.setText("1");
                    tempData.selectQuantity = "1";
                    cant_add_quantity_helper.setVisibility(View.GONE);
                }
            }
        });

        select_product_quantity_minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectQuantity = Integer.parseInt(tempData.selectQuantity);
                if(selectQuantity == 1) {
                    Toast.makeText(SingleProductView.this, "最少購買1件", Toast.LENGTH_SHORT).show();
                }
                else {
                    selectQuantity -= 1;
                    tempData.selectQuantity = String.valueOf(selectQuantity);
                    select_product_quantity.setText(tempData.selectQuantity);
                }
                cant_add_quantity_helper.setVisibility(View.GONE);
            }
        });

        select_product_quantity_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectQuantity = Integer.parseInt(tempData.selectQuantity);
                int quantity = 0;

                for (ProductSizeData data : sizeData) {
                    if(data.product_color.equals(tempData.selectColor) && data.product_size.equals(tempData.selectSize))
                        quantity = Integer.parseInt(data.product_quantity);
                }
                selectQuantity += 1;
                if(selectQuantity == quantity + 1) {
                    cant_add_quantity_helper.setVisibility(View.VISIBLE);
                }
                else {
                    tempData.selectQuantity = String.valueOf(selectQuantity);
                    select_product_quantity.setText(tempData.selectQuantity);
                }
            }
        });

        //關閉對話窗imageView的點擊事件
        dialogFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectSizeDialog.dismiss(); //關閉下方對話窗(dialog)
            }
        });

        //儲存更改過後的購買顏色、尺寸、數量，結束dialog並更新資料到DB
        btn_select_product_save.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                //處理顏色資料
                StringBuilder colorResult = new StringBuilder();
                for (ProductColorData data : tempColorData) {
                    colorResult.append(data.product_color).append(",").append(data.product_image_url).append(";");
                }
                //處理數量資料
                StringBuilder quantityResult = new StringBuilder();
                for (ProductSizeData data : sizeData) {
                    quantityResult.append(data.product_color).append(",").append(data.product_size).append(",").append(data.product_quantity).append(";");
                }

                Map<String, Object> cartData = new HashMap<>();
                cartData.put("productID", productData.getProduct_ID());
                cartData.put("productName", productData.getProduct_Name());
                cartData.put("selectColor", tempData.selectColor);
                cartData.put("selectSize", tempData.selectSize);
                cartData.put("selectQuantity", tempData.selectQuantity);
                cartData.put("productPrice", productData.getProduct_Price());
                cartData.put("sellerName", productData.getSeller_Name());
                cartData.put("sellerID", productData.getSeller_ID());
                cartData.put("productImageURL", productData.getProduct_Image_URL());
                cartData.put("setDataTime", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                if(REQUEST_STATE == REQUEST_DIRECTLY_BUY || REQUEST_STATE == REQUEST_CHOOSE_SIZE)
                    cartData.put("isChecked", "true");
                else if(REQUEST_STATE == REQUEST_ADD_TO_CART)
                    cartData.put("isChecked", "false");
                cartData.put("colorString", colorResult.toString());
                cartData.put("quantityString", quantityResult.toString());

                DB.collection("user_Information").document(USER.getCurrentUser().getUid()).collection("Shop_Cart")
                        .whereEqualTo("productID", productData.getProduct_ID())
                        .whereEqualTo("selectColor", tempData.selectColor)
                        .whereEqualTo("selectSize", tempData.selectSize)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful() && !task.getResult().isEmpty()) {
                                    DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                    String documentID = documentSnapshot.getId();

                                    Map<String, Object> updateData = new HashMap<>();
                                    updateData.put("selectQuantity", String.valueOf(Integer.parseInt(documentSnapshot.getString("selectQuantity")) + Integer.parseInt(tempData.selectQuantity)));
                                    if(REQUEST_STATE == REQUEST_DIRECTLY_BUY || REQUEST_STATE == REQUEST_CHOOSE_SIZE)
                                        updateData.put("isChecked", "true");
                                    else if(REQUEST_STATE == REQUEST_ADD_TO_CART)
                                        updateData.put("isChecked", "false");

                                    DB.collection("user_Information").document(USER.getCurrentUser().getUid()).collection("Shop_Cart")
                                            .document(documentID)
                                            .update(updateData);
                                    Toast.makeText(SingleProductView.this, "購物車已有該商品及所選規格，增加該商品購物車數量", Toast.LENGTH_SHORT).show();
                                } else {
                                    DB.collection("user_Information").document(USER.getCurrentUser().getUid()).collection("Shop_Cart")
                                            .add(cartData)
                                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                                    if(task.isSuccessful()) {
                                                        cartBadge.setNumber(cartCount += 1);
                                                        Toast.makeText(SingleProductView.this, "已將 " + productData.getProduct_Name() + " 加入購物車", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            }
                        });
                if(REQUEST_STATE == REQUEST_DIRECTLY_BUY || REQUEST_STATE == REQUEST_CHOOSE_SIZE) {
                    selectSizeDialog.dismiss();
                    Intent intent = new Intent(SingleProductView.this, Cart.class);
                    startActivity(intent);
                }
                selectSizeDialog.dismiss();
            }
        });

        if(REQUEST_STATE == REQUEST_CHOOSE_SIZE) {
            btn_product_add_to_cart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //處理顏色資料
                    StringBuilder colorResult = new StringBuilder();
                    for (ProductColorData data : tempColorData) {
                        colorResult.append(data.product_color).append(",").append(data.product_image_url).append(";");
                    }
                    //處理數量資料
                    StringBuilder quantityResult = new StringBuilder();
                    for (ProductSizeData data : sizeData) {
                        quantityResult.append(data.product_color).append(",").append(data.product_size).append(",").append(data.product_quantity).append(";");
                    }

                    Map<String, Object> cartData = new HashMap<>();
                    cartData.put("productID", productData.getProduct_ID());
                    cartData.put("productName", productData.getProduct_Name());
                    cartData.put("selectColor", tempData.selectColor);
                    cartData.put("selectSize", tempData.selectSize);
                    cartData.put("selectQuantity", tempData.selectQuantity);
                    cartData.put("productPrice", productData.getProduct_Price());
                    cartData.put("sellerName", productData.getSeller_Name());
                    cartData.put("sellerID", productData.getSeller_ID());
                    cartData.put("productImageURL", productData.getProduct_Image_URL());
                    cartData.put("setDataTime", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                    cartData.put("isChecked", "false");
                    cartData.put("colorString", colorResult.toString());
                    cartData.put("quantityString", quantityResult.toString());

                    DB.collection("user_Information").document(USER.getCurrentUser().getUid()).collection("Shop_Cart")
                            .whereEqualTo("productID", productData.getProduct_ID())
                            .whereEqualTo("selectColor", tempData.selectColor)
                            .whereEqualTo("selectSize", tempData.selectSize)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.isSuccessful() && !task.getResult().isEmpty()) {
                                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                        String documentID = documentSnapshot.getId();

                                        Map<String, Object> updateData = new HashMap<>();
                                        updateData.put("selectQuantity", String.valueOf(Integer.parseInt(documentSnapshot.getString("selectQuantity")) + Integer.parseInt(tempData.selectQuantity)));
                                        updateData.put("isChecked", "false");

                                        DB.collection("user_Information").document(USER.getCurrentUser().getUid()).collection("Shop_Cart")
                                                .document(documentID)
                                                .update(updateData);
                                        Toast.makeText(SingleProductView.this, "購物車已有該商品及所選規格，增加該商品購物車數量", Toast.LENGTH_SHORT).show();
                                    } else {
                                        DB.collection("user_Information").document(USER.getCurrentUser().getUid()).collection("Shop_Cart")
                                                .add(cartData)
                                                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                                        if(task.isSuccessful()) {
                                                            cartBadge.setNumber(cartCount += 1);
                                                            Toast.makeText(SingleProductView.this, "已將 " + productData.getProduct_Name() + " 加入購物車", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                }
                            });
                    selectSizeDialog.dismiss();
                }
            });
        }

        containedButtonStyle.setButtonUnClickable();
        if(REQUEST_STATE == REQUEST_CHOOSE_SIZE)
            outlinedButtonStyle.setButtonUnClickable();
        if(REQUEST_STATE == REQUEST_ADD_TO_CART)
            btn_select_product_save.setText("加入購物車");
        else if(REQUEST_STATE == REQUEST_DIRECTLY_BUY)
            btn_select_product_save.setText("直接購買");

        select_product_size_price.setText("$" + productData.product_Price);
        select_product_quantity.setText("1");
        select_product_quantity.setFocusable(false);
        selectSizeDialog.setContentView(view);
    }

    @Override
    public void onColorClick(int position) {
        imageViewPager2.setCurrentItem(position + 1);
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

        Intent intent = new Intent(SingleProductView.this, SingleProductView.class);
        intent.putExtra("ProductData", productData);
        intent.putExtra("colorString", colorResult.toString());
        intent.putExtra("quantityString", quantityResult.toString());
        startActivity(intent);
    }
}