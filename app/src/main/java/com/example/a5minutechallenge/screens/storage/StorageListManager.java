/** RecyclerView that displays storage items. */
package com.example.a5minutechallenge.screens.storage;

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

import com.example.a5minutechallenge.R;
import com.example.a5minutechallenge.datawrapper.subject.SubjectFile;
import com.example.a5minutechallenge.datawrapper.subject.StorageListItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StorageListManager extends RecyclerView.Adapter<StorageListManager.ViewHolder> {

    private final Context context;
    private final ArrayList<StorageListItem> allItems;
    private List<StorageListItem> filteredItems;
    private final OnItemLongClickListener longClickListener;

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    public StorageListManager(Context context, ArrayList<StorageListItem> storageItems, OnItemLongClickListener longClickListener) {
        this.context = context;
        this.allItems = storageItems;
        this.filteredItems = new ArrayList<>(storageItems);
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
        StorageListItem item = filteredItems.get(position);
        holder.title.setText(item.getTitle());

        holder.itemView.setOnClickListener(v -> {
            SubjectFile subjectFile = item.getFile();
            openFile(subjectFile);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                int actualPosition = allItems.indexOf(item);
                longClickListener.onItemLongClick(actualPosition);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return filteredItems.size();
    }

    /**
     * Filters the storage list based on the search query.
     * @param query The search query string
     */
    public void filter(String query) {
        filteredItems.clear();
        if (query == null || query.isEmpty()) {
            filteredItems.addAll(allItems);
        } else {
            String lowerQuery = query.toLowerCase();
            for (StorageListItem item : allItems) {
                String title = item.getTitle();
                if (title != null && title.toLowerCase().contains(lowerQuery)) {
                    filteredItems.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Resets filter to show all items.
     */
    public void resetFilter() {
        filteredItems.clear();
        filteredItems.addAll(allItems);
        notifyDataSetChanged();
    }

    /**
     * Notifies the adapter that the underlying data has changed.
     * This ensures filteredItems is synchronized with allItems when not filtering.
     */
    public void notifyItemsChanged() {
        // If no filter is active (search bar is empty), sync filteredItems with allItems
        filteredItems.clear();
        filteredItems.addAll(allItems);
        notifyDataSetChanged();
    }

    /**
     * Opens a file using the appropriate application
     * @param subjectFile The SubjectFile to open
     */
    private void openFile(SubjectFile subjectFile) {
        if (subjectFile == null || !subjectFile.exists()) {
            Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show();
            return;
        }

        File file = subjectFile.getFile();
        Uri fileUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                file
        );

        // Determine MIME type based on file extension
        String mimeType = getMimeType(subjectFile.getFileName());

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "No application found to open this file", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Determines MIME type based on file extension
     * @param fileName The file name
     * @return The MIME type as a string
     */
    private String getMimeType(String fileName) {
        if (fileName == null) {
            return "*/*";
        }

        String extension = "";
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            extension = fileName.substring(lastDotIndex + 1).toLowerCase();
        }

        // Common MIME types
        switch (extension) {
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt":
                return "application/vnd.ms-powerpoint";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "txt":
                return "text/plain";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "mp4":
                return "video/mp4";
            case "mp3":
                return "audio/mpeg";
            case "zip":
                return "application/zip";
            default:
                return "*/*";
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.storage_item_title);
        }
    }
}
