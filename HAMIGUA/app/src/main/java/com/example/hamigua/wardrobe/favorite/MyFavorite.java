package com.example.hamigua.wardrobe.favorite;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamigua.R;
import com.example.hamigua.wardrobe.adapter.FavoriteRecyclerViewAdapter;
import com.example.hamigua.wardrobe.model.FavoriteData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;

public class MyFavorite extends AppCompatActivity implements FavoriteRecyclerViewAdapter.OnItemClickListener{

    private TextView clothCategory, clothHashtag;
    private String[] clothArray = {"短袖上衣", "長袖上衣", "背心", "短褲", "長褲", "裙子", "外套", "套裝", "配件飾品"}; //在AlertDialog裡要顯示的衣服類別
    boolean[] selected; //在AlertDialog裡紀錄勾選狀態的陣列
    private ArrayList<String> categorySelect; //存放在AlertDialog裡已勾選的衣服類別

    private RecyclerView recyclerView;
    private ArrayList<FavoriteData> clothesImages; //存放衣服資料的動態陣列
    private FavoriteRecyclerViewAdapter favoriteRecyclerViewAdapter; //衣服RecyclerView類別物件

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String category_1 = "短袖上衣", category_2 = "長袖上衣", category_3 = "背心", category_4 = "短褲", category_5 = "長褲"
            ,category_6 = "裙子", category_7 = "外套", category_8 = "套裝", category_9 = "配件飾品"; //用在firebase查詢語法的衣服類別參數

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
        setContentView(R.layout.activity_favorite);

        //設置標題列ActionBar的左上角返回鍵
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        clothCategory = findViewById(R.id.cloth_category);
        clothHashtag = findViewById(R.id.cloth_hashtag);

        clothCategory.setOnClickListener(categoryListner);

        recyclerView = findViewById(R.id.favoriteRecyclerView);

        //取得firestore與authentication的連接
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        clothesImages = new ArrayList<>();
        categorySelect = new ArrayList<>();

        selected = new boolean[clothArray.length]; //紀錄勾選狀態的陣列要跟顯示的衣服類別長度一樣
        getImageUrl(); //從firebase讀取衣服資料
    }

    //點擊到衣服類別的TextView
    protected View.OnClickListener categoryListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MyFavorite.this);
            builder.setTitle("請選擇衣服樣式");

            //可複選的選項item
            builder.setMultiChoiceItems(clothArray, selected, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                    if (b) { //已被點擊選取的狀態
                        categorySelect.add(clothArray[i]); //將所選取到的衣服類別加進已勾選衣服類別的arraylist
                    } else { //再次點擊以取消選取狀態
                        categorySelect.remove(clothArray[i]); //從已勾選衣服類別的arraylist移除所選取到的衣服類別
                    }
                }
            });

            //按下"確定"的事件
            builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    clothesImages.clear(); //先清空存放衣服資料的動態陣列
                    add_Category(); //將選取到的衣服類別分別寫入firebase查詢語法的衣服類別參數
                    getImageUrl(); //從firebase讀取衣服資料
                }
            });

            //按下"取消"的事件
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss(); //將AlertDialog關掉
                }
            });
            builder.show();
        }
    };

    //將選取到的衣服類別分別寫入firebase查詢語法的衣服類別參數
    protected void add_Category() {

        int size = categorySelect.size();
        String[] category = {"", "", "", "", "", "", "", "", ""};

        for(int i = 0; i < size; i++) {
            category[i] = categorySelect.get(i);
        }

        if(size == 0) {
            for(int i = size; i < category.length; i++) {
                category[i] = clothArray[i];
            }
        } else {
            for(int i = size; i < category.length; i++) {
                category[i] = "";
            }
        }

        category_1 = category[0];
        category_2 = category[1];
        category_3 = category[2];
        category_4 = category[3];
        category_5 = category[4];
        category_6 = category[5];
        category_7 = category[6];
        category_8 = category[7];
        category_9 = category[8];
    }

    //從firestore讀取已上傳的衣服，放進recyclerview
    protected void getImageUrl() {

        //設定要讀取的資料路徑
        firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("MyFavorite_Clothes")
                //讀取所選擇的衣服類別的文件，先按照'index'升冪排序衣服類別(短袖~配件)，再用'set_Data_Time'降冪排序衣服上傳時間(最新~最舊)
                .whereIn("clothes_Category", Arrays.asList(category_1, category_2, category_3, category_4, category_5, category_6, category_7, category_8, category_9))
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

                                FavoriteData favoriteData = new FavoriteData(
                                        document.get("clothes_Image_URL").toString(),
                                        document.get("clothes_Name").toString(),
                                        document.get("clothes_Category").toString(),
                                        document.get("clothes_Hashtag1").toString(),
                                        document.get("clothes_Hashtag2").toString(),
                                        document.get("clothes_Hashtag3").toString(),
                                        document.get("favorite_Status").toString());

                                clothesImages.add(favoriteData); //將物件'favorite_image_data'加到'Favorite_Image_Data'型別的arraylist

                            }

                            //如果'Cloth_Image_Data'型別的arraylist的長度為0
                            if(clothesImages.size() == 0) {
                                Toast.makeText(MyFavorite.this, "還沒有任何一件喔", Toast.LENGTH_SHORT).show();
                            }

                            //將arraylist傳到衣服RecyclerView類別的建構方法
                            favoriteRecyclerViewAdapter = new FavoriteRecyclerViewAdapter(MyFavorite.this, clothesImages, MyFavorite.this, MyFavorite.this);

                            //用GridLayoutManager指定RecyclerView中每列的顯示方式：縱向、2個物件為一列
                            GridLayoutManager gridLayoutManager = new GridLayoutManager(MyFavorite.this, 2, GridLayoutManager.VERTICAL, false);
                            recyclerView.setLayoutManager(gridLayoutManager);
                            recyclerView.setAdapter(favoriteRecyclerViewAdapter);
                        } else {
                            Toast.makeText(MyFavorite.this, "從firestore抓取圖片URL失敗", Toast.LENGTH_SHORT).show();
                            Log.d("錯誤提示訊息", task.getException().getMessage());
                        }
                    }
                });
    }

    //點擊到RecyclerView裡的imageButton事件
    @Override
    public void onItemClick(FavoriteData favoriteData) {

        Intent intent = new Intent();
        intent.setClass(MyFavorite.this, ClothFavorite.class);
        intent.putExtra("cloth_image_data", favoriteData); //將衣服資料的類別物件傳到Cloth_Statistics的activity
        startActivity(intent);
    }

    //點擊到RecyclerView裡的愛心imageView事件
    @Override
    public void onFavoriteClick(FavoriteData favoriteData) {

        //使用者點選已被加到我的最愛的衣服，將該衣服從我的最愛中移出
        firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("MyFavorite_Clothes").document(favoriteData.getImageName())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(MyFavorite.this, "成功從我的最愛移除衣服 " + favoriteData.getImageName(), Toast.LENGTH_SHORT).show();
                    }
                });

        //該被點選衣服，更新在Clothes集合裡的資料欄位，我的最愛狀態(Yes -> No)
        firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("Clothes").document(favoriteData.getImageName())
                .update("favorite_Status", "No");

        clothesImages = new ArrayList<>();
        getImageUrl(); //重新從firebase讀取衣服資料(刷新我的最愛頁面)
    }
}