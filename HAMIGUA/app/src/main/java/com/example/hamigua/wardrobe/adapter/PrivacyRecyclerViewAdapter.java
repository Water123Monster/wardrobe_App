package com.example.hamigua.wardrobe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hamigua.R;
import com.example.hamigua.wardrobe.model.ClothData;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class PrivacyRecyclerViewAdapter extends RecyclerView.Adapter<PrivacyRecyclerViewAdapter.ViewHolder> {

    ArrayList<ClothData> clothesImages;
    Context context;
    LayoutInflater inflater;

    OnItemClickListener mListener;

    public PrivacyRecyclerViewAdapter(Context context, ArrayList<ClothData> clothesImages, OnItemClickListener mListener) {

        this.context = context;
        this.clothesImages = clothesImages;
        this.inflater = LayoutInflater.from(context);
        this.mListener = mListener;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.privacy_grid_layout, parent, false );
        return new ViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Glide.with(context).load(clothesImages.get(position).getImageUrl()).into(holder.privacyImageView);
        holder.privacyImageTitle.setText(String.valueOf(clothesImages.get(position).getImageName()));

        //檢查衣服的隱私狀態，若公開就顯示checkBox為勾選狀態
        if(clothesImages.get(position).getPrivateStatus().equals("unlocked")) {
            holder.checkBoxPrivacy.setChecked(true);
        }
    }

    @Override
    public int getItemCount() {
        return clothesImages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView privacyImageView;
        TextView privacyImageTitle;
        CheckBox checkBoxPrivacy;

        OnItemClickListener listener;

        public ViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);

            privacyImageView = itemView.findViewById(R.id.privacyImageView);
            privacyImageTitle = itemView.findViewById(R.id.privacyImageTitle);
            checkBoxPrivacy = itemView.findViewById(R.id.checkBoxPrivacy);

            this.listener = listener;

            privacyImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    listener.onItemClick(clothesImages.get(position));
                }
            });

            checkBoxPrivacy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    listener.onCheckBoxClick(clothesImages.get(position));
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(ClothData clothData);
        void onCheckBoxClick(ClothData clothData);
    }
}
