package com.knx.mmi.hoarders;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;
import java.util.List;

/**
 * reference https://stackoverflow.com/questions/19353255/how-to-put-google-maps-v2-on-a-fragment-using-viewpager
 *TODO move to own file, TEST FIRTS
 */

public class GameMapFragment extends Fragment {
    MapView mMapView;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private MapGameCallback mapGameCallback;

    //Game Elements
    private Location currLocation;
    //report movements at ~ 5 from previous
    private final float locationResolution = 1.8f;
    private long dTime;
    private long lastMillis;
    private long UPDATE_RATE = 10000;

    public static final String RSC_STONE = "STONE";
    public static final String RSC_GOLD = "GOLD";
    public static final String BLD_MONUMENT = "MONUMENT";

    private LocationUpdateHandler locationUpdateHandler;
    private LocationManager locationManager;
    private Criteria providerCriteria;

    private LruCache<String, Bitmap> mBitmapCache;
    private HashMap<Integer, Marker> mMarkerList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        super.onCreateView(inflater, container, savedInstance);

        View rootView = inflater.inflate(R.layout.mapfragment, container, false);

        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstance);

        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        mMarkerList = new HashMap<>();
        mBitmapCache = new LruCache<String, Bitmap>(2*1024*1024){
            @Override
            protected int sizeOf(String key, Bitmap value){
                return value.getByteCount();
            }
        };

        try{
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("DEBUG", "Map init failure");
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {

                providerCriteria = new Criteria();

                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            1);
                } else {
                    mMap = googleMap;
                    mMap.setMyLocationEnabled(true);
                    mMap.setBuildingsEnabled(true);
                    mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.style_json));
                    mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {


                        @Override
                        public boolean onMyLocationButtonClick() {

                            checkLocationPermission();
                            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(providerCriteria, false));

                            if (location == null){
                                return false;
                            }

                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 20.0f));
                            return false;
                        }
                    });

                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {

                            for (Integer i : mMarkerList.keySet()){
                                if (mMarkerList.get(i).equals(marker)){
                                    mapGameCallback.onMarkerClick(i);
                                }
                            }

                            return false;
                        }
                    });

                    mapGameCallback.onMapReady();
                }
            }
        });

        mMapView.onResume();

        return rootView;
    }

    public static GameMapFragment newInstance(LocationUpdateHandler locationUpdateHandler, MapGameCallback mapGameCallback){
        GameMapFragment mapFragment = new GameMapFragment();
        mapFragment.locationUpdateHandler = locationUpdateHandler;
        mapFragment.mapGameCallback = mapGameCallback;

        mapFragment.mapGameCallback = mapGameCallback;

        return mapFragment;
    }

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
    }

    @Override
    public void onStart(){
        super.onStart();

        loadBitmaps(RSC_STONE);
        loadBitmaps(RSC_GOLD);
        loadBitmaps(BLD_MONUMENT);
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    public void onResume(){
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory(){
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public Location getCurrLocation(){
        checkLocationPermission();
        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(providerCriteria, false));
        currLocation = location;
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15.0f));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
        return location;
    }

    boolean checkLocationPermission(){

        try{
            if ((ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)){
                requestLocationPermission();
            }
        } catch (Exception e){
            e.printStackTrace();
        }


        return true;
    }

    void requestLocationPermission(){
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                1);
    }

    private boolean loadBitmaps(String icon){
        switch (icon){
            case RSC_GOLD:
                mBitmapCache.put(RSC_GOLD, getScaledBitMap(R.drawable.gold));
                break;
            case RSC_STONE:
                mBitmapCache.put(RSC_STONE, getScaledBitMap(R.drawable.stone));
                break;
            case BLD_MONUMENT:
                mBitmapCache.put(BLD_MONUMENT, getScaledBitMap(R.drawable.monument));
                break;
            default: return false;
        }

        return true;
    }

    private Bitmap getScaledBitMap(int bitMapId){

        BitmapDrawable bitMapDrawable = (BitmapDrawable) getResources().getDrawable(bitMapId);
        Bitmap bm = bitMapDrawable.getBitmap();
        Bitmap scaledBm = Bitmap.createScaledBitmap(bm, 200, 200, false);

        return scaledBm;
    }

    public float getLocationResolution(){
        return locationResolution;
    }

    public interface LocationUpdateHandler{
        void handleLocationUpdate(Location location);
    }

    public long getUpdateDeltaTime(){
        return dTime;
    }

    public void removeResourceMarker(Integer markerId){
        Marker m = mMarkerList.get(markerId);
        if (m == null){
            Log.i("DEBUG", "Failed to remove marker");
            return;
        }

        m.remove();
    }

    public void addResourceMarker(LatLng latLng, String resource, Integer markerId, String title){

        Bitmap bitmap = mBitmapCache.get(resource);

        if (bitmap == null) {
            if (loadBitmaps(resource) == false){
                Log.i("DEBUG", "Failed to load icon");
                return;
            }

            bitmap = mBitmapCache.get(resource);
        }

        Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(bitmap)));

        if (resource == BLD_MONUMENT){
            marker.setTitle(title);
        }

        mMarkerList.put(markerId, marker);
    }

    public LatLng getDestinationPoint(LatLng source, double brng, double dist) {
        dist = dist / 6371;
        brng = Math.toRadians(brng);

        double lat1 = Math.toRadians(source.latitude), lon1 = Math.toRadians(source.longitude);
        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dist) +
                Math.cos(lat1) * Math.sin(dist) * Math.cos(brng));
        double lon2 = lon1 + Math.atan2(Math.sin(brng) * Math.sin(dist) *
                        Math.cos(lat1),
                Math.cos(dist) - Math.sin(lat1) *
                        Math.sin(lat2));
        if (Double.isNaN(lat2) || Double.isNaN(lon2)) {
            return null;
        }
        return new LatLng(Math.toDegrees(lat2), Math.toDegrees(lon2));
    }

    public GoogleMap getMap(){
        return mMap;
    }
}