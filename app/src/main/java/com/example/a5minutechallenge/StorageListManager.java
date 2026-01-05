/** RecyclerView that displays storage items. */
package com.example.a5minutechallenge;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

public class StorageListManager extends RecyclerView.Adapter<StorageListManager.ViewHolder> {

    private final Context context;
    private final ArrayList<StorageListItem> storageItems;
    private final OnItemLongClickListener longClickListener;

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    public StorageListManager(Context context, ArrayList<StorageListItem> storageItems, OnItemLongClickListener longClickListener) {
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
        StorageListItem item = storageItems.get(position);
        holder.title.setText(item.getTitle());

        holder.itemView.setOnClickListener(v -> {
            SubjectFile subjectFile = item.getFile();
            //placeholder, open file here ig
        });

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
