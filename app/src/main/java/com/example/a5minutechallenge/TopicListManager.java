package com.example.a5minutechallenge;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class TopicListManager extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.topic_list);

        String subjectTitle = getIntent().getStringExtra("SUBJECT_TITLE");
        int subjectId = getIntent().getIntExtra("SUBJECT_ID", 0);

        TextView titleTextView = findViewById(R.id.subject_screen_title);
        titleTextView.setText(subjectTitle);

        ListView topicListView = findViewById(R.id.topic_list_view);

        ArrayList<Topic> topicList = TopicDataSource.getTopicsForSubject(subjectId);

        TopicListAdapter adapter = new TopicListAdapter(this, topicList);
        topicListView.setAdapter(adapter);

        topicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Topic selectedTopic = topicList.get(position);
                Intent intent = new Intent(TopicListManager.this, FiveMinuteActivity.class);
                intent.putExtra("SUBJECT_ID", subjectId);
                intent.putExtra("TOPIC_NAME", selectedTopic.getTitle());
                startActivity(intent);
            }
        });
    }
}
