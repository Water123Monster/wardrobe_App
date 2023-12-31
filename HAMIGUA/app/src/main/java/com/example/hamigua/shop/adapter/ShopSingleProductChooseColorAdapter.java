package com.example.hamigua.shop.adapter;

import android.content.Context;
import android.util.Log;
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

public class ShopSingleProductChooseColorAdapter extends RecyclerView.Adapter<ShopSingleProductChooseColorAdapter.ViewHolder> {

    ArrayList<ProductColorData> colorData;
    Context context;

    public ShopSingleProductChooseColorAdapter(Context context, ArrayList<ProductColorData> colorData) {
        this.context = context;
        this.colorData = colorData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_singleview_color_grid_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context).load(colorData.get(position).product_image_url).into(holder.color_imageview);
    }

    @Override
    public int getItemCount() {
        return colorData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView color_imageview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            color_imageview = itemView.findViewById(R.id.color_imageview);
        }
    }
}
