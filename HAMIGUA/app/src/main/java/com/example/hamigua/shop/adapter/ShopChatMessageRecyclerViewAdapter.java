package com.example.hamigua.shop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hamigua.R;
import com.example.hamigua.shop.customer.Shop;
import com.example.hamigua.shop.model.ShopChatMessageData;
import com.makeramen.roundedimageview.RoundedImageView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ShopChatMessageRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<ShopChatMessageData> shop_chat_message_data;
    String senderID;
    Context context;
    LayoutInflater inflater;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public ShopChatMessageRecyclerViewAdapter(Context context, ArrayList<ShopChatMessageData> shop_chat_message_data, String senderID) {
        this.context = context;
        this.shop_chat_message_data = shop_chat_message_data;
        this.senderID = senderID;
        this.inflater = LayoutInflater.from(context);
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;

        if(viewType == VIEW_TYPE_SENT) {
            view = inflater.inflate(R.layout.chat_sent_message_container, parent, false );
            return new SentMessageViewHolder(view);
        } else {
            view = inflater.inflate(R.layout.chat_received_message_container, parent, false );
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).setData(shop_chat_message_data.get(position));
        } else {
            ((ReceivedMessageViewHolder) holder).setData(shop_chat_message_data.get(position), context);
        }
    }

    @Override
    public int getItemCount() {
        return shop_chat_message_data.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(shop_chat_message_data.get(position).senderID.equals(senderID)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    public static class SentMessageViewHolder extends RecyclerView.ViewHolder{

        TextView chat_text_message, chat_date_time;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            chat_text_message = itemView.findViewById(R.id.chat_text_message);
            chat_date_time = itemView.findViewById(R.id.chat_date_time);
        }

        void setData(ShopChatMessageData shop_chat_message_data) {
            chat_text_message.setText(shop_chat_message_data.message);
            chat_date_time.setText(shop_chat_message_data.dateTime);
        }
    }

    public static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{

        RoundedImageView userImage;
        TextView chat_text_message, chat_date_time;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.chat_image_profile);
            chat_text_message = itemView.findViewById(R.id.chat_text_message);
            chat_date_time = itemView.findViewById(R.id.chat_date_time);
        }

        void setData(ShopChatMessageData shop_chat_message_data, Context context) {
            Glide.with(context).load(shop_chat_message_data.image).into(userImage);
            chat_text_message.setText(shop_chat_message_data.message);
            chat_date_time.setText(shop_chat_message_data.dateTime);
        }
    }
}
