package com.knx.mmi.hoarders;

import android.location.Location;
import android.os.Handler;
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

import java.util.List;
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
    private TextView mLatLngDisplay;

    private GameDB gameDB;

    private int UPDATE_RATE = 10000;
    private long RESOURCE_LIFETIME = 1000 * 60 * 10;
    private int MAX_WORLD_ENTITIES = 15;

    private Handler mGameLoop;
    private MapGameCallback mapGameCallback;
    private ShakeSensor shakeSensor;

    private int currentSelected;

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

        mLatLngDisplay = findViewById(R.id.latLngDisplay);

        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        //Setup Firebase connections
        mFireBaseauth = FirebaseAuth.getInstance();
        mFireBaseDB = FirebaseDatabase.getInstance();
        mFireBaseFunctions = FirebaseFunctions.getInstance();

        httpMapHandler = new Handler();
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

        Random r = new Random();
        int toSpawn = r.nextInt(maxSpawn);

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
            int rsc = r.nextInt(2);
            String rscString;
            if (rsc == 0){
                rscString = GameMapFragment.RSC_GOLD;
            } else {
                rscString = GameMapFragment.RSC_STONE;
            }

            double bearing = r.nextDouble() * 360f;
            LatLng placeLatLng = mGameMapFragment.getDestinationPoint(userLatLng, bearing, r.nextDouble()*5);

            //register resource to DB
            WorldEntity resource = new WorldEntity();
            resource.setResourceId(r.nextInt());
            resource.setSpawnTime(System.currentTimeMillis());
            resource.setResourceType(rscString);
            resource.setLatitude(placeLatLng.latitude);
            resource.setLongitude(placeLatLng.longitude);

            gameDB.daoAccess().inserWorldEntity(resource);

            mGameMapFragment.addResourceMarker(placeLatLng, rscString, resource.getResourceId());
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

        shakeSensor.start();

        currentSelected = id;
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
    public void shakeSensorUpdate(float acc) {
        Log.i("DEBUG", ""+acc);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_game, container, false);
            return rootView;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter{

        public SectionsPagerAdapter(FragmentManager fm){
            super(fm);
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

                                mLatLngDisplay.setText("LATLNG  "+lat+" : "+lng + " delta "+" : "+dTime);
                            }
                        }, mapGameCallback);

                        onMapReady();
                    }
                    return mGameMapFragment;
                default: return PlaceholderFragment.newInstance(i + 1);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
