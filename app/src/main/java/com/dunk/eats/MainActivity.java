package com.dunk.eats;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.btnSignin) Button btnSignIn;
    @BindView(R.id.btnSignup) Button btnSignUp;
    @BindView(R.id.textSlogan) TextView textSlogan;


    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        btnSignIn.setOnClickListener(this);
        btnSignUp.setOnClickListener(this);

        Typeface typeface = Typeface.createFromAsset(getAssets(),"fonts/Nabila.ttf");
        textSlogan.setTypeface(typeface);

    }

    @Override
    public void onClick(View view) {

        if (view == btnSignIn) {
            Intent intent = new Intent(MainActivity.this, SignIn.class);
                startActivity(intent);
        }

        if (view == btnSignUp) {
            Intent intent = new Intent(MainActivity.this, SignUp.class);
            startActivity(intent);
        }

    }


}
