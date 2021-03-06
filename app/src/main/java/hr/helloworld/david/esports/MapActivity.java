package hr.helloworld.david.esports;


import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;



public class MapActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        LocationListener,
        OnItemSelectedListener {
    private static final String TAG = MapActivity.class.getSimpleName();

    private Button add_button;
    private MapFragment mapFragment;
    private GoogleMap map;
    private Toast msgToast;
    private GoogleApiClient googleApiClient;

    private GeofencingClient geofencingClient;

    private Marker locationMarker;

    private Location lastLocation;
    public List<Event> EVENTS = new ArrayList<Event>();


    DatabaseReference myRef;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private String sportPick;
    private Boolean filterOff = Boolean.TRUE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        geofencingClient = LocationServices.getGeofencingClient(this);

        // TIMESTAMP
        long ts = new Date().getTime();
        Log.d("**** ", String.valueOf(ts));

        //novi db FIREBASE, učitavanje podataka iz db u listu EVENTS
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("events");
        Query ref = myRef.orderByChild("mTime/time").startAt(ts);

        // Read from the database
        ref.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                //čišćenje evenata i geofenceva
                removeGeofences();
                EVENTS.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        EVENTS.add(new Event(snapshot.getValue(Event.class).naslov, snapshot.getValue(Event.class).getId(),
                                new LatLng(snapshot.getValue(Event.class).getLat(), snapshot.getValue(Event.class).getLng()),
                                snapshot.getValue(Event.class).getRadius(),
                                snapshot.getValue(Event.class).getDuration(),
                                snapshot.getValue(Event.class).getNumId(),
                                snapshot.getValue(Event.class).getSize(),
                                snapshot.getValue(Event.class).getGooing(),
                                snapshot.getValue(Event.class).getSport(),
                                snapshot.getValue(Event.class).getOwner(),
                                snapshot.getValue(Event.class).getmTime()));

                        startGeofence();
                    } catch (Exception e) {
                        Log.d("**** ", "Problem!");
                    }
                }
                Log.d("TESTING ", "novi blok");

                reDrawEvents();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        add_button = findViewById(R.id.add_button);
        add_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // dodavanje u listu evenata
                if (geoFenceMarker != null) {
                    Intent addIntent = new Intent(getApplicationContext(), AddEventActivity.class);
                    addIntent.putExtra("info", "intent za dodavanje");
                    addIntent.putExtra("Lat", geoFenceMarker.getPosition().latitude);
                    addIntent.putExtra("Long", geoFenceMarker.getPosition().longitude);
                    startActivityForResult(addIntent, 222);

                } else {
                    Toast.makeText(MapActivity.this,"Odaberite lokaciju " +
                            "clickom na karti.",Toast.LENGTH_LONG).show();
                }
            }
        });

        //filter pomocu dropdown-a
        Spinner dropdown = findViewById(R.id.spinner1);
        dropdown.setOnItemSelectedListener(this);
        String[] items = new String[]{"sve", "nogomet", "košarka", "rukomet", "ostalo"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);
        //adapter.notifyDataSetChanged();


        // initialize GoogleMaps
        initGMaps();
        // create GoogleApiClient
        createGoogleApi();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    //0. Activity communication

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (222): {
                if (resultCode == RESULT_OK) {

                    final String id = data.getStringExtra("id");
                    Log.d("TESTING:", id);
                    //parsiranje stringa u date
                    String timeStr = data.getStringExtra("time");
                    Log.d("****", timeStr);
                    SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd kk:mm");
                    Date time = null;
                    try {
                        time = dt.parse(timeStr);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Log.d("****", String.valueOf(time));
                    String sport = data.getStringExtra("sport");
                    float radius = Float.parseFloat(data.getStringExtra("rad"));
                    long duration = Long.parseLong(data.getStringExtra("dur")) * 1000 * 60; //vrijeme uneseno u minutama pretvoreno u ms
                    int size = Integer.parseInt(data.getStringExtra("size"));
                    //havtanje usernamea iz sesije
                    firebaseAuth = FirebaseAuth.getInstance();
                    firebaseUser = firebaseAuth.getCurrentUser();
                    String owner = firebaseUser.getDisplayName();

                    String newEventId = myRef.push().getKey();
                    Event newEvent = new Event(id, newEventId, geoFenceMarker.getPosition(), radius, duration, size, 0, sport, owner, time);
                    if (newEventId != null)
                        myRef.child(newEventId).setValue(newEvent);

                    Log.d("****/TAGA:", newEvent.toString());

                    EVENTS.add(newEvent);
                    msgToast = Toast.makeText(getApplicationContext(), "Event added", Toast.LENGTH_SHORT);
                    msgToast.show();
                    //startGeofence();
                    reDrawEvents();

                }
                break;
            }
        }
    }

    //1. MAPA

    private void reDrawEvents() {
        //brisanje svih markera i iscrtavanje novih iz liste EVENTS

        map.clear();
        if (lastLocation != null) {
            writeLastLocation();
        }
        for (int ix = 0; ix < EVENTS.size(); ix++) {
            Log.d("TESTING:", EVENTS.get(ix).getSport() + sportPick + filterOff);

            if (filterOff || sportPick.equals("all")) {
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(EVENTS.get(ix).getLat(), EVENTS.get(ix).getLng()))
                        .title(EVENTS.get(ix).naslov + ": " + EVENTS.get(ix).getSport() + ", " + EVENTS.get(ix).getmTime() + ", " + EVENTS.get(ix).getGooing() + "/" + EVENTS.get(ix).getSize()));

                drawCircle(new LatLng(EVENTS.get(ix).getLat(), EVENTS.get(ix).getLng()), EVENTS.get(ix).getRadius(), ix);
            } else {
                if (sportPick.equals("ostalo")) {
                    if (!EVENTS.get(ix).getSport().equals("nogomet") && !EVENTS.get(ix).getSport().equals("rukomet") && !EVENTS.get(ix).getSport().equals("košarka")) {
                        map.addMarker(new MarkerOptions()
                                .position(new LatLng(EVENTS.get(ix).getLat(), EVENTS.get(ix).getLng()))
                                .title(EVENTS.get(ix).naslov + ": " + EVENTS.get(ix).getSport() + ", " + EVENTS.get(ix).getmTime() + ", " + EVENTS.get(ix).getGooing() + "/" + EVENTS.get(ix).getSize()));

                        drawCircle(new LatLng(EVENTS.get(ix).getLat(), EVENTS.get(ix).getLng()), EVENTS.get(ix).getRadius(), ix);
                    }
                } else {
                    if (sportPick.equals(EVENTS.get(ix).getSport())) {
                        map.addMarker(new MarkerOptions()
                                .position(new LatLng(EVENTS.get(ix).getLat(), EVENTS.get(ix).getLng()))
                                .title(EVENTS.get(ix).naslov + ": " + EVENTS.get(ix).getSport() + ", " + EVENTS.get(ix).getmTime() + ", " + EVENTS.get(ix).getGooing() + "/" + EVENTS.get(ix).getSize()));

                        drawCircle(new LatLng(EVENTS.get(ix).getLat(), EVENTS.get(ix).getLng()), EVENTS.get(ix).getRadius(), ix);
                    }
                }

            }
        }
    }

    private void initGMaps() {
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    // Callback called when Map is ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady()");
        map = googleMap;
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);

        reDrawEvents();

    }

    // Callback called when Map is touched
    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG, "onMapClick(" + latLng + ")");
        markerForGeofence(latLng);

        // ~dohvat lokacije eventa lat long

    }

    // Callback called when Marker is touched
    @Override
    public boolean onMarkerClick(final Marker marker) {
        Log.d(TAG, "****onMarkerClickListener: " + marker.getPosition());

        //otvaranje nove aktivnosti preko snackbara,na klik markera
        final String[] parts = marker.getTitle().split(":");
        if (!parts[0].equals("tvoja pozicija") && !parts[0].equals("Novi event")) {
            Snackbar snackbar = Snackbar
                    .make(findViewById(R.id.LL), "Find more about event -->", Snackbar.LENGTH_LONG)
                    .setActionTextColor(getResources().getColor(R.color.colorPrimary))
                    .setAction("Details", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            for (Event event : EVENTS) {
                                if (event.naslov.equals(parts[0])) {
                                    Intent detailsIntent = new Intent(getApplicationContext(), DetailsActivity.class);
                                    detailsIntent.putExtra("id", event.getId());
                                    startActivity(detailsIntent);
                                }
                            }
                        }
                    });

            snackbar.show();
        }
        return false;
    }

    //2.GOOGLE API

    // Create GoogleApiClient instance
    private void createGoogleApi() {
        Log.d(TAG, "createGoogleApi()");
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Call GoogleApiClient connection when starting the Activity
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Disconnect GoogleApiClient when stopping Activity
        googleApiClient.disconnect();
    }

    //3. LOKACIJA

    // GoogleApiClient.ConnectionCallbacks suspended
    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "onConnectionSuspended()");
    }

    // GoogleApiClient.OnConnectionFailedListener fail
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed()");
    }

    // GoogleApiClient.ConnectionCallbacks connected
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected()");
        getLastKnownLocation();
    }

    // Get last known location
    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation()");
        if (checkPermission()) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (lastLocation != null) {
                Log.i(TAG, "LasKnown location. " +
                        "Long: " + lastLocation.getLongitude() +
                        " | Lat: " + lastLocation.getLatitude());
                //writeLastLocation();
                reDrawEvents();
                startLocationUpdates();
            } else {
                Log.w(TAG, "No location retrieved yet");
                startLocationUpdates();
            }
        } else askPermission();
    }

    private LocationRequest locationRequest;
    // Defined in mili seconds.
    // This number in extremely low, and should be used only for debug
    private final int UPDATE_INTERVAL = 5000; // * 60 * 3;
    private final int FASTEST_INTERVAL = 5000; //* 30;

    // Start location Updates
    private void startLocationUpdates() {
        Log.i(TAG, "startLocationUpdates()");
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        if (checkPermission())
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged [" + location + "]");
        lastLocation = location;
        writeActualLocation(location);
    }

    // Write location coordinates on UI
    private void writeActualLocation(Location location) {

        markerLocation(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    private void writeLastLocation() {
        writeActualLocation(lastLocation);
    }

    private final int REQ_PERMISSION = 999;

    // Check for permission to access Location
    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    // Asks for permission
    private void askPermission() {
        Log.d(TAG, "askPermission()");
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQ_PERMISSION
        );
    }

    // Verify user's response of the permission requested
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    getLastKnownLocation();

                } else {
                    // Permission denied
                    permissionsDenied();
                }
                break;
            }
        }
    }

    // App cannot work without the permissions
    private void permissionsDenied() {
        Log.w(TAG, "permissionsDenied()");
    }

    //4. MARKERI


    // Create a Location Marker
    private void markerLocation(LatLng latLng) {
        Log.i(TAG, "markerLocation(" + latLng + ")");
        String title = "tvoja pozicija";
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .title(title);
        if (map != null) {
            // Remove the anterior marker
            if (locationMarker != null)
                locationMarker.remove();
            locationMarker = map.addMarker(markerOptions);
            float zoom = 14f;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
            map.animateCamera(cameraUpdate);
        }
    }

    private Marker geoFenceMarker;

    // Create a marker for the geofence creation
    private void markerForGeofence(LatLng latLng) {
        Log.i(TAG, "markerForGeofence(" + latLng + ")");
        String title = "Novi event";
        // Define marker options
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title(title);
        if (map != null) {
            // Remove last geoFenceMarker
            if (geoFenceMarker != null)
                geoFenceMarker.remove();

            geoFenceMarker = map.addMarker(markerOptions);
            drawGeofence();

        }
    }

    //5. GEOFENCE

    private static final long GEO_DURATION = 60 * 60 * 1000; // 1 h trajanje
    private static final String GEOFENCE_REQ_ID = "My Geofence";
    private static final float GEOFENCE_RADIUS = 500.0f; // in meters

    // Create a Geofence
    private Geofence createGeofence(LatLng latLng, float radius) {
        Log.d(TAG, "createGeofence");
        return new Geofence.Builder()
                .setRequestId(EVENTS.get(EVENTS.size() - 1).naslov)
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration(EVENTS.get(EVENTS.size() - 1).getDuration())
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    // Create a Geofence Request
    private GeofencingRequest createGeofenceRequest(Geofence geofence) {
        Log.d(TAG, "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
    }

    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;

    private PendingIntent createGeofencePendingIntent() {
        Log.d(TAG, "createGeofencePendingIntent");
        if (geoFencePendingIntent != null)
            return geoFencePendingIntent;
        Intent intent = new Intent(this, GeofenceBroadcastReciver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        geoFencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return geoFencePendingIntent;


    }

    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofence(GeofencingRequest request) {
        Log.d("****", "addGeofence" + request.toString());
        Log.d(TAG, Boolean.toString(checkPermission()));
        if (checkPermission())

            geofencingClient.addGeofences(request, createGeofencePendingIntent()).addOnSuccessListener(this, new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // Geofences added
                    // ...
                    Log.d("****", "Geofence added");
                }
            })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to add geofences
                            // ...
                            msgToast.setText("Test: " + "fail");
                            msgToast.show();
                        }
                    });
        ;

    }

    public void onResult(@NonNull Status status) {
        Log.i(TAG, "onResult: " + status);
        if (status.isSuccess()) {
            drawGeofence();
        } else {
            // inform about fail
        }
    }

    // Draw Geofence circle on GoogleMap
    private Circle geoFenceLimits;

    private void drawGeofence() {
        Log.d(TAG, "drawGeofence()");

        if (geoFenceLimits != null)
            geoFenceLimits.remove();

        CircleOptions circleOptions = new CircleOptions()
                .center(geoFenceMarker.getPosition())
                .strokeColor(Color.argb(50, 70, 70, 70))
                .fillColor(Color.argb(100, 250, 150, 150)) //promijena u boji
                .radius(GEOFENCE_RADIUS);
        geoFenceLimits = map.addCircle(circleOptions);
    }

    private void drawCircle(LatLng latLng, float r, final int rmid) {
        Log.d(TAG, "drawCircle()");

        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                .strokeColor(Color.argb(50, 70, 70, 70))
                .fillColor(Color.argb(100, 150, 150, 150))
                .radius(r);
        map.addCircle(circleOptions);


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.geofence: {
                //startGeofence();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    // Start Geofence creation process
    private void startGeofence() {
        Log.d(TAG, "startGeofence()" + EVENTS.get(EVENTS.size() - 1).getId());

        LatLng loc = new LatLng(EVENTS.get(EVENTS.size() - 1).getLat(), EVENTS.get(EVENTS.size() - 1).getLng());
        Geofence geofence = createGeofence(loc, EVENTS.get(EVENTS.size() - 1).getRadius());
        GeofencingRequest geofenceRequest = createGeofenceRequest(geofence);
        addGeofence(geofenceRequest);
        Log.d("TESTING:geofenceID", geofence.getRequestId());

    }

    //remove all geofences
    private void removeGeofences() {

        geofencingClient.removeGeofences(createGeofencePendingIntent()).addOnSuccessListener(this, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Geofence removed
                // ...

                Log.d("****", "Geofence removed");
            }
        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to add geofences
                        // ...
                        msgToast.setText("Test: " + "failed removing");
                        msgToast.show();
                    }
                });
    }

    //FILTER
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        filterOff = Boolean.FALSE;
        Log.d("**** ", "filter ON");
        switch (position) {
            case 0:
                sportPick = "all";
                break;
            case 1:
                Log.d("**** ", "case 1");
                sportPick = "nogomet";
                break;
            case 2:
                sportPick = "košarka";
                break;
            case 3:
                sportPick = "rukomet";
                break;
            case 4:
                sportPick = "ostalo";
                break;

        }
        reDrawEvents();
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        sportPick = "all";
    }
}


