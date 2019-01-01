package com.absathe.byway;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.util.Function;


import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.TransitionDrawable;
import android.icu.util.TimeUnit;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.TextView;
import android.widget.Toast;

import com.github.florent37.shapeofview.shapes.ArcView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.lmntrx.android.library.livin.missme.ProgressDialog;

public class MainActivity extends AppCompatActivity {

    private int MODE = 0; //LOGIN: 0, SIGNUP 1
    private ArcView arcView;
    private final int transitionTime = 1000;
    private MaterialButton actionButton;
    private TextView header;
    private TextView question;
    private MaterialButton toggle;
    private TextInputLayout password_layout;
    private TextInputLayout email_layout;
    // IMPORTANT
    private FirebaseAuth mAuth;
    private SharedPreferences userPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // IMPORTANT
        mAuth = FirebaseAuth.getInstance();
        userPreferences = getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
        if(userPreferences.getBoolean("is_user_logged_in", false)) {
            startActivity(new Intent(this, Welcome.class));
        }

        arcView = findViewById(R.id.mainactivity_arcview);
        actionButton = findViewById(R.id.mainactivity_loginbutton);
        actionButton.setOnClickListener(loginToAccount);
        header = findViewById(R.id.mainactivity_header);
        question = findViewById(R.id.mainactivity_question);
        toggle = findViewById(R.id.mainactivity_signup);
        toggle.setOnClickListener(loginOrSignUp);
        password_layout = findViewById(R.id.mainactivity_password_layout);
        email_layout = findViewById(R.id.mainactivity_email_layout);


    }
    View.OnClickListener loginOrSignUp = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            TransitionDrawable transition = (TransitionDrawable)
                    findViewById(R.id.mainactivity_gradient_background).getBackground();
            if(actionButton == null) {
                return;
            }

            if(MODE == 0) {
                transition.startTransition(transitionTime);
                actionButton.setText(R.string.mainactivity_signup);
                header.setText(R.string.signup);
                toggle.setText(R.string.mainactivity_login);
                question.setText(R.string.mainactivity_question_user);
                password_layout.setHint(getString(R.string.mainactivity_set_password));
                MODE = 1;
            }
            else {
                transition.reverseTransition(transitionTime);
                actionButton.setText(R.string.mainactivity_login);
                header.setText(R.string.login);
                toggle.setText(R.string.mainactivity_signup);
                question.setText(R.string.newuserquestion);
                password_layout.setHint(getString(R.string.mainactivity_hint_password));
                MODE = 0;
            }
        }
    };
    View.OnClickListener loginToAccount = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AlertDialog progressDialog = new AlertDialog.Builder(MainActivity.this).create();
            progressDialog.setTitle("Loading...");
            progressDialog.getWindow().setBackgroundDrawableResource(R.drawable.rectangle_with_round_corners);
            String email = null;
            String password = null;
            progressDialog.show();
            email_layout.setError(null);
            password_layout.setError(null);
            try {
                email = email_layout.getEditText().getText().toString();
                password = password_layout.getEditText().getText().toString();
            }
            catch(NullPointerException e) {
                Toast.makeText(MainActivity.this, "Please fill both fields", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
                return;
            }
            if(!isValidEmail(email)) {
                email_layout.setError("Please enter a valid email");
                progressDialog.dismiss();
                return;
            }
            if(password.length() < 8) {
                password_layout.setError("Password must be at least 8 characters long");
                progressDialog.dismiss();
                return;
            }
            if(MODE == 1) {
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressDialog.dismiss();
                                if(!task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "An error occurred", Toast.LENGTH_LONG).show();
                                    Log.d("MainActivity-LOGIN", "Error occurred when logging in: " + task.getException().getStackTrace().toString());
                                }
                                else {
                                    FirebaseUser user = task.getResult().getUser();
                                    if(user != null) {
                                        SharedPreferences.Editor editor = userPreferences.edit();
                                        editor.putString("user_id", user.getUid());
                                        editor.putString("provider_id", user.getProviderId());
                                        editor.putBoolean("is_user_logged_in", true);
                                        editor.apply();
                                    }
                                    Intent welcome = new Intent(MainActivity.this, Welcome.class);
                                    startActivity(welcome);
                                    return;
                                }
                            }
                        });
            }
            else {
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressDialog.dismiss();
                                if(!task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "An error occurred", Toast.LENGTH_LONG).show();
                                    Log.d("MainActivity-LOGIN", "Error occurred when logging in: " + task.getException().getStackTrace().toString());
                                }
                                else {
                                    FirebaseUser user = task.getResult().getUser();
                                    if(user != null) {
                                        SharedPreferences.Editor editor = userPreferences.edit();
                                        editor.putString("user_id", user.getUid());
                                        editor.putString("provider_id", user.getProviderId());
                                        editor.putBoolean("is_user_logged_in", true);
                                        editor.apply();
                                    }
                                    Intent welcome = new Intent(MainActivity.this, Welcome.class);
                                    startActivity(welcome);
                                    return;
                                }
                            }
                        });
            }
            progressDialog.dismiss();
        }
    };

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

}
