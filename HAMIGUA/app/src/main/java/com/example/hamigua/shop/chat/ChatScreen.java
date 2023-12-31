package com.example.hamigua.shop.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.hamigua.R;
import com.example.hamigua.shop.adapter.ShopChatMessageRecyclerViewAdapter;
import com.example.hamigua.shop.model.ShopChatMessageData;
import com.example.hamigua.shop.model.ChatUserData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ChatScreen extends AppCompatActivity {

    private ChatUserData receiverUser;

    private TextView shop_chat_name;
    private EditText shop_input_message;
    private ImageView shop_chat_back, shop_chat_send;
    private RecyclerView shop_chat_recyclerView;
    private ProgressBar shop_chat_progressBar;

    private ArrayList<ShopChatMessageData> shop_chat_message_data;
    private ShopChatMessageRecyclerViewAdapter shopChatMessageRecyclerViewAdapter;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String conversationID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_chat_screen);

        shop_chat_name = findViewById(R.id.shop_chat_name);
        shop_chat_back = findViewById(R.id.shop_chat_back);
        shop_chat_back.setOnClickListener(backListener);
        shop_chat_send = findViewById(R.id.shop_chat_sends);
        shop_chat_send.setOnClickListener(sendListener);
        shop_input_message = findViewById(R.id.shop_input_message);
        shop_chat_recyclerView = findViewById(R.id.shop_chat_recyclerView);
        shop_chat_progressBar = findViewById(R.id.shop_chat_progressBar);

        loadReceiverDetails();
        init();
        readMessage();


    }

    private void init() {
        //取得firestore與authentication的連接
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        shop_chat_message_data = new ArrayList<>();
        shopChatMessageRecyclerViewAdapter = new ShopChatMessageRecyclerViewAdapter(
                        ChatScreen.this,
                        shop_chat_message_data,
                        firebaseAuth.getCurrentUser().getUid());
        shop_chat_recyclerView.setAdapter(shopChatMessageRecyclerViewAdapter);
    }

    //發送訊息
    private final View.OnClickListener sendListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //取得firebase資料裡該登入帳號的使用者名稱
            firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if(document.exists()) {

                                    HashMap<String, Object> message = new HashMap<>();
                                    message.put("senderID", firebaseAuth.getCurrentUser().getUid());
                                    message.put("senderName", document.getString("userName"));
                                    message.put("senderImage", document.getString("userImage"));
                                    message.put("receiverID", receiverUser.getUserID());
                                    message.put("receiverName", receiverUser.getUserName());
                                    message.put("receiverImage", receiverUser.imageUrl);
                                    message.put("message", shop_input_message.getText().toString());
                                    message.put("timeStamp", new Date());

                                    firebaseFirestore.collection("shop_Chat_Message").add(message);
                                    if(conversationID != null) {
                                        updateConversation(shop_input_message.getText().toString());
                                        Log.d("updateConversation", conversationID);
                                    } else {
                                        HashMap<String, Object> conversation = new HashMap<>();
                                        conversation.put("senderID", firebaseAuth.getCurrentUser().getUid());
                                        conversation.put("senderName", document.getString("userName"));
                                        conversation.put("senderImage", document.getString("userImage"));
                                        conversation.put("receiverID", receiverUser.getUserID());
                                        conversation.put("receiverName", receiverUser.getUserName());
                                        conversation.put("receiverImage", receiverUser.imageUrl);
                                        conversation.put("lastMessage", shop_input_message.getText().toString());
                                        conversation.put("timeStamp", new Date());
                                        addConversation(conversation);

                                    }
                                    shop_input_message.setText(null);
                                }
                            }
                        }
                    });
        }
    };

    private void readMessage() {
        firebaseFirestore.collection("shop_Chat_Message")
                .whereEqualTo("senderID", firebaseAuth.getCurrentUser().getUid())
                .whereEqualTo("receiverID", receiverUser.getUserID())
                .addSnapshotListener(eventListener);
        firebaseFirestore.collection("shop_Chat_Message")
                .whereEqualTo("senderID", receiverUser.getUserID())
                .whereEqualTo("receiverID", firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(eventListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
      if(error != null) {
          return;
      }
      if(value != null) {
          int count = shop_chat_message_data.size();
          for(DocumentChange documentChange : value.getDocumentChanges()) {
              if(documentChange.getType() == DocumentChange.Type.ADDED) {
                  ShopChatMessageData chat_message_data = new ShopChatMessageData();
                  chat_message_data.senderID = documentChange.getDocument().getString("senderID");
                  chat_message_data.receiverID = documentChange.getDocument().getString("receiverID");
                  chat_message_data.image = documentChange.getDocument().getString("receiverImage");
                  chat_message_data.message = documentChange.getDocument().getString("message");
                  chat_message_data.dateTime = getReadableDateTime(documentChange.getDocument().getDate("timeStamp"));
                  chat_message_data.dateObject = documentChange.getDocument().getDate("timeStamp");
                  shop_chat_message_data.add(chat_message_data);
              }
          }
          shop_chat_message_data.sort(Comparator.comparing(obj -> obj.dateObject));
          if(count == 0) {
              shopChatMessageRecyclerViewAdapter.notifyDataSetChanged();
          } else {
              shopChatMessageRecyclerViewAdapter.notifyItemRangeInserted(shop_chat_message_data.size(), shop_chat_message_data.size());
              shop_chat_recyclerView.smoothScrollToPosition(shop_chat_message_data.size());
          }
          shop_chat_recyclerView.setVisibility(View.VISIBLE);
      }
      shop_chat_progressBar.setVisibility(View.GONE);
      if(conversationID == null) {
          checkForConversation();
      }
    };

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(date);
    }

    private void addConversation(HashMap<String, Object> conversation) {
        firebaseFirestore.collection("shop_Chat_Conversation")
                .add(conversation)
                .addOnSuccessListener(documentReference -> conversationID = documentReference.getId());
    }

    private void updateConversation(String message) {
        DocumentReference documentReference =
                firebaseFirestore.collection("shop_Chat_Conversation").document(conversationID);
        documentReference.update(
                "lastMessage", message,
                "timeStamp", new Date()
        );
    }

    private void checkForConversation() {
        if(shop_chat_message_data.size() != 0) {
            checkForConversationRemotely(
                    firebaseAuth.getCurrentUser().getUid(),
                    receiverUser.userID);
            checkForConversationRemotely(
                    receiverUser.userID,
                    firebaseAuth.getCurrentUser().getUid());
        }
    }

    private void checkForConversationRemotely(String senderID, String receiverID) {
        firebaseFirestore.collection("shop_Conversation")
                .whereEqualTo("senderID", senderID)
                .whereEqualTo("receiverID", receiverID)
                .get()
                .addOnCompleteListener(conversationOnCompleteListner);
    }

    private final OnCompleteListener<QuerySnapshot> conversationOnCompleteListner = task -> {
      if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
          DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
          conversationID = documentSnapshot.getId();
      }
    };

    //接收點擊到的使用者資訊
    private void loadReceiverDetails() {
        if (getIntent().hasExtra("OnClickedUser")) {
            receiverUser = getIntent().getParcelableExtra("OnClickedUser");
            shop_chat_name.setText(receiverUser.userName);
        } else if (getIntent().hasExtra("OnClickedRecentUser")) {
            receiverUser = getIntent().getParcelableExtra("OnClickedRecentUser");
            //shop_chat_name.setText(receiverUser.id);
        }
    }

    //返回鍵
    private final View.OnClickListener backListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };
}