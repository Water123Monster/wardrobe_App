package com.example.hamigua.communicate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.hamigua.HomePage;
import com.example.hamigua.R;
import com.example.hamigua.runway.Runway;
import com.example.hamigua.shop.customer.Shop;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class Communicate extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private BottomNavigationView bottomNavigationView;
    private DrawerLayout leftDrawerLayout;

    Button btnSearch;
    ImageButton imgTest1,favbtn;
    private RecyclerView recyclerView;
    private MyAdapter adapter;
    private ArrayList<String> mData = new ArrayList<>();
    private NavigationView navigationView;
    private Button btnCommunicate;

    private FloatingActionButton btn_personalPage, btn_post, btn_explore, btn_browse, btn_chat;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communicate);
        leftDrawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_communicate_view);
        btnCommunicate = findViewById(R.id.btnCommunicate);

        bottomNavigationView = findViewById(R.id.bottomNav); //設定底部導行列
        //bottomNavigationView.setSelectedItemId(R.id.Communicate); //設定所選到的itemId
        bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavigationListener);


        /*此部分為右滑漢堡選單*/
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this,leftDrawerLayout,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        leftDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        /*此部分為右滑漢堡選單*/

        btnSearch = findViewById(R.id.btnSearch);
        imgTest1 = findViewById(R.id.imgTest1);
        favbtn= findViewById(R.id.favbtn);

        btn_personalPage = findViewById(R.id.btn_personalPage);
        btn_post = findViewById(R.id.btn_post);
        btn_explore = findViewById(R.id.btn_explore);
        btn_browse = findViewById(R.id.btn_browse);
        btn_chat = findViewById(R.id.btn_chat);

        btn_personalPage.setOnClickListener(floatingButtonListener);
        btn_post.setOnClickListener(floatingButtonListener);
        btn_explore.setOnClickListener(floatingButtonListener);
        btn_browse.setOnClickListener(floatingButtonListener);
        btn_chat.setOnClickListener(floatingButtonListener);

        //取得firestore與authentication的連接
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        // 準備資料，塞200個項目到ArrayList裡
        for (int i = 0; i < 200; i++) {
            mData.add("項目" + i);
        }

        // 連結元件
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        // 設置RecyclerView為列表型態
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // 設置格線
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // 將資料交給adapter
        adapter = new MyAdapter(mData);
        // 設置adapter給recycler_view
        recyclerView.setAdapter(adapter);

        get_Account_UserName();

        //開啟漢堡選單button
        btnCommunicate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leftDrawerLayout.openDrawer(GravityCompat.START);//選單從左邊拉出
            }
        });
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
                            }
                        }
                    }
                });
    }

    //懸浮按鈕底下的點擊事件
    protected View.OnClickListener floatingButtonListener = new View.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_personalPage:
                    Intent intent_Communicate_Person = new Intent(Communicate.this, Communicate_Person.class);
                    startActivity(intent_Communicate_Person);
                    break;
                case R.id.btn_post:
                    Intent intent_Communicate_Post = new Intent(Communicate.this, Communicate_Post.class);
                    startActivity(intent_Communicate_Post);
                    break;
                case R.id.btn_explore:
                    Intent intent_Communicate_Explore = new Intent(Communicate.this, Communicate_Explore.class);
                    startActivity(intent_Communicate_Explore);
                    break;
                case R.id.btn_browse:
                    Intent intent_Communicate_Browse = new Intent(Communicate.this, Communicate_Browse.class);
                    startActivity(intent_Communicate_Browse);
                    break;
                case R.id.btn_chat:
                    Intent intent_Communicate_Chat = new Intent(Communicate.this, Communicate_Chat.class);
                    startActivity(intent_Communicate_Chat);
                    break;
            }
        }
    };

    //控制打開或關閉懸浮按鈕的方法
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        FloatingActionMenu menu = findViewById(R.id.floatingActionMenu);
        if (ev.getAction() == MotionEvent.ACTION_UP && menu.isOpened()){
            menu.close(true);
        }
        return super.dispatchTouchEvent(ev);
    }

    //如果漢堡選單是開啟狀態，點擊其他位置可以讓漢堡選單收回去
    public void onBackPressed(){
        if(leftDrawerLayout.isDrawerOpen(GravityCompat.START)){
            leftDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //漢堡選單裡的觸擊事件
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()){
            case R.id.nav_favorite:
                Intent intent_Communicate_Favorite = new Intent(Communicate.this, Communicate_Favorite.class);
                startActivity(intent_Communicate_Favorite);
                break;
            case R.id.nav_note:
                Intent intent_Communicate_Article = new Intent(Communicate.this, Communicate_Article.class);
                startActivity(intent_Communicate_Article);
                break;
            case R.id.nav_communicate_set:
                Intent intent_Communicate_Setting = new Intent(Communicate.this, Communicate_Setting.class);
                startActivity(intent_Communicate_Setting);
                break;
            case R.id.nav_collocation:
                Intent intent_Communicate_Collocation = new Intent(Communicate.this, Communicate_Collocation.class);
                startActivity(intent_Communicate_Collocation);
                break;
            case R.id.nav_control:
                Intent intent_Communicate_Control = new Intent(Communicate.this, Communicate_Control.class);
                startActivity(intent_Communicate_Control);
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