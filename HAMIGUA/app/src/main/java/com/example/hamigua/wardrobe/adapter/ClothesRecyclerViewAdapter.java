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
import com.example.hamigua.shop.adapter.ShopCartRecyclerViewAdapter;
import com.example.hamigua.wardrobe.cloth.Cloth;
import com.example.hamigua.wardrobe.model.ClothData;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

//衣櫃子系統cloth.class裡的recyclerView實作的adapter
public class ClothesRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<ClothData> clothesImages;
    Context context;
    LayoutInflater inflater;

    //mListener為點擊到衣服imageButton監聽
    //fListener為點擊到衣服右下角的愛心imageView監聽
    //eListener為點擊到衣服左下角的修改imageView監聽
    //dListener為點擊到衣服左下角的刪除imageView監聽
    OnItemClickListener mListener, fListener, eListener, dListener;

    public static final int VIEW_TYPE_WARDROBE = 1;
    public static final int VIEW_TYPE_SHOP = 2;

    String shopClothes;

    public ClothesRecyclerViewAdapter(Context context, ArrayList<ClothData> clothesImages,
                                      OnItemClickListener mListener,
                                      OnItemClickListener fListener,
                                      OnItemClickListener eListener,
                                      OnItemClickListener dListener) {

        this.context = context;
        this.clothesImages = clothesImages;
        this.inflater = LayoutInflater.from(context);
        this.mListener = mListener;
        this.fListener = fListener;
        this.eListener = eListener;
        this.dListener = dListener;
    }

    @Override
    public int getItemViewType(int position) {
        if(clothesImages.get(position).getImageHashtag1().equals("shop"))
            return VIEW_TYPE_SHOP;
        else
            return VIEW_TYPE_WARDROBE;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view;

        if(viewType == VIEW_TYPE_WARDROBE) {
            view = layoutInflater.inflate(R.layout.favorite_grid_layout, parent, false);
            return new ViewHolder(view, mListener, fListener, eListener);
        } else {
            view = layoutInflater.inflate(R.layout.favorite_grid_layout_shop, parent, false );
            return new ViewHolderShop(view, mListener, dListener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if(getItemViewType(position) == VIEW_TYPE_WARDROBE) {
            ViewHolder viewHolder = (ViewHolder) holder;

            Glide.with(context).load(clothesImages.get(position).getImageUrl()).into(viewHolder.imageButton);
            viewHolder.imageTitle.setText(String.valueOf(clothesImages.get(position).getImageName()));

            //檢查衣服是否有被存進我的最愛，若有就將愛心imageView顯示為實心
            if(clothesImages.get(position).getFavoriteStatus().equals("Yes")) {
                viewHolder.favorite_button.setBackgroundResource(R.drawable.ic_baseline_favorite_24);
            }
        } else {
            ViewHolderShop viewHolderShop = (ViewHolderShop) holder;

            Glide.with(context).load(clothesImages.get(position).getImageUrl()).into(viewHolderShop.imageButton);
            viewHolderShop.imageTitle.setText(String.valueOf(clothesImages.get(position).getImageName()));
        }

    }

    @Override
    public int getItemCount() {
        return clothesImages.size();
    }

    public void setShopClothes(Boolean value) {
        if(value)
            shopClothes = "Yes";
        else
            shopClothes = "No";
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageButton imageButton;
        TextView imageTitle;
        ImageView favorite_button, edit_button;

        OnItemClickListener mListener, fListener, eListener;

        public ViewHolder(@NonNull View itemView, OnItemClickListener mListener, OnItemClickListener fListener, OnItemClickListener eListener) {
            super(itemView);
            imageButton = itemView.findViewById(R.id.clothebutton);
            imageTitle = itemView.findViewById(R.id.clothNameTitle);
            favorite_button = itemView.findViewById(R.id.favorite_button);
            edit_button = itemView.findViewById(R.id.edit_button);

            this.mListener = mListener;
            this.fListener = fListener;
            this.eListener = eListener;

            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    mListener.onItemClick(clothesImages.get(position), shopClothes);
                }
            });

            favorite_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    fListener.onFavoriteClick(clothesImages.get(position));
                }
            });

            edit_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    eListener.onEditClick(clothesImages.get(position), position);
                }
            });
        }
    }

    public class ViewHolderShop extends RecyclerView.ViewHolder {

        ImageButton imageButton;
        TextView imageTitle;
        ImageView delete_button;

        OnItemClickListener mListener, dListener;

        public ViewHolderShop(@NonNull View itemView, OnItemClickListener mListener, OnItemClickListener dListener) {
            super(itemView);

            imageButton = itemView.findViewById(R.id.clothebutton);
            imageTitle = itemView.findViewById(R.id.clothNameTitle);
            delete_button = itemView.findViewById(R.id.delete_button);

            this.mListener = mListener;
            this.dListener = dListener;

            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    mListener.onItemClick(clothesImages.get(position), shopClothes);
                }
            });

            delete_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    dListener.onDeleteClick(clothesImages.get(position), position);
                }
            });
        }


    }

    public interface OnItemClickListener {
        void onItemClick(ClothData clothData, String shopValue);
        void onEditClick(ClothData clothData, int position);
        void onDeleteClick(ClothData clothData, int position);
        void onFavoriteClick(ClothData clothData);
    }
}
