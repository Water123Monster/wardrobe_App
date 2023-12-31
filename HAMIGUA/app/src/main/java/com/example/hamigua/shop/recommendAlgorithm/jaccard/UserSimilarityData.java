package com.example.hamigua.shop.recommendAlgorithm.jaccard;

import java.util.ArrayList;

public class UserSimilarityData {

    public String userID;
    public float similarity;
    public ArrayList<String> purchaseList;

    public UserSimilarityData(String userID, float similarity, ArrayList<String> purchaseList) {
        this.userID = userID;
        this.similarity = similarity;
        this.purchaseList = purchaseList;
    }
}
