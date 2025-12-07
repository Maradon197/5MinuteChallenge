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

public class TopicListAdapter extends ArrayAdapter<Topic> {

    public TopicListAdapter(@NonNull Context context, List<Topic> topics) {
        super(context, 0, topics);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.topic_list_element, parent, false);
        }

        Topic topic = getItem(position);

        TextView titleTextView = convertView.findViewById(R.id.topic_title);
        titleTextView.setText(topic.getTitle());

        return convertView;
    }
}