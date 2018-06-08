package hr.helloworld.david.esports;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class FriendsFeedActivity extends AppCompatActivity {

    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 1;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference dbReference = database.getReference("events");
    protected SwipeRefreshLayout swipeContainer;
    private SearchView searchView;
    private ArrayList<Event> events=new ArrayList<>();
    private Event event;
    private RVAdapter adapter=new RVAdapter(events);
    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private User currentUser=new User(firebaseUser.getUid(), firebaseUser.getDisplayName(), firebaseUser.getEmail(), firebaseUser.getPhotoUrl());
    private ArrayList<String> goingUuid=new ArrayList<>();
    private ArrayList<String> goingUsername=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_feed);
        startLocationUpdates();
        Toolbar toolbar = findViewById(R.id.toolbarFriendsFeed);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        currentUser.getUserFriendsUUID();
        adapter.setUserFriendList(currentUser.friendsUUID);

        RecyclerView rv=findViewById(R.id.listFriendsEvents);
        LinearLayoutManager llm=new LinearLayoutManager(this);
        llm.setReverseLayout(true);
        llm.setStackFromEnd(true);
        rv.setLayoutManager(llm);
        rv.setAdapter(adapter);

        dbReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Date currentTime= Calendar.getInstance().getTime();
                for(DataSnapshot snapshot :dataSnapshot.getChildren()){
                    if (snapshot.getValue(Event.class).addMinutesToDate().before(currentTime)){
                        continue;
                    }
                    for(DataSnapshot userGoing :snapshot.child("goingUsers").getChildren()){
                        if (currentUser.friendsUUID.contains(userGoing.getKey())){
                            for (DataSnapshot goingUser :snapshot.child("goingUsers").getChildren()){
                                goingUuid.add(goingUser.getKey());
                                goingUsername.add(goingUser.child("username").getValue(String.class));
                            }
                            event=snapshot.getValue(Event.class);
                            event.setGoingUuid(goingUuid);
                            event.setGoingUsername(goingUsername);
                            events.add(event);
                            goingUuid=new ArrayList<>();
                            goingUsername=new ArrayList<>();
                            break;
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("dbCancelled", databaseError.toString());

            }
        });

        swipeContainer=findViewById(R.id.swipeContainerFriends);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                events.clear();
                currentUser.getUserFriendsUUID();
                adapter.setUserFriendList(currentUser.friendsUUID);
                dbReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Date currentTime= Calendar.getInstance().getTime();
                        for(DataSnapshot snapshot :dataSnapshot.getChildren()){
                            if (snapshot.getValue(Event.class).addMinutesToDate().before(currentTime)){
                                continue;
                            }
                            for(DataSnapshot userGoing :snapshot.child("goingUsers").getChildren()){
                                if (currentUser.friendsUUID.contains(userGoing.getKey())){
                                    for (DataSnapshot goingUser :snapshot.child("goingUsers").getChildren()){
                                        goingUuid.add(goingUser.getKey());
                                        goingUsername.add(goingUser.child("username").getValue(String.class));
                                    }
                                    event=snapshot.getValue(Event.class);
                                    event.setGoingUuid(goingUuid);
                                    event.setGoingUsername(goingUsername);
                                    events.add(event);
                                    goingUuid=new ArrayList<>();
                                    goingUsername=new ArrayList<>();
                                    break;
                                }
                            }
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

        currentUser.getUserFriendsUUID();
        adapter.setUserFriendList(currentUser.friendsUUID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.friends_feed_menu, menu);


        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search_friends_feed).getActionView();
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

        return id == R.id.search_friends_feed || super.onOptionsItemSelected(item);

    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        }
        super.onBackPressed();
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
    }

}
