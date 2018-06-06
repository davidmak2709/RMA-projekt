package hr.helloworld.david.esports;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseUser firebaseUser;
    private CardView editRange;
    private AlertDialog alertDialog;
    private CharSequence[] values = {" 5 km "," 15 km "," 30 km ", "Bez ograničenja"};
    private int[] kilometers = {5, 15, 30, 0};
    private int checkedItem=-1;
    private int currentRange;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentRange=dataSnapshot.child(firebaseUser.getUid()).child("range").getValue(Integer.class);
                switch (currentRange)
                {
                    case 5:
                        checkedItem=0;
                        break;
                    case 15:
                        checkedItem=1;
                        break;
                    case 30:
                        checkedItem=2;
                        break;
                    case 0:
                        checkedItem=3;
                        break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        editRange=findViewById(R.id.editRange);

        editRange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateAlertDialogWithRadioButtonGroup();
            }
        });

    }

    public void CreateAlertDialogWithRadioButtonGroup(){


        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Maksimalna udaljenost");

        builder.setSingleChoiceItems(values, checkedItem, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {

                switch(item)
                {
                    case 0:
                        databaseReference.child(firebaseUser.getUid()).child("range").setValue(kilometers[0]);
                        break;
                    case 1:
                        databaseReference.child(firebaseUser.getUid()).child("range").setValue(kilometers[1]);
                        break;
                    case 2:
                        databaseReference.child(firebaseUser.getUid()).child("range").setValue(kilometers[2]);
                        break;
                    case 3:
                        databaseReference.child(firebaseUser.getUid()).child("range").setValue(kilometers[3]);
                        break;
                }
                alertDialog.dismiss();
            }
        });
        alertDialog = builder.create();
        alertDialog.show();

    }

}
