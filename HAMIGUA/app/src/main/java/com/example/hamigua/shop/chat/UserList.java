package com.example.hamigua.shop.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.hamigua.R;
import com.example.hamigua.shop.adapter.ShopChatUserRecyclerViewAdapter;
import com.example.hamigua.shop.model.ChatUserData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class UserList extends AppCompatActivity implements ShopChatUserRecyclerViewAdapter.OnItemClickListener{

    private RecyclerView recyclerView;
    private ArrayList<ChatUserData> _chat_user_data; //存放賣家資料的動態陣列
    private ShopChatUserRecyclerViewAdapter shopChatUserRecyclerViewAdapter; //賣家RecyclerView類別物件

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
        setContentView(R.layout.activity_shop_chat_user_list);

        //設置標題列ActionBar的左上角返回鍵
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.shopChatRecyclerView);

        //取得firestore與authentication的連接
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        _chat_user_data = new ArrayList<>();

        get_Image_Url_Category();
    }

    //從firestore讀取賣家資料，放進recyclerview
    protected void get_Image_Url_Category() {

        //設定要讀取的資料路徑
        firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid())
                .collection("Shop_Chat").document("user").collection("Seller")
                //讀取賣家資料的文件
                .get()
                //如果讀取資料成功
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        //讀取賣家名稱、email、大頭照URL，並傳進'Shop_Chat_User_Data'類別的物件'chat_user_data'
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){

                                ChatUserData chat_user_data = new ChatUserData(
                                        document.get("sellerUserID").toString(),
                                        document.get("sellerName").toString(),
                                        document.get("sellerImageUrl").toString(),
                                        document.get("sellerEmail").toString());

                                _chat_user_data.add(chat_user_data); //將物件'chat_user_data'加到'Shop_Chat_User_Data'型別的arraylist
                            }

                            //如果'Cloth_Image_Data'型別的arraylist的長度為0
                            if(_chat_user_data.size() == 0) {
                                Toast.makeText(UserList.this, "還沒有任何一位賣家資料喔", Toast.LENGTH_SHORT).show();
                            }

                            //將arraylist傳到衣服RecyclerView類別的建構方法
                            shopChatUserRecyclerViewAdapter = new ShopChatUserRecyclerViewAdapter(UserList.this, _chat_user_data, UserList.this);
                            recyclerView.setLayoutManager(new LinearLayoutManager(UserList.this));
                            recyclerView.setAdapter(shopChatUserRecyclerViewAdapter);
                        } else {
                            Toast.makeText(UserList.this, "從firestore抓取賣家資料失敗", Toast.LENGTH_SHORT).show();
                            Log.d("錯誤提示訊息", task.getException().getMessage());
                        }
                    }
                });
    }

    @Override
    public void onItemClick(ChatUserData _chat_user_data) {
        Intent intent = new Intent(this, ChatScreen.class);
        intent.putExtra("OnClickedUser", _chat_user_data);
        startActivity(intent);
    }
}