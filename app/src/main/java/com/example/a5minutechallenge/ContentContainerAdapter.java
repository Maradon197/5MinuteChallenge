/** ArrayAdapter to display containers in the 5-minute-challenge screen **/
package com.example.a5minutechallenge;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class ContentContainerAdapter extends ArrayAdapter<ContentContainer> {

    /**
     * Constructs a ContentContainerAdapter with context and content container data.
     * @param context The application context
     * @param contentContainers The list of content containers to display
     */
    public ContentContainerAdapter(@NonNull Context context, List<ContentContainer> contentContainers) {
        super(context, 0, contentContainers);
    }

    /**
     * Returns the number of different view types supported by this adapter.
     * @return The number of view types (equal to number of ContentContainer.Types)
     */
    @Override
    public int getViewTypeCount() {
        return ContentContainer.Types.values().length;
    }

    /**
     * Returns the view type for the item at the specified position.
     * @param position The position of the item
     * @return The ordinal of the ContentContainer.Types enum for this position
     */
    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType().ordinal();
    }

    /**
     * Creates and returns the appropriate view for the content container at the specified position.
     * Inflates the correct layout based on container type and binds data to the view.
     * @param position The position of the item in the list
     * @param convertView The recycled view to reuse, if available
     * @param parent The parent ViewGroup
     * @return The View for the content container
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ContentContainer contentContainer = getItem(position);

        if (convertView == null) {
            switch (contentContainer.getType()) {
                case TITLE:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.title_container, parent, false);
                    break;
                case TEXT:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.text_container, parent, false);
                    break;
                case VIDEO:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.video_container, parent, false);
                    break;
                case QUIZ:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.quiz_container, parent, false);
                    break;
            }
        }

        switch (contentContainer.getType()) {
            case TITLE:
                TextView titleView = convertView.findViewById(R.id.title_text);
                TitleContainer titleContainer = (TitleContainer) contentContainer;
                titleView.setText(titleContainer.getTitle());
                break;
            case TEXT:
                TextView textView = convertView.findViewById(R.id.text_content);
                TextContainer textContainer = (TextContainer) contentContainer;
                textView.setText(textContainer.getText());
                break;
            case VIDEO:
            case QUIZ:
                // tbd
                break;
        }

        return convertView;
    }
}
