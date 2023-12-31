package com.example.hamigua.shop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hamigua.R;
import com.example.hamigua.shop.model.ProductColorData;
import com.example.hamigua.shop.model.ProductData;
import com.example.hamigua.shop.model.ProductSizeData;

import java.util.ArrayList;

public class ShopMyFavoriteRecyclerViewAdapter extends RecyclerView.Adapter<ShopMyFavoriteRecyclerViewAdapter.ViewHolder> {

    ArrayList<ProductData> productsData;
    Context context;
    LayoutInflater inflater;
    Boolean[] favoriteState;

    //cListener為點擊到商品cardView的監聽
    //fListener為點擊到商品cardView裡衣服右下角的愛心imageView監聽
    ShopMyFavoriteRecyclerViewAdapter.OnItemClickListener cListener, fListener;

    public ShopMyFavoriteRecyclerViewAdapter(Context context, ArrayList<ProductData> productsData, OnItemClickListener cListener, OnItemClickListener fListener) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.productsData = productsData;
        this.cListener = cListener;
        this.fListener = fListener;
    }

    public void set_FavoriteState_Array(int count) {
        favoriteState = new Boolean[count];
        for(int i = 0; i < count; i++) {
            favoriteState[i] = true;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.shop_my_favorite_grid_layout, parent, false );
        return new ShopMyFavoriteRecyclerViewAdapter.ViewHolder(view, cListener, fListener);
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
        ImageView product_grid_image, product_grid_favorite;

        ShopMyFavoriteRecyclerViewAdapter.OnItemClickListener cListener, fListener;

        public ViewHolder(@NonNull View itemView, ShopMyFavoriteRecyclerViewAdapter.OnItemClickListener clistener, ShopMyFavoriteRecyclerViewAdapter.OnItemClickListener fListener) {
            super(itemView);
            product_grid_card = itemView.findViewById(R.id.product_grid_card);
            product_grid_name = itemView.findViewById(R.id.product_grid_name);
            product_grid_price_detail = itemView.findViewById(R.id.product_grid_price_detail);
            product_grid_image = itemView.findViewById(R.id.product_grid_image);
            product_grid_favorite = itemView.findViewById(R.id.product_grid_favorite);

            this.cListener = clistener;
            this.fListener = fListener;

            product_grid_card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ProductData data = productsData.get(getAdapterPosition());
                    cListener.onItemClick(data, data.colorData, data.sizeData);
                }
            });

            product_grid_favorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fListener.onFavoriteClick(productsData.get(getAdapterPosition()));
                    if(favoriteState[getAdapterPosition()]) {
                        favoriteState[getAdapterPosition()] = false;
                        product_grid_favorite.setBackgroundResource(R.drawable.ic_baseline_favorite_border_24);
                    } else {
                        favoriteState[getAdapterPosition()] = true;
                        product_grid_favorite.setBackgroundResource(R.drawable.ic_baseline_favorite_24);
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(ProductData productData, ArrayList<ProductColorData> colorData, ArrayList<ProductSizeData> sizeData);
        void onFavoriteClick(ProductData product_data);
    }
}
