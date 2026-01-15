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
public class SimpleTextAdapter extends RecyclerView.Adapter<SimpleTextAdapter.ViewHolder> {
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

    public SimpleTextAdapter(List<String> items) {
        this(items, null);
    }

    public SimpleTextAdapter(List<String> items, @Nullable OnItemClickListener onItemClickListener) {
        this.items = items;
        this.onItemClickListener = onItemClickListener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.quiz_option_item, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(items.get(position));
        
        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(v -> {
                int adapterPosition = holder.getBindingAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClickListener.onItemClick(adapterPosition);
                }
            });
        }
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        
        ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.option_text);
        }
    }
}
