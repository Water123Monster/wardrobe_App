package com.example.hamigua.shop.recommendAlgorithm.PearsonCorrelation;

import java.util.HashMap;

public class PearsonCorrelationData {

    public String userID; //使用者ID
    public String productID; //商品ID
    public double sim; //使用者相似度
    public double weightedRating; //加權後評分，相似度*評分
    public HashMap<String, Double> rating; //初始評分紀錄
    public double recommendValue; //最終推薦指數

    public PearsonCorrelationData(String userID, double sim, HashMap<String, Double> rating) {
        this.userID = userID;
        this.sim = sim;
        this.rating = rating;
    }

    public PearsonCorrelationData(String productID, double sim, double weightedRating) {
        this.productID = productID;
        this.sim = sim;
        this.weightedRating = weightedRating;
    }

    public PearsonCorrelationData(String productID, double recommendValue) {
        this.productID = productID;
        this.recommendValue = recommendValue;
    }
}
