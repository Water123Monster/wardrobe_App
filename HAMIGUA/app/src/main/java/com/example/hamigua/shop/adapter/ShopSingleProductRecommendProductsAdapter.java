package com.example.hamigua.shop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hamigua.R;
import com.example.hamigua.shop.model.ProductColorData;
import com.example.hamigua.shop.model.ProductData;
import com.example.hamigua.shop.model.ProductSizeData;

import java.util.ArrayList;

public class ShopSingleProductRecommendProductsAdapter extends RecyclerView.Adapter<ShopSingleProductRecommendProductsAdapter.ViewHolder> {

    ArrayList<ProductData> productsData;
    Context context;
    LayoutInflater inflater;

    //cListener為點擊到商品cardView的監聽
    ShopSingleProductRecommendProductsAdapter.OnItemClickListener cListener;

    public ShopSingleProductRecommendProductsAdapter(Context context, ArrayList<ProductData> productsData, OnItemClickListener cListener) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.productsData = productsData;
        this.cListener = cListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.shop_products_grid_layout, parent, false );
        return new ShopSingleProductRecommendProductsAdapter.ViewHolder(view, cListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context).load(productsData.get(position).getProduct_Image_URL()).into(holder.product_grid_image);
        holder.product_grid_name.setText(String.valueOf(productsData.get(position).getProduct_Name()));
        holder.product_grid_price_detail.setText(String.valueOf(productsData.get(position).getProduct_Price()));
    }

    @Override
    public int getItemCount() {
        return productsData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        CardView product_grid_card;
        TextView product_grid_name, product_grid_price_detail;
        ImageView product_grid_image;

        ShopSingleProductRecommendProductsAdapter.OnItemClickListener cListener, fListener;

        public ViewHolder(@NonNull View itemView, ShopSingleProductRecommendProductsAdapter.OnItemClickListener cListener) {
            super(itemView);
            product_grid_card = itemView.findViewById(R.id.product_grid_card);
            product_grid_name = itemView.findViewById(R.id.product_grid_name);
            product_grid_price_detail = itemView.findViewById(R.id.product_grid_price_detail);
            product_grid_image = itemView.findViewById(R.id.product_grid_image);

            this.cListener = cListener;

            product_grid_card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ProductData data = productsData.get(getAdapterPosition());
                    cListener.onItemClick(data, data.colorData, data.sizeData);
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(ProductData productData, ArrayList<ProductColorData> colorData, ArrayList<ProductSizeData> sizeData);
    }
}
