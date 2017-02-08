package org.odk.collect.android.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.odk.collect.android.R;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button changeTextButton = (Button)findViewById(R.id.changeTextButton);
        final TextView textView = (TextView)findViewById(R.id.helloWorldTextView);

        changeTextButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                textView.setText("Text changed!");
            }
        });
    }
}
