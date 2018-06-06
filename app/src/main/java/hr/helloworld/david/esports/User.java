package hr.helloworld.david.esports;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class User {

    private String uuid;
    private String username;
    private String searchUsername;
    private String email;
    public ArrayList<String> friendsUUID = new ArrayList<>();
    private Uri photoUrl;
    public int numFriends = -1;


    public User() {
    }

    public User(String uuid, String username, String email, Uri photoUrl) {
        this.uuid = uuid;
        this.username = username;
        this.email = email;
        this.photoUrl =photoUrl;
    }

    public void saveNewUser(){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.child(this.uuid).child("uuid").setValue(this.uuid);
        databaseReference.child(this.uuid).child("username").setValue(this.username);
        databaseReference.child(this.uuid).child("searchUsername").setValue(this.username.toLowerCase());
        databaseReference.child(this.uuid).child("email").setValue(this.email);
        databaseReference.child(this.uuid).child("range").setValue(0);

        if(this.photoUrl != null){
            databaseReference.child(this.uuid).child("photoUrl").setValue(photoUrl.toString());
        }

    }

    public static void updateUserName(String uuid, String username) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");

        databaseReference.child(uuid).child("username").setValue(username);
        databaseReference.child(uuid).child("searchUsername").setValue(username.toLowerCase());
    }

    public static void updateUserImage(String uuid, Uri photoUrl) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        if (photoUrl != null) {
            databaseReference.child(uuid).child("photoUrl").setValue(photoUrl.toString());
        }
    }

    public void getUserFriendsUUID() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        Query query = databaseReference.child(this.uuid).child("friends");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                numFriends = (int) dataSnapshot.getChildrenCount();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    friendsUUID.add(snapshot.getKey());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    public void updateFriendsStatus() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.child(this.uuid).child("friends").removeValue();
        for (String friend : this.friendsUUID) {
            databaseReference.child(this.uuid).child("friends").child(friend).setValue(true);
        }
    }


    public String getUsername() {
        return username;
    }

    public String getUuid() {
        return uuid;
    }

    public Uri getPhotoUrl() {
        return photoUrl;
    }
}
