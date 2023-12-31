package com.example.hamigua;

import android.graphics.Color;

import com.google.android.material.button.MaterialButton;

public class SetOutlinedButtonStyle {

    MaterialButton button;

    public SetOutlinedButtonStyle(MaterialButton button) {
        this.button = button;
    }

    //button可使用樣式
    public void setButtonClickable() {
        button.setTextColor(Color.parseColor("#2894FF")); //設定button內字體顏色(藍)
        button.setStrokeColorResource(R.color.blue_main); //設定button框線顏色(藍)
        button.setClickable(true); //設定button可點擊
    }

    //設定button無法使用樣式
    public void setButtonUnClickable() {
        button.setTextColor(Color.parseColor("#888888")); //設定button內字體顏色(灰)
        button.setStrokeColorResource(R.color.light_gray_78); //設定button框線顏色(灰)
        button.setClickable(false); //設定button不可點擊
    }
}
