package com.example.hamigua.wardrobe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hamigua.R;
import com.example.hamigua.wardrobe.model.FavoriteData;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class FavoriteRecyclerViewAdapter extends  RecyclerView.Adapter<FavoriteRecyclerViewAdapter.ViewHolder>{

        ArrayList<FavoriteData> clothesImages;
        Context context;
        LayoutInflater inflater;

        //fListener為點擊到我的最愛裡衣服imageButton監聽
        //dListener為點擊到我的最愛裡衣服右下角的愛心imageView監聽
        OnItemClickListener fListener, dListener;


    public FavoriteRecyclerViewAdapter(Context context, ArrayList<FavoriteData> clothesImages, OnItemClickListener fListener, OnItemClickListener dListener) {
        this.clothesImages = clothesImages;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.fListener = fListener;
        this.dListener = dListener;
    }


    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.favorite_grid_layout, parent, false );
        return new ViewHolder(view, fListener, dListener);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Glide.with(context).load(clothesImages.get(position).getImageUrl()).into(holder.imageButton);
        holder.imageTitle.setText(String.valueOf(clothesImages.get(position).getImageName()));

        //檢查該衣服是否有被存進我的最愛，若有就將愛心imageView顯示為實心
        if(clothesImages.get(position).getFavoriteStatus().equals("Yes")) {
            holder.favoriteStatus.setBackgroundResource(R.drawable.ic_baseline_favorite_24);
        }
    }

    @Override
    public int getItemCount() {
        return clothesImages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageButton imageButton;
        TextView imageTitle;
        ImageView favoriteStatus;

        OnItemClickListener FListener, DListener;

        public ViewHolder(@NonNull View itemView, OnItemClickListener fListener, OnItemClickListener dListener) {
            super(itemView);
            imageButton = itemView.findViewById(R.id.clothebutton);
            imageTitle = itemView.findViewById(R.id.clothNameTitle);
            favoriteStatus = itemView.findViewById(R.id.favorite_button);

            this.FListener = fListener;
            this.DListener = dListener;

            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    FListener.onItemClick(clothesImages.get(position));
                }
            });

            favoriteStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    DListener.onFavoriteClick(clothesImages.get(position));
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(FavoriteData favoriteData);
        void onFavoriteClick(FavoriteData favoriteData);
    }
}

