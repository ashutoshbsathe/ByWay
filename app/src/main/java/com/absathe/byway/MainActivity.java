package com.absathe.byway;

import androidx.appcompat.app.AppCompatActivity;


import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.github.florent37.shapeofview.shapes.ArcView;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private int MODE = 0; //LOGIN: 0, SIGNUP 1
    private ArcView arcView;
    private final int transitionTime = 1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        arcView = findViewById(R.id.mainactivity_arcview);
        MaterialButton actionButton = findViewById(R.id.mainactivity_loginbutton);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TransitionDrawable transition = (TransitionDrawable)
                        findViewById(R.id.mainactivity_gradient_background).getBackground();
                if(MODE == 0) {
                    transition.startTransition(transitionTime);
                    MODE = 1;
                }
                else {
                    transition.reverseTransition(transitionTime);
                    MODE = 0;
                }
            }
        });
    }
}
