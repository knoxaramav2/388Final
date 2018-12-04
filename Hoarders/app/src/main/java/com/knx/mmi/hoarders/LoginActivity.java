package com.knx.mmi.hoarders;

import android.app.Activity;
import android.arch.persistence.room.util.StringUtil;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    TextView userNameView;
    TextView passwordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userNameView = findViewById(R.id.usernameInput);
        passwordView = findViewById(R.id.passwordInput);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
    }

    public void onBackPress(View view){
        Intent result = new Intent();
        setResult(Activity.RESULT_CANCELED, result);
        finish();
    }

    public void onSigninPress(View view){
        Intent result = new Intent();
        result.putExtra("ACTION", "LOGIN");
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    public void onRegisterPress(View view){

        String userName = userNameView.getText().toString();
        String password = passwordView.getText().toString();

        userName = "knoxarama@gmail.com";
        password = "alphabob42";

        if (userName == "" || password == ""){
            return;
        }

        mAuth.createUserWithEmailAndPassword(userName, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.i("DEBUG", "SUCCESS");
                            mUser = mAuth.getCurrentUser();
                        } else {
                            Log.i("DEBUG", "FAILURE");
                        }
                    }
                });

        if (mUser == null){
            Toast.makeText(this, "Registration failed", Toast.LENGTH_LONG).show();
        }

        Toast.makeText(this, "Registered" + mUser.getEmail(), Toast.LENGTH_LONG).show();

        //Intent result = new Intent();
        //result.putExtra("ACTION", "REGISTER");
        //setResult(Activity.RESULT_OK, result);
        //finish();
    }

}
