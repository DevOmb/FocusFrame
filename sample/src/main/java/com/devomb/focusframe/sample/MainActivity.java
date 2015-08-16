package com.devomb.focusframe.sample;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.devomb.focusframe.FocusFrame;
import com.devomb.focusframe.FocusFrameTouchHandler;


public class MainActivity extends Activity{

    private FocusFrame focusFrame;
    private TextView label;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        focusFrame = (FocusFrame) findViewById(R.id.focusFrame);
        label = (TextView) findViewById(R.id.label);

        focusFrame.setOnTouchInsideListener(new FocusFrameTouchHandler.DefaultOnTouchInsideListener() {
            @Override
            public void onTouchDown() {
                label.setText("DOWN");
            }

            @Override
            public void onTouchUp() {
                label.setText("UP");
            }

            @Override
            public void onDoubleTap() {
                label.setText("DOUBLE TAP");
                //Call to super restore original position
                super.onDoubleTap();
            }
        });
    }
}
