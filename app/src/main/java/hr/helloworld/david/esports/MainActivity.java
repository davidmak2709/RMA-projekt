package hr.helloworld.david.esports;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageView = findViewById(R.id.imageView);
        Button signOut = findViewById(R.id.button2);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();



        if(user != null){

            User.setCurrentUserInfo(user.getUid());

            Picasso.with(getApplicationContext())
                    .load("https://firebasestorage.googleapis.com/v0/b/esports-719c4.appspot.com/o/profile%2F72476?alt=media&token=ba8aeaad-b45f-4d47-9bf3-812ffd61921c")
                    .resize(100,100)
                    .centerCrop()
                    .into(imageView);
            signOut.setText(user.getDisplayName());

            if(user.getPhotoUrl() == null)
                Toast.makeText(this,"aaaaaa",Toast.LENGTH_LONG).show();
        }


        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this,StartActivity.class);
                startActivity(intent);
            }
        });
    }
}
