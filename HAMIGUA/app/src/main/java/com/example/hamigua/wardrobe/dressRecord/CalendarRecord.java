package com.example.hamigua.wardrobe.dressRecord;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.hamigua.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class CalendarRecord extends AppCompatActivity {

    private ImageView transparentcloth, transparentpants; //透明衣服、透明褲子
    private ImageView delete_record;

    private String topClothesImageUrl = "";
    private String bottomClothesImageUrl = "";

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String sDate;
    private String newDate;

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
        setContentView(R.layout.activity_calendar_record);

        //設置標題列ActionBar的左上角返回鍵
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        transparentcloth = findViewById(R.id.transparentcloth);
        transparentpants = findViewById(R.id.transparentpants);

        delete_record = findViewById(R.id.delete_record);
        //firebase
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        Intent intent=getIntent();
        sDate=intent.getStringExtra("sDate"); //從MyCalendar用intent傳來的日期格式：2021/**/**
        //因為firebase會把斜線判定成下一層目錄路徑，所以firestore文件名稱要改成別的格式
        newDate = sDate.replace("/", "."); //新的日期格式：2021.**.**

        getSupportActionBar().setTitle(sDate + "  穿搭紀錄"); //用日期設置頁面的標題列Title

        get_ClothesImage_From_Firebase(); //把穿搭紀錄裡的衣服取出來並顯示

        delete_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //刪除當下頁面日期的穿搭紀錄
                delete_Record_From_firestore();
            }
        });
    }

    protected void delete_Record_From_firestore() {

        firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("Calendar_Record").document(newDate)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(CalendarRecord.this, "成功刪除 " + sDate + " 穿搭紀錄", Toast.LENGTH_SHORT).show();
                        transparentcloth.setImageResource(R.drawable.transparent);
                        transparentpants.setImageResource(R.drawable.transparent);
                    }
                });
    }

    protected void get_ClothesImage_From_Firebase() {

        //把穿搭紀錄裡的衣服取出來並顯示，設定要讀取的資料路徑
        firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("Calendar_Record")
                //讀取欄位'record_Date'的值為"sDate"的文件
                .whereEqualTo("record_Date", sDate)//日期格式：2021/**/**
                .get()
                //如果讀取資料成功
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        //把文件裡的欄位'Top_clothes_Image_URL'的值取出來指定給'topClothesImageUrl'、
                        //'Bottom_clothes_Image_URL'的值取出來指定給'bottomClothesImageUrl'，並呼叫方法'put_Clothes_On_HAGE()'
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                topClothesImageUrl = document.get("Top_clothes_Image_URL").toString();
                                bottomClothesImageUrl = document.get("Bottom_clothes_Image_URL").toString();
                                put_Clothes_On_HAGE();
                            }
                        } else {
                            Toast.makeText(CalendarRecord.this, "從firestore抓取圖片URL失敗", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                //如果讀取資料失敗
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CalendarRecord.this, "從firestore抓取圖片URL失敗", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    protected void put_Clothes_On_HAGE() {

        Glide.with(CalendarRecord.this)
                .load(topClothesImageUrl) //傳入上衣的URL
                .centerCrop()
                .into(transparentcloth); //將圖片顯示在上衣imageView

        transparentcloth.bringToFront();//圖片帶到最上層


        Glide.with(CalendarRecord.this)
                .load(bottomClothesImageUrl) //傳入下著的URL
                .centerCrop()
                .into(transparentpants); //將圖片顯示在下著imageView

        transparentpants.bringToFront(); //圖片帶到最上層

    }
}
