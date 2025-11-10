package com.example.realestate;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.realestate.databinding.ActivityDeleteProfileBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DeleteProfileActivity extends AppCompatActivity {


    //View Binding
    private ActivityDeleteProfileBinding binding;
    //TAG for logs in logcat
    private static final String TAG = "DELETE_ACCOUNT_TAG";
    //ProgressDialog to show while deleting account
    private ProgressDialog progressDialog;
    //FirebaseAuth for auth related tasks
    private FirebaseAuth firebaseAuth;

    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //init/setup ProgressDialog to show while deleting account
        progressDialog = new ProgressDialog( this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

//get instance of FirebaseAuth for Auth related tasks
        firebaseAuth = FirebaseAuth.getInstance();
//get instance of FirebaseUser to get current user and delete
        firebaseUser = firebaseAuth.getCurrentUser();

//handle toolbarBackBtn click, go-back
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

//handle submitBtn click, start account deletion
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUserData();
            }
        });
    }
    private void deleteUserData(){
        Log.d(TAG, "deleteUserData: Deleting user data...");
        progressDialog.setMessage("Deleting user data");
        progressDialog.show();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: User data deleted...");
                        deleteUserProperties();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        MyUtils.toast( DeleteProfileActivity.this,  "Failed to delete user data due to " + e.getMessage());
                    }
                });
    }
    private void deleteUserProperties(){
        Log.d(TAG, "deleteUserProperties: Deleting user properties...");
        progressDialog.setMessage("Deleting user properties");
        progressDialog.show();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Properties");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!snapshot.exists()) {
                            Log.d(TAG, "onDataChange: No properties by this user");
                            deleteAccount();
                            return;
                        }

                        final int total = (int) snapshot.getChildrenCount();
                        final int[] deletedCount = {0};

                        for (DataSnapshot ds: snapshot.getChildren()) {
                            ds.getRef().removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            deletedCount[0]++;
                                            Log.d(TAG, "onSuccess: Property deleted: " + deletedCount[0] + "/" + total);
                                            if (deletedCount[0] == total) {
                                                Log.d(TAG, "onSuccess: All properties deleted");
                                                deleteAccount();
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e(TAG, "onFailure: Failed to delete property ", e);
                                        }
                                    });
                        }

                    } // End of onDataChange method

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void deleteAccount() {
        Log.d(TAG, "deleteAccount: Deleting user account...");

        progressDialog.setMessage("Deleting user account");
        progressDialog.show();

        firebaseUser.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Deleted user account");
                        progressDialog.dismiss();
                        startMainActivity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        MyUtils.toast( DeleteProfileActivity.this,"Failed to delete account due to " + e.getMessage());
                    }
                });
    }
    private void startMainActivity() {
        firebaseAuth.signOut();

        startActivity(new Intent( this, MainActivity.class));
        finishAffinity();
    }

    @Override
    public void onBackPressed() {
        startMainActivity();
    }
}
