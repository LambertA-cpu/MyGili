package com.example.mygili;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mlocationRequest;

    private Button mLogout, mRequest;
    private LatLng meetupLocation;
    private Boolean requestBol = false;
    private Marker meetupMarker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        mLogout = (Button) findViewById(R.id.logout);
        mRequest = (Button) findViewById(R.id.request);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(CustomerMapActivity.this,MapActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (requestBol){
                    requestBol = false;
                    geoQuery.removeAllListeners();
                    cleanerLocationRef.removeEventListener(cleanerLocationRefListener);

                    if (cleanerFound != null){
                        DatabaseReference cleanerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Cleaners").child("cleanerFoundID");
                        cleanerRef.setValue(true);
                        cleanerFoundId= null;
                    }
                    cleanerFound = false;
                    radius = 1;
                    String UserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                    GeoFire geoFire =  new GeoFire(ref);
                    geoFire.removeLocation(UserId);

                    if(meetupMarker != null){
                        meetupMarker.remove();
                    }
                    mRequest.setText("call Cleaner");
                }else{
                    requestBol = true;
                    String UserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                    GeoFire geoFire =  new GeoFire(ref);
                    geoFire.setLocation(UserId, new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()));

                    meetupLocation = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
                    meetupMarker = mMap.addMarker(new MarkerOptions().position(meetupLocation).title("Meetup Here"));

                    mRequest.setText("Getting your cleaner...");

                    getClosestCleaner();
                }

            }
        });
    }
    private int radius = 1;
    private Boolean cleanerFound = false;
    private String cleanerFoundId;

    GeoQuery geoQuery;
    private void  getClosestCleaner(){
        DatabaseReference cleanerLocation =  FirebaseDatabase.getInstance().getReference().child("cleanersAvailable");
        GeoFire geoFire = new GeoFire(cleanerLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(meetupLocation.latitude, meetupLocation.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!cleanerFound && requestBol){
                    cleanerFound = true;
                    cleanerFoundId= key;

                    DatabaseReference cleanerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Cleaners").child("cleanerFoundID");
                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map = new HashMap();
                    map.put("customerCleanId", customerId);
                    cleanerRef.updateChildren(map);

                    getCleanerLocation();
                    mRequest.setText("Searching for cleaner location...");


                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!cleanerFound){
                    radius++;
                    getClosestCleaner();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private Marker mCleanerMarker;
    private DatabaseReference cleanerLocationRef;
    private ValueEventListener cleanerLocationRefListener;
    private void getCleanerLocation(){

        cleanerLocationRef = FirebaseDatabase.getInstance().getReference().child("cleanersWorking").child(cleanerFoundId).child("l");
        cleanerLocationRefListener = cleanerLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && requestBol) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;

                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng cleanerLatlng = new LatLng(locationLat, locationLng);
                    if (mCleanerMarker != null) {
                        mCleanerMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(meetupLocation.latitude);
                    loc1.setLongitude(meetupLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(cleanerLatlng.latitude);
                    loc2.setLongitude(cleanerLatlng.longitude);
                    float distance = loc1.distanceTo(loc2);

                    if(distance<120){
                        mRequest.setText("cleaner has arrived");
                    } else {
                        mRequest.setText("A Cleaner Found" + String.valueOf(distance));
                    }
                    mCleanerMarker = mMap.addMarker(new MarkerOptions().position(cleanerLatlng).title("Your Cleaner"));
                }
            }
            @Override
            public void onCancelled(@Nullable DatabaseError databaseError) {


            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED&&ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            return;
        }
        biuldGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }
    protected synchronized void biuldGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }
    @Override
    public  void onLocationChanged(Location location) {
        mLastLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));


    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mlocationRequest = new LocationRequest();
        mlocationRequest.setInterval(1000);
        mlocationRequest.setFastestInterval(1000);
        mlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED&&ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mlocationRequest,this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStop(){
        super.onStop();
    }
    /*@Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }*/

}

