package com.example.hamigua.wardrobe.statistics;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hamigua.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

import android.os.Bundle;

public class Statistics extends AppCompatActivity {

    // 條形圖的變量
    BarChart barChart;

    // 數據的變量
    BarData barData;

    // 條形數據集的變量
    BarDataSet barDataSet;

    // 用於存儲條目的數組列表
    ArrayList barEntriesArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        // 條形圖的初始化變量
        barChart = findViewById(R.id.idBarChart);

        // 調用方法來獲取柱線條目
        getBarEntries();

        // 創建新的條形數據集
        barDataSet = new BarDataSet(barEntriesArrayList, "週期頻率");

        // 創建一個新的柱數據並傳遞我們的柱數據集
        barData = new BarData(barDataSet);

        // 將數據設置到我們的條形圖
        barChart.setData(barData);

        // 條形數據集添加顏色
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        // 設置文字顏色
        barDataSet.setValueTextColor(Color.BLACK);

        // 設置文字大小
        barDataSet.setValueTextSize(16f);
        barChart.getDescription().setEnabled(false);
    }
    private void getBarEntries() {
        // 創建一個新的數組列表
        barEntriesArrayList = new ArrayList<>();

        // 使用 bar 向數組列表添加新條目
        // 輸入並將 x 和 y 軸值傳遞
        barEntriesArrayList.add(new BarEntry(1f, 4));
        barEntriesArrayList.add(new BarEntry(2f, 6));
        barEntriesArrayList.add(new BarEntry(3f, 8));
        barEntriesArrayList.add(new BarEntry(4f, 2));
        barEntriesArrayList.add(new BarEntry(5f, 4));
        barEntriesArrayList.add(new BarEntry(6f, 1));
        barEntriesArrayList.add(new BarEntry(7f, 3));
        barEntriesArrayList.add(new BarEntry(8f, 4));
        barEntriesArrayList.add(new BarEntry(9f, 7));
        barEntriesArrayList.add(new BarEntry(10f, 1));
        barEntriesArrayList.add(new BarEntry(11f, 5));
        barEntriesArrayList.add(new BarEntry(12f, 3));
    }
}