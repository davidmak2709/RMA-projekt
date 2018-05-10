package hr.helloworld.david.esports;

import android.net.Uri;
import android.support.constraint.solver.widgets.Snapshot;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String UUID;
    private String username;
    private String email;
    private Uri photoUrl;

    public User(){

    }

    public User(String UUID,String username, String email,Uri photoUrl){
        this.UUID = UUID;
        this.username = username;
        this.email = email;
        this.photoUrl =photoUrl;
    }

    public void saveNewUser(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");


        databaseReference.child(this.UUID).child("uuid").setValue(this.UUID);
        databaseReference.child(this.UUID).child("username").setValue(this.username);
        databaseReference.child(this.UUID).child("email").setValue(this.email);

        if(this.photoUrl != null){
            databaseReference.child(this.UUID).child("photoUrl").setValue(photoUrl.toString());
        }

    }

    public static void setCurrentUserInfo(final String UUID){
       final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
       Query query = databaseReference.orderByChild("uuid").equalTo(UUID);

       query.addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
                //TODO POSTAVI SVE POTREBNE PODATKE

           }

           @Override
           public void onCancelled(DatabaseError databaseError) {

           }
       });
    }


}
