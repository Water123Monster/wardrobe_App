package com.example.hamigua.shop.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hamigua.R;
import com.example.hamigua.shop.model.ProductColorData;

import java.util.ArrayList;
import java.util.Arrays;

public class ShopSingleProductViewPagerTintAdapter extends RecyclerView.Adapter<ShopSingleProductViewPagerTintAdapter.ViewHolder> {

    ArrayList<ProductColorData> colorData;
    String[] isSelected;
    Context context;

    ShopSingleProductViewPagerTintAdapter.OnItemClickListener cListener;

    public ShopSingleProductViewPagerTintAdapter(Context context, ArrayList<ProductColorData> colorData, OnItemClickListener cListener) {
        this.context = context;
        this.colorData = colorData;
        this.cListener = cListener;
        isSelected = new String[colorData.size()];
        Arrays.fill(isSelected, "No");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_singleview_viewpagertint_grid_layout, parent, false);

        return new ViewHolder(view, cListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context).load(colorData.get(position).product_image_url).into(holder.color_imageview);
        if(isSelected[position].equals("Yes"))
            holder.color_imageview.setBackgroundResource(R.drawable.stroke_layout_select);
        else
            holder.color_imageview.setBackgroundResource(R.drawable.stroke_layout);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void set_ImageView_Stroke_Select(int index) {
        isSelected[index] = "Yes";
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void set_ImageView_Stroke_Unselect(int index) {
        isSelected[index] = "No";
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return colorData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView color_imageview;

        ShopSingleProductViewPagerTintAdapter.OnItemClickListener cListener;

        public ViewHolder(@NonNull View itemView, ShopSingleProductViewPagerTintAdapter.OnItemClickListener cListener) {
            super(itemView);
            color_imageview = itemView.findViewById(R.id.color_imageview);
            this.cListener = cListener;

            color_imageview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cListener.onColorClick(getAdapterPosition());
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onColorClick(int position);
    }
}
