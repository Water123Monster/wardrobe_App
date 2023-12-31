package com.example.hamigua.shop.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hamigua.R;
import com.example.hamigua.shop.model.ProductColorData;

import java.util.ArrayList;

public class ShopSingleProductViewPagerAdapter extends RecyclerView.Adapter<ShopSingleProductViewPagerAdapter.ViewHolder> {

    ArrayList<ProductColorData> colorData;
    Context context;

    public ShopSingleProductViewPagerAdapter(Context context, ArrayList<ProductColorData> colorData) {
        this.context = context;
        this.colorData = colorData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_image_viewpager2_item,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context).load(colorData.get(position).product_image_url).into(holder.colorImage);
    }

    @Override
    public int getItemCount() {
        return colorData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView colorImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            colorImage = itemView.findViewById(R.id.colorImage);
        }
    }

}
