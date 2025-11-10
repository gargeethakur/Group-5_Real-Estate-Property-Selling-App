package com.example.realestate;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.realestate.databinding.ActivityLoginOptionsBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginOptionsActivity extends AppCompatActivity {

    private static final String TAG = "LOGIN_OPTIONS_TAG";

    private ActivityLoginOptionsBinding binding;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginOptionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.LoginGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                beginGoogleLogin();
            }
        });

        binding.LoginEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            startActivity(new Intent(LoginOptionsActivity.this, LoginEmailActivity.class));
            }
        });

        binding.LoginPhoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginOptionsActivity.this, LoginPhoneActivity.class));
            }
        });

    }

    private void beginGoogleLogin() {
        Log.d(TAG, "beginGoogleLogin: launching Google Sign-In intent");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInARL.launch(signInIntent);
    }

    private final ActivityResultLauncher<Intent> googleSignInARL =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            Log.d(TAG, "onActivityResult: Google Sign-In result received");

                            if (result.getResultCode() == Activity.RESULT_OK) {
                                Intent data = result.getData();
                                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

                                try {
                                    GoogleSignInAccount account = task.getResult(ApiException.class);
                                    if (account != null) {
                                        Log.d(TAG, "Google Sign-In successful. Account ID: " + account.getId());
                                        firebaseAuthWithGoogleAccount(account.getIdToken());
                                    }
                                } catch (ApiException e) {
                                    Log.e(TAG, "Google Sign-In failed", e);
                                    MyUtils.toast(LoginOptionsActivity.this, "Google sign-in failed: " + e.getMessage());
                                }
                            } else {
                                Log.d(TAG, "onActivityResult: Google Sign-In cancelled");
                                MyUtils.toast(LoginOptionsActivity.this, "Google Sign-In cancelled.");
                            }
                        }
                    });

    private void firebaseAuthWithGoogleAccount(String idToken) {
        Log.d(TAG, "firebaseAuthWithGoogleAccount: idToken=" + idToken);

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    if (authResult.getAdditionalUserInfo() != null && authResult.getAdditionalUserInfo().isNewUser()) {
                        Log.d(TAG, "onSuccess: New user, saving info to database.");
                        updateUserInfoDb();
                    } else {
                        Log.d(TAG, "onSuccess: Existing user, redirecting to MainActivity.");
                        startActivity(new Intent(LoginOptionsActivity.this, MainActivity.class));
                        finishAffinity();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "firebaseAuthWithGoogleAccount: failed", e);
                    MyUtils.toast(LoginOptionsActivity.this, "Authentication failed: " + e.getMessage());
                });
    }

    private void updateUserInfoDb() {
        Log.d(TAG, "updateUserInfoDb: saving user info to Firebase DB");

        progressDialog.setMessage("Saving User Info...");
        progressDialog.show();

        long timestamp = MyUtils.timestamp();
        String uid = firebaseAuth.getUid();
        String email = firebaseAuth.getCurrentUser() != null ? firebaseAuth.getCurrentUser().getEmail() : "";
        String name = firebaseAuth.getCurrentUser() != null ? firebaseAuth.getCurrentUser().getDisplayName() : "";

        HashMap<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("email", email);
        userData.put("name", name);
        userData.put("timestamp", timestamp);
        userData.put("phoneCode", "");
        userData.put("phoneNumber", "");
        userData.put("profileImageUrl", "");
        userData.put("dob", "");
        userData.put("userType", MyUtils.USER_TYPE_GOOGLE);
        userData.put("token", "");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .setValue(userData)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Log.d(TAG, "User info saved successfully");
                    startActivity(new Intent(LoginOptionsActivity.this, MainActivity.class));
                    finishAffinity();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e(TAG, "Failed to save user info", e);
                    MyUtils.toast(LoginOptionsActivity.this, "Failed to save: " + e.getMessage());
                });
    }
}
