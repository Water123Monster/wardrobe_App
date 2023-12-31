package com.example.hamigua.shop.customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.hamigua.LoadingDialog;
import com.example.hamigua.R;
import com.example.hamigua.SetContainedButtonStyle;
import com.example.hamigua.shop.adapter.ShopCartRecyclerViewAdapter;
import com.example.hamigua.shop.eventbus.CartCountEvent;
import com.example.hamigua.shop.model.CartData;
import com.example.hamigua.shop.model.ProductColorData;
import com.example.hamigua.shop.model.ProductSizeData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Cart extends AppCompatActivity implements ShopCartRecyclerViewAdapter.OnItemClickListener{

    private RecyclerView cart_recyclerview;
    private ShopCartRecyclerViewAdapter cartAdapter;

    private CheckBox cart_select_all;
    private TextView cart_total_price;
    private MaterialButton btn_proceed_to_checkout;

    private BottomSheetDialog selectSizeDialog;

    private ArrayList<CartData> arrCartData;
    private ArrayList<ProductColorData> productColorData;
    private ArrayList<ProductSizeData> productSizeData;
    private FirebaseFirestore DB;
    private FirebaseAuth user;
    private String tempSellerID = null;

    private int checkOutCount;
    int totalPrice;

    private LoadingDialog loadingDialog;
    private SetContainedButtonStyle buttonStyle;

    private SwipeRefreshLayout cartRefreshLayout;

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
        setContentView(R.layout.activity_shop_cart);

        //設置標題列ActionBar的左上角返回鍵
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        cartRefreshLayout = findViewById(R.id.cartRefreshLayout);
        cartRefreshLayout.setColorSchemeResources(R.color.blue_main);
        cartRefreshLayout.setOnRefreshListener(refreshListener);

        cart_recyclerview = findViewById(R.id.cart_recyclerview);
        cart_select_all = findViewById(R.id.cart_select_all);
        cart_select_all.setOnCheckedChangeListener(selectAllListener);
        cart_total_price = findViewById(R.id.checkout_total_price);
        btn_proceed_to_checkout = findViewById(R.id.btn_checkout);
        btn_proceed_to_checkout.setOnClickListener(checkoutListener);

        init();
        get_DB_Cart_Data();
    }

    private void init() {
        arrCartData = new ArrayList<>();

        DB = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance();

        selectSizeDialog = new BottomSheetDialog(this);
        loadingDialog = new LoadingDialog(Cart.this);
    }

    //重新載入activity
    protected SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            finish();
            startActivity(getIntent());
            //設定切換activity動畫效果
            //教學https://blog.csdn.net/ccpat/article/details/84883418
            overridePendingTransition(0, 1);
            cartRefreshLayout.setRefreshing(false);
        }
    };

    protected final View.OnClickListener checkoutListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(checkOutCount == 0)
                Toast.makeText(Cart.this, "請先勾選商品再進行結帳", Toast.LENGTH_LONG).show();
            else {
                Intent intent = new Intent(Cart.this, Checkout.class);
                startActivity(intent);
            }
            /*
            for(int i = 0; i < arrCartData.size(); i++) {
                if(arrCartData.get(i).productID != null && arrCartData.get(i).isChecked.equals("true")) {
                    CartData data = arrCartData.get(i);
                    Toast.makeText(Cart.this, "結帳商品：" + data.productName + " , 價格：" + data.productPrice +
                            "\n顏色：" + data.selectColor + " , 尺寸：" + data.selectSize + " , 數量：" + data.selectQuantity + "件", Toast.LENGTH_LONG).show();
                }
            }
            */
        }
    };

    protected CompoundButton.OnCheckedChangeListener selectAllListener = new CompoundButton.OnCheckedChangeListener() {
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked) {
                for (CartData data : arrCartData) {
                    data.setIsChecked("true");
                }
            } else {
                for (CartData data : arrCartData) {
                    data.setIsChecked("false");
                }
            }
            // 在UI執行緒執行notifyDataSetChanged()
            // https://stackoverflow.com/questions/43221847/cannot-call-this-method-while-recyclerview-is-computing-a-layout-or-scrolling-wh
            cart_recyclerview.post(new Runnable()
            {
                @Override
                public void run() {
                    cartAdapter.notifyDataSetChanged();
                }
            });
            cart_total_price.setText(String.valueOf(calculateTotalPrice()));

        }
    };

    //計算已勾選商品總金額
    protected int calculateTotalPrice() {
        totalPrice = 0;
        for (CartData data : arrCartData) {
            if(data.productID != null && data.isChecked.equals("true")) {
                totalPrice += Integer.parseInt(data.productPrice) * Integer.parseInt(data.selectQuantity);
            }
        }
        return totalPrice;
    }

    protected void get_DB_Cart_Data() {
        DB.collection("user_Information").document(user.getCurrentUser().getUid()).collection("Shop_Cart")
                .orderBy("sellerID")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for(QueryDocumentSnapshot document : task.getResult()) {

                                //處理賣家名稱資料
                                if(tempSellerID == null || !Objects.equals(tempSellerID, document.getString("sellerID"))) {
                                    tempSellerID = document.getString("sellerID");
                                    CartData data = new CartData(null, null, null, null,
                                            null, null, document.getString("sellerName"), document.getString("sellerID"),
                                            null, null, "false", null, null);
                                    arrCartData.add(data);
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
                                arrCartData.add(data);
                            }
                            //如果'CartData'型別的arraylist的長度為0
                            if(arrCartData.size() == 0)
                                Toast.makeText(Cart.this, "購物車內還沒有任何一項商品喔", Toast.LENGTH_SHORT).show();
                            //將陣列傳到商品RecyclerView類別的建構方法
                            cartAdapter = new ShopCartRecyclerViewAdapter(Cart.this, arrCartData,
                                    Cart.this,
                                    Cart.this,
                                    Cart.this,
                                    Cart.this,
                                    Cart.this,
                                    Cart.this,
                                    Cart.this,
                                    Cart.this);

                            //用GridLayoutManager指定RecyclerView中每列的顯示方式：縱向、1個物件為一列
                            GridLayoutManager gridLayoutManager = new GridLayoutManager(Cart.this, 1, GridLayoutManager.VERTICAL, false);
                            cart_recyclerview.setLayoutManager(gridLayoutManager);
                            cart_recyclerview.setAdapter(cartAdapter);
                        }
                    }
                });
    }

    protected void update_DB_selectQuantity(CartData data, int quantity) {
        DB.collection("user_Information").document(user.getCurrentUser().getUid()).collection("Shop_Cart")
                .whereEqualTo("productID", data.getProductID())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                            String documentID = documentSnapshot.getId();
                            DB.collection("user_Information").document(user.getCurrentUser().getUid()).collection("Shop_Cart")
                                    .document(documentID)
                                    .update("selectQuantity", String.valueOf(quantity));
                        }
                    }
                });
    }

    protected void update_DB_selectColor_Size_Quantity(CartData data, String color, String size, String quantity) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("selectColor", color);
        updateData.put("selectSize", size);
        updateData.put("selectQuantity", quantity);

        DB.collection("user_Information").document(user.getCurrentUser().getUid()).collection("Shop_Cart")
                .whereEqualTo("productID", data.getProductID())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                            String documentID = documentSnapshot.getId();
                            DB.collection("user_Information").document(user.getCurrentUser().getUid()).collection("Shop_Cart")
                                    .document(documentID)
                                    .update(updateData)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @SuppressLint("NotifyDataSetChanged")
                                        @Override
                                        public void onSuccess(Void unused) {
                                            cartAdapter.notifyDataSetChanged();
                                            cart_total_price.setText(String.valueOf(calculateTotalPrice()));
                                            loadingDialog.dismiss_Dialog();
                                        }
                                    });
                        }
                    }
                });
    }

    protected void update_DB_IsChecked(CartData data, String isChecked) {
        DB.collection("user_Information").document(user.getCurrentUser().getUid()).collection("Shop_Cart")
                .whereEqualTo("productID", data.getProductID())
                .whereEqualTo("selectColor", data.getSelectColor())
                .whereEqualTo("selectQuantity", data.getSelectQuantity())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                            String documentID = documentSnapshot.getId();
                            DB.collection("user_Information").document(user.getCurrentUser().getUid()).collection("Shop_Cart")
                                    .document(documentID)
                                    .update("isChecked", isChecked);
                        }
                    }
                });
    }

    protected void delete_DB_Cart_Data(CartData data) {
        DB.collection("user_Information").document(user.getCurrentUser().getUid()).collection("Shop_Cart")
                .whereEqualTo("productID", data.getProductID())
                .whereEqualTo("selectColor", data.getSelectColor())
                .whereEqualTo("selectQuantity", data.getSelectQuantity())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                            String documentID = documentSnapshot.getId();
                            DB.collection("user_Information").document(user.getCurrentUser().getUid()).collection("Shop_Cart")
                                    .document(documentID)
                                    .delete();
                        }
                    }
                });
    }

    protected void set_Delete_Dialog(CartData data, int position) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setMessage("您確定要從購物車移除此商品嗎");
        alertDialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String deleteSellerID = arrCartData.get(position).sellerID;
                arrCartData.remove(position);
                cartAdapter.notifyItemRemoved(position);

                //去買單button自動結算商品數量
                int isCheckedCount = 0;
                for (CartData tempData : arrCartData) {
                    if(tempData.productID != null && tempData.getIsChecked().equals("true"))
                        isCheckedCount += 1;
                }
                btn_proceed_to_checkout.setText("去買單(" + isCheckedCount + ")");
                checkOutCount = isCheckedCount;

                //----------------------------------------------------------------------------------

                //若刪除該商品是該賣家的在購物車的最後一個商品，刪除賣家的data
                int sellerIDCount = 0;
                for (CartData data : arrCartData) {
                    if (data.productID != null && data.sellerID.equals(deleteSellerID)) {
                        sellerIDCount += 1;
                    }
                }
                if(sellerIDCount == 0) {
                    arrCartData.removeIf(cartData -> cartData.sellerID.equals(deleteSellerID));
                    cartAdapter.notifyItemRemoved(position - 1);
                }
                delete_DB_Cart_Data(data);
                cart_total_price.setText(String.valueOf(calculateTotalPrice()));

                //----------------------------------------------------------------------------------

                //結算購物車商品數量，並用EventBus傳結果給訂閱者(電商首頁(shop.class))
                int cartCount = 0;
                for (CartData tempData : arrCartData) {
                    if(tempData.productID != null)
                        cartCount += 1;
                }
                if(cartCount == 0)
                    cart_select_all.setChecked(false);
                EventBus.getDefault().postSticky(new CartCountEvent(cartCount));
            }
        });
        alertDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cartAdapter.close_viewBinderHelper(data.getProductID());
            }
        });
        alertDialog.setCancelable(true);
        alertDialog.show();
    }

    @SuppressLint("SetTextI18n")
    protected void create_Bottom_Dialog(CartData cartData, int position) {
        View view = getLayoutInflater().inflate(R.layout.select_product_size_bottom_dialog, null, false); //設定對話窗(dialog)的layout樣式

        //暫時存放的購物車model，用於儲存商品規格前的UI顯示
        CartData tempData = new CartData(cartData.productID, cartData.productName, cartData.selectColor, cartData.selectSize,
                cartData.selectQuantity, cartData.productPrice, cartData.sellerName, cartData.sellerID, cartData.productImageURL,
                cartData.setDataTime, cartData.isChecked, cartData.colorData, cartData.sizeData);

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

        buttonStyle = new SetContainedButtonStyle(btn_select_product_save, Cart.this);

        //設定顏色規格chip
        int colorChipIdIndex = 0;
        for (ProductColorData colorData : cartData.colorData) {
            LayoutInflater inflater = LayoutInflater.from(this);
            Chip chip = (Chip)inflater.inflate(R.layout.product_color_chip_item, null, false); //設定自訂chip樣式
            chip.setText(colorData.product_color); //設定chip文字
            chip.setCheckable(true); //設定chip可勾選
            chip.setClickable(true); //設定chip點擊
            chip.setId(colorChipIdIndex);
            select_product_color_chipGroup.addView(chip); //新增chip至顏色ChipGroup
            colorChipIdIndex += 1;

            if(colorData.product_color.equals(cartData.selectColor)) {
                chip.setChecked(true);
                Glide.with(Cart.this).load(colorData.product_image_url).into(select_product_size_imageview);
                tempData.selectColor = colorData.product_color;
            }
        }

        //設定顏色規格chipGroup的點擊事件，設置為單選模式(singleSelection = true)
        select_product_color_chipGroup.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                if(checkedIds.size() != 0) {
                    int select = checkedIds.get(0);
                    Chip chip = (Chip) group.getChildAt(select);
                    for (ProductColorData colorData : cartData.colorData) {
                        if(colorData.product_color.contentEquals(chip.getText())) {
                            Glide.with(Cart.this).load(colorData.product_image_url).into(select_product_size_imageview);
                            tempData.selectColor = (String) chip.getText();
                        }
                    }
                    for (ProductSizeData sizeData : cartData.sizeData) {
                        if(sizeData.product_color.contentEquals(chip.getText()) && sizeData.product_size.equals(tempData.selectSize))
                            product_size_quantity.setText("商品數量：" + sizeData.product_quantity);
                    }
                    if(!tempData.selectSize.equals(""))
                        buttonStyle.setButtonClickable(); //設定button可使用樣式
                } else {
                    buttonStyle.setButtonUnClickable(); //設定button無法使用樣式
                    Glide.with(Cart.this).load(cartData.productImageURL).into(select_product_size_imageview);
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
        for (ProductSizeData sizeData : cartData.sizeData) {
            if(sizeIndex < cartData.sizeData.size() / cartData.colorData.size()) {
                LayoutInflater inflater = LayoutInflater.from(this);
                Chip chip = (Chip)inflater.inflate(R.layout.product_size_chip_item, null, false); //設定自訂chip樣式
                chip.setText(sizeData.product_size); //設定chip文字
                chip.setCheckable(true); //設定chip可勾選
                chip.setClickable(true); //設定chip點擊
                chip.setId(sizeChipIdIndex);
                select_product_size_chipGroup.addView(chip); //新增chip至顏色ChipGroup
                sizeChipIdIndex += 1;

                if(sizeData.product_size.equals(tempData.selectSize)) {
                    chip.setChecked(true);
                    tempData.selectSize = sizeData.product_size;
                }

                for (ProductSizeData sizeData1 : cartData.sizeData) {
                    if(sizeData1.product_color.equals(tempData.selectColor) && sizeData1.product_size.equals(tempData.selectSize))
                        product_size_quantity.setText("商品數量：" + sizeData1.product_quantity);
                }
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
                    for (ProductSizeData sizeData : cartData.sizeData) {
                        if(sizeData.product_size.contentEquals(chip.getText()))
                            tempData.selectSize = (String) chip.getText();
                        if(sizeData.product_color.equals(tempData.selectColor) && sizeData.product_size.contentEquals(chip.getText())) {
                            product_size_quantity.setText("商品數量：" + sizeData.product_quantity);
                            tempData.selectSize = (String) chip.getText();
                        }
                    }
                    if(!tempData.selectColor.equals(""))
                        buttonStyle.setButtonClickable(); //設定button可使用樣式
                    select_product_quantity.setText("1");
                    tempData.selectQuantity = "1";
                    cant_add_quantity_helper.setVisibility(View.GONE);
                } else {
                    buttonStyle.setButtonUnClickable(); //設定button無法使用樣式
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
                    Toast.makeText(Cart.this, "最少購買1件", Toast.LENGTH_SHORT).show();
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

                for (ProductSizeData sizeData : cartData.sizeData) {
                    if(sizeData.product_color.equals(tempData.selectColor) && sizeData.product_size.equals(tempData.selectSize))
                        quantity = Integer.parseInt(sizeData.product_quantity);
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

        select_product_size_price.setText("$" + cartData.productPrice);
        select_product_quantity.setText(tempData.selectQuantity);
        select_product_quantity.setFocusable(false);

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
                arrCartData.get(position).selectColor = tempData.selectColor;
                arrCartData.get(position).selectSize = tempData.selectSize;
                arrCartData.get(position).selectQuantity = tempData.selectQuantity;
                selectSizeDialog.dismiss();
                loadingDialog.start_Loading_Dialog();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        update_DB_selectColor_Size_Quantity(arrCartData.get(position), tempData.selectColor, tempData.selectSize, tempData.selectQuantity);
                    }
                }, 800);

            }
        });

        selectSizeDialog.setContentView(view);
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    @Override
    public void onCheckBoxChanged(CartData data, int position, boolean isChecked) {

        int isCheckedCount = 0;

        if(isChecked)
            arrCartData.get(position).setIsChecked("true");
        else
            arrCartData.get(position).setIsChecked("false");
        cart_total_price.setText(String.valueOf(calculateTotalPrice()));

        update_DB_IsChecked(data, String.valueOf(isChecked));

        //去買單button自動結算商品數量
        for (CartData tempData : arrCartData) {
            if(tempData.productID != null && tempData.getIsChecked().equals("true"))
                isCheckedCount += 1;
        }
        btn_proceed_to_checkout.setText("去買單(" + isCheckedCount + ")");
        checkOutCount = isCheckedCount;

        int trueCount = 0;
        int productCount = 0;
        //賣家商品全選檢查
        for (CartData tempData : arrCartData) {
            if(tempData.productID != null && tempData.sellerID.equals(data.sellerID)) {
                productCount += 1;
            }
        }

        for (CartData tempData : arrCartData) {
            if(tempData.productID != null && tempData.sellerID.equals(data.sellerID) && tempData.isChecked.equals("true")) {
                trueCount += 1;
            }
        }
        for (CartData tempData : arrCartData) {
            if(tempData.productID == null && tempData.sellerID.equals(data.sellerID)) {
                if(trueCount != productCount) {
                    tempData.isChecked = "false";
                } else {
                    tempData.isChecked = "true";
                }
                // 在UI執行緒執行notifyDataSetChanged()
                // https://stackoverflow.com/questions/43221847/cannot-call-this-method-while-recyclerview-is-computing-a-layout-or-scrolling-wh
                cart_recyclerview.post(new Runnable() {
                    @Override
                    public void run() {
                        cartAdapter.notifyDataSetChanged();
                    }
                });
            }
        }

        //全選檢查
        for (CartData tempData : arrCartData) {
            if(tempData.productID != null && tempData.getIsChecked().equals("false")) {
                cart_select_all.setOnCheckedChangeListener(null);
                cart_select_all.setChecked(false);
                cart_select_all.setOnCheckedChangeListener(selectAllListener);
                return;
            }
        }
        cart_select_all.setChecked(true);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onQuantityMinusClick(CartData data, int position, String isChecked) {
        int quantity = Integer.parseInt(arrCartData.get(position).getSelectQuantity());
        if(quantity == 1)
            set_Delete_Dialog(data, position);
        else {
            quantity -= 1;
            update_DB_selectQuantity(data, quantity);
            arrCartData.get(position).setSelectQuantity(String.valueOf(quantity));
            cartAdapter.notifyDataSetChanged();
            cart_total_price.setText(String.valueOf(calculateTotalPrice()));
        }
    }

    //增加商品數量之後還要做先檢查庫存
    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onQuantityPlusClick(CartData data, int position, String isChecked) {
        int quantity = 0;
        for (ProductSizeData sizeData : data.sizeData) {
            if(sizeData.product_color.equals(data.selectColor) && sizeData.product_size.equals(data.selectSize))
                quantity = Integer.parseInt(sizeData.product_quantity);
        }

        int selectQuantity = Integer.parseInt(arrCartData.get(position).getSelectQuantity());
        selectQuantity += 1;

        //檢查增加一件數量後有無超過庫存數量
        if(selectQuantity <= quantity) {
            update_DB_selectQuantity(data, selectQuantity);
            arrCartData.get(position).setSelectQuantity(String.valueOf(selectQuantity));
            cartAdapter.notifyDataSetChanged();
            cart_total_price.setText(String.valueOf(calculateTotalPrice()));
        } else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
            alertDialog.setMessage("非常抱歉，本商品最多可購買" + quantity + "件");
            alertDialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //無任何需要執行程式碼
                }
            });
            alertDialog.setCancelable(true);
            alertDialog.show();
        }
    }

    @Override
    public void onProductDeleteClick(CartData cartData, int position) {
        set_Delete_Dialog(cartData, position);
    }

    @Override
    public void onSelectSizeClick(CartData cartData, int position) {
        create_Bottom_Dialog(cartData, position);
        selectSizeDialog.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onSellerCheckBoxChanged(CartData cartData, int position, boolean isChecked) {

        if(isChecked) {
            for (CartData data : arrCartData) {
                if(data.productID != null && data.sellerID.equals(cartData.sellerID)) {
                    data.isChecked = "true";
                    cartAdapter.notifyDataSetChanged();
                    update_DB_IsChecked(data, String.valueOf(true));
                }
            }
            cartData.isChecked = "true";
        } else {
            for (CartData data : arrCartData) {
                if(data.productID != null && data.sellerID.equals(cartData.sellerID)) {
                    data.isChecked = "false";
                    cartAdapter.notifyDataSetChanged();
                    update_DB_IsChecked(data, String.valueOf(false));
                }
            }
            cartData.isChecked = "false";
        }
    }

    @Override
    public void onSellerNameClick(CartData cartData, int position) {
        Toast.makeText(Cart.this, "前往賣家首頁\n" + "賣家ID：" + cartData.sellerID + "\n賣家名稱：" + cartData.sellerName, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSellerEditClick(CartData cartData, String state) {

    }
}