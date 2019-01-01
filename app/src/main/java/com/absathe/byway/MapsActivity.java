package com.absathe.byway;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arsy.maps_library.MapRipple;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private TextView origin;
    private TextView destination;
    private Marker source_marker = null;
    private Marker destination_marker = null;

    private final long MAP_RIPPLE_DURATION = 10000;
    private final long MAP_INTER_RIPPLE_DURATION = 3333;
    private final long MAP_RIPPLE_DISTANCE = 500;
    private final int RIDE = 0;
    private final int SHARE = 1;
    private int MODE  = RIDE;
    private final int REQUEST_TYPE_SOURCE = 2;
    private final int REQUEST_TYPE_DESTNN = 3;

    LocationRequest mLocationRequest;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Bundle bundle = getIntent().getExtras();
        if(bundle.getString("token", "ABSENT").equals("RIDE")) {
            MODE = RIDE;
        }
        else if(bundle.getString("token", "ABSENT").equals("SHARE")) {
            MODE = SHARE;
        }
        origin = findViewById(R.id.mapsactivity_origin_textview);
        destination = findViewById(R.id.mapsactivity_destination_textview);
        origin.setOnClickListener(originClicked);
        destination.setOnClickListener(destinationClicked);
        initFAB();
    }
    private void initFAB() {
        SpeedDialView speedDialView = findViewById(R.id.mapsactivity_speedDial);
        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.mapsactivity_fab_my_location, R.drawable.ic_my_location)
                        .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, getTheme()))
                        .setFabImageTintColor(Color.parseColor("#FFFFFF"))
                        .setLabel(R.string.mapsactivity_fab_my_location)
                        .create());
        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.mapsactivity_fab_view_coriders, R.drawable.ic_map_person)
                        .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, getTheme()))
                        .setFabImageTintColor(Color.parseColor("#FFFFFF"))
                        .setLabel(R.string.mapsactivity_fab_view_coriders)
                        .create());
        /*
         * TODO:
         * Customize the string label of following item according to MODE
         */
        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.mapsactivity_fab_ride_from_current_location, R.drawable.ic_adjust)
                        .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, getTheme()))
                        .setFabImageTintColor(Color.parseColor("#FFFFFF"))
                        .setLabel(R.string.mapsactivity_fab_ride_from_current_location)
                        .create());
        speedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                switch (actionItem.getId()) {
                    case R.id.mapsactivity_fab_view_coriders:
                        Coriders fragment;
                        if(MODE == SHARE) {
                            fragment = Coriders.newInstance(1);
                        }
                        else {
                            fragment = Coriders.newInstance(0);
                        }
                        fragment.show(getSupportFragmentManager(), "custom_bottom_fragment_coriders");
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }
    TextView.OnClickListener originClicked = new TextView.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                Intent selectOrigin = new
                        PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                        .build(MapsActivity.this);
                Toast toast = Toast.makeText(MapsActivity.this, "Enter your starting location",
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
                startActivityForResult(selectOrigin, REQUEST_TYPE_SOURCE);
            }
            catch(GooglePlayServicesRepairableException e) {
                Toast.makeText(MapsActivity.this, "Please repair your Google Play Services", Toast.LENGTH_LONG).show();
            }
            catch(GooglePlayServicesNotAvailableException e) {
                Toast.makeText(MapsActivity.this, "Please insatll Google Play Services to continue", Toast.LENGTH_LONG).show();
            }
        }
    };
    TextView.OnClickListener destinationClicked = new TextView.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                Intent selectOrigin = new
                        PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                        .build(MapsActivity.this);
                Toast toast = Toast.makeText(MapsActivity.this, "Enter your destination",
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
                startActivityForResult(selectOrigin, REQUEST_TYPE_DESTNN);
            }
            catch(GooglePlayServicesRepairableException e) {
                Toast.makeText(MapsActivity.this, "Please repair your Google Play Services", Toast.LENGTH_LONG).show();
            }
            catch(GooglePlayServicesNotAvailableException e) {
                Toast.makeText(MapsActivity.this, "Please insatll Google Play Services to continue", Toast.LENGTH_LONG).show();
            }
        }
    };
    @Override
    protected void  onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_TYPE_SOURCE) {
            if(resultCode == Activity.RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(MapsActivity.this, data);
                origin.setText(place.getName());
                if(source_marker != null)  {
                    source_marker.remove();
                }
                if(mMap != null) {
                    source_marker = mMap.addMarker(new MarkerOptions()
                            .position(place.getLatLng())
                            .title("Ride starts here"));
                }
            }
            else if(resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(MapsActivity.this, data);
                Log.d("MapsActivity", "\n**************\nPlaceAPIError\n**************\n" + status);
            }
            else if(resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(MapsActivity.this, "Ride origin unchanged", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == REQUEST_TYPE_DESTNN) {
            if(resultCode == Activity.RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(MapsActivity.this, data);
                destination.setText(place.getName());
                if(destination_marker != null) {
                    destination_marker.remove();
                }
                if(mMap != null) {
                    destination_marker = mMap.addMarker(new MarkerOptions()
                    .position(place.getLatLng())
                    .title("Ride ends here"));
                }
            }
            else if(resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(MapsActivity.this, data);
                Log.d("MapsActivity", "\n**************\nPlaceAPIError\n**************\n" + status);
            }
            else if(resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(MapsActivity.this, "Destination location unchanged", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        if(mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }
    /*
     * Here's the original page from which a lot of code below has been taken
     * https://stackoverflow.com/questions/44992014/how-to-get-current-location-in-googlemap-using-fusedlocationproviderclient
     */
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
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        LatLng shaniwarwada = new LatLng(18.5194589,73.8531296);
        mMap.addMarker(new MarkerOptions()
                            .position(shaniwarwada)
                            .icon(vectorToBitmap(R.drawable.ic_navigation, Color.parseColor("#3F51B5")))
                            .title("You are here")
        );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(shaniwarwada, 16));
        MapRipple mapRipple = new MapRipple(mMap, shaniwarwada, MapsActivity.this);
        mapRipple.withDistance(MAP_RIPPLE_DISTANCE);
        mapRipple.withRippleDuration(MAP_RIPPLE_DURATION);
        mapRipple.withDurationBetweenTwoRipples(MAP_INTER_RIPPLE_DURATION);
        mapRipple.withFillColor(getColor(R.color.colorAccent));
        mapRipple.withNumberOfRipples(3);
        mapRipple.withTransparency(0.85f);
        mapRipple.startRippleMapAnimation();
        /**
         * Don't make continuous location request for the sake of demo
         */
        /*
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        Log.d("MapsActivity:", "\n----------------------------\nI'm in onMapReady Callback\n----------------------------");
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback,
                        Looper.myLooper());
            }
            else {
                checkLocationPermission();
            }
        }
        */
        // Add a marker in Sydney and move the camera
        /*
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        */
    }
    private BitmapDescriptor vectorToBitmap(@DrawableRes int id, @ColorInt int color) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(), id, null);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        DrawableCompat.setTint(vectorDrawable, color);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location location = locationResult.getLastLocation();
            Log.i("MapsActivity", "Location:" + location.getLatitude() + " " +
                    location.getLongitude());
            mLastLocation = location;
            if(mCurrLocationMarker != null) {
                mCurrLocationMarker.remove();
            }
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("You are here");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker());
            mCurrLocationMarker = mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }
    };
    public static final int MY_PERMISSION_REQUEST_LOCATION = 99;
    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("MapsActivity:", "LOCATION PERMISSION NOT GRANTED");
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Required")
                        .setMessage("ByWay cannot function without location permissions. Please accept to use location functionality.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSION_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            }
            else {
                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSION_REQUEST_LOCATION);
            }
        }
        else {
            Log.d("MapsActivity:", "LOCATION PERMISSION GRANTED");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permission,int[] grantResults) {
        switch(requestCode) {
            case MY_PERMISSION_REQUEST_LOCATION: {
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else {
                    Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_LONG).show();
                }
            }
            default:
        }
    }
}
