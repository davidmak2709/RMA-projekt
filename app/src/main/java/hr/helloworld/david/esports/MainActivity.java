package hr.helloworld.david.esports;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 1;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference dbReference = database.getReference("events");
    protected SwipeRefreshLayout swipeContainer;
    private SearchView searchView;
    private ArrayList<Event> events=new ArrayList<>();
    private RVAdapter adapter=new RVAdapter(events);
    private Location userLocation;
    private Location eventLocation=new Location("");
    private FirebaseUser firebaseUser;
    private int userRange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startLocationUpdates();
        setTitle(getString(R.string.main_activity_title));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference dbUserRange = database.getReference("users").child(firebaseUser.getUid()).child("range");
        dbUserRange.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userRange=dataSnapshot.getValue(Integer.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        RecyclerView rv=findViewById(R.id.listEvents);
        LinearLayoutManager llm=new LinearLayoutManager(this);
        llm.setReverseLayout(true);
        llm.setStackFromEnd(true);
        rv.setLayoutManager(llm);
        rv.setAdapter(adapter);

        dbReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Date currentTime=Calendar.getInstance().getTime();
                for(DataSnapshot snapshot :dataSnapshot.getChildren()){
                    if (userLocation!=null && userRange>0){
                        eventLocation.setLatitude(snapshot.getValue(Event.class).getLat());
                        eventLocation.setLongitude(snapshot.getValue(Event.class).getLng());
                        float distanceInMeters=userLocation.distanceTo(eventLocation);
                        if ((distanceInMeters/1000)>userRange){
                            continue;
                        }
                    }
                    if (snapshot.getValue(Event.class).addMinutesToDate().before(currentTime)){
                        continue;
                    }
                    events.add(snapshot.getValue(Event.class));
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("dbCancelled", databaseError.toString());

            }
        });

        swipeContainer=findViewById(R.id.swipeContainer);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                events.clear();
                dbReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Date currentTime=Calendar.getInstance().getTime();
                        for(DataSnapshot snapshot :dataSnapshot.getChildren()){
                            if (userLocation!=null && userRange>0){
                                eventLocation.setLatitude(snapshot.getValue(Event.class).getLat());
                                eventLocation.setLongitude(snapshot.getValue(Event.class).getLng());
                                float distanceInMeters=userLocation.distanceTo(eventLocation);
                                if ((distanceInMeters/1000)>userRange){
                                    continue;
                                }
                            }
                            if (snapshot.getValue(Event.class).addMinutesToDate().before(currentTime)){
                                continue;
                            }
                            events.add(snapshot.getValue(Event.class));
                        }
                        adapter.notifyDataSetChanged();
                        swipeContainer.setRefreshing(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("dbCancelled", databaseError.toString());

                    }
                });
            }
        });

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


    }

    @Override
    protected void onResume() {
        super.onResume();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        ImageView profilePictureHeader = headerView.findViewById(R.id.userImageMainActivity);
        TextView usernameHeader = headerView.findViewById(R.id.usernameMainActivity);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            Picasso.with(MainActivity.this)
                    .load(firebaseUser.getPhotoUrl())
                    .resize(200, 250)
                    .centerCrop()
                    .into(profilePictureHeader);
            usernameHeader.setText(firebaseUser.getDisplayName());
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }else if (!searchView.isIconified()){
            searchView.setIconified(true);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                adapter.getFilter().filter(query);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            return true;
        }



        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_map) {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_edit_profile) {
            Intent intent = new Intent(MainActivity.this, EditProfileActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, StartActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_friends_list) {
            Intent intent = new Intent(MainActivity.this, FriendListActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_past_events) {
            Intent intent=new Intent(MainActivity.this, PastEventsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            Intent intent=new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void startLocationUpdates() {
        LocationRequest mLocationRequest = new LocationRequest();
        long UPDATE_INTERVAL = 10 * 1000;
        long FASTEST_INTERVAL = 2000;


        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();


        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_ACCESS_FINE_LOCATION);
            return;
        }


        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }

    public void onLocationChanged(Location location) {
        adapter.setUserLocation(location);
        userLocation=location;
    }
}
