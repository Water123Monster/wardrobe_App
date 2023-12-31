package com.example.hamigua.wardrobe.statistics;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.hamigua.R;
import com.example.hamigua.wardrobe.model.ClothData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class ClothStatistics extends AppCompatActivity {

    private Button btnAnalysis;
    private ImageView clothImageview;
    private TextView cloth_record_Count, cloth_record_Date, cloth_hashtag;
    private ClothData clothData; //衣服資料的類別物件

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private int count = 0;
    private String date = "", hashtag = "";

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
        setContentView(R.layout.activity_cloth_statistics);

        //設置標題列ActionBar的左上角返回鍵
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        btnAnalysis=findViewById(R.id.btnAnalysis);
        clothImageview = findViewById(R.id.cloth_imageview);
        cloth_record_Count = findViewById(R.id.cloth_record_Count);
        cloth_record_Date = findViewById(R.id.cloth_record_Date);
        cloth_hashtag = findViewById(R.id.cloth_hashtag);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        set_Cloth_Image_Data(); //讀取intent傳來的衣服資料類別物件，並提出資料顯示

        get_Calendar_Data(); //讀取月曆裡的穿搭紀錄

        get_Clothes_Hashtag(); //讀取衣服裡的#hashtag


        //穿搭次數分析的button點擊事件
        btnAnalysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ClothStatistics.this, Statistics.class);
                startActivity(intent);
            }
        });
    }

    //讀取intent傳來的衣服資料類別物件，並提出資料顯示
    protected void set_Cloth_Image_Data() {

        Intent intent = getIntent(); //接收intent
        clothData = intent.getParcelableExtra("cloth_image_data"); //將傳來的物件指定給這個類別裡的衣服資料類別物件

        getSupportActionBar().setTitle("統計分析  " + clothData.getImageName()); //用點選到的衣服名稱設置頁面的標題列Title

        if(clothData.getImageCategory().equals("長褲")) //若傳來的衣服類別是長褲，調整Imageview的長寬完整顯示長褲
        {
            clothImageview.getLayoutParams().height = 1200;
            clothImageview.getLayoutParams().width = 800;
        } else if (clothData.getImageCategory().equals("長袖上衣")) //若傳來的衣服類別是長袖上衣，調整Imageview的長寬完整顯示長袖
        {
            clothImageview.getLayoutParams().height = 800;
            clothImageview.getLayoutParams().width = 1200;
        }

        //從衣服資料類別物件提出衣服的URL，並用Glide插件顯示在clothImageview
        Glide.with(ClothStatistics.this)
                .load(clothData.getImageUrl()) //傳入衣服的URL
                .centerCrop()
                .into(clothImageview); //將圖片顯示在imageView

    }

    //讀取月曆裡的穿搭紀錄
    protected void get_Calendar_Data() {

        String category = "";

        //若衣服的類別為上衣，把'category'設為"Top_clothes_Image_URL"；下著則設為"Bottom_clothes_Image_URL"
        if((clothData.getImageCategory().equals("短袖上衣")) || (clothData.getImageCategory().equals("長袖上衣")) ||
                (clothData.getImageCategory().equals("背心")) || (clothData.getImageCategory().equals("外套")) ||
                (clothData.getImageCategory().equals("套裝"))) {
            category = "Top_clothes_Image_URL";
        } else if ((clothData.getImageCategory().equals("短褲")) || (clothData.getImageCategory().equals("長褲")) ||
                (clothData.getImageCategory().equals("裙子"))) {
            category = "Bottom_clothes_Image_URL";
        } else {
            category = "Accessories_Image_URL";
        }

        //要讀取的firestore路徑，用衣服的url進行where比對
        firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("Calendar_Record")
                .whereEqualTo(category, clothData.getImageUrl())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for(QueryDocumentSnapshot document : task.getResult()) {
                                count++;
                                date += document.get("record_Date").toString() + "  ";
                            }
                            cloth_record_Count.setText("穿搭次數：" + count + " 次");
                            cloth_record_Date.setText("穿搭日期：" + date);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ClothStatistics.this, "讀取穿搭紀錄失敗", Toast.LENGTH_SHORT).show();
                        Log.d("讀取穿搭紀錄失敗", e.getMessage());
                    }
                });
    }

    //要讀取的firestore路徑，用衣服的url進行where比對
    protected void get_Clothes_Hashtag() {

        firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("Clothes")
                .whereEqualTo("clothes_Image_URL", clothData.getImageUrl())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for(QueryDocumentSnapshot document : task.getResult()) {
                                hashtag += document.get("clothes_Image_Hashtag1").toString() + "  ";
                                hashtag += document.get("clothes_Image_Hashtag2").toString() + "  ";
                                hashtag += document.get("clothes_Image_Hashtag3").toString();
                            }
                            cloth_hashtag.setText("#Hashtag：" + hashtag);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ClothStatistics.this, "讀取衣服#hashtag失敗", Toast.LENGTH_SHORT).show();
                        Log.d("讀取衣服#hashtag失敗", e.getMessage());
                    }
                });
    }
}