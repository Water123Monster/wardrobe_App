package com.example.hamigua.shop.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.example.hamigua.R;
import com.example.hamigua.shop.adapter.ShopChatRecentConversationRecyclerViewAdapter;
import com.example.hamigua.shop.model.ShopChatMessageData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class RecentConversation extends AppCompatActivity implements ShopChatRecentConversationRecyclerViewAdapter.OnItemClickListener{

    private ArrayList<ShopChatMessageData> conversations;
    private ShopChatRecentConversationRecyclerViewAdapter conversationAdapter;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private RecyclerView shopChatConversationRecyclerView;
    private ProgressBar shop_chat_recent_progressBar;

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
        setContentView(R.layout.activity_shop_chat_recent_conversation);

        //設置標題列ActionBar的左上角返回鍵
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        shopChatConversationRecyclerView = findViewById(R.id.shopChatConversationRecyclerView);
        shop_chat_recent_progressBar = findViewById(R.id.shop_chat_recent_progressBar);

        init();
        listenConversations();
    }

    private void init() {
        //取得firestore與authentication的連接
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        conversations = new ArrayList<>();
        conversationAdapter = new ShopChatRecentConversationRecyclerViewAdapter(
                RecentConversation.this,
                conversations,
                RecentConversation.this);
        shopChatConversationRecyclerView.setLayoutManager(new LinearLayoutManager(RecentConversation.this));
        shopChatConversationRecyclerView.setAdapter(conversationAdapter);
    }

    private void listenConversations() {
        firebaseFirestore.collection("shop_Chat_Conversation")
                .whereEqualTo("senderID", firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(eventListener);
        firebaseFirestore.collection("shop_Chat_Conversation")
                .whereEqualTo("receiverID", firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(eventListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderID = documentChange.getDocument().getString("senderID");
                    String receiverID = documentChange.getDocument().getString("receiverID");
                    ShopChatMessageData shop_chat_message_data = new ShopChatMessageData();
                    shop_chat_message_data.senderID = senderID;
                    shop_chat_message_data.receiverID = receiverID;
                    if (firebaseAuth.getCurrentUser().getUid().equals(senderID)) {
                        shop_chat_message_data.conversationName = documentChange.getDocument().getString("receiverName");
                        shop_chat_message_data.conversationID = documentChange.getDocument().getString("receiverID");
                    } else {
                        shop_chat_message_data.conversationName = documentChange.getDocument().getString("senderName");
                        shop_chat_message_data.conversationID = documentChange.getDocument().getString("senderID");
                    }
                    shop_chat_message_data.message = documentChange.getDocument().getString("lastMessage");
                    shop_chat_message_data.dateObject = documentChange.getDocument().getDate("timeStamp");
                    conversations.add(shop_chat_message_data);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < conversations.size(); i++) {
                        String senderID = documentChange.getDocument().getString("senderID");
                        String receiverID = documentChange.getDocument().getString("receiverID");
                        if (conversations.get(i).senderID.equals(senderID) && conversations.get(i).receiverID.equals(receiverID)) {
                            conversations.get(i).message = documentChange.getDocument().getString("lastMessage");
                            conversations.get(i).dateObject = documentChange.getDocument().getDate("timeStamp");
                            break;
                        }
                    }
                }
            }
            conversations.sort((obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            conversationAdapter.notifyDataSetChanged();
            shopChatConversationRecyclerView.smoothScrollToPosition(0);
            shopChatConversationRecyclerView.setVisibility(View.VISIBLE);
            shop_chat_recent_progressBar.setVisibility(View.GONE);
        }
    };


    @Override
    public void onItemClick(ChatUser user) {
        /*
        Intent intent = new Intent(this, Shop_Chat_Screen.class);
        intent.putExtra("OnClickedRecentUser", (Parcelable) user);
        startActivity(intent);

         */
    }
}