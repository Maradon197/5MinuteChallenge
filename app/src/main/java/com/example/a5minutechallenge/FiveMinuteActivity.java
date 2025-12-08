package com.example.a5minutechallenge;

import android.os.Bundle;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class FiveMinuteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_five_minute);

        ListView boxList = findViewById(R.id.box_list);

        List<Box> boxes = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            boxes.add(new Box(i));
        }

        BoxAdapter adapter = new BoxAdapter(this, boxes);
        boxList.setAdapter(adapter);
    }
}
