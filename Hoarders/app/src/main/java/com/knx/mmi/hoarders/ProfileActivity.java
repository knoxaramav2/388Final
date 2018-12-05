package com.knx.mmi.hoarders;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private TextView username_field;
    private TextView email_field;
    private TextView gold_field;
    private TextView stone_field;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        username_field = findViewById(R.id.username_field);
        email_field = findViewById(R.id.email_field);
        gold_field = findViewById(R.id.gold_field);
        stone_field = findViewById(R.id.stone_field);

        GameDB db = GameDB.getInstance(getApplicationContext());
        UserEntity user = db.daoAccess().getUserByEmail(mUser.getEmail());

        username_field.setText(user.getUserName());
        email_field.setText("Email:  "+user.getUserMail());
        gold_field.setText("Gold:    "+user.getGold());
        stone_field.setText("Stone:  "+user.getStone());
    }

    public void onBackPressed(View view){
        Intent result = new Intent();
        setResult(Activity.RESULT_OK, result);
        finish();
    }

}
