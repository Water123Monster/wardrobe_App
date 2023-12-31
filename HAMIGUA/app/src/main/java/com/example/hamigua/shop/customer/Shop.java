package com.example.hamigua.shop.customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.hamigua.HomePage;
import com.example.hamigua.R;
import com.example.hamigua.shop.adapter.ShopProductsRecyclerViewAdapter;
import com.example.hamigua.communicate.Communicate;
import com.example.hamigua.runway.Runway;
import com.example.hamigua.shop.adapter.ShopSingleProductRecommendProductsAdapter;
import com.example.hamigua.shop.chat.UserList;
import com.example.hamigua.shop.eventbus.CartCountEvent;
import com.example.hamigua.shop.model.ProductColorData;
import com.example.hamigua.shop.model.ProductSizeData;
import com.example.hamigua.shop.recommendAlgorithm.PearsonCorrelation.PearsonCorrelationScore;
import com.example.hamigua.shop.seller.AddNewProducts;
import com.example.hamigua.shop.model.ProductData;
import com.example.hamigua.shop.seller.SellingList;
import com.example.hamigua.wardrobe.calendar.MyCalendar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nex3z.notificationbadge.NotificationBadge;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Shop extends AppCompatActivity implements ShopProductsRecyclerViewAdapter.OnItemClickListener{

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private Button btn_Choose;

    private ImageView open_chat, open_cart;
    private FloatingActionButton openUserList;

    private EditText customer_search_edit;

    private RecyclerView shop_recyclerView;
    private ArrayList<ProductData> productsData; //存放商品資料的動態陣列
    private ArrayList<ProductColorData> productColorData;
    private ArrayList<ProductSizeData> productSizeData;
    private ShopProductsRecyclerViewAdapter productsAdapter; //商品RecyclerView類別物件

    private FirebaseFirestore DB;
    private FirebaseAuth USER;

    //private SwipeRefreshLayout shopRefreshLayout;

    private NotificationBadge cart_badge;
    private int cartCount = 0;

    Button btnFilterUNIQLO, btnFilterZARA, btnFilterADIDAS, btnFilterHandM, btnFilterPAZZO, btnFilterGAP;
    Button btnMenShortSleeve, btnMenLongSleeve, btnMenVest, btnMenShorts, btnMenPants, btnMenCoat,
            btnMenSuit, btnMenAccessories;
    Button btn_Women_Short_Sleeve, btn_Women_Long_Sleeve, btn_Women_Vest, btn_Women_Shorts, btn_Women_Pants,
            btn_Women_Skirt, btn_Women_Coat, btn_Women_Suit, btn_Women_Accessories;
    private ArrayList<String> productFilter;

    //演算法用參數
    String tempBuyerID = null;
    HashMap<String, Double> productRating;
    HashMap<String, HashMap<String, Double>> ratingList = new HashMap<>();

    ImageView brandLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        //shopRefreshLayout = findViewById(R.id.shopRefreshLayout);
        //shopRefreshLayout.setColorSchemeResources(R.color.blue_main);
        //shopRefreshLayout.setOnRefreshListener(refreshListener);

        cart_badge = findViewById(R.id.cart_badge);

        drawerLayout = findViewById(R.id.shop_drawer_layout);
        navigationView = findViewById(R.id.shop_nav_view);

        bottomNavigationView = findViewById(R.id.bottomNav); //設定底部導行列
        bottomNavigationView.setSelectedItemId(R.id.Shop); //設定所選到的itemId
        bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavigationListener);

        /*此部分為右滑漢堡選單*/
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this,drawerLayout,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(Drawer_Listner);
        /*此部分為右滑漢堡選單*/

        open_chat = findViewById(R.id.open_chat);
        open_chat.setOnClickListener(open_chat_Listener);
        open_cart = findViewById(R.id.open_cart);
        open_cart.setOnClickListener(open_cart_Listener);
        openUserList = findViewById(R.id.openUserList);
        openUserList.setOnClickListener(open_UserList_Listner);
        btn_Choose = findViewById(R.id.btnChoose);
        btn_Choose.setOnClickListener(open_Drawer_Listener);

        customer_search_edit = findViewById(R.id.customer_search_edit);
        customer_search_edit.setFocusable(false);
        customer_search_edit.setOnClickListener(productSearchListener);

        shop_recyclerView = findViewById(R.id.shop_recyclerView);
        shop_recyclerView.addOnScrollListener(HideOrShowFAB);

        btnFilterUNIQLO = findViewById(R.id.btnFilterUNIQLO);
        btnFilterUNIQLO.setOnClickListener(brandFilterListener);
        btnFilterZARA = findViewById(R.id.btnFilterZARA);
        btnFilterZARA.setOnClickListener(brandFilterListener);
        btnFilterADIDAS = findViewById(R.id.btnFilterADIDAS);
        btnFilterADIDAS.setOnClickListener(brandFilterListener);
        btnFilterHandM = findViewById(R.id.btnFilterHandM);
        btnFilterHandM.setOnClickListener(brandFilterListener);
        btnFilterPAZZO = findViewById(R.id.btnFilterPAZZO);
        btnFilterPAZZO.setOnClickListener(brandFilterListener);
        btnFilterGAP = findViewById(R.id.btnFilterGAP);
        btnFilterGAP.setOnClickListener(brandFilterListener);

        btnMenShortSleeve = findViewById(R.id.btn_Men_Short_Sleeve);
        btnMenShortSleeve.setOnClickListener(categoryFilterListener);
        btnMenLongSleeve = findViewById(R.id.btn_Men_Long_Sleeve);
        btnMenLongSleeve.setOnClickListener(categoryFilterListener);
        btnMenVest = findViewById(R.id.btn_Men_Vest);
        btnMenVest.setOnClickListener(categoryFilterListener);
        btnMenShorts = findViewById(R.id.btn_Men_Shorts);
        btnMenShorts.setOnClickListener(categoryFilterListener);
        btnMenPants = findViewById(R.id.btn_Men_Pants);
        btnMenPants.setOnClickListener(categoryFilterListener);
        btnMenCoat = findViewById(R.id.btn_Men_Coat);
        btnMenCoat.setOnClickListener(categoryFilterListener);
        btnMenSuit = findViewById(R.id.btn_Men_Suit);
        btnMenSuit.setOnClickListener(categoryFilterListener);
        btnMenAccessories = findViewById(R.id.btn_Men_Accessories);
        btnMenAccessories.setOnClickListener(categoryFilterListener);

        btn_Women_Short_Sleeve = findViewById(R.id.btn_Women_Short_Sleeve);
        btn_Women_Short_Sleeve.setOnClickListener(categoryFilterListener);
        btn_Women_Long_Sleeve = findViewById(R.id.btn_Women_Long_Sleeve);
        btn_Women_Long_Sleeve.setOnClickListener(categoryFilterListener);
        btn_Women_Vest = findViewById(R.id.btn_Women_Vest);
        btn_Women_Vest.setOnClickListener(categoryFilterListener);
        btn_Women_Shorts = findViewById(R.id.btn_Women_Shorts);
        btn_Women_Shorts.setOnClickListener(categoryFilterListener);
        btn_Women_Pants = findViewById(R.id.btn_Women_Pants);
        btn_Women_Pants.setOnClickListener(categoryFilterListener);
        btn_Women_Skirt = findViewById(R.id.btn_Women_Skirt);
        btn_Women_Skirt.setOnClickListener(categoryFilterListener);
        btn_Women_Coat = findViewById(R.id.btn_Women_Coat);
        btn_Women_Coat.setOnClickListener(categoryFilterListener);
        btn_Women_Suit = findViewById(R.id.btn_Women_Suit);
        btn_Women_Suit.setOnClickListener(categoryFilterListener);
        btn_Women_Accessories = findViewById(R.id.btn_Women_Accessories);
        btn_Women_Accessories.setOnClickListener(categoryFilterListener);

        brandLogo = findViewById(R.id.brandLogo);
        brandLogo.setOnClickListener(listener);

        init();

        //取得firebase資料裡該登入帳號的使用者名稱
        get_Account_UserName();

        //getProduct(); //取得所有上架商品
        get_DB_Cart_Count();


        check_DB_PurchaseList(); //檢查使用者是否有購買紀錄，若有則取得推薦商品；若無則取得所有商品且無序
    }

    protected void init() {
        //取得firestore與authentication的連接
        DB = FirebaseFirestore.getInstance();
        USER = FirebaseAuth.getInstance();
        productsData = new ArrayList<>();
        productFilter = new ArrayList<>();
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
        cart_badge.setNumber(event.getCartCount());
    }

    /*
    //重新載入activity
    protected SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            finish();
            startActivity(getIntent());
            //設定切換activity動畫效果
            //教學https://blog.csdn.net/ccpat/article/details/84883418
            overridePendingTransition(0, 1);
            shopRefreshLayout.setRefreshing(false);
        }
    };
    */

    //開啟漢堡選單button
    protected View.OnClickListener open_Drawer_Listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            drawerLayout.openDrawer(GravityCompat.START);//選單從左邊拉出
        }
    };

    protected View.OnClickListener productSearchListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Shop.this, SearchingProducts.class);
            startActivity(intent);
            overridePendingTransition(R.anim.nav_default_pop_enter_anim, 0);
        }
    };

    //開啟電商購物車
    protected View.OnClickListener open_cart_Listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(Shop.this, Cart.class);
            startActivity(intent);
        }
    };

    protected View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DB.collection("products_Information")
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                DB.collection("products_Information").document(document.getId())
                                        .update("searching_State", "false");
                            }

                        }
                    });
        }
    };

    //開啟電商聊天室
    protected View.OnClickListener open_chat_Listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent Clothes = new Intent();
            //Clothes.setClass(Shop.this, Shop_Chat_Recent_Conversation.class);
            Clothes.setClass(Shop.this, UserList.class);
            startActivity(Clothes);
        }
    };

    //開啟電商聊天室的賣家列表
    protected View.OnClickListener open_UserList_Listner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent Clothes = new Intent();
            //Clothes.setClass(Shop.this, Shop_Chat_UserList.class);
            Clothes.setClass(Shop.this, AddNewProducts.class);
            startActivity(Clothes);
        }
    };

    //商品RecyclerView往下捲動時懸浮按鈕隱藏，往上捲動時懸浮按鈕顯示
    protected RecyclerView.OnScrollListener HideOrShowFAB = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            if (dy > 0) {
                openUserList.hide();
            } else {
                openUserList.show();
            }
            super.onScrolled(recyclerView, dx, dy);
        }
    };

    protected View.OnClickListener brandFilterListener = new View.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnFilterUNIQLO:
                    get_DB_Product_Brand_Filter("UNIQLO");
                    break;
                case R.id.btnFilterZARA:
                    get_DB_Product_Brand_Filter("ZARA");
                    break;
                case R.id.btnFilterADIDAS:
                    get_DB_Product_Brand_Filter("ADIDAS");
                    break;
                case R.id.btnFilterHandM:
                    get_DB_Product_Brand_Filter("HandM");
                    break;
                case R.id.btnFilterPAZZO:
                    get_DB_Product_Brand_Filter("PAZZO");
                    break;
                case R.id.btnFilterGAP:
                    get_DB_Product_Brand_Filter("GAP");
                    break;
            }
        }
    };

    protected View.OnClickListener categoryFilterListener = new View.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_Men_Short_Sleeve:
                    get_DB_Product_Category_Filter("男裝", "短袖上衣");
                    break;
                case R.id.btn_Men_Long_Sleeve:
                    get_DB_Product_Category_Filter("男裝", "長袖上衣");
                    break;
                case R.id.btn_Men_Vest:
                    get_DB_Product_Category_Filter("男裝", "背心");
                    break;
                case R.id.btn_Men_Shorts:
                    get_DB_Product_Category_Filter("男裝", "短褲");
                    break;
                case R.id.btn_Men_Pants:
                    get_DB_Product_Category_Filter("男裝", "長褲");
                    break;
                case R.id.btn_Men_Coat:
                    get_DB_Product_Category_Filter("男裝", "外套");
                    break;
                case R.id.btn_Men_Suit:
                    get_DB_Product_Category_Filter("男裝", "套裝");
                    break;
                case R.id.btn_Men_Accessories:
                    get_DB_Product_Category_Filter("男裝", "配件飾品");
                    break;
                case R.id.btn_Women_Short_Sleeve:
                    get_DB_Product_Category_Filter("女裝", "短袖上衣");
                    break;
                case R.id.btn_Women_Long_Sleeve:
                    get_DB_Product_Category_Filter("女裝", "長袖上衣");
                    break;
                case R.id.btn_Women_Vest:
                    get_DB_Product_Category_Filter("女裝", "背心");
                    break;
                case R.id.btn_Women_Shorts:
                    get_DB_Product_Category_Filter("女裝", "短褲");
                    break;
                case R.id.btn_Women_Pants:
                    get_DB_Product_Category_Filter("女裝", "長褲");
                    break;
                case R.id.btn_Women_Skirt:
                    get_DB_Product_Category_Filter("女裝", "裙子");
                    break;
                case R.id.btn_Women_Coat:
                    get_DB_Product_Category_Filter("女裝", "外套");
                    break;
                case R.id.btn_Women_Suit:
                    get_DB_Product_Category_Filter("女裝", "套裝");
                    break;
                case R.id.btn_Women_Accessories:
                    get_DB_Product_Category_Filter("女裝", "配件飾品");
                    break;
            }
        }
    };

    //取得firebase資料裡該登入帳號的使用者名稱，並顯示在側滑選單的header位置
    protected void get_Account_UserName() {
        DB.collection("user_Information").document(USER.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()) {
                                //取得Header
                                View header = navigationView.getHeaderView(0);
                                //取得Header中的TextView
                                TextView txtHeader = (TextView) header.findViewById(R.id.txtHeader);
                                txtHeader.setText(document.getString("userName"));

                                //設定使用者大頭照
                                ImageView userImage = (ImageView) header.findViewById(R.id.userImage);
                                Glide.with(Shop.this).load(document.getString("userImage")).into(userImage);
                            }
                        }
                    }
                });
    }

    //從firestore讀取已上架的商品，放進recyclerview
    protected void getProduct(){
        //設定要讀取的資料路徑
        DB.collection("products_Information")
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
                                Toast.makeText(Shop.this, "還沒有任何一項商品喔", Toast.LENGTH_SHORT).show();
                            }
                            productsAdapter = new ShopProductsRecyclerViewAdapter(Shop.this, productsData, Shop.this); //將陣列傳到商品RecyclerView類別的建構方法

                            //用GridLayoutManager指定RecyclerView中每列的顯示方式：縱向、2個物件為一列
                            GridLayoutManager gridLayoutManager = new GridLayoutManager(Shop.this, 2, GridLayoutManager.VERTICAL, false);
                            shop_recyclerView.setLayoutManager(gridLayoutManager);
                            shop_recyclerView.setAdapter(productsAdapter);
                        }
                    }
                });
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

    protected void get_DB_Product_Brand_Filter(String filterBrand) {
        DB.collection("products_Information")
                .whereEqualTo("product_Brand", filterBrand)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                productFilter.add(document.getId());
                            }
                        }
                        set_DB_Searching_State_Yes(productFilter);

                        String brand = "";
                        if(filterBrand.equals("HandM"))
                            brand = "H&M";
                        else
                            brand = filterBrand;

                        Intent intent = new Intent(Shop.this, SearchingProductsResult.class);
                        intent.putExtra("searchResult", productFilter);
                        intent.putExtra("searchString", brand);
                        startActivity(intent);
                        productFilter.clear();
                    }
                });
    }

    protected void get_DB_Product_Category_Filter(String gender, String filterCategory) {
        DB.collection("products_Information")
                .whereEqualTo("product_Gender", gender)
                .whereEqualTo("product_Category", filterCategory)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                productFilter.add(document.getId());
                            }
                        }
                        set_DB_Searching_State_Yes(productFilter);

                        Intent intent = new Intent(Shop.this, SearchingProductsResult.class);
                        intent.putExtra("searchResult", productFilter);
                        intent.putExtra("searchString", gender + filterCategory);
                        startActivity(intent);
                        productFilter.clear();
                    }
                });
    }

    protected void set_DB_Searching_State_Yes(ArrayList<String> productFilter) {
        for (String documentID : productFilter) {
            DB.collection("products_Information").document(documentID)
                    .update("searching_State", "true");
        }
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
                                get_DB_Common_Products();
                            } else {
                                get_DB_Recommend_Products();
                            }
                        }
                    }
                });
    }

    protected void get_DB_Common_Products() {
        //從firebase讀取所有商品資料，無序
        DB.collection("products_Information")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && task.getResult() != null) {
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
                                productsData.add(product_data);
                            }

                            productsAdapter = new ShopProductsRecyclerViewAdapter(Shop.this, productsData, Shop.this); //將陣列傳到商品RecyclerView類別的建構方法

                            //用GridLayoutManager指定RecyclerView中每列的顯示方式：縱向、2個物件為一列
                            GridLayoutManager gridLayoutManager = new GridLayoutManager(Shop.this, 2, GridLayoutManager.VERTICAL, false);
                            shop_recyclerView.setLayoutManager(gridLayoutManager);
                            shop_recyclerView.setAdapter(productsAdapter);
                            //shop_recyclerView.setNestedScrollingEnabled(false);
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
                                    productRating = new HashMap<>();
                                } else if(!tempBuyerID.equals(document.getString("buyerID"))) { //找到不同買家的購買紀錄
                                    ratingList.put(tempBuyerID, productRating); //儲存前一位買家的購買紀錄
                                    tempBuyerID = document.getString("buyerID");
                                    productRating = new HashMap<>();
                                }

                                productRating.put(document.getString("productID"), Double.parseDouble(Objects.requireNonNull(document.getString("ratingValue"))));

                                tempCount += 1;
                                if(tempCount == documentCount) { //儲存最後一位買家的購買紀錄
                                    ratingList.put(tempBuyerID, productRating);
                                }
                            }
                            PearsonCorrelationScore pearson = new PearsonCorrelationScore(ratingList);
                            pearson.get_Recommend_Data();

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
                                                    productsData.add(product_data);
                                                }

                                                //從firebase讀取推薦商品以外的商品資料，放在推薦商品後面，無序
                                                DB.collection("products_Information").whereEqualTo("recommendValue", "0")
                                                        .get()
                                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                if(task.isSuccessful() && task.getResult() != null) {
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
                                                                        productsData.add(product_data);
                                                                    }

                                                                    productsAdapter = new ShopProductsRecyclerViewAdapter(Shop.this, productsData, Shop.this); //將陣列傳到商品RecyclerView類別的建構方法

                                                                    //用GridLayoutManager指定RecyclerView中每列的顯示方式：縱向、2個物件為一列
                                                                    GridLayoutManager gridLayoutManager = new GridLayoutManager(Shop.this, 2, GridLayoutManager.VERTICAL, false);
                                                                    shop_recyclerView.setLayoutManager(gridLayoutManager);
                                                                    shop_recyclerView.setAdapter(productsAdapter);
                                                                    //shop_recyclerView.setNestedScrollingEnabled(false);
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    //漢堡選單裡的觸擊事件
    protected NavigationView.OnNavigationItemSelectedListener Drawer_Listner =
            new NavigationView.OnNavigationItemSelectedListener() {
                @SuppressLint("NonConstantResourceId")
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Intent intent;
                    switch (item.getItemId()){
                        case R.id.nav_1:
                            //Intent intent_1=new Intent(Shop.this, Article.class);
                            //startActivity(intent_1);
                            Toast.makeText(Shop.this, "我的賣場", Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.nav_2:
                            intent = new Intent(Shop.this, PurchaseList.class);
                            startActivity(intent);
                            break;
                        case R.id.nav_3:
                            intent = new Intent(Shop.this, SellingList.class);
                            startActivity(intent);
                            break;
                        case R.id.nav_4:
                            intent = new Intent(Shop.this, MyFavoriteProducts.class);
                            startActivity(intent);
                            break;
                            /*
                        case R.id.nav_5:
                            //Intent intent_4=new Intent(Shop.this, MyCalendar.class);
                            //startActivity(intent_4);
                            Toast.makeText(Shop.this, "瀏覽紀錄", Toast.LENGTH_SHORT).show();
                            break;

                             */
                    }
                    return true;
                }
            };

    //底部導行列的觸擊事件
    public BottomNavigationView.OnNavigationItemSelectedListener bottomNavigationListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId())
                    {
                        case R.id.Runway:
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(getApplicationContext(), Runway.class));
                                    overridePendingTransition(0,0);
                                }
                            }, 200);
                            return true;
                        case R.id.Cloth:
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(getApplicationContext(), HomePage.class));
                                    overridePendingTransition(0,0);
                                }
                            }, 200);
                            return true;
                            /*
                        case R.id.Communicate:
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(getApplicationContext(), Communicate.class));
                                    overridePendingTransition(0,0);
                                }
                            }, 200);
                            return true;

                             */
                        case R.id.Shop:
                            return true;
                    }
                    return false;
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

        Intent intent = new Intent(Shop.this, SingleProductView.class);
        intent.putExtra("ProductData", productData);
        intent.putExtra("colorString", colorResult.toString());
        intent.putExtra("quantityString", quantityResult.toString());
        startActivity(intent);
    }
}