package com.example.hamigua.shop.adapter;

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

public class ShopTryOnRecyclerViewAdapter extends RecyclerView.Adapter<ShopTryOnRecyclerViewAdapter.ViewHolder> {

    ArrayList<ProductColorData> colorImageData;
    String productCategory;
    Context context;
    LayoutInflater inflater;
    ShopTryOnRecyclerViewAdapter.OnItemClickListener pListener;

    public ShopTryOnRecyclerViewAdapter(Context context, ArrayList<ProductColorData> colorImageData, String productCategory, OnItemClickListener pListener) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.colorImageData = colorImageData;
        this.productCategory = productCategory;
        this.pListener = pListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.product_color_grid_layout_avg_spacing, parent, false );
        return new ShopTryOnRecyclerViewAdapter.ViewHolder(view, pListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.product_color_name.setText(colorImageData.get(position).getProduct_color());
        if(colorImageData.get(position).product_image_url != null) {
            Glide.with(context).load(colorImageData.get(position).product_image_url).into(holder.product_color_image);
        }
    }

    @Override
    public int getItemCount() {
        return colorImageData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView product_color_name;
        ImageView product_color_image;
        ShopTryOnRecyclerViewAdapter.OnItemClickListener pListener;

        public ViewHolder(@NonNull View itemView, ShopTryOnRecyclerViewAdapter.OnItemClickListener pListener) {
            super(itemView);
            product_color_name = itemView.findViewById(R.id.product_color_name);
            product_color_image = itemView.findViewById(R.id.product_color_image);

            this.pListener = pListener;

            product_color_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pListener.onImageClick(colorImageData.get(getAdapterPosition()), productCategory);
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onImageClick(ProductColorData productColorData, String productCategory);
    }
}
