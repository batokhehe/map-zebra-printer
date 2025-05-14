package com.werhoz.mapzebraprinter.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.werhoz.mapzebraprinter.R;
import com.werhoz.mapzebraprinter.model.TemplateModel;

import java.util.List;

public class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.TemplateViewHolder> {
    private Context context;
    private List<TemplateModel> imageList;

    private OnItemClickListener listener;

    // Interface for click callback
    public interface OnItemClickListener {
        void onItemClick(TemplateModel model, int position);
    }

    public TemplateAdapter(Context context, List<TemplateModel> imageList, OnItemClickListener itemClickListener) {
        this.context = context;
        this.imageList = imageList;
        this.listener = itemClickListener;
    }

    @NonNull
    @Override
    public TemplateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_template, parent, false);
        return new TemplateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TemplateViewHolder holder, int position) {
        TemplateModel model = imageList.get(position);
        holder.imageView.setImageResource(model.imageResId);
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(model, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class TemplateViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public TemplateViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_template);
        }
    }
}

