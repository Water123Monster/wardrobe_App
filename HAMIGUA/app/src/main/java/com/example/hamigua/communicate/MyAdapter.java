package com.example.hamigua.communicate;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamigua.R;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{
    private List<String> mData;

    MyAdapter(List<String> data) {
        mData = data;
    }

    public MyAdapter(Communicate_Explore communicate_explore, String[] text, int[] imageId) {
    }
    // 建立ViewHolder
    class ViewHolder extends RecyclerView.ViewHolder{
        // 宣告元件
        private TextView txtItem;

        ViewHolder(View itemView) {
            super(itemView);
            txtItem = (TextView) itemView.findViewById(R.id.txtItem);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 連結項目布局檔list_item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item, parent, false);
        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // 設置txtItem要顯示的內容
        holder.txtItem.setText(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}

