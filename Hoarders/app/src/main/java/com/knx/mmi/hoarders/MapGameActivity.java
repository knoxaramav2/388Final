package com.knx.mmi.hoarders;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class MapGameActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private Location mLastLocation;
    private FusedLocationProviderClient mFusedLocationClient;

    private Bitmap bMonument;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_game);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));

        loadBitmapIcons();
        initMap();
    }

    //https://stackoverflow.com/questions/37986082/android-googlemaps-mylocation-permission
    public void initMap(){

        if (mFusedLocationClient != null){
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        } else {
            //Update camera settings
            mMap.setMyLocationEnabled(true);
            mMap.setBuildingsEnabled(true);

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 18.0f));
                            mMap.addMarker(new MarkerOptions().position(latLng).title("Marker in ASDASDASD").icon(BitmapDescriptorFactory.fromBitmap(bMonument)));
                        }
                    });
        }


    }

    //generate marker icons
    private void loadBitmapIcons(){
        bMonument = getScaledBitMap(R.drawable.monument);
    }

    private Bitmap getScaledBitMap(int bitMapId){

        BitmapDrawable bitMapDrawable = (BitmapDrawable) getResources().getDrawable(bitMapId);
        Bitmap bm = bitMapDrawable.getBitmap();
        Bitmap scaledBm = Bitmap.createScaledBitmap(bm, 200, 200, false);

        return scaledBm;
    }
}
