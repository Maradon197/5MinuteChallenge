package com.example.a5minutechallenge;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StorageListManager extends RecyclerView.Adapter<StorageListManager.ViewHolder> {

    private final Context context;
    private final ArrayList<StorageItem> storageItems;
    private final OnItemLongClickListener longClickListener;

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    public StorageListManager(Context context, ArrayList<StorageItem> storageItems, OnItemLongClickListener longClickListener) {
        this.context = context;
        this.storageItems = storageItems;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.storage_list_element, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StorageItem item = storageItems.get(position);
        holder.title.setText(item.getTitle());

        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, "Clicked: " + item.getTitle(), Toast.LENGTH_SHORT).show();
        });//placeholder, open file here ig

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(position);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return storageItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.storage_item_title);
        }
    }
}
