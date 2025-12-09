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

    public ContentContainerAdapter(@NonNull Context context, List<ContentContainer> contentContainers) {
        super(context, 0, contentContainers);
    }

    @Override
    public int getViewTypeCount() {
        return ContentContainer.Types.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType().ordinal();
    }

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
