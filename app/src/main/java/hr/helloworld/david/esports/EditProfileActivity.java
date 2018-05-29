package hr.helloworld.david.esports;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class EditProfileActivity extends AppCompatActivity {

    private EditText usernameET;
    private ImageView imageSelector;
    private FirebaseUser firebaseUser;
    private Uri selectedImage;
    private Uri retUri;
    private UserProfileChangeRequest.Builder profileUpdates;


    private static int RESULT_LOAD_IMAGE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 100;
    private static final int THUMBNAIL_SIZE = 100;


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
                String[] options = {getResources().getString(R.string.PickerGalleryOption),
                        getResources().getString(R.string.PickerCameraOption)};

                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                builder.setTitle(getResources().getString(R.string.PickerTitle));

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0:
                                Intent i = new Intent(Intent.ACTION_PICK,
                                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(i, RESULT_LOAD_IMAGE);
                                break;
                            case 1:
                                Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                                if (ContextCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                                        == PackageManager.PERMISSION_GRANTED &&
                                        ContextCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.CAMERA)
                                                == PackageManager.PERMISSION_GRANTED) {

                                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                });
                builder.show();
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
    protected void onStart() {
        super.onStart();

        if (ContextCompat.checkSelfPermission(EditProfileActivity.this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EditProfileActivity.this,
                    new String[]{android.Manifest.permission.CAMERA},
                    1);
        }


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            selectedImage = data.getData();
            imageSelector.setImageURI(selectedImage);
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            selectedImage = data.getData();
            imageSelector.setImageURI(selectedImage);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EditProfileActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    2);
        }

    }

    public Bitmap getThumbnail(Uri uri) throws FileNotFoundException, IOException {
        InputStream input = this.getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();

        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1)) {
            return null;
        }

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

        double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither = true; //optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//
        input = this.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }

    private static int getPowerOfTwoForSampleRatio(double ratio) {
        int k = Integer.highestOneBit((int) Math.floor(ratio));
        if (k == 0) return 1;
        else return k;
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
