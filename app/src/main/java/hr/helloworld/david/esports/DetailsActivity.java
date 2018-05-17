package hr.helloworld.david.esports;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
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
    ArrayList<Event> events = new ArrayList<Event>();

    private String idevent;
    private TextView inputTextView;
    private FirebaseListAdapter<ChatMessage> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);


        //dohvacanje intenta
        final Intent detailsIntent = getIntent();
        idevent = "-LCh_Z3VmqS_An3TGnte";

        //spajanje na bazu TESTNO
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("events");

        //botun za dolazak
        Button btn = findViewById(R.id.detailsbtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Query ref = myRef.orderByChild("id").equalTo(idevent);

                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            try {
                                // UPDATE Događaja TODO napravit NEDOLAZIM u slucaju ako korisnik vec dolazi i validacija ako nema mjesta
                                String key = snapshot.getKey();
                                int gooing = snapshot.getValue(Event.class).getGooing();
                                HashMap<String, Object> result = new HashMap<>();
                                result.put("gooing", gooing + 1);
                                myRef.child(key).updateChildren(result);
                            } catch (Exception e) {
                                Log.d("Fail: ", "Problem!");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

        });

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                LinearLayout linearLayout = findViewById(R.id.LLDetails);
                TextView textView;
                TextView textView1;
                TextView textView2;
                TextView textView3;
                TextView textView4;


                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        //Ako stavimo da je obavezno polje nece imati mogućnost generiranja NULL
                        events.add(new Event(snapshot.getValue(Event.class).getId(),
                                new LatLng(snapshot.getValue(Event.class).getLat(), snapshot.getValue(Event.class).getLng()),
                                snapshot.getValue(Event.class).getRadius(),
                                snapshot.getValue(Event.class).getDuration(),
                                snapshot.getValue(Event.class).getNumId(),
                                snapshot.getValue(Event.class).getSize(),
                                snapshot.getValue(Event.class).getGooing(),
                                snapshot.getValue(Event.class).getSport(),
                                snapshot.getValue(Event.class).getOwner(),
                                snapshot.getValue(Event.class).getmTime()));
                    } catch (Exception e) {
                        Log.d("Fail: ", "Problem!");
                    }
                }

                for (Event event : events) {
                    if (event.getId().equals(idevent)) {
                        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

                        linearLayout.removeAllViews();
                        textView = new TextView(getApplicationContext());
                        textView.setText(event.getId());
                        textView1 = new TextView(getApplicationContext());
                        textView1.setText(event.getGooing() + "/" + event.getSize());
                        textView2 = new TextView(getApplicationContext());
                        textView2.setText(event.getOwner());
                        textView3 = new TextView(getApplicationContext());
                        textView3.setText(event.getSport());
                        textView4 = new TextView(getApplicationContext());
                        textView4.setText(df.format(event.getmTime()));
                        linearLayout.addView(textView);
                        linearLayout.addView(textView1);
                        linearLayout.addView(textView2);
                        linearLayout.addView(textView3);
                        linearLayout.addView(textView4);

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //Log.w(TAG, "Failed to read value.", error.toException());
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
