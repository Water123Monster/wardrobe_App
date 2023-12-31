package com.example.hamigua.shop.recommendAlgorithm.PearsonCorrelation;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class PearsonCorrelationScore {

    /** 讀取當前登入的使用者及ID，資料庫 */
    FirebaseUser USER;
    /** 讀取資料庫 */
    FirebaseFirestore DB;
    /** 鍵為使用者ID，值為HashMap<商品ID, 評分></> */
    HashMap<String, HashMap<String, Double>> ratingList;
    /** 所有使用者相似度物件的list */
    ArrayList<PearsonCorrelationData> simSort = new ArrayList<>();
    /** 所有商品評分 * 相似度的list，用在加權排序法 */
    ArrayList<PearsonCorrelationData> weightedRating = new ArrayList<>();
    /** 主使用者、其他使用者的購買紀錄 + 評分、最終推薦商品清單 */
    HashMap<String, Double> mainUserPurchaseList, otherUserPurchaseList;
    /** 最終推薦商品清單 */
    ArrayList<String> recommendProduct;
    /** 使用者相似度 */
    float similarity;
    /** 主使用者沒購買但 相似其他使用者有購買的商品清單(差集) */
    HashMap<String, Double> purchase_list;

    public PearsonCorrelationScore(HashMap<String, HashMap<String, Double>> ratingList) {
        this.ratingList = ratingList;
        USER = FirebaseAuth.getInstance().getCurrentUser();
        DB = FirebaseFirestore.getInstance();

        /*
        for (Map.Entry<String, HashMap<String, Double>> entry : ratingList.entrySet()) {
            for (Map.Entry<String, Double> entry2 : entry.getValue().entrySet()) {
                Log.d("Pearson測試", "使用者ID：" + entry.getKey() + ", 購買商品ID：" + entry2.getKey() + "，評分：" + entry2.getValue());
            }
        }

         */
    }

    public void get_Recommend_Data() {
        mainUserPurchaseList = ratingList.get(USER.getUid());

        for (Map.Entry<String, HashMap<String, Double>> entry : ratingList.entrySet()) {
            if(entry.getKey().equals(USER.getUid()))
                continue;
            otherUserPurchaseList = ratingList.get(entry.getKey());
            similarity = (float) sim_pearson(mainUserPurchaseList, otherUserPurchaseList);
            Log.d("測試", "使用者" + entry.getKey() + "相似度" + similarity);
            purchase_list = equals(mainUserPurchaseList, otherUserPurchaseList);

            //if similarity < 0.3就視為相關性低
            if(similarity > 0.3)
                simSort.add(new PearsonCorrelationData(USER.getUid(), similarity, purchase_list)) ;
        }

        simSort.sort(new Comparator<PearsonCorrelationData>() {
            @Override
            public int compare(PearsonCorrelationData o1, PearsonCorrelationData o2) {
                if (o1.sim > o2.sim)
                    return -1;
                else if (o1.sim < o2.sim)
                    return 1;
                else
                    return 0;
            }
        });

        for(PearsonCorrelationData data : simSort){
            for(Map.Entry<String, Double> entry : data.rating.entrySet()) {
                //商品評分*相似度的物件，參數為 商品ID, 相似度, 相似度*評分
                PearsonCorrelationData pearsonData = new PearsonCorrelationData(
                        entry.getKey(), data.sim, entry.getValue() * data.sim);
                weightedRating.add(pearsonData);
            }
        }

        ArrayList<PearsonCorrelationData> weightedRatingSort = recommendProductList(weightedRating);
        weightedRatingSort.sort(new Comparator<PearsonCorrelationData>() {
            @Override
            public int compare(PearsonCorrelationData o1, PearsonCorrelationData o2) {
                if (o1.recommendValue > o2.recommendValue)
                    return -1;
                else if (o1.recommendValue < o2.recommendValue)
                    return 1;
                else
                    return 0;
            }
        });

        recommendProduct = new ArrayList<>();
        for (PearsonCorrelationData data : weightedRatingSort) {
            Log.d("Pearson測試", "推薦商品: " + data.productID + "; 推薦指數: " + data.recommendValue);
            recommendProduct.add(data.productID);
        }

        //依照recommendProduct將推薦順序寫入firebase
        for(int i = recommendProduct.size(); i > 0; i--) {
            Map<String, Object> recommendData = new HashMap<>();
            recommendData.put("recommendValue", String.valueOf(i));
            recommendData.put("recommendAlgorithm", "PearsonCorrelation");
            recommendData.put("recommendUser", USER.getUid());

            DB.collection("products_Information").document(recommendProduct.get(i - 1))
                    .update(recommendData);
        }

        //不在recommendProduct的商品推薦值為0寫入firebase
        Map<String, Object> recommendData = new HashMap<>();
        recommendData.put("recommendValue", String.valueOf(0));
        recommendData.put("recommendAlgorithm", "PearsonCorrelation");
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
     * @param mainUserPurchaseList
     *            主使用者的購買紀錄
     * @param otherUserPurchaseList
     *            其他使用者的購買紀錄
     * @return 皮爾遜相似度
     */
    protected double sim_pearson(HashMap<String, Double> mainUserPurchaseList, HashMap<String, Double> otherUserPurchaseList) {
        // 比對雙方的同名的數據
        ArrayList<String> productIDs = new ArrayList<>();
        for (Map.Entry<String, Double> entry : mainUserPurchaseList.entrySet()) {
            if(otherUserPurchaseList.containsKey(entry.getKey()))
                productIDs.add(entry.getKey());
        }

        double sumX = 0.0;
        double sumY = 0.0;
        double sumX_Sq = 0.0;
        double sumY_Sq = 0.0;
        double sumXY = 0.0;
        int N = productIDs.size();

        for (String productID : productIDs) {

            sumX += mainUserPurchaseList.get(productID);
            sumY += otherUserPurchaseList.get(productID);
            sumX_Sq += Math.pow(mainUserPurchaseList.get(productID), 2);
            sumY_Sq += Math.pow(otherUserPurchaseList.get(productID), 2);
            sumXY += mainUserPurchaseList.get(productID) * otherUserPurchaseList.get(productID);
        }

        double numerator = sumXY - sumX * sumY / N;
        double denominator = Math.sqrt((sumX_Sq - sumX * sumX / N)
                * (sumY_Sq - sumY * sumY / N));

        // 分母不能為0
        if (denominator == 0) {
            return 0;
        }

        return numerator / denominator;
    }

    protected HashMap<String, Double> equals(HashMap<String, Double> mainPurchaseList, HashMap<String, Double> othersPurchaseList ){
        HashMap<String,Double> product_equals = new HashMap<>();//存放使用者N有買使用者1沒買的商品評分紀錄<商品ID，評分>
        for (Map.Entry<String, Double> entry : othersPurchaseList.entrySet()) {
            //判斷HashMap othersPurchaseList的key是否與HashMap mainPurchaseList的一樣
            if(!mainPurchaseList.containsKey(entry.getKey())) //if商品ID不重複才存入
                product_equals.put(entry.getKey(), entry.getValue());
        }
        //傳回產品
        return product_equals;//傳回使用者N有買使用者1沒買的商品評分紀錄<商品ID，評分>
    }


    public ArrayList<PearsonCorrelationData> recommendProductList(ArrayList<PearsonCorrelationData> weightedRating) {
        double rating = 0;
        double simCount = 0;
        ArrayList<PearsonCorrelationData> weightedRatingSortID = new ArrayList<>();
        ArrayList<PearsonCorrelationData> recommendProductList = new ArrayList<>();

        ArrayList<String> productID = new ArrayList<>();
        for (PearsonCorrelationData data : weightedRating) {
            productID.add(data.productID);
        }

        LinkedHashSet<String> hashSet2 = new LinkedHashSet<>(productID); //刪除重複商品ID
        ArrayList<String> productIDWithoutDuplicates = new ArrayList<>(hashSet2);

        //輸出按照商品ID排序的arraylist
        for (String productId : productIDWithoutDuplicates) {
            for (PearsonCorrelationData data : weightedRating) {
                if (data.productID.equals(productId)) {
                    weightedRatingSortID.add(data);
                }
            }
        }


        if(weightedRatingSortID.size() != 0) {
            String temp = weightedRatingSortID.get(0).productID;
            int count = 0;
            for (PearsonCorrelationData data : weightedRatingSortID) {
                if(!temp.equals(data.productID)) {
                    recommendProductList.add(new PearsonCorrelationData(temp, rating / simCount));
                    Log.d("分子分母測試", temp + " , " + rating + " / " + simCount);
                    rating = 0;
                    simCount = 0;
                }
                temp = data.productID;
                rating += data.weightedRating;
                simCount += data.sim;
                count ++;
                if(count == weightedRatingSortID.size()) {
                    recommendProductList.add(new PearsonCorrelationData(temp, rating / simCount));
                    Log.d("分子分母測試", temp + " , " + rating + " / " + simCount);
                }
            }
        }

        return recommendProductList;

    }
}
