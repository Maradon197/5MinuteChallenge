package com.example.a5minutechallenge.screens.fiveminute;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a5minutechallenge.R;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Simple RecyclerView adapter for displaying text items in a list.
 * Used by content containers to populate RecyclerViews with string data.
 */
public class ContentContainerAdapter extends RecyclerView.Adapter<ContentContainerAdapter.ViewHolder> {
    private final List<String> items;
    @Nullable
    private final OnItemClickListener onItemClickListener;
    private final Set<Integer> selectedIndices = new HashSet<>();
    private final Set<Integer> correctIndices = new HashSet<>();
    private final Set<Integer> incorrectIndices = new HashSet<>();
    private boolean answersRevealed = false;
    private boolean shouldAnimateReveal = false;

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

    /**
     * Returns the items list. Subclasses can use this to access or modify the list.
     * @return The items list
     */
    protected List<String> getItems() {
        return items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.option_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(items.get(position));
        
        // Update background color based on state
        Context context = holder.itemView.getContext();
        if (answersRevealed) {
            int targetColor;
            if (correctIndices.contains(position)) {
                targetColor = ContextCompat.getColor(context, R.color.correct_answer);
            } else if (incorrectIndices.contains(position)) {
                targetColor = ContextCompat.getColor(context, R.color.incorrect_answer);
            } else {
                targetColor = ContextCompat.getColor(context, R.color.unselected_answer);
            }
            
            // Apply fade-in animation for answer reveal
            if (shouldAnimateReveal && (correctIndices.contains(position) || incorrectIndices.contains(position))) {
                int currentColor = ContextCompat.getColor(context, 
                        selectedIndices.contains(position) ? R.color.selected_answer : R.color.unselected_answer);
                ColorDrawable[] colorLayers = new ColorDrawable[2];
                colorLayers[0] = new ColorDrawable(currentColor);
                colorLayers[1] = new ColorDrawable(targetColor);
                TransitionDrawable transition = new TransitionDrawable(colorLayers);
                holder.itemView.setBackground(transition);
                transition.startTransition(300);
            } else {
                holder.itemView.setBackgroundColor(targetColor);
            }
        } else if (selectedIndices.contains(position)) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.selected_answer));
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.unselected_answer));
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null && !answersRevealed) {
                onItemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Toggles selection state for the given position
     */
    public void toggleSelection(int position) {
        if (selectedIndices.contains(position)) {
            selectedIndices.remove(position);
        } else {
            selectedIndices.add(position);
        }
        notifyItemChanged(position);
    }

    /**
     * Sets single selection (clears previous selections)
     */
    public void setSingleSelection(int position) {
        selectedIndices.clear();
        selectedIndices.add(position);
        notifyDataSetChanged();
    }

    /**
     * Reveals answers with correct/incorrect highlighting and fade-in animation
     */
    public void revealAnswers(List<Integer> correctAnswerIndices, List<Integer> userSelectedIndices) {
        answersRevealed = true;
        shouldAnimateReveal = true;
        correctIndices.clear();
        incorrectIndices.clear();
        
        // Mark all correct answers as correct
        correctIndices.addAll(correctAnswerIndices);
        
        // Mark user selections that are incorrect
        for (Integer selected : userSelectedIndices) {
            if (!correctAnswerIndices.contains(selected)) {
                incorrectIndices.add(selected);
            }
        }
        
        notifyDataSetChanged();
        
        // Reset the animation flag after a delay to avoid re-animating on scroll
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            shouldAnimateReveal = false;
        }, 400);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        
        ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.option_text);
        }
    }
}
