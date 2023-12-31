package com.example.hamigua.wardrobe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hamigua.R;
import com.example.hamigua.wardrobe.model.ClothData;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class StatisticsRecyclerViewAdapter extends RecyclerView.Adapter<StatisticsRecyclerViewAdapter.ViewHolder> {

    ArrayList<ClothData> clothesImages;
    Context context;
    LayoutInflater inflater;

    OnItemClickListener mListener;

    public StatisticsRecyclerViewAdapter(Context context, ArrayList<ClothData> clothesImages, OnItemClickListener mListener) {

        this.context = context;
        this.clothesImages = clothesImages;
        this.inflater = LayoutInflater.from(context);
        this.mListener = mListener;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.clothes_grid_layout, parent, false );
        return new ViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Glide.with(context).load(clothesImages.get(position).getImageUrl()).into(holder.imageButton);
        holder.imageTitle.setText(String.valueOf(clothesImages.get(position).getImageName()));

    }

    @Override
    public int getItemCount() {
        return clothesImages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageButton imageButton;
        TextView imageTitle;

        OnItemClickListener listener;

        public ViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            imageButton = itemView.findViewById(R.id.clothebutton);
            imageTitle = itemView.findViewById(R.id.clothNameTitle);

            this.listener = listener;
            imageButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            //imageButton.setBackgroundResource(R.drawable.clothes_imagebutton_selected_style);
            listener.onItemClick(clothesImages.get(position));
        }
    }

    public interface OnItemClickListener {
        void onItemClick(ClothData clothData);
    }
}
