package com.example.realestate;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.realestate.databinding.ActivityForgotPasswordBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;

    //TAG for logs in logcat
    private static final String TAG = "FORGOT_PASSWORD_TAG";

    private ProgressDialog progressDialog;

    //Firebase Auth for auth related tasks
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init view binding... activity_forgot_password.xml = ActivityForgotPasswordBinding
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //handle submitBtn click, validate data to start password recovery
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

    }

    private String email = "";

    private void validateData() {
        //input data i.e. email
        email = binding.emailEt.getText().toString().trim();

        Log.d(TAG, "validateData: Email: " + email);

        //validate data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            //invalid email pattern, show error in emailEt
            binding.emailEt.setError("Invalid Email Pattern");
            binding.emailEt.requestFocus();
        } else {
            //valid email pattern, start sending recovery email instructions
            sendPasswordRecoveryInstructions();
        }
    }

    private void sendPasswordRecoveryInstructions() {
        Log.d(TAG, "sendPasswordRecoveryInstructions: ");

        //show progress
        progressDialog.setMessage("Sending password recovery instructions to " + email);
        progressDialog.show();

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //instructions sent, check email, sometimes it goes in spam folder so if not in inbox check your spam folder
                        Log.d(TAG, "onSuccess: Instructions Sent");
                        progressDialog.dismiss();
                        MyUtils.toast(ForgotPasswordActivity.this, "Instructions to reset password sent to " + email);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to send instructions, show exception in logcat, show exception message to user using Toast
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        MyUtils.toast(ForgotPasswordActivity.this, "Failed to send due to " + e.getMessage());
                    }
                });
    }


}
