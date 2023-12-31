package com.example.hamigua.shop.customer.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamigua.R;
import com.example.hamigua.shop.customer.RatingProducts;
import com.example.hamigua.shop.customer.adapter.CustomerUnratedAdapter;
import com.example.hamigua.shop.model.OrderData;
import com.example.hamigua.shop.seller.adapter.SellerUnratedAdapter;
import com.example.hamigua.shop.seller.fragment.SellerUnratedFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class CustomerUnratedFragment extends Fragment implements CustomerUnratedAdapter.OnItemClickListener {

    RecyclerView recyclerView;
    ConstraintLayout no_data_hint;

    FirebaseFirestore DB;
    FirebaseAuth USER;
    ArrayList<OrderData> arrOrderData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop_customer_unrated, container, false);

        recyclerView = view.findViewById(R.id.unrated_recyclerview);
        no_data_hint = view.findViewById(R.id.no_data_hint);

        init();
        get_DB_Unrated_Order();

        return view;
    }

    protected void init() {
        DB = FirebaseFirestore.getInstance();
        USER = FirebaseAuth.getInstance();

        arrOrderData = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
    }

    protected void get_DB_Unrated_Order() {

        DB.collection("shop_Order_Information")
                .whereEqualTo("orderStatus", "unrated")
                .whereEqualTo("buyerID", USER.getCurrentUser().getUid())
                .orderBy("setDataTime", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && task.getResult() != null) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                OrderData data = new OrderData(
                                        document.getString("productIdString"),
                                        document.getString("productNameString"),
                                        document.getString("productImageUrlString"),
                                        document.getString("selectColorString"),
                                        document.getString("selectSizeString"),
                                        document.getString("selectQuantityString"),
                                        document.getString("productPriceString"),
                                        document.getId(),
                                        document.getString("orderCount"),
                                        document.getString("orderPrice"),
                                        document.getString("sellerName"),
                                        document.getString("sellerID"),
                                        document.getString("buyerName"),
                                        document.getString("buyerID")
                                );
                                arrOrderData.add(data);
                            }
                            recyclerView.setAdapter(new CustomerUnratedAdapter(getActivity(), arrOrderData, CustomerUnratedFragment.this));

                            if(arrOrderData.size() == 0) {
                                recyclerView.setVisibility(View.GONE);
                                no_data_hint.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });

        arrOrderData.clear();
    }

    @Override
    public void onPause() {
        super.onPause();
        get_DB_Unrated_Order();
    }

    @Override
    public void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                get_DB_Unrated_Order();
            }
        }, 500);

    }

    @Override
    public void onItemClick(OrderData orderData) {
        Intent intent = new Intent(getActivity(), RatingProducts.class);
        intent.putExtra("OrderData", orderData);
        startActivity(intent);
    }
}