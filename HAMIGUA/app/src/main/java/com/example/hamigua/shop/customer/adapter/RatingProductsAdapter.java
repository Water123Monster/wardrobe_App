package com.example.hamigua.shop.customer.adapter;

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
import com.example.hamigua.shop.model.ProductRatingData;

import java.util.ArrayList;

public class RatingProductsAdapter extends RecyclerView.Adapter<RatingProductsAdapter.ViewHolder> {

    ArrayList<ProductRatingData> arrProductRatingData;
    Context context;

    OnItemClickListener star1_Listener, star2_Listener, star3_Listener, star4_Listener, star5_Listener;

    public RatingProductsAdapter(ArrayList<ProductRatingData> arrProductRatingData, Context context,
                                 OnItemClickListener star1_Listener,
                                 OnItemClickListener star2_Listener,
                                 OnItemClickListener star3_Listener,
                                 OnItemClickListener star4_Listener,
                                 OnItemClickListener star5_Listener) {
        this.arrProductRatingData = arrProductRatingData;
        this.context = context;
        this.star1_Listener = star1_Listener;
        this.star2_Listener = star2_Listener;
        this.star3_Listener = star3_Listener;
        this.star4_Listener = star4_Listener;
        this.star5_Listener = star5_Listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RatingProductsAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shop_rating_products_grid_layout, parent, false),
                star1_Listener, star2_Listener, star3_Listener, star4_Listener, star5_Listener);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductRatingData data = arrProductRatingData.get(position);

        //設定商品名稱
        holder.rating_product_name.setText(data.productName);

        //設定商品選項
        holder.rating_product_size.setText("選項：" + data.selectColor + " , " + data.selectSize);

        //設定商品數量
        holder.rating_product_quantity.setText("x" + data.selectQuantity);

        //設定商品金額
        holder.rating_product_price.setText("$" + data.productPrice);

        //設定商品圖片
        Glide.with(context).load(data.productImageUrl).into(holder.rating_product_imageview);

        switch (data.ratingValue) {
            case "0":
                holder.ratingStar_1.setBackgroundResource(R.drawable.ic_round_star_border_24);
                holder.ratingStar_2.setBackgroundResource(R.drawable.ic_round_star_border_24);
                holder.ratingStar_3.setBackgroundResource(R.drawable.ic_round_star_border_24);
                holder.ratingStar_4.setBackgroundResource(R.drawable.ic_round_star_border_24);
                holder.ratingStar_5.setBackgroundResource(R.drawable.ic_round_star_border_24);
                break;
            case "1":
                holder.ratingStar_1.setBackgroundResource(R.drawable.ic_round_star_24);
                holder.ratingStar_2.setBackgroundResource(R.drawable.ic_round_star_border_24);
                holder.ratingStar_3.setBackgroundResource(R.drawable.ic_round_star_border_24);
                holder.ratingStar_4.setBackgroundResource(R.drawable.ic_round_star_border_24);
                holder.ratingStar_5.setBackgroundResource(R.drawable.ic_round_star_border_24);
                break;
            case "2":
                holder.ratingStar_1.setBackgroundResource(R.drawable.ic_round_star_24);
                holder.ratingStar_2.setBackgroundResource(R.drawable.ic_round_star_24);
                holder.ratingStar_3.setBackgroundResource(R.drawable.ic_round_star_border_24);
                holder.ratingStar_4.setBackgroundResource(R.drawable.ic_round_star_border_24);
                holder.ratingStar_5.setBackgroundResource(R.drawable.ic_round_star_border_24);
                break;
            case "3":
                holder.ratingStar_1.setBackgroundResource(R.drawable.ic_round_star_24);
                holder.ratingStar_2.setBackgroundResource(R.drawable.ic_round_star_24);
                holder.ratingStar_3.setBackgroundResource(R.drawable.ic_round_star_24);
                holder.ratingStar_4.setBackgroundResource(R.drawable.ic_round_star_border_24);
                holder.ratingStar_5.setBackgroundResource(R.drawable.ic_round_star_border_24);
                break;
            case "4":
                holder.ratingStar_1.setBackgroundResource(R.drawable.ic_round_star_24);
                holder.ratingStar_2.setBackgroundResource(R.drawable.ic_round_star_24);
                holder.ratingStar_3.setBackgroundResource(R.drawable.ic_round_star_24);
                holder.ratingStar_4.setBackgroundResource(R.drawable.ic_round_star_24);
                holder.ratingStar_5.setBackgroundResource(R.drawable.ic_round_star_border_24);
                break;
            case "5":
                holder.ratingStar_1.setBackgroundResource(R.drawable.ic_round_star_24);
                holder.ratingStar_2.setBackgroundResource(R.drawable.ic_round_star_24);
                holder.ratingStar_3.setBackgroundResource(R.drawable.ic_round_star_24);
                holder.ratingStar_4.setBackgroundResource(R.drawable.ic_round_star_24);
                holder.ratingStar_5.setBackgroundResource(R.drawable.ic_round_star_24);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return arrProductRatingData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView rating_product_imageview;
        TextView rating_product_name, rating_product_size, rating_product_quantity, rating_product_price;
        ImageView ratingStar_1, ratingStar_2, ratingStar_3, ratingStar_4, ratingStar_5;

        OnItemClickListener star1_listener, star2_listener, star3_listener, star4_listener, star5_listener;

        public ViewHolder(View itemView,
                          OnItemClickListener star1_listener,
                          OnItemClickListener star2_listener,
                          OnItemClickListener star3_listener,
                          OnItemClickListener star4_listener,
                          OnItemClickListener star5_listener) {
            super(itemView);

            rating_product_imageview = itemView.findViewById(R.id.rating_product_imageview);
            rating_product_name = itemView.findViewById(R.id.rating_product_name);
            rating_product_size = itemView.findViewById(R.id.rating_product_size);
            rating_product_quantity = itemView.findViewById(R.id.rating_product_quantity);
            rating_product_price = itemView.findViewById(R.id.rating_product_price);
            ratingStar_1 = itemView.findViewById(R.id.ratingStar_1);
            ratingStar_2 = itemView.findViewById(R.id.ratingStar_2);
            ratingStar_3 = itemView.findViewById(R.id.ratingStar_3);
            ratingStar_4 = itemView.findViewById(R.id.ratingStar_4);
            ratingStar_5 = itemView.findViewById(R.id.ratingStar_5);

            this.star1_listener = star1_listener;
            this.star2_listener = star2_listener;
            this.star3_listener = star3_listener;
            this.star4_listener = star4_listener;
            this.star5_listener = star5_listener;

            ratingStar_1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ProductRatingData data = arrProductRatingData.get(getAdapterPosition());
                    star1_listener.onStar1Click(getAdapterPosition(), data);
                }
            });

            ratingStar_2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ProductRatingData data = arrProductRatingData.get(getAdapterPosition());
                    star2_listener.onStar2Click(getAdapterPosition(), data);
                }
            });

            ratingStar_3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ProductRatingData data = arrProductRatingData.get(getAdapterPosition());
                    star3_listener.onStar3Click(getAdapterPosition(), data);
                }
            });

            ratingStar_4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ProductRatingData data = arrProductRatingData.get(getAdapterPosition());
                    star4_listener.onStar4Click(getAdapterPosition(), data);
                }
            });

            ratingStar_5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ProductRatingData data = arrProductRatingData.get(getAdapterPosition());
                    star5_listener.onStar5Click(getAdapterPosition(), data);
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onStar1Click(int position, ProductRatingData productRatingData);
        void onStar2Click(int position, ProductRatingData productRatingData);
        void onStar3Click(int position, ProductRatingData productRatingData);
        void onStar4Click(int position, ProductRatingData productRatingData);
        void onStar5Click(int position, ProductRatingData productRatingData);
    }
}
