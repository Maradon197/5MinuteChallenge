package com.example.a5minutechallenge.screens.fiveminute;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a5minutechallenge.R;

import java.util.List;

/**
 * Simple RecyclerView adapter for displaying text items in a list.
 * Used by content containers to populate RecyclerViews with string data.
 */
public class ContentContainerAdapter extends RecyclerView.Adapter<ContentContainerAdapter.ViewHolder> {
    private final List<String> items;
    @Nullable
    private final OnItemClickListener onItemClickListener;

    /**
     * Listener interface for item click events in the RecyclerView.
     */
    public interface OnItemClickListener {
        /**
         * Called when an item in the RecyclerView is clicked.
         * @param position The adapter position of the clicked item
         */
        void onItemClick(int position);
    }
    public ContentContainerAdapter(List<String> items, @Nullable OnItemClickListener onItemClickListener) {
        this.items = items;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.text_container, parent, false);//this is wrong
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(items.get(position));
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        
        ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text_content);//thgis is wrong
        }
    }
}
