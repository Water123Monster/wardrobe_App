package com.example.hamigua.shop.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.example.hamigua.R;
import com.example.hamigua.shop.model.CartData;
import com.example.hamigua.shop.model.ProductColorData;

import java.util.ArrayList;

public class ShopCartRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<CartData> cartData;
    Context context;
    LayoutInflater inflater;

    //checkboxListener為點擊到商品checkbox的監聽
    //minusListener, plusListener為點擊到更改商品數量的監聽
    //deleteListener為左滑layout的移除商品監聽
    ShopCartRecyclerViewAdapter.OnItemClickListener checkboxListener, minusListener, plusListener, deleteListener, sizeListener;
    ShopCartRecyclerViewAdapter.OnItemClickListener sellerCheckboxListener, sellerNameListener, sellerEditListener;
    ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

    public static final int VIEW_TYPE_SELLER = 1;
    public static final int VIEW_TYPE_PRODUCT = 2;

    public ShopCartRecyclerViewAdapter(Context context, ArrayList<CartData> cartData,
                                       ShopCartRecyclerViewAdapter.OnItemClickListener checkboxListener,
                                       ShopCartRecyclerViewAdapter.OnItemClickListener minusListener,
                                       ShopCartRecyclerViewAdapter.OnItemClickListener plusListener,
                                       ShopCartRecyclerViewAdapter.OnItemClickListener deleteListener,
                                       ShopCartRecyclerViewAdapter.OnItemClickListener sizeListener,
                                       ShopCartRecyclerViewAdapter.OnItemClickListener sellerCheckboxListener,
                                       ShopCartRecyclerViewAdapter.OnItemClickListener sellerNameListener,
                                       ShopCartRecyclerViewAdapter.OnItemClickListener sellerEditListener) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.cartData = cartData;
        this.checkboxListener = checkboxListener;
        this.minusListener = minusListener;
        this.plusListener = plusListener;
        this.deleteListener = deleteListener;
        this.sizeListener = sizeListener;
        this.sellerCheckboxListener = sellerCheckboxListener;
        this.sellerNameListener = sellerNameListener;
        this.sellerEditListener = sellerEditListener;
    }

    @Override
    public int getItemViewType(int position) {
        if(cartData.get(position).productName == null)
            return VIEW_TYPE_SELLER;
        else
            return VIEW_TYPE_PRODUCT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view;

        if(viewType == VIEW_TYPE_SELLER) {
            view = layoutInflater.inflate(R.layout.cart_seller_grid_layout, parent, false);
            return new SellerViewHolder(view, sellerCheckboxListener, sellerNameListener, sellerEditListener);
        } else {
            view = layoutInflater.inflate(R.layout.cart_products_grid_layout, parent, false );
            return new ProductViewHolder(view, checkboxListener, minusListener, plusListener, deleteListener, sizeListener);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        CartData data = cartData.get(position);

        if(getItemViewType(position) == VIEW_TYPE_SELLER) {
            //綁定SellerViewHolder
            SellerViewHolder sellerViewHolder = (SellerViewHolder) holder;
            sellerViewHolder.cart_seller_name.setText(data.sellerName);

            if(data.isChecked.equals("true")) {
                sellerViewHolder.cart_seller_checkbox.setOnCheckedChangeListener(null);
                sellerViewHolder.cart_seller_checkbox.setChecked(true);
                sellerViewHolder.cart_seller_checkbox.setOnCheckedChangeListener(sellerViewHolder.sellerListener);
            } else {
                sellerViewHolder.cart_seller_checkbox.setOnCheckedChangeListener(null);
                sellerViewHolder.cart_seller_checkbox.setChecked(false);
                sellerViewHolder.cart_seller_checkbox.setOnCheckedChangeListener(sellerViewHolder.sellerListener);
            }

            sellerViewHolder.cart_seller_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String state = sellerViewHolder.cart_seller_edit.getText().toString();
                    viewBinderHelper.setOpenOnlyOne(false);
                    if(state.equals("編輯")) {
                        sellerViewHolder.cart_seller_edit.setText("完成");
                        for (CartData cartData : cartData) {
                            if(cartData.productID != null && cartData.sellerID.equals(data.sellerID)) {
                                open_Seller_viewBinderHelper(cartData.productID);
                            }
                        }
                    } else if(state.equals("完成")){
                        sellerViewHolder.cart_seller_edit.setText("編輯");
                        for (CartData cartData : cartData) {
                            if(cartData.productID != null && cartData.sellerID.equals(data.sellerID)) {
                                viewBinderHelper.unlockSwipe(cartData.productID);
                                close_viewBinderHelper(cartData.productID);
                            }
                        }
                    }
                }
            });

            viewBinderHelper.setOpenOnlyOne(true);
        } else {
            //綁定ProductViewHolder
            ProductViewHolder productViewHolder = (ProductViewHolder) holder;
            viewBinderHelper.bind(productViewHolder.swipeRevealLayout, data.getProductID());
            viewBinderHelper.setOpenOnlyOne(true);

            int price = Integer.parseInt(data.getProductPrice());

            String colorUrl = "";
            for (ProductColorData colorData : data.colorData) {
                if(data.selectColor.equals(colorData.product_color))
                    colorUrl = colorData.product_image_url;
            }

            Glide.with(context).load(colorUrl).into(productViewHolder.cart_product_imageview); //商品個別顏色照片
            productViewHolder.cart_product_name.setText(data.getProductName());
            productViewHolder.cart_product_size.setText("規格：" + data.getSelectColor() + " , " + data.getSelectSize());
            productViewHolder.cart_product_price.setText("$" + price);
            productViewHolder.cart_product_quantity.setText(data.getSelectQuantity());
            productViewHolder.cart_product_checkbox.setChecked(data.getIsChecked().equals("true"));
        }
    }

    public void close_viewBinderHelper(String Id) {
        viewBinderHelper.closeLayout(Id);
    }

    public void open_Seller_viewBinderHelper(String Id) {
        viewBinderHelper.openLayout(Id);
        viewBinderHelper.lockSwipe(Id);
    }

    @Override
    public int getItemCount() {
        return cartData.size();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {

        SwipeRevealLayout swipeRevealLayout;
        LinearLayout layout_delete;

        CheckBox cart_product_checkbox;
        ImageView cart_product_imageview, cart_product_quantity_minus, cart_product_quantity_plus;
        TextView cart_product_name, cart_product_size, cart_product_price;
        EditText cart_product_quantity;

        ShopCartRecyclerViewAdapter.OnItemClickListener checkboxListener, minusListener, plusListener, deleteListener, sizeListener;

        public ProductViewHolder(@NonNull View itemView,
                                 ShopCartRecyclerViewAdapter.OnItemClickListener checkboxListener,
                                 ShopCartRecyclerViewAdapter.OnItemClickListener minusListener,
                                 ShopCartRecyclerViewAdapter.OnItemClickListener plusListener,
                                 ShopCartRecyclerViewAdapter.OnItemClickListener deleteListener,
                                 ShopCartRecyclerViewAdapter.OnItemClickListener sizeListener) {
            super(itemView);

            swipeRevealLayout = itemView.findViewById(R.id.swipeRevealLayout);
            layout_delete = itemView.findViewById(R.id.layout_delete);

            cart_product_checkbox = itemView.findViewById(R.id.cart_product_checkbox);
            cart_product_imageview = itemView.findViewById(R.id.rating_product_imageview);
            cart_product_quantity_minus = itemView.findViewById(R.id.cart_product_quantity_minus);
            cart_product_quantity_plus = itemView.findViewById(R.id.select_product_quantity_plus);
            cart_product_quantity = itemView.findViewById(R.id.select_product_quantity);
            cart_product_quantity.setFocusable(false); //使用者無法手動輸入
            cart_product_name = itemView.findViewById(R.id.rating_product_name);
            cart_product_size = itemView.findViewById(R.id.rating_product_size);
            cart_product_price = itemView.findViewById(R.id.rating_product_price);
            this.checkboxListener = checkboxListener;
            this.minusListener = minusListener;
            this.plusListener = plusListener;
            this.deleteListener = deleteListener;
            this.sizeListener = sizeListener;

            cart_product_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    checkboxListener.onCheckBoxChanged(cartData.get(getAdapterPosition()), getAdapterPosition(), isChecked);
                }
            });

            cart_product_quantity_minus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    minusListener.onQuantityMinusClick(cartData.get(getAdapterPosition()), getAdapterPosition(), cartData.get(getAdapterPosition()).getIsChecked());
                }
            });

            cart_product_quantity_plus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    plusListener.onQuantityPlusClick(cartData.get(getAdapterPosition()), getAdapterPosition(), cartData.get(getAdapterPosition()).getIsChecked());
                }
            });

            cart_product_size.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sizeListener.onSelectSizeClick(cartData.get(getAdapterPosition()), getAdapterPosition());
                }
            });

            layout_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteListener.onProductDeleteClick(cartData.get(getAdapterPosition()), getAdapterPosition());
                }
            });
        }
    }

    public class SellerViewHolder extends RecyclerView.ViewHolder {

        CheckBox cart_seller_checkbox;
        TextView cart_seller_name, cart_seller_edit;

        ShopCartRecyclerViewAdapter.OnItemClickListener sellerCheckboxListener, sellerNameListener, sellerEditListener;

        public SellerViewHolder(@NonNull View itemView,
                                ShopCartRecyclerViewAdapter.OnItemClickListener sellerCheckboxListener,
                                ShopCartRecyclerViewAdapter.OnItemClickListener sellerNameListener,
                                ShopCartRecyclerViewAdapter.OnItemClickListener sellerEditListener) {
            super(itemView);
            cart_seller_checkbox = itemView.findViewById(R.id.cart_seller_checkbox);
            cart_seller_name = itemView.findViewById(R.id.cart_seller_name);
            cart_seller_edit = itemView.findViewById(R.id.cart_seller_edit);
            this.sellerCheckboxListener = sellerCheckboxListener;
            this.sellerNameListener= sellerNameListener;

            cart_seller_checkbox.setOnCheckedChangeListener(sellerListener);
            cart_seller_name.setOnClickListener(nameListener);
            cart_seller_edit.setOnClickListener(editListener);
        }

        CompoundButton.OnCheckedChangeListener sellerListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sellerCheckboxListener.onSellerCheckBoxChanged(cartData.get(getAdapterPosition()), getAdapterPosition(), isChecked);
            }
        };

        View.OnClickListener nameListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sellerNameListener.onSellerNameClick(cartData.get(getAdapterPosition()), getAdapterPosition());
            }
        };

        View.OnClickListener editListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sellerEditListener.onSellerEditClick(cartData.get(getAdapterPosition()), );
            }
        };
    }

    public interface OnItemClickListener {
        void onCheckBoxChanged(CartData cartData, int position, boolean isChecked);
        void onQuantityMinusClick(CartData cartData, int position, String isChecked);
        void onQuantityPlusClick(CartData cartData, int position, String isChecked);
        void onProductDeleteClick(CartData cartData, int position);
        void onSelectSizeClick(CartData cartData, int position);
        void onSellerCheckBoxChanged(CartData cartData, int position, boolean isChecked);
        void onSellerNameClick(CartData cartData, int position);
        void onSellerEditClick(CartData cartData, String state);
    }
}
