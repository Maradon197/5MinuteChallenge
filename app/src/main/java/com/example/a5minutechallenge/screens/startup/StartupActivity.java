/** The first launched activity (not MainActivity!) that displays a simple startup screen.
 *  From here, the MainActivity is launched (primarily for debugging reasons).
 *  AndroidManifest.xml contains the refrence you have to change to MainActivity to remove this.
 */
package com.example.a5minutechallenge.screens.startup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a5minutechallenge.R;
import com.example.a5minutechallenge.screens.main.MainActivity;

public class StartupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startup_screen);

        View rootView = findViewById(android.R.id.content);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartupActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}