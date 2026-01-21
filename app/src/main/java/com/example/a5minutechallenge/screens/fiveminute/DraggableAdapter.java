package com.example.a5minutechallenge.screens.fiveminute;

import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

/**
 * Extended adapter with drag-and-drop support for sortable items.
 * Used by sorting_task and wire_connecting containers.
 */
public class DraggableAdapter extends ContentContainerAdapter {
    
    private final ItemTouchHelper itemTouchHelper;
    private final OnItemMovedListener onItemMovedListener;
    
    /**
     * Listener interface for item movement events.
     */
    public interface OnItemMovedListener {
        /**
         * Called when an item is moved to a new position.
         * @param fromPosition Original position
         * @param toPosition New position
         */
        void onItemMoved(int fromPosition, int toPosition);
    }
    
    public DraggableAdapter(List<String> items, @Nullable OnItemClickListener onItemClickListener, 
                           @Nullable OnItemMovedListener onItemMovedListener) {
        super(items, onItemClickListener);
        this.onItemMovedListener = onItemMovedListener;
        
        // Create ItemTouchHelper for drag-and-drop
        this.itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, 
                                @NonNull RecyclerView.ViewHolder viewHolder, 
                                @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                
                // Swap items in the list
                Collections.swap(items, fromPosition, toPosition);
                
                // Notify adapter
                notifyItemMoved(fromPosition, toPosition);
                
                // Notify listener
                if (onItemMovedListener != null) {
                    onItemMovedListener.onItemMoved(fromPosition, toPosition);
                }
                
                return true;
            }
            
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // No swipe to dismiss
            }
            
            @Override
            public boolean isLongPressDragEnabled() {
                // Enable long press to start drag
                return true;
            }
        });
    }
    
    /**
     * Attaches the ItemTouchHelper to the RecyclerView.
     * Must be called after the adapter is set on the RecyclerView.
     */
    public void attachToRecyclerView(RecyclerView recyclerView) {
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        
        // Add visual feedback for draggable items
        holder.itemView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // Slight scale down on touch to indicate draggability
                v.setScaleX(0.98f);
                v.setScaleY(0.98f);
            } else if (event.getAction() == MotionEvent.ACTION_UP || 
                       event.getAction() == MotionEvent.ACTION_CANCEL) {
                // Return to normal size
                v.setScaleX(1.0f);
                v.setScaleY(1.0f);
            }
            return false;
        });
    }
}
