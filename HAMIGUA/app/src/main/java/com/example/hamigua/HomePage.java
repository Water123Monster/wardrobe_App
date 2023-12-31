package com.example.hamigua;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.example.hamigua.communicate.Communicate;
import com.example.hamigua.runway.Runway;
import com.example.hamigua.shop.customer.Shop;
import com.example.hamigua.wardrobe.cloth.AddClothes;
import com.example.hamigua.wardrobe.Article;
import com.example.hamigua.wardrobe.cloth.Cloth;
import com.example.hamigua.wardrobe.model.ClothData;
import com.example.hamigua.wardrobe.statistics.MainStatistics;
import com.example.hamigua.wardrobe.calendar.MyCalendar;
import com.example.hamigua.wardrobe.favorite.MyFavorite;
import com.example.hamigua.wardrobe.ScaleImage;
import com.example.hamigua.wardrobe.cloth.PrivacySetting;
import com.example.hamigua.wardrobe.Setting;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class HomePage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private Button btn_Choose;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private ImageView HAGE; //transparentcloth, transparentpants, ; //透明衣服、透明褲子、哈哥
    private ImageButton btnFavotite;//愛心觸及
    boolean buttonOn;//判斷愛心
    private ImageButton imgclear;
    private FloatingActionButton btn_Add_Clothes;

    private String topClothesImageUrl = ""; //上衣的圖片URL
    private String bottomClothesImageUrl = ""; //下著的圖片URL
    private String record_Date, newDate;

    private ClothData top_Cloth_data, bottom_Cloth_data;

    private float x, y; //原本圖片存在的X,Y軸位置
    private int mx, my; //圖片被拖曳的X,Y軸距離長度

    private ImageView top_ScaleImage, bottom_ScaleImage;

    private ImageView record_outfit, open_wardrobe;
    private String categoryIndex = "";

    private String bodyType, gender, armSwing, bodyRatio, height;
    private int intHeight;

    private ImageView toplocationpoint,bottomlocationpoint;//上衣定位點高度ImageView
    private float toppointheight=515,bottompointheight=720;//水怪手機設定//上衣定位點高度，1080p解析度，新哈哥
    //private float toppointheight=420,bottompointheight=630;//原始模擬器設定//上衣定位點高度，1080p解析度，新哈哥
    private float HAheight=1,HAweight=1;//哈哥身高定義

    private ImageView shopClothesTint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        HAGE = findViewById(R.id.Ha);
        //transparentcloth = findViewById(R.id.transparentcloth);
        //transparentpants = findViewById(R.id.transparentpants);
        //transparentcloth.setOnTouchListener(imgListener);//觸控時監聽
        //transparentpants.setOnTouchListener(imgListener);//觸控時監聽

        top_ScaleImage =findViewById(R.id.top_scale_image);
        bottom_ScaleImage =findViewById(R.id.bottom_scale_image);

        btn_Choose = findViewById(R.id.btnChoose);
        btn_Add_Clothes = findViewById(R.id.add_Clothes);

        //btnFavotite=findViewById(R.id.btnFavotite);


        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        imgclear = findViewById(R.id.imgclear);
        imgclear.setOnClickListener(clearClothesListener);

        record_outfit = findViewById(R.id.record_outfit);
        open_wardrobe = findViewById(R.id.open_wardrobe);
        record_outfit.setOnClickListener(record_outfit_Listner);
        open_wardrobe.setOnClickListener(open_wardrobe_Listner);

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this,drawerLayout,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        bottomNavigationView = findViewById(R.id.bottomNav); //設定底部導行列id
        bottomNavigationView.setSelectedItemId(R.id.Cloth); //設定所選到的itemId
        bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavigationListener);


        //取得firestore與authentication的連接
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();


        toplocationpoint =findViewById(R.id.toplocationpoint);
        toplocationpoint.setTranslationY(toppointheight);//設定定位點高度
        toplocationpoint.bringToFront();

        bottomlocationpoint=findViewById(R.id.bottomlocationpoint);
        bottomlocationpoint.setTranslationY(bottompointheight);
        bottomlocationpoint.bringToFront();

        shopClothesTint = findViewById(R.id.shopClothesTint);

        //開啟漢堡選單button
        btn_Choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //update_ClothesImage_Position();
                drawerLayout.openDrawer(GravityCompat.START);//選單從左邊拉出
            }
        });

        //新增衣服button
        btn_Add_Clothes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_Add_Clothes = new Intent();
                intent_Add_Clothes.setClass(HomePage.this, AddClothes.class);
                startActivity(intent_Add_Clothes);
            }
        });

        /*
        //把哈哥身上的衣服放進我的最愛
        btnFavotite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_Clothes_To_Favorite();
            }
        });
        */

        //取得firebase資料裡該登入帳號的使用者名稱
        get_Account_UserName();

        //取得firebase資料裡該登入帳號的性別、身高、身形，設置對應的哈哥圖片
        set_HAGE_Image();

        //呼叫取得transparentcloth, transparentpants最後存在firebase位置的方法
        //get_Last_ClothesImage_Position();

        //呼叫取得已選擇的衣服的方法
        get_ClothesImage_From_Firebase();

        //檢查是否有電商衣服試穿
        setShopClothesTint();
    }

    //取得firebase資料裡該登入帳號的使用者名稱，並顯示在側滑選單的header位置
    protected void get_Account_UserName() {
        firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid())
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
                                Glide.with(HomePage.this).load(document.getString("userImage")).into(userImage);
                            }
                        }
                    }
                });
    }

    protected void set_HAGE_Image() {

        firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()) {
                                bodyType = document.getString("bodyType");
                                gender = document.getString("gender");
                                armSwing = document.getString("armSwing");
                                bodyRatio = document.getString("bodyRatio");
                                height = document.getString("height");
                            }
                        }

                        if(gender.equals("男性")) {
                            switch (bodyType) {
                                case "偏瘦":
                                    if (armSwing.equals("擺福較高")) {
                                        if (bodyRatio.equals("55身")) {
                                            HAGE.setImageResource(R.drawable.hege_male_thin_armswinghigh_55);
                                        } else {
                                            HAGE.setImageResource(R.drawable.hege_male_thin_armswinghigh_64);
                                        }
                                    } else if (armSwing.equals("擺福較低")) {
                                        if (bodyRatio.equals("55身")) {
                                            HAGE.setImageResource(R.drawable.hege_male_thin_armswinglow_55);
                                        } else {
                                            HAGE.setImageResource(R.drawable.hege_male_thin_armswinglow_64);
                                        }
                                    }
                                    break;
                                case "適中":
                                    if (armSwing.equals("擺福較高")) {
                                        if (bodyRatio.equals("55身")) {
                                            HAGE.setImageResource(R.drawable.hege_male_fit_armswinghigh_55);
                                        } else {
                                            HAGE.setImageResource(R.drawable.hege_male_fit_armswinghigh_64);
                                        }
                                    } else if (armSwing.equals("擺福較低")) {
                                        if (bodyRatio.equals("55身")) {
                                            HAGE.setImageResource(R.drawable.hege_male_fit_armswinglow_55);
                                        } else {
                                            HAGE.setImageResource(R.drawable.hege_male_fit_armswinglow_64);
                                        }
                                    }
                                    break;
                                case "偏胖":
                                    if (armSwing.equals("擺福較高")) {
                                        if (bodyRatio.equals("55身")) {
                                            HAGE.setImageResource(R.drawable.hege_male_fat_armswinghigh_55);
                                        } else {
                                            HAGE.setImageResource(R.drawable.hege_male_fat_armswinghigh_64);
                                        }
                                    } else if (armSwing.equals("擺福較低")) {
                                        if (bodyRatio.equals("55身")) {
                                            HAGE.setImageResource(R.drawable.hege_male_fat_armswinglow_55);
                                        } else {
                                            HAGE.setImageResource(R.drawable.hege_male_fat_armswinglow_64);
                                        }
                                    }
                                    break;
                            }
                        } else if (gender.equals("女性")) {
                            switch (bodyType) {
                                case "偏瘦":
                                    if (armSwing.equals("擺福較高")) {
                                        if (bodyRatio.equals("55身")) {
                                            HAGE.setImageResource(R.drawable.hege_female_thin_armswinghigh_55);
                                        } else {
                                            HAGE.setImageResource(R.drawable.hege_female_thin_armswinghigh_64);
                                        }
                                    } else if (armSwing.equals("擺福較低")) {
                                        if (bodyRatio.equals("55身")) {
                                            HAGE.setImageResource(R.drawable.hege_female_thin_armswinglow_55);
                                        } else {
                                            HAGE.setImageResource(R.drawable.hege_female_thin_armswinglow_64);
                                        }
                                    }
                                    break;
                                case "適中":
                                    if (armSwing.equals("擺福較高")) {
                                        if (bodyRatio.equals("55身")) {
                                            HAGE.setImageResource(R.drawable.hege_female_fit_armswinghigh_55);
                                        } else {
                                            HAGE.setImageResource(R.drawable.hege_female_fit_armswinghigh_64);
                                        }
                                    } else if (armSwing.equals("擺福較低")) {
                                        if (bodyRatio.equals("55身")) {
                                            HAGE.setImageResource(R.drawable.hege_female_fit_armswinglow_55);
                                        } else {
                                            HAGE.setImageResource(R.drawable.hege_female_fit_armswinglow_64);
                                        }
                                    }
                                    break;
                                case "偏胖":
                                    if (armSwing.equals("擺福較高")) {
                                        if (bodyRatio.equals("55身")) {
                                            HAGE.setImageResource(R.drawable.hege_female_fat_armswinghigh_55);
                                        } else {
                                            HAGE.setImageResource(R.drawable.hege_female_fat_armswinghigh_64);
                                        }
                                    } else if (armSwing.equals("擺福較低")) {
                                        if (bodyRatio.equals("55身")) {
                                            HAGE.setImageResource(R.drawable.hege_female_fat_armswinglow_55);
                                        } else {
                                            HAGE.setImageResource(R.drawable.hege_female_fat_armswinglow_64);
                                        }
                                    }
                                    break;
                            }
                        }

                        //調整哈哥/哈姐身高
                        intHeight = Integer.parseInt(height);

                        HAheight = HAheight - 0.05f;
                        HAweight = HAweight - 0.05f;
                        HAGE.setScaleX(HAweight);
                        HAGE.setScaleY(HAheight);

                        if (intHeight >= 170) {
                            intHeight=intHeight-170;
                            HAheight=HAheight+(intHeight*0.0058823529411765f);
                            HAweight=HAweight+(intHeight*0.0058823529411765f);
                            HAGE.setScaleX(HAweight);
                            HAGE.setScaleY(HAheight);
                            toppointheight=toppointheight-(intHeight*2.5f);
                            bottompointheight=bottompointheight-(intHeight*0.75f);
                            toplocationpoint.setTranslationY(toppointheight);
                            bottomlocationpoint.setTranslationY(bottompointheight);
                        }
                        else{
                            intHeight=170-intHeight;
                            HAheight=HAheight-(intHeight*0.0058823529411765f);
                            HAweight=HAweight-(intHeight*0.0058823529411765f);
                            HAGE.setScaleX(HAweight);
                            HAGE.setScaleY(HAheight);
                            toppointheight=toppointheight+(intHeight*2.5f);
                            bottompointheight=bottompointheight+(intHeight*0.75f);
                            toplocationpoint.setTranslationY(toppointheight);
                            bottomlocationpoint.setTranslationY(bottompointheight);
                        }

                        //intHeight=intHeight-170;
                        /*if(intHeight>170){
                            for(intHeight=intHeight-170;intHeight>=0;intHeight--) {
                            HAheight = HAheight + 0.0058823529411765f;
                            HAweight = HAweight + 0.0058823529411765f;
                            HAGE.setScaleX(HAweight);
                            HAGE.setScaleY(HAheight);
                            toppointheight=toppointheight-2.5f;
                            bottompointheight=bottompointheight-0.8f;
                            toplocationpoint.setTranslationY(toppointheight);
                            bottomlocationpoint.setTranslationY(bottompointheight);

                            }

                        }
                        else{
                            for(intHeight=170-intHeight;intHeight>=0;intHeight--){
                                HAheight = HAheight - 0.0058823529411765f;
                                HAweight = HAweight - 0.0058823529411765f;
                                HAGE.setScaleX(HAweight);
                                HAGE.setScaleY(HAheight);
                                toppointheight=toppointheight+2.5f;
                                bottompointheight=bottompointheight+0.8f;
                                toplocationpoint.setTranslationY(toppointheight);
                                bottomlocationpoint.setTranslationY(bottompointheight);
                            }
                        }*/




                    }
                });
    }

    //從firebase上讀取已選擇衣服的集合內的資料，並指定到'topClothesImageUrl'、'bottomClothesImageUrl'
    protected void get_ClothesImage_From_Firebase() {

        //設定要讀取的資料路徑
        firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("Virtual_Model")
                //讀取欄位'clothes_Position'的值為"上衣"的文件
                .whereEqualTo("clothes_Position", "上衣")
                .get()
                //如果讀取資料成功
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        //把文件裡的欄位'clothes_Image_URL'的值取出來指定給'topClothesImageUrl'並呼叫方法'put_Clothes_On_HAGE()'
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){

                                top_Cloth_data = new ClothData(
                                        document.get("clothes_Image_URL").toString(),
                                        document.get("clothes_Image_Name").toString(),
                                        document.get("clothes_Category").toString(),
                                        document.get("clothes_Image_Hashtag1").toString(),
                                        document.get("clothes_Image_Hashtag2").toString(),
                                        document.get("clothes_Image_Hashtag3").toString(),
                                        document.get("favorite_Status").toString(),
                                        document.get("private_Status").toString());

                                topClothesImageUrl = top_Cloth_data.getImageUrl();

                                //topClothesImageUrl = document.get("clothes_Image_URL").toString();
                                put_Clothes_On_HAGE(top_Cloth_data.getImageCategory());
                            }
                        } else {
                            Toast.makeText(HomePage.this, "從firestore抓取圖片URL失敗", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                //如果讀取資料失敗
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HomePage.this, "從firestore抓取圖片URL失敗", Toast.LENGTH_SHORT).show();
                    }
                });

        //設定要讀取的資料路徑
        firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("Virtual_Model")
                //讀取欄位'clothes_Position'的值為"下著"的文件
                .whereEqualTo("clothes_Position", "下著")
                .get()
                //如果讀取資料成功
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if(task.isSuccessful()){
                            //把文件裡的欄位'clothes_Image_URL'的值取出來指定給'bottomClothesImageUrl'並呼叫方法'put_Clothes_On_HAGE()'
                            for(QueryDocumentSnapshot document : task.getResult()){

                                bottom_Cloth_data = new ClothData(
                                        document.get("clothes_Image_URL").toString(),
                                        document.get("clothes_Image_Name").toString(),
                                        document.get("clothes_Category").toString(),
                                        document.get("clothes_Image_Hashtag1").toString(),
                                        document.get("clothes_Image_Hashtag2").toString(),
                                        document.get("clothes_Image_Hashtag3").toString(),
                                        document.get("favorite_Status").toString(),
                                        document.get("private_Status").toString());

                                bottomClothesImageUrl = bottom_Cloth_data.getImageUrl();
                                //bottomClothesImageUrl = document.get("clothes_Image_URL").toString();
                                put_Clothes_On_HAGE(bottom_Cloth_data.getImageCategory());
                            }
                        } else {
                            Toast.makeText(HomePage.this, "從firestore抓取圖片URL失敗", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                //如果讀取資料失敗
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HomePage.this, "從firestore抓取圖片URL失敗", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //用第三方插件Glide設置URL到imageview
    protected void put_Clothes_On_HAGE(String category) {

        //transparentcloth.layout(mx, my, mx + transparentcloth.getWidth(), my + transparentcloth.getHeight());

        Glide.with(HomePage.this)
                .load(topClothesImageUrl) //傳入上衣的URL
                .into(top_ScaleImage); //將圖片顯示在上衣imageView

        top_ScaleImage.bringToFront(); //圖片帶到最上層

        top_ScaleImage.setTranslationY(toppointheight);//把上衣設定到定位點上，上衣定位點高度，1080p解析度，新哈哥


        //transparentpants.layout(mx, my, mx + transparentpants.getWidth(), my + transparentpants.getHeight());



        Glide.with(HomePage.this)
                .load(bottomClothesImageUrl) //傳入下著的URL
                .into(bottom_ScaleImage); //將圖片顯示在下著imageView


        if(category.equals("長褲") || category.equals("裙子")) {
            bottom_ScaleImage.setScaleY(0.73f); //長褲
            bottom_ScaleImage.setScaleX(0.73f); //長褲
            bottom_ScaleImage.setTranslationY(bottompointheight);
        } else if(category.equals("短褲")) {
            //pixel5模擬器短褲縮小倍率
            //bottom_ScaleImage.setScaleY(0.65f); //短褲
            //bottom_ScaleImage.setScaleX(0.65f); //短褲
            //水怪手機短褲縮小倍率
            bottom_ScaleImage.setScaleY(0.61f);  //短褲
            bottom_ScaleImage.setScaleX(0.61f); //短褲
            bottom_ScaleImage.setTranslationY(bottompointheight+40);//移動短褲位置
        }

        bottom_ScaleImage.bringToFront(); //圖片帶到最上層
        top_ScaleImage.bringToFront(); //圖片帶到最上層

        //bottom_ScaleImage.bringToFront();
        toplocationpoint.bringToFront();

    }

    protected void setShopClothesTint() {
        firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("Virtual_Model")
                .whereEqualTo("shop_Clothes", "Yes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.getResult().size() == 0)
                            shopClothesTint.setVisibility(View.INVISIBLE);
                        else
                            shopClothesTint.setVisibility(View.VISIBLE);
                    }
                });

    }

    //將transparentcloth, transparentpants最後的位置儲存到firebase
    protected void update_ClothesImage_Position() {

        Map<String, String> positionData = new HashMap<>();
        positionData.put("clothes_Image_X_Value", String.valueOf(mx));
        positionData.put("clothes_Image_Y_Value", String.valueOf(my));

        firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("Virtual_Model").document("top_Clothes")
                .set(positionData, SetOptions.merge());

    }

    /**從firebase讀取transparentcloth, transparentpants最後的位置並設定
     * 位置抓的下來，但設定layout部分還有問題還沒解決(圖片會亂跑)，先擱置這個功能 */
    protected void get_Last_ClothesImage_Position() {

        firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("Virtual_Model").document("top_Clothes")
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        mx = Integer.valueOf(documentSnapshot.getString("clothes_Image_X_Value"));
                        my = Integer.valueOf(documentSnapshot.getString("clothes_Image_Y_Value"));
                        Log.d("X and Y", String.valueOf(mx) + "~~" + String.valueOf(my));
                        //Log.d("X and Y", String.valueOf(transparentcloth.getWidth()) + "~~" + String.valueOf(transparentcloth.getHeight()));

                        /*
                        transparentcloth.setX(mx);
                        transparentcloth.setY(my);
                         */

                        //transparentcloth.layout(mx, my, mx + transparentcloth.getWidth(), my + transparentcloth.getWidth());
                        //transparentpants.layout(mx, my, mx + transparentpants.getWidth(), my + transparentpants.getHeight());
                    }
                });

        Log.d("X and Y", String.valueOf(mx) + "~~" + String.valueOf(my));

    }

    //移除哈哥身上所有衣服
    protected View.OnClickListener clearClothesListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            //從firebase上刪除已選擇衣服的集合內的所有資料
            firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("Virtual_Model").document("top_Clothes")
                    .delete();
            firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("Virtual_Model").document("bottom_Clothes")
                    .delete();
            firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("Virtual_Model").document("accessories")
                    .delete();

            //將上衣imageView跟下著imageView重新設為透明
            //transparentcloth.setImageResource(R.drawable.transparent);
            //transparentpants.setImageResource(R.drawable.transparent);
            top_ScaleImage.setImageResource(R.drawable.transparent);
            bottom_ScaleImage.setImageResource(R.drawable.transparent);
            //將上衣圖片Url跟下著圖片Url重新設為空字串
            topClothesImageUrl = "";
            bottomClothesImageUrl = "";

            shopClothesTint.setVisibility(View.INVISIBLE);
        }
    };

    /*
    //把衣服資料存進我的最愛
    protected void add_Clothes_To_Favorite() {

        //用自定義格式取得系統當前時間
        String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());

        String[] clothArray = {"短袖上衣", "長袖上衣", "背心", "短褲", "長褲", "裙子", "外套", "套裝", "配件飾品"};

        //如果'topClothesImageUrl'不是空字串(哈哥身上有上衣)，就將該件衣服資料存進firestore
        if(topClothesImageUrl.isEmpty()) {
            //
        } else {

            for(int i = 0; i < 9; i++) {
                if(clothArray[i].equals(top_Cloth_data.getImageCategory())) {
                    categoryIndex = Integer.toString(i + 1);
                }
            }

            Map<String, Object> top_favoriteData = new HashMap<>();
            top_favoriteData.put("clothes_Name", top_Cloth_data.getImageName());
            top_favoriteData.put("clothes_Category", top_Cloth_data.getImageCategory());
            top_favoriteData.put("clothes_Image_URL", top_Cloth_data.getImageUrl());
            top_favoriteData.put("clothes_Hashtag1", top_Cloth_data.getImageHashtag1());
            top_favoriteData.put("clothes_Hashtag2", top_Cloth_data.getImageHashtag2());
            top_favoriteData.put("clothes_Hashtag3", top_Cloth_data.getImageHashtag3());
            top_favoriteData.put("set_Data_Time", timeStamp);
            top_favoriteData.put("index", categoryIndex);
            top_favoriteData.put("favorite_Status", "Yes");


            //如果愛心為實體的,就將衣服儲存至我的最愛
            if (!buttonOn) {
                buttonOn = true;
                btnFavotite.setBackground(getResources().getDrawable(R.drawable.ic_baseline_favorite_24));
                //用set方法存進firestore，文件名稱設為該衣服名稱，set方法的特性(若文件名稱不存在則建立新文件，若已存在則覆蓋舊的文件)讓同件衣服不會重複出現在'MyFavorite_Clothes'集合
                firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("MyFavorite_Clothes").document(top_Cloth_data.getImageName())
                        .set(top_favoriteData)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(Home_page.this, "成功儲存衣服 " + top_Cloth_data.getImageName() + " 至我的最愛", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else if(buttonOn){              //如果愛心為空心的,就將衣服移除至我的最愛
                buttonOn = false;
                btnFavotite.setBackground(getResources().getDrawable(R.drawable.ic_baseline_favorite_border));
                firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("MyFavorite_Clothes").document(top_Cloth_data.getImageName())
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(Home_page.this, "成功移除衣服 " + top_Cloth_data.getImageName() + " 至我的最愛", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }

        //如果'bottomClothesImageUrl'不是空字串(哈哥身上有下著)，就將該件衣服資料存進firestore
        if(bottomClothesImageUrl.isEmpty()) {
            //
        } else {
            for(int i = 0; i < 9; i++) {
                if(clothArray[i].equals(bottom_Cloth_data.getImageCategory())) {
                    categoryIndex = Integer.toString(i + 1);
                }
            }

            Map<String, Object> bottom_favoriteData = new HashMap<>();
            bottom_favoriteData.put("clothes_Name", bottom_Cloth_data.getImageName());
            bottom_favoriteData.put("clothes_Category", bottom_Cloth_data.getImageCategory());
            bottom_favoriteData.put("clothes_Image_URL", bottom_Cloth_data.getImageUrl());
            bottom_favoriteData.put("clothes_Hashtag1", bottom_Cloth_data.getImageHashtag1());
            bottom_favoriteData.put("clothes_Hashtag2", bottom_Cloth_data.getImageHashtag2());
            bottom_favoriteData.put("clothes_Hashtag3", bottom_Cloth_data.getImageHashtag3());
            bottom_favoriteData.put("set_Data_Time", timeStamp);
            bottom_favoriteData.put("index", categoryIndex);
            bottom_favoriteData.put("favorite_Status", "Yes");

            if (!buttonOn) {
                buttonOn = true;
                btnFavotite.setBackground(getResources().getDrawable(R.drawable.ic_baseline_favorite_24));
                //用set方法存進firestore，文件名稱設為該衣服名稱，set方法的特性(若文件名稱不存在則建立新文件，若已存在則覆蓋舊的文件)讓同件衣服不會重複出現在'MyFavorite_Clothes'集合
                firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("MyFavorite_Clothes").document(bottom_Cloth_data.getImageName())
                        .set(bottom_favoriteData)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(Home_page.this, "成功儲存衣服 " + bottom_Cloth_data.getImageName() + " 至我的最愛", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                buttonOn = false;
                btnFavotite.setBackground(getResources().getDrawable(R.drawable.ic_baseline_favorite_border));
                firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("MyFavorite_Clothes").document(bottom_Cloth_data.getImageName())
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(Home_page.this, "成功移除衣服 " + bottom_Cloth_data.getImageName() + " 至我的最愛", Toast.LENGTH_SHORT).show();
                            }
                        });
            }


        }
    }
    */

    //開啟衣櫃
    protected View.OnClickListener open_wardrobe_Listner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent Clothes = new Intent();
            Clothes.setClass(HomePage.this, Cloth.class);
            startActivity(Clothes);
        }
    };

    //把哈哥身上的衣服存進穿搭紀錄
    protected View.OnClickListener record_outfit_Listner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Toast.makeText(HomePage.this, "選擇日期以儲存穿搭紀錄", Toast.LENGTH_SHORT).show();

            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(java.util.Calendar.YEAR);
            int month = calendar.get(java.util.Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(HomePage.this, android.R.style.Theme_DeviceDefault_Dialog, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                    record_Date = year + "/" + (month + 1) + "/" + dayOfMonth;
                    newDate = record_Date.replace("/", "."); //新的日期格式：2021.**.**

                    Map<String, Object> recordData = new HashMap<>();
                    recordData.put("record_Date", record_Date); //日期格式：2021/**/**
                    recordData.put("Top_clothes_Image_URL", topClothesImageUrl);
                    recordData.put("Bottom_clothes_Image_URL", bottomClothesImageUrl);
                    recordData.put("record_YearMonth", year + "/" + (month + 1));
                    recordData.put("record_DaysInMonth", Integer.toString(dayOfMonth));

                    firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid())
                            .collection("Calendar_Record").document(newDate) //日期格式：2021.**.**
                            .set(recordData)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(HomePage.this, "成功儲存穿搭至 " + record_Date, Toast.LENGTH_SHORT).show();
                                }
                            });

                }
            }, year, month, day);
            dialog.show();

        }
    };

    protected View.OnTouchListener topTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            top_ScaleImage.bringToFront();
            return true;
        }
    };

    protected ImageButton.OnTouchListener imgListener = new ImageButton.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // Log.e("View", v.toString());
            switch (event.getAction()) {          //判斷觸控的動作

                case MotionEvent.ACTION_DOWN:// 按下圖片時

                    x = event.getX();                  //觸控的X軸位置
                    y = event.getY();                  //觸控的Y軸位置

                case MotionEvent.ACTION_MOVE:// 移動圖片時

                    //getX()：是獲取當前控件(View)的座標

                    //getRawX()：是獲取相對顯示螢幕左上角的座標
                    mx = (int) (event.getRawX() - x);
                    my = (int) (event.getRawY() - y);
                    v.layout(mx, my, mx + v.getWidth(), my + v.getHeight());

                    break;
            }
            Log.e("address", String.valueOf(mx) + "~~" + String.valueOf(my)); // 記錄目前位置
            return true;
        }
    };

    //如果漢堡選單是開啟狀態，點擊其他位置可以讓漢堡選單收回去
    public void onBackPressed(){
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //漢堡選單裡的觸擊事件
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()){
            /*
            case R.id.nav_article:
                Intent intent_article=new Intent(HomePage.this, Article.class);
                startActivity(intent_article);
                break;
            case R.id.nav_set:
                Intent intent_set=new Intent(HomePage.this, PrivacySetting.class);
                startActivity(intent_set);
                break;

             */
            case R.id.nav_statistics:
                Intent intent_statistics=new Intent(HomePage.this, MainStatistics.class);
                startActivity(intent_statistics);
                break;
            case R.id.nav_calendar:
                Intent intent_calendar=new Intent(HomePage.this, MyCalendar.class);
                startActivity(intent_calendar);
                break;
            case R.id.nav_like:
                Intent intent_like=new Intent(HomePage.this, MyFavorite.class);
                startActivity(intent_like);
                break;
            case R.id.nav_setting:
                Intent intent_setting=new Intent(HomePage.this, Setting.class);
                startActivity(intent_setting);
                break;
        }
        return true;
    }

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
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(getApplicationContext(), Shop.class));
                                    overridePendingTransition(0,0);
                                }
                            }, 200);
                            return true;
                    }
                    return false;
                }
            };

}