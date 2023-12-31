package com.example.hamigua.shop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamigua.R;
import com.example.hamigua.shop.model.ShopChatMessageData;
import com.example.hamigua.shop.chat.ChatUser;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;

public class ShopChatRecentConversationRecyclerViewAdapter extends RecyclerView.Adapter<ShopChatRecentConversationRecyclerViewAdapter.ConversationViewHolder> {

    ArrayList<ShopChatMessageData> shop_chat_message_data;
    Context context;
    LayoutInflater inflater;

    ShopChatRecentConversationRecyclerViewAdapter.OnItemClickListener chatListener;

    public ShopChatRecentConversationRecyclerViewAdapter(Context context, ArrayList<ShopChatMessageData> shop_chat_message_data, ShopChatRecentConversationRecyclerViewAdapter.OnItemClickListener chatListener) {
        this.context = context;
        this.shop_chat_message_data = shop_chat_message_data;
        this.inflater = LayoutInflater.from(context);
        this.chatListener = chatListener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.chat_recent_conversation_container, parent, false );
        return new ConversationViewHolder(view, chatListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {

        //Glide.with(context).load(chat_user_data.get(position).getImageUrl()).into(holder.imageButton);
        holder.userName.setText(String.valueOf(shop_chat_message_data.get(position).getConversationName()));
        holder.recentMessage.setText(String.valueOf(shop_chat_message_data.get(position).getMessage()));
    }

    @Override
    public int getItemCount() {
        return shop_chat_message_data.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {

        RoundedImageView userImage;
        TextView userName, recentMessage;

        ShopChatRecentConversationRecyclerViewAdapter.OnItemClickListener chatListener;

        public ConversationViewHolder(@NonNull View itemView, ShopChatRecentConversationRecyclerViewAdapter.OnItemClickListener chatListener) {
            super(itemView);
            userImage = itemView.findViewById(R.id.userImage);
            userName = itemView.findViewById(R.id.userName);
            recentMessage = itemView.findViewById(R.id.recentMessage);

            this.chatListener = chatListener;

            //itemView.setOnClickListener(this);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    ChatUser user = new ChatUser();
                    user.id = shop_chat_message_data.get(position).conversationID;
                    user.name = shop_chat_message_data.get(position).conversationName;
                    //user.image = shop_chat_message_data.get(position).conversationImage;
                    chatListener.onItemClick(user);
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(ChatUser user);
    }
}
