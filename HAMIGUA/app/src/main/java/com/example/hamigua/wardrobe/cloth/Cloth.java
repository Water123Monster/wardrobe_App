package com.example.hamigua.wardrobe.cloth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamigua.HomePage;
import com.example.hamigua.R;
import com.example.hamigua.shop.eventbus.CartCountEvent;
import com.example.hamigua.wardrobe.adapter.ClothesRecyclerViewAdapter;
import com.example.hamigua.wardrobe.eventbus.DeleteClothEvent;
import com.example.hamigua.wardrobe.model.ClothData;
import com.example.hamigua.wardrobe.recommendAlgorithm.ClothAttributesData;
import com.example.hamigua.wardrobe.recommendAlgorithm.PersonalPreferenceData;
import com.example.hamigua.wardrobe.recommendAlgorithm.Score;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Cloth extends AppCompatActivity implements ClothesRecyclerViewAdapter.OnItemClickListener{

    private RecyclerView recyclerView;
    private ArrayList<ClothData> clothesImages; //存放衣服資料的動態陣列
    private ClothesRecyclerViewAdapter clothesRecyclerViewAdapter; //衣服RecyclerView類別物件

    private Button btn_Clothes_Selected, btn_Short_Sleeve, btn_Long_Sleeve, btn_Vest, btn_Shorts,
                   btn_Pants, btn_Skirt, btn_Coat, btn_Suit, btn_Accessories, btn_Shop;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private final Button[] btnArray = new Button[11];

    String categoryIndex = "";

    PersonalPreferenceData preferenceData;
    ClothAttributesData attributesData;
    ArrayList<ClothAttributesData> clothArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloth);

        recyclerView = findViewById(R.id.clothesRecyclerView);

        btn_Clothes_Selected = findViewById(R.id.btn_Clothes_Selected);
        btn_Short_Sleeve = findViewById(R.id.btn_Short_Sleeve);
        btn_Long_Sleeve = findViewById(R.id.btn_Long_Sleeve);
        btn_Vest = findViewById(R.id.btn_Vest);
        btn_Shorts = findViewById(R.id.btn_Shorts);
        btn_Pants = findViewById(R.id.btn_Pants);
        btn_Skirt = findViewById(R.id.btn_Skirt);
        btn_Coat = findViewById(R.id.btn_Coat);
        btn_Suit = findViewById(R.id.btn_Suit);
        btn_Accessories = findViewById(R.id.btn_Accessories);
        btn_Shop = findViewById(R.id.btn_Shop);

        btnArray[0] = btn_Clothes_Selected;
        btnArray[1] = btn_Short_Sleeve;
        btnArray[2] = btn_Long_Sleeve;
        btnArray[3] = btn_Vest;
        btnArray[4] = btn_Shorts;
        btnArray[5] = btn_Pants;
        btnArray[6] = btn_Skirt;
        btnArray[7] = btn_Coat;
        btnArray[8] = btn_Suit;
        btnArray[9] = btn_Accessories;
        btnArray[10] = btn_Shop;

        //設定每個button對應到相同的觸擊事件
        btn_Clothes_Selected.setOnClickListener(categoryListener);
        btn_Short_Sleeve.setOnClickListener(categoryListener);
        btn_Long_Sleeve.setOnClickListener(categoryListener);
        btn_Vest.setOnClickListener(categoryListener);
        btn_Shorts.setOnClickListener(categoryListener);
        btn_Pants.setOnClickListener(categoryListener);
        btn_Skirt.setOnClickListener(categoryListener);
        btn_Coat.setOnClickListener(categoryListener);
        btn_Suit.setOnClickListener(categoryListener);
        btn_Accessories.setOnClickListener(categoryListener);
        btn_Shop.setOnClickListener(categoryListener);

        //取得firestore與authentication的連接
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        init();
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
    public void onDeleteClothEvent(DeleteClothEvent event) {
        // 收到DeleteClothEvent時要做的事寫在這裡
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                clothesImages.remove(event.position);
                clothesRecyclerViewAdapter.notifyItemRemoved(event.position);
            }
        }, 500);
    }

    protected void init() {
        clothesImages = new ArrayList<>();
        clothesRecyclerViewAdapter = new ClothesRecyclerViewAdapter(Cloth.this,
                clothesImages, Cloth.this, Cloth.this, Cloth.this, Cloth.this);

        //用GridLayoutManager指定RecyclerView中每列的顯示方式：縱向、2個物件為一列
        GridLayoutManager gridLayoutManager = new GridLayoutManager(Cloth.this, 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(clothesRecyclerViewAdapter);
    }

    //每個button的觸擊事件，實例化arrayList並用switch來寫不同的衣服類別傳入方法getImageUrl()
    private Button.OnClickListener categoryListener = new Button.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v){
            switch (v.getId()){
                case R.id.btn_Clothes_Selected:
                    button_selected_style(btn_Clothes_Selected);
                    //getCurrentlyImageUrl();
                    break;
                case R.id.btn_Short_Sleeve:
                    button_selected_style(btn_Short_Sleeve);
                    clothesImages.clear();
                    getImageUrl("短袖上衣");
                    break;
                case R.id.btn_Long_Sleeve:
                    button_selected_style(btn_Long_Sleeve);
                    clothesImages.clear();
                    getImageUrl("長袖上衣");
                    break;
                case R.id.btn_Vest:
                    button_selected_style(btn_Vest);
                    clothesImages.clear();
                    getImageUrl("背心");
                    break;
                case R.id.btn_Shorts:
                    button_selected_style(btn_Shorts);
                    clothesImages.clear();
                    getImageUrl("短褲");
                    break;
                case R.id.btn_Pants:
                    button_selected_style(btn_Pants);
                    clothesImages.clear();
                    getImageUrl("長褲");
                    break;
                case R.id.btn_Skirt:
                    button_selected_style(btn_Skirt);
                    clothesImages.clear();
                    getImageUrl("裙子");
                    break;
                case R.id.btn_Coat:
                    button_selected_style(btn_Coat);
                    clothesImages.clear();
                    getImageUrl("外套");
                    break;
                case R.id.btn_Suit:
                    button_selected_style(btn_Suit);
                    clothesImages.clear();
                    getImageUrl("套裝");
                    break;
                case R.id.btn_Accessories:
                    button_selected_style(btn_Accessories);
                    clothesImages.clear();
                    getImageUrl("配件飾品");
                    break;
                case R.id.btn_Shop:
                    button_selected_style(btn_Shop);
                    clothesImages.clear();
                    get_Shop_Clothes();
                    break;
            }
        }
    };

    //做出button已點擊的特效
    protected void button_selected_style(Button button_selected) {
        for (Button button : btnArray) {
            if(button == button_selected) {
                button.setBackgroundResource(R.drawable.button_style_blue_selected);
            } else {
                button.setBackgroundResource(R.drawable.button_style_blue);
            }
        }
    }

    //從firestore讀取已上傳的衣服，放進recyclerview
    protected void getImageUrl(String clothes_Category){

        //設定要讀取的資料路徑
        firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("Clothes")
                //讀取所選擇的衣服類別的文件，用'set_Data_Time'降冪排序衣服上傳時間(最新~最舊)
                .whereEqualTo("clothes_Category", clothes_Category)
                .orderBy("set_Data_Time", Query.Direction.DESCENDING)
                .get()
                //如果讀取資料成功
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        //讀取衣服名稱、類別、URL、三個hashtag，並傳進'Cloth_Image_Data'類別的物件'cloth_image_data'
                        if(task.isSuccessful()){
                            clothArray = new ArrayList<>();
                            for(QueryDocumentSnapshot document : task.getResult()) {


                                if(document.getString("index").equals("10"))
                                    continue;
                                /*
                                ClothData clothData = new ClothData(
                                        document.get("clothes_Image_URL").toString(),
                                        document.get("clothes_Image_Name").toString(),
                                        document.get("clothes_Category").toString(),
                                        document.get("clothes_Image_Hashtag1").toString(),
                                        document.get("clothes_Image_Hashtag2").toString(),
                                        document.get("clothes_Image_Hashtag3").toString(),
                                        document.get("favorite_Status").toString(),
                                        document.get("privacy_Status").toString());

                                clothesImages.add(clothData); //將物件'cloth_image_data'加到'Cloth_Image_Data'型別的arraylist

                                 */

                                attributesData = new ClothAttributesData(
                                        document.getId(),
                                        Integer.parseInt(document.getString("H")),
                                        Integer.parseInt(document.getString("S")),
                                        Integer.parseInt(document.getString("V")),
                                        document.getString("width"),
                                        document.getString("length")
                                );
                                clothArray.add(attributesData);
                            }
                            check_Personal_Preference();
                            //get_Personal_Preference();
                            //如果選到的類別是上衣
                            if((clothes_Category.equals("短袖上衣")) || (clothes_Category.equals("長袖上衣")) ||
                                    (clothes_Category.equals("背心")) || (clothes_Category.equals("外套")) ||
                                    (clothes_Category.equals("套裝"))) {
                                Score score = new Score(preferenceData, clothArray, "top_Clothes");
                                score.scoreCalculating();
                            }//如果選到的類別是下著
                            else if((clothes_Category.equals("短褲")) || (clothes_Category.equals("長褲")) ||
                                    (clothes_Category.equals("裙子"))) {
                                Score score = new Score(preferenceData, clothArray, "bottom_Clothes");
                                score.scoreCalculating();
                            }

                            //clothesImages = new ArrayList<>();

                            firebaseFirestore.collection("user_Information")
                                    .document(firebaseAuth.getCurrentUser().getUid()).collection("Clothes")
                                    .whereEqualTo("clothes_Category", clothes_Category)
                                    .orderBy("recommendValue", Query.Direction.DESCENDING)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                if(document.getString("index").equals("10"))
                                                    continue;

                                                ClothData clothData = new ClothData(
                                                        document.get("clothes_Image_URL").toString(),
                                                        document.get("clothes_Image_Name").toString(),
                                                        document.get("clothes_Category").toString(),
                                                        document.get("clothes_Image_Hashtag1").toString(),
                                                        document.get("clothes_Image_Hashtag2").toString(),
                                                        document.get("clothes_Image_Hashtag3").toString(),
                                                        document.get("favorite_Status").toString(),
                                                        document.get("privacy_Status").toString());

                                                clothesImages.add(clothData);
                                            }
                                            clothesRecyclerViewAdapter.setShopClothes(false);
                                            //clothesRecyclerViewAdapter.notifyDataSetChanged();

                                            ClothesRecyclerViewAdapter clothesRecyclerViewAdapter = new ClothesRecyclerViewAdapter(Cloth.this, clothesImages, Cloth.this, Cloth.this, Cloth.this, Cloth.this);

                                            //用GridLayoutManager指定RecyclerView中每列的顯示方式：縱向、2個物件為一列
                                            GridLayoutManager gridLayoutManager = new GridLayoutManager(Cloth.this, 2, GridLayoutManager.VERTICAL, false);
                                            recyclerView.setLayoutManager(gridLayoutManager);
                                            recyclerView.setAdapter(clothesRecyclerViewAdapter);
                                        }
                                    });

                            /*
                            clothesRecyclerViewAdapter = new ClothesRecyclerViewAdapter(Cloth.this, clothesImages, Cloth.this, Cloth.this, Cloth.this, Cloth.this); //將陣列傳到衣服RecyclerView類別的建構方法

                            //用GridLayoutManager指定RecyclerView中每列的顯示方式：縱向、2個物件為一列
                            GridLayoutManager gridLayoutManager = new GridLayoutManager(Cloth.this, 2, GridLayoutManager.VERTICAL, false);
                            recyclerView.setLayoutManager(gridLayoutManager);
                            recyclerView.setAdapter(clothesRecyclerViewAdapter);
                            */
                        } else {
                            Toast.makeText(Cloth.this, "從firestore抓取圖片URL失敗", Toast.LENGTH_SHORT).show();
                            Log.d("錯誤提示", task.getException().getMessage());
                        }
                    }
                })
                //如果讀取資料失敗
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Cloth.this, "從firestore抓取圖片URL失敗", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    protected void get_Shop_Clothes() {
        //設定要讀取的資料路徑
        firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("Clothes")
                //讀取所選擇的衣服類別的文件，用'set_Data_Time'降冪排序衣服上傳時間(最新~最舊)
                .whereEqualTo("index", "10")
                .orderBy("set_Data_Time", Query.Direction.DESCENDING)
                .get()
                //如果讀取資料成功
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        //讀取衣服名稱、類別、URL、三個hashtag，並傳進'Cloth_Image_Data'類別的物件'cloth_image_data'
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){

                                ClothData clothData = new ClothData(
                                        document.get("clothes_Image_URL").toString(),
                                        document.get("clothes_Image_Name").toString(),
                                        document.get("clothes_Category").toString(),
                                        document.get("clothes_Image_Hashtag1").toString(),
                                        document.get("clothes_Image_Hashtag2").toString(),
                                        document.get("clothes_Image_Hashtag3").toString(),
                                        document.get("favorite_Status").toString(),
                                        document.get("privacy_Status").toString());

                                clothesImages.add(clothData); //將物件'cloth_image_data'加到'Cloth_Image_Data'型別的arraylist
                            }

                            //如果'Cloth_Image_Data'型別的arraylist的長度為0
                            if(clothesImages.size() == 0) {
                                Toast.makeText(Cloth.this, "還沒有任何一件商城試穿之商品", Toast.LENGTH_SHORT).show();
                            }
                            clothesRecyclerViewAdapter.setShopClothes(true);
                            clothesRecyclerViewAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(Cloth.this, "從firestore抓取圖片URL失敗", Toast.LENGTH_SHORT).show();
                            Log.d("錯誤提示", task.getException().getMessage());
                        }
                    }
                })
                //如果讀取資料失敗
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Cloth.this, "從firestore抓取圖片URL失敗", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    protected void check_Personal_Preference() {
        firebaseFirestore.collection("personal_Preference")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(document.getId().equals(firebaseAuth.getCurrentUser().getUid())) {
                                    get_Personal_Preference();
                                    break;
                                } else {
                                    Toast.makeText(Cloth.this, "請先至設定完成「穿搭喜好設定」", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });
    }

    protected void get_Personal_Preference() {
        firebaseFirestore.collection("personal_Preference").document(firebaseAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            preferenceData = new PersonalPreferenceData(
                                    document.getString("01_Red"),
                                    document.getString("02_Orange"),
                                    document.getString("03_Yellow"),
                                    document.getString("04_ChartreuseGreen"),
                                    document.getString("05_Green"),
                                    document.getString("06_SpringGreen"),
                                    document.getString("07_Cyan"),
                                    document.getString("08_Azure"),
                                    document.getString("09_Blue"),
                                    document.getString("10_Violet"),
                                    document.getString("11_Magenta"),
                                    document.getString("12_Rose"),
                                    document.getString("13_BB"),
                                    document.getString("14_BT"),
                                    document.getString("15_TB"),
                                    document.getString("16_TT"),
                                    document.getString("17_LL"),
                                    document.getString("18_LS"),
                                    document.getString("19_SL"),
                                    document.getString("20_SS"),
                                    document.getString("21_Hs_Hv"),
                                    document.getString("22_Hs_Mv"),
                                    document.getString("23_Hs_Lv"),
                                    document.getString("24_Ms_Hv"),
                                    document.getString("25_Ms_Mv"),
                                    document.getString("26_Ms_Lv"),
                                    document.getString("27_Ls_Hv"),
                                    document.getString("28_LS_Mv"),
                                    document.getString("29_Ls_Lv"),
                                    document.getString("30_Same"),
                                    document.getString("31_Contrasting"),
                                    document.getString("32_Achromatic"),
                                    document.getString("33_Analogous"),
                                    document.getString("34_importance_HSV"),
                                    document.getString("35_importance_BT"),
                                    document.getString("36_importance_SL")
                            );
                        }
                    }
                });
    }

    //從firestore讀取使用者已選擇放到哈哥身上的衣服，放進recyclerview
    protected void getCurrentlyImageUrl() {

        //設定要讀取的資料路徑
        firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("Virtual_Model")
                .get()
                //如果讀取資料成功
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        //讀取衣服名稱、類別、URL，並傳進'Cloth_Image_Data'類別的物件'cloth_image_data'
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){

                                ClothData clothData = new ClothData(
                                        document.get("clothes_Image_URL").toString(),
                                        document.get("clothes_Image_Name").toString(),
                                        document.get("clothes_Category").toString(),
                                        document.get("clothes_Image_Hashtag1").toString(),
                                        document.get("clothes_Image_Hashtag2").toString(),
                                        document.get("clothes_Image_Hashtag3").toString(),
                                        document.get("favorite_Status").toString(),
                                        document.get("privacy_Status").toString());

                                clothesImages.add(clothData); //將物件'cloth_image_data'加到'Cloth_Image_Data'型別的arraylist
                            }

                            //如果'Cloth_Image_Data'型別的arraylist的長度為0
                            if(clothesImages.size() == 0) {
                                Toast.makeText(Cloth.this, "還沒有選擇任何一件喔", Toast.LENGTH_SHORT).show();
                            }
                            clothesRecyclerViewAdapter = new ClothesRecyclerViewAdapter(Cloth.this, clothesImages, Cloth.this, Cloth.this, Cloth.this, Cloth.this); //將陣列傳到衣服RecyclerView類別的建構方法

                            //用GridLayoutManager指定RecyclerView中每列的顯示方式：縱向、2個物件為一列
                            GridLayoutManager gridLayoutManager = new GridLayoutManager(Cloth.this, 2, GridLayoutManager.VERTICAL, false);
                            recyclerView.setLayoutManager(gridLayoutManager);
                            recyclerView.setAdapter(clothesRecyclerViewAdapter);
                        } else {
                            Toast.makeText(Cloth.this, "從firestore抓取圖片URL失敗", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                //如果讀取資料失敗
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Cloth.this, "從firestore抓取圖片URL失敗", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //實做'ClothesRecyclerViewAdapter'裡的介面'OnItemClickListener'的方法
    //點擊到我的最愛裡衣服imageButton的監聽
    @Override
    public void onItemClick(ClothData clothData, String shopValue) {

        //把'Cloth_Image_Data'型別的arraylist裡使用者從recyclerView選擇的某件衣服，以'cloth_image_data'物件的形式傳進來
        //'cloth_image_data'呼叫'Cloth_Image_Data'類別裡的'getImageCategory()'方法，取得所選到的衣服的類別
        String category = clothData.getImageCategory();

        //如果選到的類別是上衣
        if((category.equals("短袖上衣")) || (category.equals("長袖上衣")) || (category.equals("背心")) || (category.equals("外套")) || (category.equals("套裝"))) {
            //將"文件名稱(top_Clothes)"、所選到的'cloth_image_data'物件、"文件裡的欄位(上衣or下著or配件)"傳進方法'getCurrentlyClothes()'
            getCurrentlyClothes("top_Clothes", clothData, "上衣", shopValue);
        }

        //如果選到的類別是下著
        if((category.equals("短褲")) || (category.equals("長褲")) || (category.equals("裙子"))) {
            //將"文件名稱(bottom_Clothes)"、所選到的'cloth_image_data'物件、"文件裡的欄位(上衣or下著or配件)"傳進方法'getCurrentlyClothes()'
            getCurrentlyClothes("bottom_Clothes", clothData, "下著", shopValue);
        }

        //如果選到的類別是配件
        if(category.equals("配件飾品")) {
            //將"文件名稱(accessories)"、所選到的'cloth_image_data'物件、"文件裡的欄位(上衣or下著or配件)"傳進方法'getCurrentlyClothes()'
            getCurrentlyClothes("accessories", clothData, "配件", shopValue);
        }

    }

    @Override
    public void onEditClick(ClothData clothData, int position) {
        Intent intent = new Intent(Cloth.this, EditCloth.class);
        intent.putExtra("clothData", clothData);
        intent.putExtra("position", position);
        startActivity(intent);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onDeleteClick(ClothData clothData, int position) {
        firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("Clothes")
                .document(clothData.getImageName())
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(Cloth.this, "已移除商城試穿之衣服", Toast.LENGTH_SHORT).show();
                        clothesImages.remove(position);
                        clothesRecyclerViewAdapter.notifyItemRemoved(position);
                    }
                });
    }

    //實做'ClothesRecyclerViewAdapter'裡的介面'onFavoriteClick'的方法
    //點擊到我的最愛裡衣服右下角的愛心imageView的監聽
    @Override
    public void onFavoriteClick(ClothData clothData) {

        //用自定義格式取得系統當前時間
        String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
        String[] clothArray = {"短袖上衣", "長袖上衣", "背心", "短褲", "長褲", "裙子", "外套", "套裝", "配件飾品"};

        //索引順序(1~9)
        for(int i = 0; i < 9; i++) {
            if(clothArray[i].equals(clothData.getImageCategory())) {
                categoryIndex = Integer.toString(i + 1);
            }
        }

        //若被點選衣服的我的最愛狀態為No，就是要將該衣服加進我的最愛
        if(clothData.getFavoriteStatus().equals("No")) {

            Map<String, Object> favoriteData = new HashMap<>();
            favoriteData.put("clothes_Name", clothData.getImageName());
            favoriteData.put("clothes_Category", clothData.getImageCategory());
            favoriteData.put("clothes_Image_URL", clothData.getImageUrl());
            favoriteData.put("clothes_Hashtag1", clothData.getImageHashtag1());
            favoriteData.put("clothes_Hashtag2", clothData.getImageHashtag2());
            favoriteData.put("clothes_Hashtag3", clothData.getImageHashtag3());
            favoriteData.put("set_Data_Time", timeStamp);
            favoriteData.put("index", categoryIndex);
            favoriteData.put("favorite_Status", "Yes");

            //將點到的衣服寫進firebase我的最愛集合裡，文件名稱為該衣服名稱
            firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("MyFavorite_Clothes").document(clothData.getImageName())
                    .set(favoriteData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(Cloth.this, "成功儲存衣服 " + clothData.getImageName() + " 至我的最愛", Toast.LENGTH_SHORT).show();
                        }
                    });

            //該被點選衣服，更新在Clothes集合裡的資料欄位，我的最愛狀態(No -> Yes)
            firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("Clothes").document(clothData.getImageName())
                    .update("favorite_Status", "Yes");


            clothesImages = new ArrayList<>();
            getImageUrl(clothData.getImageCategory()); //重新從firebase讀取衣服資料(刷新開啟衣櫃頁面)
        }
        //若被點選衣服的我的最愛狀態為Yes，就是要將該衣服移出我的最愛
        else if(clothData.getFavoriteStatus().equals("Yes")) {

            //使用者點選已被加到我的最愛的衣服，將該衣服從我的最愛中移出
            firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("MyFavorite_Clothes").document(clothData.getImageName())
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(Cloth.this, "成功從我的最愛移除衣服 " + clothData.getImageName(), Toast.LENGTH_SHORT).show();
                        }
                    });

            //該被點選衣服，更新在Clothes集合裡的資料欄位，我的最愛狀態(Yes -> No)
            firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("Clothes").document(clothData.getImageName())
                    .update("favorite_Status", "No");

            clothesImages = new ArrayList<>();
            getImageUrl(clothData.getImageCategory()); //重新從firebase讀取衣服資料(刷新開啟衣櫃頁面)

        }
    }

    //將資料寫入進'Virtual_Model'的集合內，存放哈哥身上「已選擇」的衣服
    protected void getCurrentlyClothes(String clothesPosition, ClothData cloth_image_data, String clothes_Position, String shopValue) {

        //將衣服名稱、衣服類別、衣服URL、衣服位置(上衣or下著or配件)放進HashMap
        Map<String, Object> clothData = new HashMap<>();
        clothData.put("clothes_Image_Name", cloth_image_data.getImageName());
        clothData.put("clothes_Category", cloth_image_data.getImageCategory());
        clothData.put("clothes_Image_URL", cloth_image_data.getImageUrl());
        clothData.put("clothes_Position", clothes_Position);
        clothData.put("clothes_Image_Hashtag1", cloth_image_data.getImageHashtag1());
        clothData.put("clothes_Image_Hashtag2", cloth_image_data.getImageHashtag2());
        clothData.put("clothes_Image_Hashtag3", cloth_image_data.getImageHashtag3());
        clothData.put("favorite_Status", cloth_image_data.getFavoriteStatus());
        clothData.put("private_Status", cloth_image_data.getPrivateStatus());
        clothData.put("shop_Clothes", shopValue);
        

        //指定要存放資料的firestore路徑，文件名稱指定為'clothesPosition'(top_Clothes、bottom_Clothes、accessories)
        firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("Virtual_Model").document(clothesPosition)
                //set的效果是如果文件名稱(top_Clothes、bottom_Clothes、accessories)不存在就創建一個新的文件，若已存在就覆蓋過去
                .set(clothData)
                //新增firestore文件成功
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(Cloth.this, "已選擇衣服 " + cloth_image_data.getImageName(), Toast.LENGTH_SHORT).show();
                    }
                })
                //新增firestore文件失敗
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Cloth.this, "新增已選擇衣服URL至firestore失敗\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


        //設置延遲，1000毫秒後跳轉回Home_page.class
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Cloth.this, HomePage.class);
                startActivity(intent);
            }
        }, 1000);
    }
}

