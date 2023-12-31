package com.example.hamigua.wardrobe.recommendAlgorithm;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.hamigua.wardrobe.cloth.Cloth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class Score {

    static PersonalPreferenceData personal_preferences; //個人喜好物件建立
    static ArrayList<ClothAttributesData> Cloth = new ArrayList<ClothAttributesData>();//衣服物件陣列

    String clothPosition;

    FirebaseFirestore DB;
    FirebaseUser USER;

    public Score(PersonalPreferenceData personal_preferences, ArrayList<ClothAttributesData> cloth, String clothPosition) {
        this.personal_preferences = personal_preferences;
        this.Cloth = cloth;
        this.clothPosition = clothPosition;

        DB = FirebaseFirestore.getInstance();
        USER = FirebaseAuth.getInstance().getCurrentUser();
    }

    public void scoreCalculating() {

        DB.collection("user_Information").document(USER.getUid()).collection("Virtual_Model")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        /** 哈哥沒穿，單一推薦 */
                        if(task.getResult().size() == 0) {
                            get_single();
                            Log.d("哈哥沒穿，單一推薦", "a");
                        } else {
                            /** 哈哥有穿1件 */
                            if(task.getResult().size() == 1) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    /** 哈哥有穿1件，上衣 */
                                    if(document.getId().equals("top_Clothes")) {
                                        /** 哈哥有穿1件，上衣，點擊上衣類別，單一推薦 */
                                        if(clothPosition.equals("top_Clothes")) {
                                            get_single();
                                            Log.d("哈哥有穿1件，上衣，點擊上衣類別，單一推薦", "a");
                                        }
                                        /** 哈哥有穿1件，上衣，點擊下著類別，多件推薦 */
                                        else if(clothPosition.equals("bottom_Clothes")) {
                                            get_other(document);
                                            Log.d("哈哥有穿1件，上衣，點擊下著類別，多件推薦", "a");
                                        }
                                    }
                                    /** 哈哥有穿1件，下著，多件推薦 */
                                    else if(document.getId().equals("bottom_Clothes")) {
                                        /** 哈哥有穿1件，下著，點擊上衣類別，多件推薦 */
                                        if(clothPosition.equals("top_Clothes")) {
                                            get_other(document);
                                            Log.d("哈哥有穿1件，下著，點擊上衣類別，多件推薦", "a");
                                        }
                                        /** 哈哥有穿1件，下著，點擊下著類別，單一推薦 */
                                        else if(clothPosition.equals("bottom_Clothes")) {
                                            get_single();
                                            Log.d("哈哥有穿1件，下著，點擊下著類別，單一推薦", "a");
                                        }
                                    }
                                }
                            }
                            else if(task.getResult().size() == 2) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    /** 哈哥有穿2件，點擊上衣類別，多件下著推薦 */
                                    if(clothPosition.equals("top_Clothes")) {
                                        if(document.getId().equals("bottom_Clothes")) {
                                            get_other(document);
                                            Log.d("哈哥有穿2件，點擊上衣類別，多件下著推薦", "a");
                                        }
                                    }
                                    /** 哈哥有穿2件，點擊下著類別，多件上衣推薦 */
                                    else if(clothPosition.equals("bottom_Clothes")) {
                                        if(document.getId().equals("top_Clothes")) {
                                            get_other(document);
                                            Log.d("哈哥有穿2件，點擊下著類別，多件上衣推薦", "a");
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
    }

    void get_single() {
        for(int i=0; i<Cloth.size(); i++) {
            Cloth.get(i).get_data();//獲取演算法所需資料
            get_score_single(i);//獲取衣服分數
            //根據權重計算喜愛分數
            Cloth.get(i).sum_score();
            Cloth.get(i).sum_score/=(double) 10;//規一
            update_DB_score(Cloth.get(i).ID, Cloth.get(i).sum_score);
            Log.d("哈哥測試單一", Cloth.get(i).ID +  ", " + Cloth.get(i).sum_score);
        }
    }

    void get_other(QueryDocumentSnapshot document) {

        DB.collection("user_Information").document(USER.getUid()).collection("Clothes")
                .document(document.getString("clothes_Image_Name"))
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        ClothAttributesData other_cloth = new ClothAttributesData(
                                documentSnapshot.getId(),
                                Integer.parseInt(documentSnapshot.getString("H")),
                                Integer.parseInt(documentSnapshot.getString("S")),
                                Integer.parseInt(documentSnapshot.getString("V")),
                                documentSnapshot.getString("width"),
                                documentSnapshot.getString("length")
                        );//如哈哥身上有其他衣服，衣服建立
                        for(int i=0; i<Cloth.size(); i++) {
                            Cloth.get(i).get_data();//獲取演算法所需資料
                            get_score_other(i,other_cloth);//獲取衣服搭配分數
                            //根據權重計算喜愛分數
                            Cloth.get(i).sum_score_other(personal_preferences.importance[0],personal_preferences.importance[1],personal_preferences.importance[2]);
                            Cloth.get(i).sum_score/=(double) 30;//規一
                            update_DB_score(Cloth.get(i).ID, Cloth.get(i).sum_score);
                            Log.d("哈哥測試多件", Cloth.get(i).ID +  ", " + Cloth.get(i).sum_score);
                        }
                    }
                });
    }

    void get_score_other(int i,ClothAttributesData other_cloth) {
        Cloth.get(i).H_score = personal_preferences.get_H(Math.abs(Cloth.get(i).H_num-1));
        Cloth.get(i).BT_score = personal_preferences.get_BT(Cloth.get(i).BT_cloth);
        Cloth.get(i).SL_score = personal_preferences.get_SL(Cloth.get(i).SL_cloth);
        Cloth.get(i).SV_score = personal_preferences.get_SV(Cloth.get(i).s_sring,Cloth.get(i).v_sring);
        Cloth.get(i).match_score = personal_preferences.get_match(Cloth.get(i).HSV,other_cloth.HSV);
    }

    void get_score_single(int i) {
        Cloth.get(i).H_score = personal_preferences.get_H(Math.abs(Cloth.get(i).H_num-1));
        Cloth.get(i).SV_score = personal_preferences.get_SV(Cloth.get(i).s_sring,Cloth.get(i).v_sring);
    }

    void update_DB_score(String ID, Double score) {
        HashMap<String, Object> scoreData = new HashMap<>();
        String temp = String.valueOf(score * 10);
        temp = temp.substring(0,temp.length()-2);
        if(temp.equals("10"))
            temp = "9";
        scoreData.put("recommendValue",temp);

        DB.collection("user_Information").document(USER.getUid()).collection("Clothes")
                .document(ID)
                .update(scoreData);
    }
}
