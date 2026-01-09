/** Simple RecyclerView adapter for displaying text items in a list.
 * Used by content containers to populate RecyclerViews with string data.
 */
package com.example.a5minutechallenge;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SimpleTextAdapter extends RecyclerView.Adapter<SimpleTextAdapter.ViewHolder> {
    private final List<String> items;
    
    public SimpleTextAdapter(List<String> items) {
        this.items = items;
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
    }
    
    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        
        ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.option_text);
        }
    }
}
