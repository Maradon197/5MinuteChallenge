package com.example.a5minutechallenge.screens.fiveminute;

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
                
                // Swap items in the list using the getter method
                Collections.swap(getItems(), fromPosition, toPosition);
                
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
        
        // Visual indicator that items are draggable (optional)
        // The long press will trigger the drag operation
    }
}
