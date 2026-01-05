/** Activity for the 5-minute-screen opened by the user. Calls ContentContainerAdapter
 *  for content creation and display.
 */
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

        ListView contentListView = findViewById(R.id.box_list);

        String topicName = getIntent().getStringExtra("TOPIC_NAME");
        if (topicName == null) {
            topicName = "Default Topic";
        }

        List<ContentContainer> contentContainers = new ArrayList<>();
        contentContainers.add(new TitleContainer(0).setTitle(topicName));
        contentContainers.add(new TextContainer(1).setText("This is the first paragraph of the 5-minute challenge."));
        contentContainers.add(new TextContainer(2).setText("This is the second paragraph. You should read this quickly!"));
        contentContainers.add(new TextContainer(3).setText("This is the final paragraph. Time is almost up! It is very long though. In fact, I pasted a whole wikipedia article: \n Filler text (also placeholder text or dummy text) is text that shares some characteristics of a real written text, but is random or otherwise generated. \nIt may be used to display a sample of fonts, generate text for testing, or to spoof an e-mail spam filter. The process of using filler text is sometimes called greeking, although the text itself may be nonsense, or largely Latin, as in Lorem ipsum.\n" +
                "\n" +
                "Asdf\n" +
                "ASDF is the sequence of letters that appear on the first four keys on the home row of a QWERTY or QWERTZ keyboard. They are often used as a sample or test case or as random, meaningless nonsense. It is also a common learning tool for keyboard classes, since all four keys are located on the home row."));


        ContentContainerAdapter adapter = new ContentContainerAdapter(this, contentContainers);
        contentListView.setAdapter(adapter);
    }
}
