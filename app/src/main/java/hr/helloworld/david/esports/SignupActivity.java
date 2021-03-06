package hr.helloworld.david.esports;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class SignupActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText pwdEditText;
    private EditText resetPwdEditText;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private Uri selectedImage;

    private ProgressDialog dialog;
    private StorageReference storageReference;


    private static int RESULT_LOAD_IMAGE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();


        profileImageView = findViewById(R.id.SignUpActivityImageSelector);
        usernameEditText = findViewById(R.id.SignUpActivityUsernameEditView);
        emailEditText = findViewById(R.id.SignUpActivityEmailEditView);
        pwdEditText = findViewById(R.id.SignUpActivityPasswordEditView);
        resetPwdEditText = findViewById(R.id.SignUpActivityRePasswordEditView);
        Button continueButton = findViewById(R.id.SignUpActivityCreateAccountButton);

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] options = {getResources().getString(R.string.PickerGalleryOption),
                        getResources().getString(R.string.PickerCameraOption)};

                AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
                builder.setTitle(getResources().getString(R.string.PickerTitle));

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent i = new Intent(Intent.ACTION_PICK,
                                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(i, RESULT_LOAD_IMAGE);
                                break;
                            case 1:
                                Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                                if (ContextCompat.checkSelfPermission(SignupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                                        == PackageManager.PERMISSION_GRANTED &&
                                        ContextCompat.checkSelfPermission(SignupActivity.this, Manifest.permission.CAMERA)
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

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateForm()) {
                    createNewUser();
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (ContextCompat.checkSelfPermission(SignupActivity.this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SignupActivity.this,
                    new String[]{android.Manifest.permission.CAMERA},
                    1);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            selectedImage = data.getData();
            profileImageView.setImageURI(selectedImage);

        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            selectedImage = data.getData();
            profileImageView.setImageURI(selectedImage);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SignupActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    2);
        }

    }

    private void createNewUser() {
        if (!validateForm()) {
            return;
        }

        dialog = new ProgressDialog(SignupActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(getResources().getString(R.string.SignUpActivityDialogString));
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();

        String email = emailEditText.getText().toString();
        String password = pwdEditText.getText().toString();


        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        signInUser();
                        firebaseUser = authResult.getUser();

                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        storageReference = storage.getReference();


                        if (selectedImage == null)
                            selectedImage = Uri.parse("android.resource://" + SignupActivity.this.getPackageName() + "/mipmap/ic_launcher");

                        storageReference.child(getResources().getString(R.string.FirebaseStorageProfilePictureFolder)
                                + firebaseUser.getUid()).putFile(selectedImage)
                                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        if (task.isSuccessful()) {

                                            StorageMetadata metadata = new StorageMetadata.Builder()
                                                    .setCustomMetadata("isThumb", "false").build();

                                            storageReference.child(getResources().getString(R.string.FirebaseStorageProfilePictureFolder)
                                                    + firebaseUser.getUid()).updateMetadata(metadata);

                                            setUserInfo();
                                        } else if (task.getException() != null) {
                                            createUserError(task.getException());
                                        }
                                    }
                                });


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                createUserError(e);
            }
        });


    }

    private void setUserInfo() {

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();


        storageReference.child(getResources().getString(R.string.FirebaseStorageProfilePictureFolder)
                + firebaseUser.getUid()).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        User user = new User(firebaseUser.getUid(), usernameEditText.getText().toString(),
                                emailEditText.getText().toString(), uri);

                        user.saveNewUser();


                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().
                                setDisplayName(usernameEditText.getText().toString()).
                                setPhotoUri(uri).build();

                        firebaseUser.updateProfile(profileUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                createUserError(e);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                createUserError(e);
            }
        });
    }

    private void signInUser() {
        String email = emailEditText.getText().toString();
        String password = pwdEditText.getText().toString();

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful() && task.getException() != null) {
                            createUserError(task.getException());
                        }
                    }
                });
    }

    private void createUserError(Exception e) {
        dialog.cancel();
        Toast.makeText(SignupActivity.this, e.getMessage(),
                Toast.LENGTH_LONG).show();
    }

    private boolean validateForm() {
        boolean valid = true;


        String username = usernameEditText.getText().toString();
        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Required.");
            valid = false;
        } else {
            usernameEditText.setError(null);
        }

        String mail = emailEditText.getText().toString();
        if (TextUtils.isEmpty(mail)) {
            emailEditText.setError("Required.");
            valid = false;
        } else {
            emailEditText.setError(null);
        }


        String pwd = pwdEditText.getText().toString();
        if (pwd.matches("")) {
            pwdEditText.setError("Required.");
            valid = false;
        } else {
            pwdEditText.setError(null);
        }

        String rpwd = resetPwdEditText.getText().toString();
        if (rpwd.matches("")) {
            resetPwdEditText.setError("Required.");
            valid = false;
        } else {
            resetPwdEditText.setError(null);
        }

        if (!TextUtils.equals(pwd, rpwd)) {
            resetPwdEditText.setError("Must be equal.");
            pwdEditText.setError("Must be equal.");
            valid = false;
        } else if (pwd.length() < 6 || rpwd.length() < 6) {
            resetPwdEditText.setError("Min 6 char.");
            pwdEditText.setError("Min 6 char.");
            valid = false;
        } else {
            resetPwdEditText.setError(null);
            pwdEditText.setError(null);
        }

        return valid;
    }

}
