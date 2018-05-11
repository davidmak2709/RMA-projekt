package hr.helloworld.david.esports;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageView = findViewById(R.id.imageView);
        Button signOut = findViewById(R.id.button2);


        Picasso.with(getApplicationContext())
                .load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
                .resize(100, 100)
                .centerCrop()
                .into(imageView);

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
}
