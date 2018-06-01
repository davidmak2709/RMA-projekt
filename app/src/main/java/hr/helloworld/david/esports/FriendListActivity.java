package hr.helloworld.david.esports;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.arch.persistence.room.Database;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;


public class FriendListActivity extends AppCompatActivity {

    private User user;
    private ListView friendsList;
    private FirebaseUser firebaseUser;
    private ProgressBar dialog;
    private CustomListView customListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null){
            user = new User(firebaseUser.getUid(), firebaseUser.getDisplayName(), firebaseUser.getEmail(),
                    firebaseUser.getPhotoUrl());

            user.getUserFriendsUUID();
        }



        setContentView(R.layout.activity_friend_list);

        Toolbar toolbar = findViewById(R.id.toolbarSearchActivity);
        toolbar.setTitle(" ");
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        friendsList = findViewById(R.id.userFriendsList);

    }

    @Override
    protected void onStart() {
        super.onStart();

        dialog = findViewById(R.id.progressBar);
        dialog.setIndeterminate(true);
        dialog.setVisibility(View.VISIBLE);

        new GetData().execute();

    }

    private class GetData extends AsyncTask<Void, Void, Void> {

        private ArrayList<String> uuids = new ArrayList<>();
        private ArrayList<String> urls = new ArrayList<>();
        private ArrayList<String> usernames = new ArrayList<>();

        @Override
        protected Void doInBackground(Void... strings) {
            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
            Query query = databaseReference.orderByKey();

            while (user.friendsUUID.isEmpty()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Log.d("TAG", e.getMessage());
                }

                if (user.numFriends != -1) break;
            }

            for (String uuid : user.friendsUUID) {
                query.equalTo(uuid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            usernames.add(snapshot.child("username").getValue().toString());
                            urls.add(snapshot.child("photoUrl").getValue().toString());
                            uuids.add(snapshot.child("uuid").getValue().toString());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            while ((usernames.isEmpty() && urls.isEmpty() && uuids.isEmpty()) && user.numFriends != 0) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Log.d("TAG", e.getMessage());
                }
            }

            customListView = new CustomListView(FriendListActivity.this,
                    urls, usernames, uuids, user);

            Log.d("tagg:", "lala");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            while (customListView.getUrls().isEmpty() && customListView.getUserNames().isEmpty() && customListView.getUuid().isEmpty()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Log.d("TAG", e.getMessage());
                }
            }

            dialog.setVisibility(View.INVISIBLE);
            friendsList.setAdapter(customListView);
        }
    }
}
