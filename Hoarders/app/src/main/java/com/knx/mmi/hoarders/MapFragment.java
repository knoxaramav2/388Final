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
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

/**
 * reference https://stackoverflow.com/questions/19353255/how-to-put-google-maps-v2-on-a-fragment-using-viewpager
 *TODO move to own file, TEST FIRTS
 */
public class MapFragment extends Fragment {
    MapView mMapView;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    //Game Elements
    private Bitmap bMonument;
    private Location lastLocation;
    private Location currLocation;
    //report movements at ~ 5 from previous
    private final float locationResolution = 1.8f;
    private long dTime;
    private long lastMillis;

    private LocationUpdateHandler locationUpdateHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        View rootView = inflater.inflate(R.layout.mapfragment, container, false);

        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstance);

        mMapView.onResume();

        try{
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("DEBUG", "Map init failure");
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {

                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            1);
                } else {
                    if (mFusedClient != null){
                        return;
                    }

                    mMap = googleMap;
                    mMap.setMyLocationEnabled(true);
                    mMap.setBuildingsEnabled(true);
                    mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.style_json));
                    mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                        LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
                        Criteria providerCriteria = new Criteria();

                        @Override
                        public boolean onMyLocationButtonClick() {

                            if (!checkLocationPermission()){
                                requestLocationPermission();
                            }

                            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(providerCriteria, false));

                            if (location == null){
                                return false;
                            }

                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 20.0f));
                            return false;
                        }
                    });

                    mFusedClient = LocationServices.getFusedLocationProviderClient(getContext());

                    mFusedClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 18.0f));
                            mMap.addMarker(new MarkerOptions().position(latLng).title("Marker in ASDASDASD").icon(BitmapDescriptorFactory.fromBitmap(bMonument)));
                        }
                    });

                    mLocationRequest = new LocationRequest();
                    mLocationRequest.setInterval(3000);

                    lastMillis = System.currentTimeMillis();
                    dTime = 0;

                    mLocationCallback = new LocationCallback(){

                        @Override
                        public void onLocationResult(LocationResult locationResult){

                            //Update time data
                            long currMillis = System.currentTimeMillis();
                            dTime = currMillis - lastMillis;
                            lastMillis = currMillis;

                            List<Location> locationList = locationResult.getLocations();
                            int dbgLocationUpdates = locationList.size();

                            if (locationList.size() == 0){
                                Toast.makeText(getContext(), "NO MAP UPDATES", Toast.LENGTH_LONG);
                                return;
                            }

                            Location location = locationList.get(0);

                            //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 18.0f));
                            locationUpdateHandler.handleLocationUpdate(location);
                        }
                    };

                    mFusedClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                }
            }
        });

        return rootView;
    }

    public static MapFragment newInstance(LocationUpdateHandler locationUpdateHandler){
        MapFragment mapFragment = new MapFragment();
        mapFragment.locationUpdateHandler = locationUpdateHandler;
        return mapFragment;
    }

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
    }

    @Override
    public void onStart(){
        super.onStart();

        loadBitmaps();
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
        mFusedClient.removeLocationUpdates(mLocationCallback);
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

    boolean checkLocationPermission(){
        return !(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED);
    }

    void requestLocationPermission(){
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                1);
    }

    private void loadBitmaps(){
        bMonument = getScaledBitMap(R.drawable.monument);
    }

    private Bitmap getScaledBitMap(int bitMapId){

        BitmapDrawable bitMapDrawable = (BitmapDrawable) getResources().getDrawable(bitMapId);
        Bitmap bm = bitMapDrawable.getBitmap();
        Bitmap scaledBm = Bitmap.createScaledBitmap(bm, 200, 200, false);

        return scaledBm;
    }

    public float getLastDistance(Location cLoc){

        Location l1 = lastLocation;
        Location l2 = cLoc == null ? currLocation : cLoc;

        if (l1 == null || l2 == null){
            return 0;
        }

        float [] results = new float[3];

        Location.distanceBetween(
                l1.getLatitude(),
                l1.getLongitude(),
                l2.getLatitude(),
                l2.getLongitude(),
                results
        );

        return results[0];
    }

    public float getLocationResolution(){
        return locationResolution;
    }

    public Location[] getLocations(){
        Location[] ret = new Location[2];
        ret[0] = lastLocation;
        ret[1] = currLocation;

        return ret;
    }

    public interface LocationUpdateHandler{
        void handleLocationUpdate(Location location);
    }

    public long getUpdateDeltaTime(){
        return dTime;
    }
}