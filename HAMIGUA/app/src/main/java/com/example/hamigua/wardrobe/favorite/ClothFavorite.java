package com.example.hamigua.wardrobe.favorite;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.hamigua.R;
import com.example.hamigua.wardrobe.model.FavoriteData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class ClothFavorite extends AppCompatActivity {

    private ImageView clothImageview;
    private TextView cloth_hashtag;
    private FavoriteData favoriteData; //衣服資料的類別物件

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String hashtag = "";

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
        setContentView(R.layout.activity_cloth_favorite);

        //設置標題列ActionBar的左上角返回鍵
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        clothImageview = findViewById(R.id.cloth_imageview);
        cloth_hashtag = findViewById(R.id.cloth_hashtag);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        set_Cloth_Image_Data(); //讀取intent傳來的衣服資料類別物件，並提出資料顯示

        get_Clothes_Hashtag(); //讀取衣服裡的#hashtag
    }

    //讀取intent傳來的衣服資料類別物件，並提出資料顯示
    protected void set_Cloth_Image_Data() {

        Intent intent = getIntent(); //接收intent
        favoriteData = intent.getParcelableExtra("cloth_image_data"); //將傳來的物件指定給這個類別裡的衣服資料類別物件


        getSupportActionBar().setTitle("我的最愛  " + favoriteData.getImageName()); //用點選到的衣服名稱設置頁面的標題列Title

        if(favoriteData.getImageCategory().equals("長褲")) //若傳來的衣服類別是長褲，調整Imageview的長寬完整顯示長褲
        {
            clothImageview.getLayoutParams().height = 1200;
            clothImageview.getLayoutParams().width = 800;
        } else if (favoriteData.getImageCategory().equals("長袖上衣")) //若傳來的衣服類別是長袖上衣，調整Imageview的長寬完整顯示長袖
        {
            clothImageview.getLayoutParams().height = 800;
            clothImageview.getLayoutParams().width = 1200;
        }

        //從衣服資料類別物件提出衣服的URL，並用Glide插件顯示在clothImageview
        Glide.with(ClothFavorite.this)
                .load(favoriteData.getImageUrl()) //傳入衣服的URL
                .centerCrop()
                .into(clothImageview); //將圖片顯示在imageView

    }

    //要讀取的firestore路徑，用衣服的url進行where比對
    protected void get_Clothes_Hashtag() {

        firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid()).collection("Clothes")
                .whereEqualTo("clothes_Image_URL", favoriteData.getImageUrl())
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
                        Toast.makeText(ClothFavorite.this, "讀取衣服#hashtag失敗", Toast.LENGTH_SHORT).show();
                        Log.d("讀取衣服#hashtag失敗", e.getMessage());
                    }
                });
    }
}