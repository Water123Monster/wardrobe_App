package com.example.hamigua.shop.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.hamigua.R;
import com.example.hamigua.shop.model.ProductSizeData;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class ShopProductQuantityRecyclerViewAdapter extends RecyclerView.Adapter<ShopProductQuantityRecyclerViewAdapter.ViewHolder> {

    ArrayList<ProductSizeData> sizeData;
    String[] Quantity;
    Context context;
    LayoutInflater inflater;

    public ShopProductQuantityRecyclerViewAdapter(Context context, ArrayList<ProductSizeData> sizeData) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.sizeData = sizeData;
        Quantity = new String[sizeData.size()];
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.product_quantity_grid_layout, parent, false );
        return new ShopProductQuantityRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.productColor.setText(sizeData.get(position).getProduct_color());
        holder.productSize.setText(sizeData.get(position).getProduct_size());
    }

    @Override
    public int getItemCount() {
        return sizeData.size();
    }

    public String[] getQuantity() {
        return Quantity;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView productColor, productSize;
        EditText edtQuantity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productColor = itemView.findViewById(R.id.productColor);
            productSize = itemView.findViewById(R.id.productSize);
            edtQuantity = itemView.findViewById(R.id.edtQuantity);

            edtQuantity.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Quantity[getAdapterPosition()] = s.toString();
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
    }
}
