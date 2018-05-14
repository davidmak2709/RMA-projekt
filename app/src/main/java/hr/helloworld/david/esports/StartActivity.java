package hr.helloworld.david.esports;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.support.design.widget.Snackbar;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.GoogleAuthProvider;

public class StartActivity extends AppCompatActivity {

    private static final String GOOGLE_TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;

    private EditText emailEditView;
    private EditText pwdEditView;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;


    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager callbackManager;

    private View.OnClickListener sendMailListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_start);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();


        /*
         *   provjera ako je korinsik već logiran
         */
        if (firebaseUser != null) {
            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        emailEditView = findViewById(R.id.StartActivityEmailEditView);
        pwdEditView = findViewById(R.id.StartActivityPasswordEditView);

        Button loginButton = findViewById(R.id.StartActivityLoginButton);
        Button signupButton = findViewById(R.id.StartActivitySignUpButton);

        SignInButton googleButton = findViewById(R.id.StartActivityGoogleButton);


        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logInUser();
            }
        });

        sendMailListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPassword();
            }
        };


        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });

        callbackManager = CallbackManager.Factory.create();
        LoginButton facebookButton = findViewById(R.id.StartActivityFacebookButton);
        facebookButton.setReadPermissions("email", "public_profile");
        facebookButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
                String e = "Failed to login. " + error.getMessage();
                Toast.makeText(StartActivity.this, e, Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w(GOOGLE_TAG, "Google sign in failed" + e.getMessage());

            }
        } else if (CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode() == requestCode) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private boolean logInUser() {
        String mail = emailEditView.getText().toString();
        String pwd = pwdEditView.getText().toString();
        if (!validateForm()) {
            return false;
        }


        firebaseAuth.signInWithEmailAndPassword(mail, pwd)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                            Intent intent = new Intent(StartActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            Exception exception = task.getException();
                            if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                                LinearLayout linearLayout = findViewById(R.id.BottomLinearLayout);
                                Snackbar.make(linearLayout, "Zaboravljena lozinka?",
                                        Snackbar.LENGTH_LONG).setAction("Reset",
                                        sendMailListener).setActionTextColor(Color.RED).show();
                            } else {
                                Toast.makeText(StartActivity.this, exception.getMessage(),
                                        Toast.LENGTH_SHORT).show();

                            }
                        }
                    }
                });


        return true;
    }

    private boolean validateForm() {
        boolean valid = true;

        String mail = emailEditView.getText().toString();
        if (TextUtils.isEmpty(mail)) {
            emailEditView.setError("Required.");
            valid = false;
        } else {
            emailEditView.setError(null);
        }

        String pwd = pwdEditView.getText().toString();
        if (TextUtils.isEmpty(pwd)) {
            pwdEditView.setError("Required.");
            valid = false;
        } else if (pwd.length() < 6) {
            pwdEditView.setError("Too short.");
            valid = false;
        } else {
            pwdEditView.setError(null);
        }

        return valid;
    }

    private void resetPassword() {
        String mail = emailEditView.getText().toString();
        if (mail != "") {
            firebaseAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(StartActivity.this, "Mail poslan",
                            Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(StartActivity.this, e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void signInWithGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(GOOGLE_TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Snackbar.make(findViewById(R.id.BottomLinearLayout), "signInWithCredential:success",
                                    Snackbar.LENGTH_LONG).show();

                            firebaseUser = firebaseAuth.getCurrentUser();
                            User user = new User(firebaseUser.getUid(), firebaseUser.getDisplayName(),
                                    firebaseUser.getEmail(), firebaseUser.getPhotoUrl());

                            user.saveNewUser();

                            Intent intent = new Intent(StartActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Snackbar.make(findViewById(R.id.BottomLinearLayout), "Authentication Failed.",
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            firebaseUser = firebaseAuth.getCurrentUser();
                            User user = new User(firebaseUser.getUid(), firebaseUser.getDisplayName(),
                                    firebaseUser.getEmail(), firebaseUser.getPhotoUrl());

                            user.saveNewUser();

                            Intent intent = new Intent(StartActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(StartActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}
