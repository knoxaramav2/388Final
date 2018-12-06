package com.knx.mmi.hoarders;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class GameActivity extends AppCompatActivity
        implements  HttpFireBaseAsync.ResultHandler,
        MapGameCallback, ShakeSensor.IShakeSensor
{
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private FusedLocationProviderClient mFusedLocationClient;

    private Handler httpMapHandler;

    //Firebase details
    private FirebaseAuth mFireBaseauth;
    private FirebaseUser mFireBaseUser;
    private FirebaseDatabase mFireBaseDB;
    private FirebaseFunctions mFireBaseFunctions;

    private GameMapFragment mGameMapFragment;
    private GameDB gameDB;

    private int UPDATE_RATE = 10000;
    private long RESOURCE_LIFETIME = 1000 * 60 * 10;
    private int MAX_WORLD_ENTITIES = 15;

    private String monumentTitle;

    private Handler mGameLoop;
    private MapGameCallback mapGameCallback;
    private ShakeSensor shakeSensor;

    private Random rand;
    private int currentSelected;

    //Intent returns
    private int REQ_SPEECH_INTENT_RESULT = 5;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mapGameCallback = this;
        mGameLoop = new Handler();
        shakeSensor = new ShakeSensor(getApplicationContext(), this);

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.tabs);
        FloatingActionButton buildMonumentButton = findViewById(R.id.buildMntBtn);
        buildMonumentButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                buildMonument();
            }
        });

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout){
            @Override
            public void onPageSelected(int position){
                if (position != 1){
                    return;
                }

                mSectionsPagerAdapter.notifyDataSetChanged();
            }
        });
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager){

        });

        //Setup Firebase connections
        mFireBaseauth = FirebaseAuth.getInstance();
        mFireBaseDB = FirebaseDatabase.getInstance();
        mFireBaseFunctions = FirebaseFunctions.getInstance();
        mFireBaseUser = mFireBaseauth.getCurrentUser();

        httpMapHandler = new Handler();
        rand = new Random();
        rand.setSeed(System.currentTimeMillis());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game, menu);
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

    @Override
    public void handleHttpResult(String result) {

    }

    @Override
    public void onStart(){
        super.onStart();

        currentSelected = 0;

        gameDB = GameDB.getInstance(getApplicationContext());
    }

    //Remove world entities from DB and remove sprite
    private void removeExpiredResources(){
        long currTime = System.currentTimeMillis();
        List<WorldEntity> worldEntityList = gameDB.daoAccess().getWorldEntitiesByExpired(currTime - RESOURCE_LIFETIME);

        for(WorldEntity w : worldEntityList){
            Integer i = w.getResourceId();
            mGameMapFragment.removeResourceMarker(i);
            gameDB.daoAccess().deleteWorldEntityById(w);
        }
    }

    //Add sprite to map, register with DB
    private void generateResources(){
        List<WorldEntity> worldEntities = gameDB.daoAccess().getAllWorldEntities();

        int maxSpawn = MAX_WORLD_ENTITIES - worldEntities.size();

        if (maxSpawn < 2){
            return;
        }

        int toSpawn = rand.nextInt(maxSpawn);

        Location userLoc = mGameMapFragment.getCurrLocation();

        if (userLoc == null){
            Log.i("DEBUG","Failed to get location");
            return;
        }

        LatLng userLatLng = new LatLng(
                userLoc.getLatitude(),
                userLoc.getLongitude()
        );

        if (userLoc == null){
            Log.i("DEBUG","Location Fail on Resource");
            return;
        }

        for (int i=0; i<toSpawn; ++i){
            int rsc = rand.nextInt(2);
            String rscString;
            if (rsc == 0){
                rscString = GameMapFragment.RSC_GOLD;
            } else {
                rscString = GameMapFragment.RSC_STONE;
            }

            double bearing = rand.nextDouble() * 360f;
            LatLng placeLatLng = mGameMapFragment.getDestinationPoint(userLatLng, bearing, rand.nextDouble()*5);

            //register resource to DB
            WorldEntity resource = new WorldEntity();
            resource.setResourceId(rand.nextInt());
            resource.setSpawnTime(System.currentTimeMillis());
            resource.setResourceType(rscString);
            resource.setLatitude(placeLatLng.latitude);
            resource.setLongitude(placeLatLng.longitude);

            gameDB.daoAccess().inserWorldEntity(resource);

            mGameMapFragment.addResourceMarker(placeLatLng, rscString, resource.getResourceId(), null);
        }
    }

    @Override
    public void onMapReady() {
        List<WorldEntity> worldEntities = gameDB.daoAccess().getAllWorldEntities();
        gameDB.daoAccess().clearWorldEntities(worldEntities);
        mGameLoop.post(gameLoop);
    }

    @Override
    public void onMarkerClick(int id){
        Toast.makeText(this, "Shake device to mine", Toast.LENGTH_LONG).show();

        if (currentSelected != 0){
            shakeSensor.stop();
        }

        currentSelected = id;

        WorldEntity wd = gameDB.daoAccess().getWorldEntityById(id);
        if (wd == null){
            Toast.makeText(this, "Could not determine resource", Toast.LENGTH_SHORT).show();
            return;
        }
        //Toast.makeText(this, "Selected "+wd.getResourceType(), Toast.LENGTH_SHORT).show();
        shakeSensor.start();
    }

    public void buildMonument(){

        UserEntity user = gameDB.daoAccess().getUserByEmail(mFireBaseUser.getEmail());

        if (user.getStone() < 10){
            Toast.makeText(this, "Need " + (10-user.getStone()) + " more stone", Toast.LENGTH_SHORT).show();
            return;
        }

        user.setStone(user.getStone()-10);
        gameDB.daoAccess().updateUser(user);

        Intent recIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        recIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak thy name of your monument, mortal.");

        try{
            startActivityForResult(recIntent, REQ_SPEECH_INTENT_RESULT);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Speech application not supported", Toast.LENGTH_SHORT).show();
        }

    }

    private void spawnMonument(String title){
        Location loc = mGameMapFragment.getCurrLocation();
        LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());

        mGameMapFragment.addResourceMarker(latLng, mGameMapFragment.BLD_MONUMENT, rand.nextInt(), title+" "+mFireBaseUser.getEmail());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_SPEECH_INTENT_RESULT){
            if (resultCode == RESULT_OK && data != null){
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                spawnMonument(result.get(0));
            }
        }
    }

    private Runnable gameLoop = new Runnable() {
        @Override
        public void run() {

            if (mGameMapFragment == null || mGameMapFragment.getMap() == null){
                mGameLoop.postDelayed(this, 500);
                return;
            }

            removeExpiredResources();
            generateResources();

            mGameLoop.postDelayed(this, UPDATE_RATE);
        }
    };

    @Override
    public void onSpeech(String str){

    }

    @Override
    public void shakeSensorUpdate(int shakes) {
        //Log.i("DEBUG", "SHAKES: "+shakes);

        if (shakes < ShakeSensor.reqShakes){
            return;
        }

        shakeSensor.stop();

        WorldEntity wd = gameDB.daoAccess().getWorldEntityById(currentSelected);
        if (wd == null){
            Toast.makeText(this, "Resource cannot be mined", Toast.LENGTH_LONG).show();
            return;
        }

        String email = mFireBaseUser.getEmail();
        UserEntity user = gameDB.daoAccess().getUserByEmail(email);

        switch (wd.getResourceType()){
            case GameMapFragment.RSC_GOLD:
                user.setGold(user.getGold()+3);
                Toast.makeText(this, "+3 Gold", Toast.LENGTH_SHORT).show();
                break;
            case GameMapFragment.RSC_STONE:
                user.setStone(user.getGold()+3);
                Toast.makeText(this, "+3 Stone", Toast.LENGTH_SHORT).show();
                break;
            case GameMapFragment.BLD_MONUMENT:
                Toast.makeText(this, "Monument destroyed", Toast.LENGTH_SHORT).show();
                break;
        }

        gameDB.daoAccess().updateUser(user);

        mGameMapFragment.removeResourceMarker(currentSelected);
        gameDB.daoAccess().deleteWorldEntityById(wd);
        currentSelected=0;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class ResourceFragment extends Fragment implements FragmentUpdateable{
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public ResourceFragment() {
        }

        TextView rscStone;
        TextView rscGold;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static ResourceFragment newInstance(int sectionNumber) {
            ResourceFragment fragment = new ResourceFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.content_resources, container, false);

            rscStone = rootView.findViewById(R.id.stoneField);
            rscGold = rootView.findViewById(R.id.goldField);

            update();

            return rootView;
        }

        @Override
        public void onResume(){
            super.onResume();
        }

        @Override
        public void update(){
            GameDB gameDB = GameDB.getInstance(getContext());
            FirebaseAuth auth = FirebaseAuth.getInstance();
            UserEntity user = gameDB.daoAccess().getUserByEmail(auth.getCurrentUser().getEmail());

            rscStone.setText("Stone:  " + user.getStone().toString());
            rscGold.setText("Gold:  " + user.getGold().toString());
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter{

        public SectionsPagerAdapter(FragmentManager fm){
            super(fm);
        }

        //https://stackoverflow.com/questions/18088076/update-fragment-from-viewpager
        @Override
        public int getItemPosition(Object object){

            if (!(object instanceof ResourceFragment)){
                return super.getItemPosition(object);
            }

            ResourceFragment rscFragment = (ResourceFragment) object;
            if (rscFragment != null){
                rscFragment.update();
            }

            return super.getItemPosition(object);
        }

        @Override
        public Fragment getItem(int i) {
            switch(i){
                case 0:
                    if (mGameMapFragment == null){
                        mGameMapFragment = GameMapFragment.newInstance(new GameMapFragment.LocationUpdateHandler() {
                            @Override
                            public void handleLocationUpdate(Location location) {
                                String lat = String.format("%.3f", location.getLatitude());
                                String lng = String.format("%.3f", location.getLongitude());
                                long dTime = mGameMapFragment.getUpdateDeltaTime();
                                }
                        }, mapGameCallback);

                        onMapReady();
                    }

                    return mGameMapFragment;
                default: return ResourceFragment.newInstance(i + 1);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    interface FragmentUpdateable{
        void update();
    }
}
