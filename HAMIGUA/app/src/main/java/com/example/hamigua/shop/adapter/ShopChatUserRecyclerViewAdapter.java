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
import com.example.hamigua.shop.model.ChatUserData;
import com.makeramen.roundedimageview.RoundedImageView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ShopChatUserRecyclerViewAdapter extends RecyclerView.Adapter<ShopChatUserRecyclerViewAdapter.ViewHolder> {


    ArrayList<ChatUserData> chat_user_data;
    Context context;
    LayoutInflater inflater;

    ShopChatUserRecyclerViewAdapter.OnItemClickListener chatListener;

    public ShopChatUserRecyclerViewAdapter(Context context, ArrayList<ChatUserData> chat_user_data, ShopChatUserRecyclerViewAdapter.OnItemClickListener chatListener) {

        this.context = context;
        this.chat_user_data = chat_user_data;
        this.inflater = LayoutInflater.from(context);
        this.chatListener = chatListener;
    }

    @NotNull
    @Override
    public ShopChatUserRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.chat_user_container, parent, false );
        return new ViewHolder(view, chatListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopChatUserRecyclerViewAdapter.ViewHolder holder, int position) {

        Glide.with(context).load(chat_user_data.get(position).getImageUrl()).into(holder.userImage);
        holder.userName.setText(String.valueOf(chat_user_data.get(position).getUserName()));
        holder.userEmail.setText(String.valueOf(chat_user_data.get(position).getEmail()));

    }

    @Override
    public int getItemCount() {
        return chat_user_data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        RoundedImageView userImage;
        TextView userName, userEmail;

        ShopChatUserRecyclerViewAdapter.OnItemClickListener chatListener;

        public ViewHolder(@NonNull View itemView, ShopChatUserRecyclerViewAdapter.OnItemClickListener chatListener) {
            super(itemView);
            userImage = itemView.findViewById(R.id.userImage);
            userName = itemView.findViewById(R.id.userName);
            userEmail = itemView.findViewById(R.id.userEmail);

            this.chatListener = chatListener;

            //itemView.setOnClickListener(this);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    chatListener.onItemClick(chat_user_data.get(position));
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(ChatUserData _chat_user_data);
    }
}
