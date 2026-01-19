package com.example.a5minutechallenge.screens.fiveminute;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a5minutechallenge.R;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Simple RecyclerView adapter for displaying text items in a list.
 * Used by content containers to populate RecyclerViews with string data.
 * Supports visual selection state for quiz options.
 */
public class SimpleTextAdapter extends RecyclerView.Adapter<SimpleTextAdapter.ViewHolder> {
    private final List<String> items;
    @Nullable
    private final OnItemClickListener onItemClickListener;
    private final Set<Integer> selectedIndices;
    private boolean allowMultipleSelection;

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
        this(items, onItemClickListener, false);
    }

    public SimpleTextAdapter(List<String> items, @Nullable OnItemClickListener onItemClickListener, boolean allowMultipleSelection) {
        this.items = items;
        this.onItemClickListener = onItemClickListener;
        this.selectedIndices = new HashSet<>();
        this.allowMultipleSelection = allowMultipleSelection;
    }

    /**
     * Sets whether multiple items can be selected at once.
     * @param allowMultiple true to allow multiple selections
     */
    public void setAllowMultipleSelection(boolean allowMultiple) {
        this.allowMultipleSelection = allowMultiple;
    }

    /**
     * Toggles selection state for the item at the given position.
     * For single selection mode, clears other selections first.
     * @param position The position of the item to toggle
     */
    public void toggleSelection(int position) {
        if (allowMultipleSelection) {
            if (selectedIndices.contains(position)) {
                selectedIndices.remove(position);
            } else {
                selectedIndices.add(position);
            }
        } else {
            // Single selection mode: clear previous and select new
            selectedIndices.clear();
            selectedIndices.add(position);
        }
        notifyDataSetChanged();
    }

    /**
     * Sets the selected indices directly.
     * @param indices Set of selected indices
     */
    public void setSelectedIndices(Set<Integer> indices) {
        selectedIndices.clear();
        if (indices != null) {
            selectedIndices.addAll(indices);
        }
        notifyDataSetChanged();
    }

    /**
     * Gets the current selected indices.
     * @return Set of selected indices
     */
    public Set<Integer> getSelectedIndices() {
        return new HashSet<>(selectedIndices);
    }

    /**
     * Clears all selections.
     */
    public void clearSelection() {
        selectedIndices.clear();
        notifyDataSetChanged();
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
        
        // Update visual selection state
        boolean isSelected = selectedIndices.contains(position);
        holder.itemView.setSelected(isSelected);
        holder.itemView.setActivated(isSelected);
        
        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                toggleSelection(adapterPosition);
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(adapterPosition);
                }
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
            textView = itemView.findViewById(R.id.option_text);
        }
    }
}
