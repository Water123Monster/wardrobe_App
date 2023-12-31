package com.example.hamigua.wardrobe.calendar;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamigua.R;

import java.util.ArrayList;

class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder>
{
    private final ArrayList<String> daysOfMonth;
    private final ArrayList<String> record_Days;
    private final OnItemListener onItemListener;

    public CalendarAdapter(ArrayList<String> daysOfMonth, OnItemListener onItemListener, ArrayList<String> record_days)
    {
        this.daysOfMonth = daysOfMonth;
        this.onItemListener = onItemListener;
        this.record_Days = record_days;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (parent.getHeight() * 0.166666666);
        return new CalendarViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position)
    {
        holder.dayOfMonth.setText(daysOfMonth.get(position));

        if (checkRecord(daysOfMonth.get(position))){
            holder.dayOfMonth.setTextColor(Color.parseColor("#FF0000")); //設置紅色字體
            holder.dayOfMonth.getPaint().setFakeBoldText(true); //設置加粗字體
        }
    }

    //檢查該所選月份有哪些日期有穿搭紀錄
    public boolean checkRecord(String record) {
        for(String day : record_Days) {
            if(record.equals(day))
                return true;
        }
        return false;
    }

    @Override
    public int getItemCount()
    {
        return daysOfMonth.size();
    }

    public interface  OnItemListener
    {
        void onItemClick(int position, String dayText);
    }
}