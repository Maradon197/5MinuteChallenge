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

public class BoxAdapter extends ArrayAdapter<Box> {

    public BoxAdapter(@NonNull Context context, List<Box> boxes) {
        super(context, 0, boxes);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.box_list_item, parent, false);
        }

        Box box = getItem(position);

        TextView idTextView = convertView.findViewById(R.id.box_id);
        idTextView.setText(String.valueOf(box.getId()));

        return convertView;
    }
}
