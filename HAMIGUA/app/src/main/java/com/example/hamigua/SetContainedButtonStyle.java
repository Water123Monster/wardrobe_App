package com.example.hamigua;

import android.app.Activity;
import android.graphics.Color;

import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

public class SetContainedButtonStyle {

    MaterialButton button;
    Activity activity;

    public SetContainedButtonStyle(MaterialButton button, Activity activity) {
        this.button = button;
        this.activity = activity;
    }

    //button可使用樣式
    public void setButtonClickable() {
        button.setTextColor(Color.parseColor("#FFFFFF")); //設定button內字體顏色(白)
        button.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.blue_main)); //設定button背景顏色(藍)
        button.setClickable(true); //設定button可點擊
    }

    //設定button無法使用樣式
    public void setButtonUnClickable() {
        button.setTextColor(Color.parseColor("#A1A1A1")); //設定button內字體顏色(灰)
        button.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.mid_gray_90)); //設定button背景顏色(淺灰)
        button.setClickable(false); //設定button不可點擊
    }
}
