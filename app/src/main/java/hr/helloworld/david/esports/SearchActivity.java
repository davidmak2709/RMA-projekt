package hr.helloworld.david.esports;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;


public class SearchActivity extends AppCompatActivity {

    private EditText searchBar;
    private ListView usersList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

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

        searchBar = findViewById(R.id.searchEditText);
        usersList = findViewById(R.id.userList);
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
                    new GetData().execute(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @SuppressLint("StaticFieldLeak")
    private class GetData extends AsyncTask<String, Void, Void> {


        @Override
        protected Void doInBackground(final String... strings) {

            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            final DatabaseReference database = firebaseDatabase.getReference("users");
            Query query = database.orderByChild("searchUsername")
                    .startAt(strings[0])
                    .limitToFirst(10);

            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ArrayList<String> urls = new ArrayList<>();
                    ArrayList<String> userNames = new ArrayList<>();


                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.child("searchUsername").exists() &&
                                Objects.requireNonNull(snapshot.child("searchUsername")
                                        .getValue(String.class)).startsWith(strings[0])) {

                            userNames.add(snapshot.child("username").getValue(String.class));

                            if (snapshot.child("photoUrl").exists())
                                urls.add(snapshot.child("photoUrl").getValue(String.class));
                        }
                    }

                    CustomListView customListView = new CustomListView(SearchActivity.this, urls, userNames);
                    usersList.setAdapter(customListView);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            return null;
        }
    }
}