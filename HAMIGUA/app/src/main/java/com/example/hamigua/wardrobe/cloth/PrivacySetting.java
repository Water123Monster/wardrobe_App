package com.example.hamigua.wardrobe.cloth;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamigua.R;
import com.example.hamigua.wardrobe.adapter.PrivacyRecyclerViewAdapter;
import com.example.hamigua.wardrobe.model.ClothData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class PrivacySetting extends AppCompatActivity implements PrivacyRecyclerViewAdapter.OnItemClickListener{

    private RecyclerView recyclerView;
    private ArrayList<ClothData> clothesImages;
    private PrivacyRecyclerViewAdapter privacyRecyclerViewAdapter;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private Button btn_Clothes_Selected, btn_Short_Sleeve, btn_Long_Sleeve, btn_Vest, btn_Shorts,
            btn_Pants, btn_Skirt, btn_Coat, btn_Suit, btn_Accessories;

    private final Button[] btnArray = new Button[10];

    private CheckBox checkBox_all;
    //private String selectAllStatus = "false"; //全選的暫存狀態
    private String category = "所有衣服"; //使用者選取的衣服類別
    private int clothCount = 0;
    private ArrayList<String> clothName;

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
        setContentView(R.layout.activity_privacy_setting);

        //設置標題列ActionBar的左上角返回鍵
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);



        recyclerView = findViewById(R.id.privacyRecyclerView);
        checkBox_all = findViewById(R.id.checkbox_all);
        //checkBox_all.setOnCheckedChangeListener(checkBoxListener);

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

        //取得firestore與authentication的連接
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        clothesImages = new ArrayList<>();

        //預設一開始進入此頁面是顯示所有衣服
        get_All_Clothes_Privacy_Setting();

    }

    //每個button的觸擊事件，實例化arrayList並用switch來寫不同的衣服類別傳入方法get_Clothes_Privacy_Setting()
    private Button.OnClickListener categoryListener = new Button.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v){
            switch (v.getId()){
                case R.id.btn_Clothes_Selected:
                    button_selected_style(btn_Clothes_Selected);
                    category = "所有衣服";
                    clothesImages = new ArrayList<>();
                    get_All_Clothes_Privacy_Setting();
                    break;
                case R.id.btn_Short_Sleeve:
                    button_selected_style(btn_Short_Sleeve);
                    category = "短袖上衣";
                    clothesImages = new ArrayList<>();
                    get_Clothes_Privacy_Setting("短袖上衣");
                    break;
                case R.id.btn_Long_Sleeve:
                    button_selected_style(btn_Long_Sleeve);
                    category = "長袖上衣";
                    clothesImages = new ArrayList<>();
                    get_Clothes_Privacy_Setting("長袖上衣");
                    break;
                case R.id.btn_Vest:
                    button_selected_style(btn_Vest);
                    category = "背心";
                    clothesImages = new ArrayList<>();
                    get_Clothes_Privacy_Setting("背心");
                    break;
                case R.id.btn_Shorts:
                    button_selected_style(btn_Shorts);
                    category = "短褲";
                    clothesImages = new ArrayList<>();
                    get_Clothes_Privacy_Setting("短褲");
                    break;
                case R.id.btn_Pants:
                    button_selected_style(btn_Pants);
                    category = "長褲";
                    clothesImages = new ArrayList<>();
                    get_Clothes_Privacy_Setting("長褲");
                    break;
                case R.id.btn_Skirt:
                    button_selected_style(btn_Skirt);
                    category = "裙子";
                    clothesImages = new ArrayList<>();
                    get_Clothes_Privacy_Setting("裙子");
                    break;
                case R.id.btn_Coat:
                    button_selected_style(btn_Coat);
                    category = "外套";
                    clothesImages = new ArrayList<>();
                    get_Clothes_Privacy_Setting("外套");
                    break;
                case R.id.btn_Suit:
                    button_selected_style(btn_Suit);
                    category = "套裝";
                    clothesImages = new ArrayList<>();
                    get_Clothes_Privacy_Setting("套裝");
                    break;
                case R.id.btn_Accessories:
                    button_selected_style(btn_Accessories);
                    category = "配件飾品";
                    clothesImages = new ArrayList<>();
                    get_Clothes_Privacy_Setting("配件飾品");
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

    //全選的點擊事件，還沒完成
    private CompoundButton.OnCheckedChangeListener checkBoxListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if(isChecked) {
                changeTo_unlocked_Privacy_Status();
                Toast.makeText(PrivacySetting.this, "全選 " + category, Toast.LENGTH_SHORT).show();
            } else {
                changeTo_locked_Privacy_Status();
                Toast.makeText(PrivacySetting.this, "取消全選 " + category, Toast.LENGTH_SHORT).show();
            }
        }
    };

    protected void changeTo_locked_Privacy_Status() {

        Toast.makeText(PrivacySetting.this, "全隱藏 " + category, Toast.LENGTH_SHORT).show();

        clothName = new ArrayList<>();

        if(category.equals("所有衣服")) {

        } else {
            //設定要讀取的資料路徑
            firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("Clothes")
                    //讀取特定的所選衣服類別的文件，用'set_Data_Time'降冪排序衣服上傳時間(最新~最舊)
                    .whereEqualTo("clothes_Category", category)
                    .orderBy("set_Data_Time", Query.Direction.DESCENDING)
                    .get()
                    //如果讀取資料成功
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                for(QueryDocumentSnapshot document : task.getResult()){
                                    clothName.add(document.get("clothes_Image_Name").toString());
                                    clothCount += 1;
                                }
                            }

                            for(int i = 0; i < clothCount; i++) {
                                firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("Clothes").document(clothName.get(i))
                                        .update("privacy_Status", "locked");
                            }
                        }
                    });

            clothCount = 0;

            clothesImages = new ArrayList<>();
            if(category.equals("所有衣服")) {
                get_All_Clothes_Privacy_Setting();
            } else {
                get_Clothes_Privacy_Setting(category); //重新從firebase讀取衣服資料(刷新隱私頁面)
            }
        }
    }

    protected void changeTo_unlocked_Privacy_Status() {


        Toast.makeText(PrivacySetting.this, "全公開 " + category, Toast.LENGTH_SHORT).show();
        clothName = new ArrayList<>();

        if(category.equals("所有衣服")) {

        } else {
            //設定要讀取的資料路徑
            firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("Clothes")
                    //讀取特定的所選衣服類別的文件，用'set_Data_Time'降冪排序衣服上傳時間(最新~最舊)
                    .whereEqualTo("clothes_Category", category)
                    .orderBy("set_Data_Time", Query.Direction.DESCENDING)
                    .get()
                    //如果讀取資料成功
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                for(QueryDocumentSnapshot document : task.getResult()){
                                    clothName.add(document.get("clothes_Image_Name").toString());
                                    clothCount += 1;
                                }
                            }
                            for(int i = 0; i < clothCount; i++) {
                                firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("Clothes").document(clothName.get(i))
                                        .update("privacy_Status", "unlocked");
                            }
                        }
                    });

            clothCount = 0;

            clothesImages = new ArrayList<>();
            if(category.equals("所有衣服")) {
                get_All_Clothes_Privacy_Setting();
            } else {
                get_Clothes_Privacy_Setting(category); //重新從firebase讀取衣服資料(刷新隱私頁面)
            }
        }
    }

    protected void get_Clothes_Privacy_Setting(String clothes_Category) {

        //設定要讀取的資料路徑
        firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("Clothes")
                //讀取特定的所選衣服類別的文件，用'set_Data_Time'降冪排序衣服上傳時間(最新~最舊)
                .whereEqualTo("clothes_Category", clothes_Category)
                .orderBy("set_Data_Time", Query.Direction.DESCENDING)
                .get()
                //如果讀取資料成功
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                                Toast.makeText(PrivacySetting.this, "還沒有任何一件喔", Toast.LENGTH_SHORT).show();
                            }

                            //將arraylist傳到衣服RecyclerView類別的建構方法
                            privacyRecyclerViewAdapter = new PrivacyRecyclerViewAdapter(PrivacySetting.this, clothesImages, PrivacySetting.this);

                            //用GridLayoutManager指定RecyclerView中每列的顯示方式：縱向、1個物件為一列
                            GridLayoutManager gridLayoutManager = new GridLayoutManager(PrivacySetting.this, 1, GridLayoutManager.VERTICAL, false);
                            recyclerView.setLayoutManager(gridLayoutManager);
                            recyclerView.setAdapter(privacyRecyclerViewAdapter);

                            for(int i = 0; i < clothesImages.size(); i++) {
                                if(clothesImages.get(i).getPrivateStatus().equals("unlocked")){
                                    //
                                } else {
                                    //selectAllStatus = "false";
                                    checkBox_all.setChecked(false);
                                    return;
                                }
                            }

                            //selectAllStatus = "true";
                            checkBox_all.setChecked(true);


                        } else {
                            Toast.makeText(PrivacySetting.this, "從firestore抓取圖片URL失敗", Toast.LENGTH_SHORT).show();
                            Log.d("錯誤提示訊息", task.getException().getMessage());
                        }
                    }
                });
    }

    //顯示所有衣服
    protected void get_All_Clothes_Privacy_Setting() {

        //設定要讀取的資料路徑
        firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("Clothes")
                //讀取所有的衣服類別的文件，先按照'index'升冪排序衣服類別(短袖~配件)，再用'set_Data_Time'降冪排序衣服上傳時間(最新~最舊)
                .orderBy("index", Query.Direction.ASCENDING)
                .orderBy("set_Data_Time", Query.Direction.DESCENDING)
                .get()
                //如果讀取資料成功
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                                Toast.makeText(PrivacySetting.this, "還沒有任何一件喔", Toast.LENGTH_SHORT).show();
                            }

                            //將arraylist傳到衣服RecyclerView類別的建構方法
                            privacyRecyclerViewAdapter = new PrivacyRecyclerViewAdapter(PrivacySetting.this, clothesImages, PrivacySetting.this);

                            //用GridLayoutManager指定RecyclerView中每列的顯示方式：縱向、1個物件為一列
                            GridLayoutManager gridLayoutManager = new GridLayoutManager(PrivacySetting.this, 1, GridLayoutManager.VERTICAL, false);
                            recyclerView.setLayoutManager(gridLayoutManager);
                            recyclerView.setAdapter(privacyRecyclerViewAdapter);

                            for(int i = 0; i < clothesImages.size(); i++) {
                                if(clothesImages.get(i).getPrivateStatus().equals("unlocked")){
                                    //
                                } else {
                                    checkBox_all.setChecked(false);
                                    return;
                                }
                            }

                            checkBox_all.setChecked(true);

                        } else {
                            Toast.makeText(PrivacySetting.this, "從firestore抓取圖片URL失敗", Toast.LENGTH_SHORT).show();
                            Log.d("錯誤提示訊息", task.getException().getMessage());
                        }
                    }
                });
    }

    @Override
    public void onItemClick(ClothData clothData) {
        Toast.makeText(PrivacySetting.this, "點擊到衣服 " + clothData.getImageName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCheckBoxClick(ClothData clothData) {

        //若被點選衣服的隱私狀態為"公開unlocked"，就將該衣服的狀態改為"隱藏locked"
        if(clothData.getPrivateStatus().equals("unlocked")) {
            firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("Clothes").document(clothData.getImageName())
                    .update("privacy_Status", "locked")
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //Toast.makeText(Privacy_Setting.this, "隱藏衣服 " + cloth_image_data.getImageName() + " 的隱私權限", Toast.LENGTH_SHORT).show();
                        }
                    });

            clothesImages = new ArrayList<>();
            if(category.equals("所有衣服")) {
                get_All_Clothes_Privacy_Setting();
            } else {
                get_Clothes_Privacy_Setting(category); //重新從firebase讀取衣服資料(刷新隱私頁面)
            }
        }
        //若被點選衣服的隱私狀態為"隱藏locked"，就將該衣服的狀態改為"公開unlocked"
        else if(clothData.getPrivateStatus().equals("locked")) {
            firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("Clothes").document(clothData.getImageName())
                    .update("privacy_Status", "unlocked")
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //Toast.makeText(Privacy_Setting.this, "公開衣服 " + cloth_image_data.getImageName() + " 的隱私權限", Toast.LENGTH_SHORT).show();
                        }
                    });

            clothesImages = new ArrayList<>();
            if(category.equals("所有衣服")) {
                get_All_Clothes_Privacy_Setting();
            } else {
                get_Clothes_Privacy_Setting(category); //重新從firebase讀取衣服資料(刷新隱私頁面)
            }
        }
    }
}