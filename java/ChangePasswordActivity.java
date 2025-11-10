package com.example.realestate;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.realestate.databinding.ActivityChangePasswordBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private ActivityChangePasswordBinding binding;

    //TAG for logs in logcat
    private static final String TAG = "CHANGE_PASSWORD_TAG";

    //Firebase Auth for auth related tasks
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init view binding... activity_change_password.xml = ActivityChangePasswordBinding
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        //init/setup ProgressDialog to show while changing password
        Context context;
        progressDialog = new ProgressDialog( this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

//handle toolbarBackBtn click, go-back
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
            }
        });
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }
    private String currentPassword = "";
    private String newPassword = "";
    private String confirmNewPassword = "";

    private void validateData() {
        Log.d(TAG,"validateOn");
        currentPassword = binding.currentPasswordEt.getText().toString().trim();
        newPassword = binding.newPasswordEt.getText().toString().trim();
        confirmNewPassword = binding.confirmNewPasswordEt.getText().toString().trim();

        //validate data
        if (currentPassword.isEmpty()) {
            binding.currentPasswordEt.setError("Enter current password!");
            binding.currentPasswordEt.requestFocus();
        } else if (newPassword.isEmpty()) {
            binding.newPasswordEt.setError("Enter new password!");
            binding.newPasswordEt.requestFocus();
        } else if (confirmNewPassword.isEmpty()) {
            binding.confirmNewPasswordEt.setError("Enter confirm password");
            binding.confirmNewPasswordEt.requestFocus();
        } else if (!newPassword.equals(confirmNewPassword)) {
            binding.confirmNewPasswordEt.setError("Password doesn't match");
            binding.confirmNewPasswordEt.requestFocus();
        }
        else{
            aunthenticateUserForUpdatePassword();
        }
    }

    private void aunthenticateUserForUpdatePassword() {

        Log.d(TAG, "authenticateUserForUpdatePassword: ");

        //show progress
        progressDialog.setMessage("Authenticating User");
        progressDialog.show();

        //before changing password re-authenticate the user to check if the user has entered correct current password
        AuthCredential authCredential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), currentPassword);
        firebaseUser.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.e(TAG,"onSuccess : Authentication Success");
                        updatePassword();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) { //Failed to authenticate user, maybe wrong current password entered
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Object context;
                        MyUtils.toast(ChangePasswordActivity.this, "Failed to authenticate due to "+e.getMessage());
                    }
                });
    }
    private void updatePassword(){
        Log.d(TAG, "updatePassword: ");

//show progress
        progressDialog.setMessage("Updating password");
        progressDialog.show();

        firebaseUser.updatePassword(newPassword)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Password updated");
                        progressDialog.dismiss();
                        Object context;
                        MyUtils.toast(ChangePasswordActivity.this, "Password updated...");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Object context;
                        MyUtils.toast(ChangePasswordActivity.this, "Failed to update due to "+e.getMessage());
                    }
                });
    }

}
