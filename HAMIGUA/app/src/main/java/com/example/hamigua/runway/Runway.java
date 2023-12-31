package com.example.hamigua.runway;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hamigua.HomePage;
import com.example.hamigua.R;
import com.example.hamigua.communicate.Communicate;
import com.example.hamigua.loginAcount.MainActivity;
import com.example.hamigua.shop.customer.Shop;
import com.example.hamigua.wardrobe.HAGE_Setting;
import com.example.hamigua.wardrobe.Setting;
import com.example.hamigua.wardrobe.Setting_Authority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

public class Runway extends AppCompatActivity {

    public String[] setting_str = {"我的資料", "人偶設定", "登出", "刪除帳號"};
    private ListView setting;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;


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
        setContentView(R.layout.activity_setting);

        //設置標題列ActionBar的左上角返回鍵
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setting = findViewById(R.id.setting);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, setting_str);
        setting.setAdapter(adapter);
        setting.setOnItemClickListener(onClickListView);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

    }
    private ListView.OnItemClickListener onClickListView = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch(position){
                case 0:
                    Intent intent_Setting_Authority = new Intent(Runway.this, Setting_Authority.class);
                    startActivity(intent_Setting_Authority);
                    break;
                case 1:
                    Intent intent = new Intent(Runway.this, HAGE_Setting.class);
                    startActivity(intent);
                    break;
                case 2:
                    log_Out_Account();
                    break;
                case 3:
                    delete_Account();
                    break;
            }
        }
    };

    protected void log_Out_Account() {
        //設置確認對話視窗
        AlertDialog.Builder dialog = new AlertDialog.Builder(Runway.this);
        dialog.setTitle("登出");
        dialog.setMessage("您確定要登出以下帳號嗎？\n" + firebaseAuth.getCurrentUser().getEmail());

        //若使用者按下"登出"的觸擊事件
        dialog.setPositiveButton("登出", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                firebaseAuth.signOut();
                Toast.makeText(Runway.this, "登出成功，感謝您使用哈密瓜智慧衣櫃", Toast.LENGTH_SHORT).show();

                //設置延遲，跳出登出訊息Toast後1000毫秒再進行跳轉頁面至(MainActivity.java)
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent_logout = new Intent(Runway.this, MainActivity.class);
                        startActivity(intent_logout);
                    }
                }, 1000);
            }
        });

        //若使用者按下"取消"的觸擊事件
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //離開這個確認對話視窗
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }

    private void delete_Account() {
        //設置確認對話視窗
        AlertDialog.Builder dialog = new AlertDialog.Builder(Runway.this);
        dialog.setTitle("刪除帳號 " + firebaseAuth.getCurrentUser().getEmail());
        dialog.setMessage("您確定要刪除此帳號嗎？\n此帳號的所有資料將會一併刪除且無法復原");

        //若使用者按下"刪除"的觸擊事件
        dialog.setPositiveButton("刪除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //指定要刪除的firestore集合路徑，要刪除的文件名稱為authentication的該使用者ID(Uid)，再對該使用者ID(Uid)刪除authentication裡的使用者資料
                firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid())
                        .delete()
                        //刪除firestore文件成功
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                //取得該登入的使用者並刪除
                                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                                firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                        //若刪除authentication裡的使用者資料成功
                                        if(task.isSuccessful()){
                                            Toast.makeText(Runway.this, "刪除帳號成功\n期待您再次使用哈密瓜智慧衣櫃", Toast.LENGTH_SHORT).show();
                                            //設置延遲，跳出登出訊息Toast後1500毫秒再進行跳轉頁面至(MainActivity.java)
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Intent intent_logout = new Intent(Runway.this, MainActivity.class);
                                                    startActivity(intent_logout);
                                                }
                                            }, 1500);
                                        }
                                        //若刪除authentication裡的使用者資料失敗
                                        else {
                                            Toast.makeText(Runway.this, "刪除帳號失敗\n" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        })
                        //刪除firestore文件失敗
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull @NotNull Exception e) {
                                Toast.makeText(Runway.this, "刪除帳號失敗\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        //若使用者按下"取消"的觸擊事件
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //離開這個確認對話視窗
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }

}