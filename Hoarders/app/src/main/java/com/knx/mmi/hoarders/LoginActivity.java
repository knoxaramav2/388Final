package com.knx.mmi.hoarders;

import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import java.util.Observable;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private UserEntity user;

    TextView userNameView;
    TextView passwordView;

    GameDB db;

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

        db = GameDB.getInstance(getApplicationContext());
    }

    public void onBackPress(View view){
        Intent result = new Intent();
        setResult(Activity.RESULT_CANCELED, result);
        finish();
    }

    public void onSigninPress(View view){

        final String userName = userNameView.getText().toString();
        String password = passwordView.getText().toString();

        mAuth.signInWithEmailAndPassword(userName, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        mUser = mAuth.getCurrentUser();

                        user = db.daoAccess().getUserByEmail(userNameView.getText().toString());

                        if (user != null){
                            exitLoginActivity("ACTION", "LOGIN", Activity.RESULT_OK);
                            return;
                        }

                        user = buildDefaultEntity(
                                userNameView.getText().toString(),
                                userNameView.getText().toString());

                        db.daoAccess().insertNewProfile(user);

                        if (user != null){
                            exitLoginActivity("ACTION", "LOGIN", Activity.RESULT_OK);
                            return;
                        }

                        Toast.makeText(getApplicationContext(), "Signin fail on DB", Toast.LENGTH_LONG).show();

                    } else {
                        mUser = null;
                        Toast.makeText(getApplicationContext(), "Login failed", Toast.LENGTH_LONG);
                        Log.i("DEBUG", "LOGIN FAILED");
                    }
                }
            });

        if (mUser == null){
            return;
        }

        //exitLoginActivity("ACTION", "LOGIN", Activity.RESULT_OK);
    }

    public void onRegisterPress(View view){

        String userName = userNameView.getText().toString();
        String password = passwordView.getText().toString();

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
                            UserEntity user = buildDefaultEntity(
                                    userNameView.getText().toString(),
                                    userNameView.getText().toString()
                            );
                            Toast.makeText(getApplicationContext(), "Registered " + user.getUserName(), Toast.LENGTH_LONG);
                        } else {
                            Log.i("DEBUG", "FAILURE");
                        }
                    }
                });

        if (mUser == null){
            Toast.makeText(this, "Registration failed", Toast.LENGTH_LONG).show();
        }

        Toast.makeText(this, "Registered" + mUser.getEmail(), Toast.LENGTH_LONG).show();

        exitLoginActivity("ACTION", "REGISTER", Activity.RESULT_OK);
    }

    private void exitLoginActivity(String key, String value, int returnCode){
        Intent result = new Intent();
        result.putExtra(key, value);
        setResult(returnCode);
        finish();
    }

    UserEntity buildDefaultEntity(String userName, String email){
        UserEntity user = new UserEntity();

        user.setUserName(userName);
        user.setUserMail(email);
        user.setStone(5);
        user.setGold(5);

        return user;
    }
}

