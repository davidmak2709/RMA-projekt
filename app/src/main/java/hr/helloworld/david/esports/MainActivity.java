package hr.helloworld.david.esports;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private EditText searchBar;
    private ListView usersList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        searchBar = findViewById(R.id.searchEditText);
        usersList = findViewById(R.id.userList);

        ImageView imageView = findViewById(R.id.imageView);
        Button signOut = findViewById(R.id.button2);
        Uri imageUri = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();

        if (imageUri != null) {
            Picasso.with(getApplicationContext())
                    .load(imageUri)
                    .resize(50, 50)
                    .centerCrop()
                    .into(imageView);
        } else {
            Picasso.with(getApplicationContext())
                    .load(R.mipmap.ic_launcher)
                    .resize(50, 50)
                    .centerCrop()
                    .into(imageView);
        }
        signOut.setText("Sign Out");


        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, StartActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0)
                    searchForUser(s.toString());
                else
                    usersList.removeAllViews();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void searchForUser(String searchString) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference database = firebaseDatabase.getReference("users");
        Query query = database.orderByChild("searchUsername").startAt(searchString).limitToFirst(10);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> urls = new ArrayList<>();
                ArrayList<String> usernames = new ArrayList<>();


                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.child("username").exists())
                        usernames.add(snapshot.child("username").getValue().toString());

                    if (snapshot.child("photoUrl").exists())
                        urls.add(snapshot.child("photoUrl").getValue().toString());
                }

                CustomListView customListView = new CustomListView(MainActivity.this, urls, usernames);
                usersList.setAdapter(customListView);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
