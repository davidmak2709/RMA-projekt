package hr.helloworld.david.esports;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class User {

    private String uuid;
    private String username;
    private String searchUsername;
    private String email;
    private Uri photoUrl;

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


    public String getuuid() {
        return uuid;
    }

    public void setuuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSearchUsername() {
        return searchUsername;
    }

    public void setSearchUsername(String searchUsername) {
        this.searchUsername = searchUsername;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Uri getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(Uri photoUrl) {
        this.photoUrl = photoUrl;
    }
}
