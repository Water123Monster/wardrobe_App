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
import com.example.hamigua.shop.model.OrderData;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class CustomerCompletedAdapter extends RecyclerView.Adapter<CustomerCompletedAdapter.ViewHolder> {

    ArrayList<OrderData> arrOrderData;
    Context context;

    OnItemClickListener cListener;

    public CustomerCompletedAdapter(Context context, ArrayList<OrderData> arrOrderData, OnItemClickListener cListener) {
        this.context = context;
        this.arrOrderData = arrOrderData;
        this.cListener = cListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shop_customer_completed_order_grid_layout, parent, false), cListener);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderData data = arrOrderData.get(position);

        //設定賣場名稱
        holder.unconfirmed_seller_name.setText(data.sellerName);

        //設定訂單商品名稱
        String[] productNameResult = data.stringProductName.split(";");
        holder.unconfirmed_product_name.setText(productNameResult[0]);

        //設定訂單商品選項
        String[] productColorResult = data.stringSelectColor.split(";");
        String[] productSizeResult = data.stringSelectSize.split(";");
        holder.unconfirmed_product_size.setText("選項：" + productColorResult[0] + " , " + productSizeResult[0]);

        //設定訂單商品數量
        String[] productQuantityResult = data.stringSelectQuantity.split(";");
        holder.unconfirmed_product_quantity.setText("x" + productQuantityResult[0]);

        //設定訂單商品金額
        String[] productPriceResult = data.stringProductPrice.split(";");
        holder.unconfirmed_product_price.setText("$" + productPriceResult[0]);

        //設定訂單總商品數量
        holder.unconfirmed_layout_helper.setText(data.orderCount + " 商品");

        //設定訂單總金額
        holder.unconfirmed_price.setText("$" + data.orderPrice);

        //設定訂單商品圖片
        String[] productImageUrlResult = data.stringProductImageUrl.split(";");
        Glide.with(context).load(productImageUrlResult[0]).into(holder.unconfirmed_product_imageview);
    }

    @Override
    public int getItemCount() {
        return arrOrderData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView unconfirmed_seller_name, unconfirmed_product_name, unconfirmed_product_size,
                unconfirmed_product_quantity, unconfirmed_product_price;
        TextView unconfirmed_layout_helper, unconfirmed_price;
        ImageView unconfirmed_product_imageview;
        MaterialButton unconfirmed_ChatWithSeller;

        OnItemClickListener cListener;

        public ViewHolder(@NonNull View itemView, OnItemClickListener cListener) {
            super(itemView);

            unconfirmed_seller_name = itemView.findViewById(R.id.unconfirmed_seller_name);
            unconfirmed_product_name = itemView.findViewById(R.id.rating_product_name);
            unconfirmed_product_size = itemView.findViewById(R.id.rating_product_size);
            unconfirmed_product_quantity = itemView.findViewById(R.id.rating_product_quantity);
            unconfirmed_product_price = itemView.findViewById(R.id.rating_product_price);
            unconfirmed_layout_helper = itemView.findViewById(R.id.unconfirmed_layout_helper);
            unconfirmed_price = itemView.findViewById(R.id.unconfirmed_price);
            unconfirmed_product_imageview = itemView.findViewById(R.id.rating_product_imageview);
            unconfirmed_ChatWithSeller = itemView.findViewById(R.id.unconfirmed_ChatWithSeller);

            this.cListener = cListener;

            unconfirmed_ChatWithSeller.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OrderData data = arrOrderData.get(getAdapterPosition());
                    cListener.onItemClick(data);
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(OrderData orderData);
    }
}
