package com.example.hamigua.communicate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.hamigua.R;

public class MyAdapter_Explore extends BaseAdapter {
    private Context context;
    private final String[] text;
    private final int[] imageId;

    public MyAdapter_Explore(Context context, String[] text, int[] imageId) {
        this.context = context;
        this.text = text;
        this.imageId = imageId;
    }

    @Override
    public int getCount() {
        return text.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View grid;
        // Context 動態放入communicate_explore
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            grid = new View(context);
            // 將grid_item_explore 動態載入(image+text)
            grid = layoutInflater.inflate(R.layout.grid_item, null);
            TextView textView = (TextView) grid.findViewById(R.id.txtItem);
            ImageView imageView = (ImageView) grid.findViewById(R.id.imgTest1);
            textView.setText(text[position]);
            imageView.setImageResource(imageId[position]);
        } else {
            grid = (View) convertView;
        }
        return grid;
    }
}



