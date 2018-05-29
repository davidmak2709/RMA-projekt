package hr.helloworld.david.esports;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;

import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;


public class DetailsActivity extends AppCompatActivity {

    private String idevent;
    private TextView inputTextView;
    private FirebaseListAdapter<ChatMessage> adapter;
    private Toolbar toolbar;
    private View.OnClickListener goingClickListener;
    private View.OnClickListener cantGoClickListener;
    private View.OnClickListener showGoingUsersList;
    private DatabaseReference myRef;
    private FirebaseUser firebaseUser;
    private User user;
    private AlertDialog.Builder builder;
    private boolean  isClicked;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        toolbar = findViewById(R.id.toolbarDetailsActivity);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.updateFriendsStatus();
                finish();
            }
        });



        //dohvacanje intenta
        final Intent detailsIntent = getIntent();
        idevent = detailsIntent.getStringExtra("id");

        //spajanje na bazu TESTNO
        // Write a message to the database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("events");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(firebaseUser != null){
            user = new User(firebaseUser.getUid(),firebaseUser.getDisplayName(),firebaseUser.getEmail(),
                    firebaseUser.getPhotoUrl());

            user.getUserFriendsUUID();
        }

        goingClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Query ref = myRef.orderByChild("id").equalTo(idevent);
                isClicked=false;
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String key = snapshot.getKey();
                            int gooing = snapshot.getValue(Event.class).getGooing();
                            HashMap<String, Object> result = new HashMap<>();
                            result.put("gooing", gooing + 1);
                            myRef.child(key).updateChildren(result);

                            myRef.child(key).child("goingUsers").child(firebaseUser.getUid())
                                    .child("username").setValue(firebaseUser.getDisplayName());

                            myRef.child(key).child("goingUsers").child(firebaseUser.getUid())
                                    .child("photoUrl").setValue(firebaseUser.getPhotoUrl().toString());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        cantGoClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Query ref = myRef.orderByChild("id").equalTo(idevent);
                isClicked=false;
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String key = snapshot.getKey();
                            int gooing = snapshot.getValue(Event.class).getGooing();
                            HashMap<String, Object> result = new HashMap<>();
                            result.put("gooing", gooing - 1);
                            myRef.child(key).updateChildren(result);

                            myRef.child(key).child("goingUsers").child(firebaseUser.getUid()).removeValue();

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };



        showGoingUsersList = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder = new AlertDialog.Builder(DetailsActivity.this);
                isClicked = true;
                myRef.child(idevent).child("goingUsers").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<String> urls = new ArrayList<>();
                        ArrayList<String> userNames = new ArrayList<>();
                        ArrayList<String> uuid = new ArrayList<>();

                        Log.d("***TAGG: ", idevent);
                        for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                            if(firebaseUser.getUid().equals(snapshot.getKey())) {
                                Log.d("***TAGG: ", snapshot.getKey());
                                uuid.add(0,snapshot.getKey());
                                userNames.add(0,snapshot.child("username").getValue(String.class));
                                urls.add(0,snapshot.child("photoUrl").getValue(String.class));
                            }
                            else{
                                Log.d("***TAGG: ", snapshot.getKey());
                                uuid.add(snapshot.getKey());
                                userNames.add(snapshot.child("username").getValue(String.class));
                                urls.add(snapshot.child("photoUrl").getValue(String.class));
                            }
                        }

                        CustomListView customListView;
                        customListView = new CustomListView(DetailsActivity.this,urls,userNames,uuid,user);

                        builder.setTitle(getResources().getString(R.string.DetailsGoingUserAlertDialogLabel));
                        builder.setAdapter(customListView,null);
                        if (isClicked)
                            builder.show();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        // Read from the database
        myRef.orderByChild("id").equalTo(idevent).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LinearLayout linearLayout = findViewById(R.id.LLDetails);
                LinearLayout goingLinearLayout = findViewById(R.id.LLGoingUsers);
                LinearLayout buttonLinearLayout = findViewById(R.id.LLHeader);
                boolean userGoes = false;
                TextView textViewGoingUsers;
                TextView textViewSize;
                TextView textViewSpace;
                TextView textViewSport;
                TextView textViewTime;
                Button goingButton;

                Event event = null;


                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    event = new Event(snapshot.getValue(Event.class).naslov
                            , snapshot.getValue(Event.class).getId(),
                            new LatLng(snapshot.getValue(Event.class).getLat(), snapshot.getValue(Event.class).getLng()),
                            snapshot.getValue(Event.class).getRadius(),
                            snapshot.getValue(Event.class).getDuration(),
                            snapshot.getValue(Event.class).getNumId(),
                            snapshot.getValue(Event.class).getSize(),
                            snapshot.getValue(Event.class).getGooing(),
                            snapshot.getValue(Event.class).getSport(),
                            snapshot.getValue(Event.class).getOwner(),
                            snapshot.getValue(Event.class).getmTime());

                    userGoes = snapshot.child("goingUsers").child(firebaseUser.getUid()).exists();
                }

                @SuppressWarnings("SimpleDateFormat")
                DateFormat df = new SimpleDateFormat("MM/dd/yy HH:mm");

                if(event != null) {

                    linearLayout.removeAllViews();
                    buttonLinearLayout.removeAllViews();
                    goingLinearLayout.removeAllViews();

                    /**
                     * Linear layout event info
                     */
                    textViewSport = new TextView(getApplicationContext());
                    textViewSport.setText(event.getSport());
                    textViewSport.setAllCaps(true);
                    textViewSport.setTextColor(getResources().getColor(R.color.colorTextLabel));
                    textViewSport.setTextSize(22);
                    textViewSport.setTypeface(null,Typeface.BOLD);

                    textViewTime = new TextView(getApplicationContext());
                    textViewTime.setText(df.format(event.getmTime()));
                    textViewTime.setTextSize(16);

                    linearLayout.addView(textViewSport);
                    linearLayout.addView(textViewTime);

                    /**
                     * Linear layout za button
                     */
                    if(event.getGooing() < event.getSize() || userGoes) {

                        goingButton = new Button(getApplicationContext());
                        goingButton.setId(R.id.DetailsActivityGoingButtonId);

                        if (!userGoes) {
                            goingButton.setOnClickListener(goingClickListener);
                            goingButton.setText(getResources().getString(R.string.DetailsGoingLabel));
                        } else {
                            goingButton.setText(getResources().getString(R.string.DetailsCantGoLabel));
                            goingButton.setOnClickListener(cantGoClickListener);
                        }
                        buttonLinearLayout.addView(goingButton);

                    }
                    /**
                     * Linear layout koji sadrži podatke o tome koliko ljudi idu
                     */

                    textViewGoingUsers = new TextView(getApplicationContext());
                    textViewGoingUsers.setText(String.valueOf(event.getGooing()));
                    textViewGoingUsers.setTextColor(getResources().getColor(R.color.colorTextLabel));
                    textViewGoingUsers.setTextSize(72);
//                    textViewGoingUsers.setOnClickListener(showGoingUsersList);

                    textViewSpace = new TextView(getApplicationContext());
                    textViewSpace.setText("/");
                    textViewSpace.setTextSize(42);

                    if(event.getGooing() < event.getSize()){
                        textViewGoingUsers.setTextColor(getResources().getColor(R.color.colorTextLabelGreen));
                    }else {
                        textViewGoingUsers.setTextColor(getResources().getColor(R.color.colorTextLabelRed));
                    }

                    textViewSize = new TextView(getApplicationContext());
                    textViewSize.setText(String.valueOf(event.getSize()));
                    textViewSize.setTextColor(getResources().getColor(R.color.colorTextLabel));
                    textViewSize.setTextSize(32);


                    goingLinearLayout.setOnClickListener(showGoingUsersList);
                    goingLinearLayout.addView(textViewGoingUsers);
                    goingLinearLayout.addView(textViewSpace);
                    goingLinearLayout.addView(textViewSize);

                    //TODO ili neki drugi naziv
                    toolbar.setTitle(event.getOwner());
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });


        inputTextView = findViewById(R.id.input_message);

        FloatingActionButton floatingActionButton = findViewById(R.id.send_message_button);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null)
                    FirebaseDatabase.getInstance()
                            .getReference()
                            .child("events")
                            .child(idevent)
                            .child("messages")
                            .push()
                            .setValue(new ChatMessage(inputTextView.getText().toString(),
                                    firebaseUser.getDisplayName()));

                inputTextView.setText("");
            }
        });

        displayMessages();
    }

    @Override
    protected void onStart() {
        super.onStart();

        adapter.startListening();
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        user.updateFriendsStatus();
        finish();
    }

    private void displayMessages() {
        ListView listOfMessages = findViewById(R.id.list_of_messages);


        Query reference = FirebaseDatabase.getInstance().getReference()
                .child("events").child(idevent).child("messages");

        FirebaseListOptions<ChatMessage> options = new FirebaseListOptions.Builder<ChatMessage>()
                .setQuery(reference, ChatMessage.class)
                .setLayout(R.layout.message)
                .build();

        adapter = new FirebaseListAdapter<ChatMessage>(options) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                TextView messageText = v.findViewById(R.id.message_text);
                TextView messageUser = v.findViewById(R.id.message_user);
                TextView messageTime = v.findViewById(R.id.message_time);

                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());

                @SuppressLint("SimpleDateFormat")
                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yy HH:mm");

                messageTime.setText(dateFormat.format(model.getMessageTime()));
            }
        };

        listOfMessages.setAdapter(adapter);
    }
}
