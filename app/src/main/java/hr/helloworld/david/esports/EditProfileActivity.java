package hr.helloworld.david.esports;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class EditProfileActivity extends AppCompatActivity {

    private EditText usernameET;
    private ImageView imageSelector;
    private FirebaseUser firebaseUser;
    private Uri selectedImage;
    private Uri retUri;
    private UserProfileChangeRequest.Builder profileUpdates;


    private static int RESULT_LOAD_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Toolbar toolbar = findViewById(R.id.toolbarEditProfileActivity);
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

        usernameET = findViewById(R.id.EditProfileActivityUsernameEditView);

        imageSelector = findViewById(R.id.EditProfileActivityImageSelector);


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            usernameET.setText(firebaseUser.getDisplayName());


            Picasso.with(EditProfileActivity.this)
                    .load(firebaseUser.getPhotoUrl())
                    .centerCrop()
                    .resize(250, 250)
                    .into(imageSelector);
        }

        imageSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });


        Button saveChangesButton = findViewById(R.id.saveChanges);

        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String result;
                switch (updateUserProfile()) {
                    case 0:
                        result = getResources().getString(R.string.EditProfileNoChanges);
                        break;
                    case 1:
                        result = getResources().getString(R.string.EditProfileImageChange);
                        break;
                    case 2:
                        result = getResources().getString(R.string.EditProfileUsernameChange);
                        break;
                    case 3:
                        result = getResources().getString(R.string.EditProfileAllChange);
                        break;
                    default:
                        result = "";
                        break;
                }


                Snackbar.make(findViewById(R.id.EditProfileActivityBottomLL),
                        result,
                        Snackbar.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            selectedImage = data.getData();
            imageSelector.setImageURI(selectedImage);

        }


    }

    private int updateUserProfile() {
        String newUsername = usernameET.getText().toString();
        profileUpdates = new UserProfileChangeRequest.Builder();
        int retVal = 0;


        if (selectedImage == null && newUsername.equals(firebaseUser.getDisplayName())) {
            return retVal;
        } else if (selectedImage != null && newUsername.equals(firebaseUser.getDisplayName())) {
            retVal = 1;
        } else if (selectedImage == null && !newUsername.equals(firebaseUser.getDisplayName())) {
            profileUpdates.setDisplayName(newUsername);
            User.updateUserName(firebaseUser.getUid(), newUsername);
            retVal = 2;
        } else if (selectedImage != null && !newUsername.equals(firebaseUser.getDisplayName())) {
            profileUpdates.setDisplayName(newUsername);
            User.updateUserName(firebaseUser.getUid(), newUsername);
            retVal = 3;
        }

        if (selectedImage != null) {
            new SaveUserInfo().execute("");
        }

        firebaseUser.updateProfile(profileUpdates.build());


        return retVal;
    }

    @SuppressLint("StaticFieldLeak")
    public class SaveUserInfo extends AsyncTask<String, Void, Void> {

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        @Override
        protected Void doInBackground(String... strings) {
            storageReference.child(getResources().getString(R.string.FirebaseStorageProfilePictureFolder)
                    + firebaseUser.getUid()).putFile(selectedImage);
            storageReference.child(getResources().getString(R.string.FirebaseStorageProfilePictureFolder)
                    + firebaseUser.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    retUri = uri;
                }
            });

            while (retUri == null) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            profileUpdates.setPhotoUri(retUri);
            firebaseUser.updateProfile(profileUpdates.build());
            User.updateUserImage(firebaseUser.getUid(), retUri);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }


}
