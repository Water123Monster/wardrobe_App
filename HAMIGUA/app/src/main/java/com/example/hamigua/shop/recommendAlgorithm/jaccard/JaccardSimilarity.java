package com.example.hamigua.shop.recommendAlgorithm.jaccard;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.hamigua.shop.customer.SingleProductView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JaccardSimilarity {

    /** 讀取當前登入的使用者及ID，資料庫 */
    FirebaseUser USER;
    /** 讀取資料庫 */
    FirebaseFirestore DB;
    /** 鍵為使用者ID，值為購買商品列表 */
    HashMap<String, ArrayList<String>> purchaseList;
    /** 所有使用者相似度物件的list */
    ArrayList<UserSimilarityData> simSort = new ArrayList<>();
    /** 主使用者、其他使用者的購買紀錄、最終推薦商品清單 */
    ArrayList<String> mainUserPurchaseList, otherUserPurchaseList, recommendProduct;
    /** 使用者相似度 */
    float similarity;
    /** 主使用者沒購買但 相似其他使用者有購買的商品清單(差集) */
    ArrayList<String> purchase_list;

    public JaccardSimilarity(HashMap<String, ArrayList<String>> purchaseList) {
        this.purchaseList = purchaseList;
        USER = FirebaseAuth.getInstance().getCurrentUser();
        DB = FirebaseFirestore.getInstance();
    }

    public void get_Recommend_Data() {
        mainUserPurchaseList = purchaseList.get(USER.getUid()); //主使用者的購買紀錄
        /*
        for (String str : mainUserPurchaseList) {
            Log.d("主使用者的購買紀錄測試", str);
        }
        */
        //其他使用者的購買紀錄
        for (Map.Entry<String, ArrayList<String>> entry : purchaseList.entrySet()) {
            if(entry.getKey().equals(USER.getUid()))
                continue;
            otherUserPurchaseList = purchaseList.get(entry.getKey());
            /*
            for (String str : otherUserPurchaseList) {
                Log.d("其他使用者的購買紀錄測試", entry.getKey() + " , " + str);
            }
            */
            similarity = jaccard(mainUserPurchaseList, otherUserPurchaseList);
            purchase_list = equals(mainUserPurchaseList, otherUserPurchaseList);

            //把其他使用者與userId1的相似度存起来，只存不為0的
            if(similarity != 0)
                simSort.add(new UserSimilarityData(entry.getKey(), similarity, purchase_list));
        }

        simSort.sort(new Comparator<UserSimilarityData>() {
            @Override
            public int compare(UserSimilarityData o1, UserSimilarityData o2) {
                if (o1.similarity > o2.similarity)
                    return -1;
                else if (o1.similarity < o2.similarity)
                    return 1;
                else
                    return 0;
            }
        });

        recommendProduct = new ArrayList<>();
        for(UserSimilarityData data : simSort) {
            Log.d("測試", "主使用者 與 使用者 " + data.userID + " 的相似度為" + data.similarity + " 推薦清單為 "+ data.purchaseList);
            for (String productID : data.purchaseList) {
                if(!recommendProduct.contains(productID))
                    recommendProduct.add(productID);
            }
        }
        simSort.clear();

        //依照recommendProduct將推薦順序寫入firebase
        for(int i = recommendProduct.size(); i > 0; i--) {
            Map<String, Object> recommendData = new HashMap<>();
            recommendData.put("recommendValue", String.valueOf(i));
            recommendData.put("recommendAlgorithm", "JaccardSimilarity");
            recommendData.put("recommendUser", USER.getUid());

            DB.collection("products_Information").document(recommendProduct.get(i - 1))
                    .update(recommendData);
        }

        //不在recommendProduct的商品推薦值為0寫入firebase
        Map<String, Object> recommendData = new HashMap<>();
        recommendData.put("recommendValue", String.valueOf(0));
        recommendData.put("recommendAlgorithm", "JaccardSimilarity");
        recommendData.put("recommendUser", USER.getUid());


        DB.collection("products_Information")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && task.getResult() != null) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(!recommendProduct.contains(document.getId())) {
                                    DB.collection("products_Information").document(document.getId())
                                            .update(recommendData);
                                }
                            }
                        }
                    }
                });
    }

    /**
     * 计算jaccard系数
     * @param mainUserPurchaseList 	某位使用者購買商品的集合,是string类型
     * @param otherUserPurchaseList	另一位使用者購買商品集合,是string类型
     * @return Jac jaccard相似度
     */
    private float jaccard(ArrayList<String> mainUserPurchaseList, ArrayList<String> otherUserPurchaseList) {
        float commonNum = 0, mergeNum;
        float Jac;
        String P1, P2;
        for(int i = 0; i < mainUserPurchaseList.size(); i++) {
            P1 = mainUserPurchaseList.get(i);
            for (int j = 0; j < otherUserPurchaseList.size(); j++) {
                P2 = otherUserPurchaseList.get(j);
                if(P1.equals(P2))
                    commonNum++; /**計算交集*/
            }
        }
        mergeNum = (mainUserPurchaseList.size() + otherUserPurchaseList.size() - (commonNum));
        if(commonNum == 0 && mergeNum == 0) /**若交集跟聯集皆為空時，相似度為1*/
            Jac = 1;
        else
            Jac = commonNum / mergeNum;

        return Jac;
    }

    private ArrayList<String> equals(ArrayList<String> mainUserPurchaseList, ArrayList<String> otherUserPurchaseList){
        //temp放比較過後都有購買的產品
        ArrayList<String> temp = new ArrayList<>();
        Set<String> set = new LinkedHashSet<>();
        set.addAll(otherUserPurchaseList);
        for (int j = 0; j < mainUserPurchaseList.size(); j++) {
            //不能存入set就代表元素重複=重複購買的產品
            if(!set.add(mainUserPurchaseList.get(j))){
                temp.add(mainUserPurchaseList.get(j));
            }
            set.add(mainUserPurchaseList.get(j));
        }
        //刪除set中跟temp一樣的元素，代表set中只剩不重複的產品
        temp.forEach(set::remove);
        mainUserPurchaseList.forEach(set::remove);
        //傳回產品
        return new ArrayList<String>(set);
    }
}
