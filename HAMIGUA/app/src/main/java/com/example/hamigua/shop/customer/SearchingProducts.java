package com.example.hamigua.shop.customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.example.hamigua.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchingProducts extends AppCompatActivity {

    ImageView productsSearch_back;
    EditText productsSearch;
    ListView productsListview;

    FirebaseFirestore DB;
    FirebaseAuth USER;

    Client client;
    Index indexProductName;
    ArrayList<String> searchResult;
    ArrayList<String> searchRecord;

    View footerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_searching_products);

        productsSearch_back = findViewById(R.id.searchResult_back);
        productsSearch_back.setOnClickListener(backListener);
        productsSearch = findViewById(R.id.checkoutTitle);
        productsSearch.requestFocus();

        productsListview = findViewById(R.id.productsListview);
        footerView = getLayoutInflater().inflate(R.layout.product_search_record_footer, null, false);
        TextView deleteAllSearchRecord = footerView.findViewById(R.id.deleteAllSearchRecord);
        deleteAllSearchRecord.setOnClickListener(deleteRecordListener);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showSoftInput(SearchingProducts.this, productsSearch);
            }
        }, 100);

        init();
        //get_DB_Products_Data(); //取得商品總覽
        get_DB_Search_Record(footerView);
        searchListener();
    }

    protected void init() {
        DB = FirebaseFirestore.getInstance();
        USER = FirebaseAuth.getInstance();
        client = new Client("6ZACPJA7TS", "5749eae6e04dfac7277e43594d47132c");
        indexProductName = client.getIndex("product_Name");
        searchResult = new ArrayList<>();
        searchRecord = new ArrayList<>();
    }

    protected void get_DB_Products_Data() {
        DB.collection("products_Information")
                .orderBy("set_Data_Time")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && task.getResult() != null){
                            List<String> productsList = new ArrayList<>();
                            for(DocumentSnapshot document : task.getResult()) {
                                productsList.add(document.getString("product_Name"));
                            }
                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, productsList);
                            productsListview.setAdapter(arrayAdapter);
                        }
                    }
                });
    }

    protected void get_DB_Search_Record(View footerView) {
        DB.collection("user_Information").document(USER.getCurrentUser().getUid()).collection("Shop_Search_Record")
                .orderBy("set_Data_Time", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && task.getResult() != null) {
                            for(DocumentSnapshot document : task.getResult()) {
                                searchRecord.add(document.getString("searchRecord"));
                            }
                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, searchRecord);
                            productsListview.setAdapter(arrayAdapter);
                            productsListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Query query = new Query(searchRecord.get(position))
                                            .setAttributesToRetrieve("product_Name")
                                            .setHitsPerPage(50);
                                    indexProductName.searchAsync(query, new CompletionHandler() {
                                        @Override
                                        public void requestCompleted(JSONObject content, AlgoliaException error) {
                                            try {
                                                JSONArray hits = content.getJSONArray("hits");
                                                List<String> list = new ArrayList<>();
                                                List<String> productsID = new ArrayList<>();
                                                for (int i = 0; i < hits.length(); i++) {
                                                    JSONObject jsonObject = hits.getJSONObject(i);
                                                    String productName = jsonObject.getString("product_Name");
                                                    String productID = jsonObject.getString("objectID");
                                                    list.add(productName + " / " + productID);
                                                    productsID.add(productID);
                                                }
                                                searchResult.addAll(productsID);
                                                set_DB_Searching_State_Yes(searchResult);

                                                Intent intent = new Intent(SearchingProducts.this, SearchingProductsResult.class);
                                                intent.putExtra("searchResult", searchResult);
                                                intent.putExtra("searchString", searchRecord.get(position));
                                                startActivity(intent);
                                                searchResult.clear();
                                                finish();

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            });

                            if(task.getResult().isEmpty()) {
                                productsListview.removeFooterView(footerView);
                            } else {
                                productsListview.addFooterView(footerView);
                            }
                        }
                    }
                });
    }

    @SuppressLint("SimpleDateFormat")
    protected void save_Search_Record_To_DB(String searchRecord) {
        Map<String, Object> recordData = new HashMap<>();
        recordData.put("searchRecord", searchRecord);
        recordData.put("set_Data_Time", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()));

        DB.collection("user_Information").document(USER.getCurrentUser().getUid()).collection("Shop_Search_Record")
                .add(recordData);
    }

    protected void set_DB_Searching_State_Yes(ArrayList<String> searchResult) {
        for (String documentID : searchResult) {
            DB.collection("products_Information").document(documentID)
                    .update("searching_State", "true");
        }
    }

    protected View.OnClickListener deleteRecordListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DB.collection("user_Information").document(USER.getCurrentUser().getUid()).collection("Shop_Search_Record")
                    .orderBy("set_Data_Time", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful() && task.getResult() != null) {
                                ArrayList<String> searchRecordID = new ArrayList<>();
                                ArrayList<String> tempData = new ArrayList<>();
                                for(DocumentSnapshot document : task.getResult()) {
                                    searchRecordID.add(document.getId());
                                }
                                for (String recordID : searchRecordID) {
                                    DB.collection("user_Information").document(USER.getCurrentUser().getUid()).collection("Shop_Search_Record")
                                            .document(recordID)
                                            .delete();
                                }
                                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, tempData);
                                productsListview.setAdapter(arrayAdapter);
                                productsListview.removeFooterView(footerView);
                            }
                        }
                    });
        }
    };

    protected void searchListener() {
        productsSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                Query query = new Query(s.toString())
                        .setAttributesToRetrieve("product_Name")
                        .setHitsPerPage(50);
                indexProductName.searchAsync(query, new CompletionHandler() {
                    @Override
                    public void requestCompleted(JSONObject content, AlgoliaException error) {
                        try {
                            //Log.d("aaaa", content.toString());
                            JSONArray hits = content.getJSONArray("hits");
                            //List<String> list = new ArrayList<>();
                            List<String> productsID = new ArrayList<>();
                            for (int i = 0; i < hits.length(); i++) {
                                JSONObject jsonObject = hits.getJSONObject(i);
                                //String productName = jsonObject.getString("product_Name");
                                String productID = jsonObject.getString("objectID");
                                //list.add(productName + " / " + productID);
                                productsID.add(productID);
                            }
                            /* 商品搜尋結果測試
                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, list);
                            productsListview.setAdapter(arrayAdapter);
                            */

                            productsSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                                @Override
                                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                                    if (actionId == EditorInfo.IME_ACTION_DONE) { //按下鍵盤的輸入(enter)後
                                        InputMethodManager manager = ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE));
                                        if (manager != null) {
                                            manager.hideSoftInputFromWindow(SearchingProducts.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS); //關閉鍵盤
                                        }

                                        searchResult.addAll(productsID);
                                        set_DB_Searching_State_Yes(searchResult);
                                        save_Search_Record_To_DB(productsSearch.getText().toString());

                                        Intent intent = new Intent(SearchingProducts.this, SearchingProductsResult.class);
                                        intent.putExtra("searchResult", searchResult);
                                        intent.putExtra("searchString", productsSearch.getText().toString());
                                        startActivity(intent);
                                        searchResult.clear();
                                        finish();
                                    }
                                    return false;
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    protected View.OnClickListener backListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
            overridePendingTransition(0,0);
        }
    };

    protected static void showSoftInput(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(view != null && imm != null)
            imm.showSoftInput(view, 0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0,0);
    }
}