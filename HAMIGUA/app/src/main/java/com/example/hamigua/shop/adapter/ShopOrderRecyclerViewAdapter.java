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
import com.example.hamigua.shop.model.CartData;
import com.example.hamigua.shop.model.ProductColorData;

import java.util.ArrayList;

public class ShopOrderRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<CartData> orderProductData;
    Context context;
    LayoutInflater inflater;

    public static final int VIEW_TYPE_SELLER = 1;
    public static final int VIEW_TYPE_PRODUCT = 2;
    public static final int VIEW_TYPE_ORDER_PRICE = 3;

    public ShopOrderRecyclerViewAdapter(Context context, ArrayList<CartData> orderProductData) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.orderProductData = orderProductData;
    }

    @Override
    public int getItemViewType(int position) {
        CartData data = orderProductData.get(position);
        if(data.productName == null && data.sellerName != null)
            return VIEW_TYPE_SELLER;
        else if(data.productName == null)
            return VIEW_TYPE_ORDER_PRICE;
        else
            return VIEW_TYPE_PRODUCT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view;

        if(viewType == VIEW_TYPE_SELLER) {
            view = layoutInflater.inflate(R.layout.checkout_seller_grid_layout, parent, false);
            return new SellerViewHolder(view);
        } else if(viewType == VIEW_TYPE_PRODUCT){
            view = layoutInflater.inflate(R.layout.checkout_products_grid_layout, parent, false );
            return new ProductViewHolder(view);
        } else {
            view = layoutInflater.inflate(R.layout.checkout_order_price_layout, parent, false );
            return new OrderPriceViewHolder(view);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        CartData data = orderProductData.get(position);

        if(getItemViewType(position) == VIEW_TYPE_SELLER) {
            //綁定SellerViewHolder
            SellerViewHolder sellerViewHolder = (SellerViewHolder) holder;
            sellerViewHolder.checkout_seller_name.setText(data.sellerName);
        } else if(getItemViewType(position) == VIEW_TYPE_PRODUCT) {
            //綁定ProductViewHolder
            ProductViewHolder productViewHolder = (ProductViewHolder) holder;

            int price = Integer.parseInt(data.getProductPrice());
            String colorUrl = "";
            for (ProductColorData colorData : data.colorData) {
                if(data.selectColor.equals(colorData.product_color))
                    colorUrl = colorData.product_image_url;
            }

            Glide.with(context).load(colorUrl).into(productViewHolder.checkout_product_imageview); //商品個別顏色照片
            productViewHolder.checkout_product_name.setText(data.getProductName());
            productViewHolder.checkout_product_size.setText("選項：" + data.getSelectColor() + " , " + data.getSelectSize());
            productViewHolder.checkout_product_price.setText("$" + price);
            productViewHolder.checkout_product_quantity.setText("x" + data.getSelectQuantity());
        } else {
            //綁定OrderPriceViewHolder
            OrderPriceViewHolder orderPriceViewHolder = (OrderPriceViewHolder) holder;
            orderPriceViewHolder.checkout_layout_helper.setText("訂單金額 (" + data.selectQuantity + "商品)：");
            orderPriceViewHolder.checkout_price.setText("$" + data.productPrice);
        }
    }

    @Override
    public int getItemCount() {
        return orderProductData.size();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {

        ImageView checkout_product_imageview;
        TextView checkout_product_name, checkout_product_size, checkout_product_price, checkout_product_quantity;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            checkout_product_imageview = itemView.findViewById(R.id.rating_product_imageview);
            checkout_product_name = itemView.findViewById(R.id.rating_product_name);
            checkout_product_size = itemView.findViewById(R.id.rating_product_size);
            checkout_product_price = itemView.findViewById(R.id.rating_product_price);
            checkout_product_quantity = itemView.findViewById(R.id.rating_product_quantity);
        }
    }

    public class SellerViewHolder extends RecyclerView.ViewHolder {

        TextView checkout_seller_name;

        public SellerViewHolder(@NonNull View itemView) {
            super(itemView);
            checkout_seller_name = itemView.findViewById(R.id.checkout_seller_name);
        }
    }

    public class OrderPriceViewHolder extends RecyclerView.ViewHolder {

        TextView checkout_layout_helper, checkout_price;

        public OrderPriceViewHolder(@NonNull View itemView) {
            super(itemView);
            checkout_layout_helper = itemView.findViewById(R.id.unconfirmed_layout_helper);
            checkout_price = itemView.findViewById(R.id.unconfirmed_price);
        }
    }
}
