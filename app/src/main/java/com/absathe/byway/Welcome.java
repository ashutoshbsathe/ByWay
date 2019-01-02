package com.absathe.byway;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;

import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;


public class Welcome extends Activity {

    float dY;
    float originalY;
    int lastAction;
    int EXPAND_ANIMATION_SHOWN = 0;
    int EXPANT_ANIMATION_DURATION = 200;
    int MAX_SCROLL_AMOUNT = 240;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Toolbar toolbar = findViewById(R.id.welcome_fake_toolbar);
        toolbar.inflateMenu(R.menu.welcomeactivity_top_menu);
        toolbar.setOnMenuItemClickListener(topMenuListener);
        View swipe = findViewById(R.id.welcome_swipe_container_parent);
        originalY = swipe.getY();
        final TextView messageOnSwipe = findViewById(R.id.welcome_swipe_text);
        final TextView headerText = findViewById(R.id.welcome_mode_text);
        final View swipeContainer = findViewById(R.id.welcome_swipe_holder);
        swipe.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                String text = (String) messageOnSwipe.getText();
                switch(motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                       dY = view.getY() - motionEvent.getRawY();
                       lastAction = MotionEvent.ACTION_DOWN;
                       break;
                    case MotionEvent.ACTION_MOVE:
                        if(EXPAND_ANIMATION_SHOWN == 0) {
                            float width= getResources().getDimension(R.dimen.slider_width);
                            float height = getResources().getDimension(R.dimen.slider_height_expanded);
                            //swipeContainer.setLayoutParams(new RelativeLayout.LayoutParams((int)width, (int)height));
                            ResizeAnimation resizeAnimation = new ResizeAnimation(
                                    swipeContainer,
                                    (int)height,
                                    swipeContainer.getHeight()
                            );
                            resizeAnimation.setDuration(EXPANT_ANIMATION_DURATION);
                            swipeContainer.startAnimation(resizeAnimation);
                            EXPAND_ANIMATION_SHOWN = 1;
                        }
                        float newY = motionEvent.getRawY() +dY;
                        if(newY > MAX_SCROLL_AMOUNT)
                            view.setTranslationY(MAX_SCROLL_AMOUNT);
                        else if(newY < -MAX_SCROLL_AMOUNT)
                            view.setTranslationY(-MAX_SCROLL_AMOUNT);
                        else
                            view.setTranslationY(newY) ;
                        if(newY > originalY && !text.equals(getString(R.string.welcome_swipe_share))) {
                            messageOnSwipe.setText(getString(R.string.welcome_swipe_share));
                            headerText.setText(getString(R.string.welcome_swipe_share));
                        }
                        else if(newY < originalY && !text.equals(getString(R.string.welcome_swipe_ride))) {
                            messageOnSwipe.setText(getString(R.string.welcome_swipe_ride));
                            headerText.setText(getString(R.string.welcome_swipe_ride));
                        }
                        lastAction= MotionEvent.ACTION_MOVE;
                        break;
                    case MotionEvent.ACTION_UP:
                        String token = null;
                        float orig_width= getResources().getDimension(R.dimen.slider_width);
                        float orig_height = getResources().getDimension(R.dimen.slider_height);
                        float newY_reset = motionEvent.getRawY() +dY;
                        if(newY_reset > MAX_SCROLL_AMOUNT)
                            newY_reset = MAX_SCROLL_AMOUNT;
                        else if(newY_reset < -MAX_SCROLL_AMOUNT)
                            newY_reset = -MAX_SCROLL_AMOUNT;
                        if(newY_reset == MAX_SCROLL_AMOUNT) {
                            Toast.makeText(Welcome.this, "SHARE SELECTED", Toast.LENGTH_LONG).show();
                            token = "SHARE";
                        }
                        else if(newY_reset == -MAX_SCROLL_AMOUNT) {
                            Toast.makeText(Welcome.this, "RIDE SELECTED", Toast.LENGTH_LONG).show();
                            token = "RIDE";
                        }
                        if(token != null) {
                            Intent intent = new Intent(Welcome.this, MapsActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("token", token);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            break;
                        }
                        if(lastAction == MotionEvent.ACTION_DOWN)
                            Toast.makeText(Welcome.this,  "Clicked", Toast.LENGTH_LONG).show();
                        view.setTranslationY(originalY);
                        EXPAND_ANIMATION_SHOWN = 0;
                        messageOnSwipe.setText(getString(R.string.welcome_swipe_neutral));
                        headerText.setText(getString(R.string.welcome_swipe_neutral_header));
                        ResizeAnimation resizeAnimationOrig = new ResizeAnimation(
                                swipeContainer,
                                (int)orig_height,
                                swipeContainer.getHeight()
                        );
                        resizeAnimationOrig.setDuration(EXPANT_ANIMATION_DURATION);
                        swipeContainer.startAnimation(resizeAnimationOrig);
                        break;
                    default:
                        return false;

                }
                return true;
            }
        });
        /*
        SeekBar seekBar = findViewById(R.id.welcome_seekbar);
        seekBar.setBackground(getDrawable(R.drawable.square));
        */
    }
    private Toolbar.OnMenuItemClickListener topMenuListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch(item.getItemId())  {
                case R.id.app_bar_about:
                    startActivity(new Intent(Welcome.this, About.class));
                    break;
                default:
                    break;
            }
            return false;
        }
    };

}
