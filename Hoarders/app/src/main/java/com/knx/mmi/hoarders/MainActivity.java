package com.knx.mmi.hoarders;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements HttpFireBaseAsync.ResultHandler {

    private GameDB gameDB;
    private HttpFireBaseAsync httpFireBaseAsync;
    private FirebaseFunctions mFunctions;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private final int ACTIVITY_LOGIN = 1;
    private final int ACTIVITY_PLAY = 2;
    private final int ACTIVITY_SETTINGS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        httpFireBaseAsync = new HttpFireBaseAsync(this);
        mFunctions = FirebaseFunctions.getInstance();
        mAuth = FirebaseAuth.getInstance();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        gameDB = Room.databaseBuilder(this, GameDB.class, "gameDB")
            .fallbackToDestructiveMigration()
            .build();
    }

    @Override
    protected void onStart(){
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null){
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivityForResult(loginIntent, ACTIVITY_LOGIN);
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        mAuth.signOut();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            case ACTIVITY_LOGIN:
                if (resultCode == RESULT_CANCELED){
                    return;
                }

                mUser = mAuth.getCurrentUser();

                if (mUser == null){
                    Toast.makeText(this, "Login/Register failed", Toast.LENGTH_LONG);
                    Log.i("DEBUG", "Login/Register failed");
                    return;
                }

                Toast.makeText(this, "Login/Register " + mUser.getDisplayName(), Toast.LENGTH_LONG);
                Log.i("DEBUG", "Login/Register " + mUser.getDisplayName());

                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onStartButtonClick(View view){
        //Intent gameViewIntent = new Intent(this, MapGameActivity.class);
        Intent gameViewIntent = new Intent(this, GameActivity.class);
        startActivity(gameViewIntent);


    }

    public void onTheButtonClick(View view){
        HttpFireBaseAsync httpFireBaseAsync = new HttpFireBaseAsync(this);
        httpFireBaseAsync.execute("https://us-central1-final-2532b.cloudfunctions.net/testFunction2");
    }

    public void handleHttpResult(String result){

        String jres = "ERR";

        try{
            JSONObject job = new JSONObject(result);
            jres = job.getString("val");
        } catch (JSONException e){
            e.printStackTrace();
        }

        Toast.makeText(this, ">> " + jres, Toast.LENGTH_LONG).show();
    }

    public void onLogOutPressed(View view){
        mAuth.signOut();

        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivityForResult(loginIntent, ACTIVITY_LOGIN);
    }

}
