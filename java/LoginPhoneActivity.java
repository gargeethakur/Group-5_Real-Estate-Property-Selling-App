package com.example.realestate;

import android.app.ProgressDialog;
import android.content.Context;
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

import com.example.realestate.databinding.ActivityLoginPhoneBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class LoginPhoneActivity extends AppCompatActivity {

    private ActivityLoginPhoneBinding binding;

    // no usages
    private static final String TAG = "LOGIN_PHONE_TAG";

    // 1 usage
    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;

    // no usages
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    // no usages
    private String mVerificationId;

    private PhoneAuthProvider.ForceResendingToken forceResendingToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginPhoneBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Context context;
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        // for the start show phone input ui and hide otp ui
        binding.phoneInputRL.setVisibility(View.VISIBLE);
        binding.otpInputRL.setVisibility(View.GONE);
        //Firebase Auth for auth related tasks
        firebaseAuth = FirebaseAuth.getInstance();

        phoneLoginCallBack();

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        binding.sendOtpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
        binding.resendOtpTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resendVerificationCode(forceResendingToken);
            }
        });

        binding.verifyOtpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String otp = binding.otpET.getText().toString().trim();

                if (otp.isEmpty()) {
                    binding.otpET.setError("Enter OTP");
                    binding.otpET.requestFocus();
                } else if (otp.length() < 6) {
                    binding.otpET.setError("OTP length must be 6 characters");
                    binding.otpET.requestFocus();
                } else {
                    verifyPhoneNumberWithCode(otp);
                }
            }
        });


    }

    //handle toolbarBackBtn click, go-back
    private String phoneCode = "", phoneNumber = "", phoneNumberWithCode = "";

    // 1 usage
    private void validateData() {
        phoneCode = binding.phoneCodeTil.getSelectedCountryCodeWithPlus();
        phoneNumber = binding.phoneNumberEt.getText().toString().trim();
        phoneNumberWithCode = phoneCode + phoneNumber;

        Log.d(TAG, "msg: validateData: Phone Code: " + phoneCode);
        Log.d(TAG, "msg: validateData: Phone Number: " + phoneNumber);
        Log.d(TAG, "msg: validateData: Phone Number With Code: " + phoneNumberWithCode);
        //validate data
        if (phoneNumber.isEmpty()) {
            //Phone Number is not entered, show error
            binding.phoneNumberEt.setError("Enter Phone Number");
            binding.phoneNumberEt.requestFocus();
        } else {
            startPhoneNumberVerification();
        }
    }
    // 1 usage
    private void startPhoneNumberVerification(){


        //show progress
        progressDialog.setMessage("Sending OTP to " + phoneNumberWithCode);
        progressDialog.show();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phoneNumberWithCode)
                        .setTimeout( 60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void resendVerificationCode(PhoneAuthProvider.ForceResendingToken token){
        progressDialog.setMessage("Resending OTP to " + phoneNumberWithCode);
        progressDialog.show();
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phoneNumberWithCode)
                        .setTimeout( 60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .setForceResendingToken(token)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyPhoneNumberWithCode(String otp){
        Log.d(TAG, "verifyPhoneNumberWithCode: OTP: " + otp);

        progressDialog.setMessage("Verifying OTP...");
        progressDialog.show();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
        signInWithPhoneAuthCredential(credential);
    }

    private void phoneLoginCallBack() {
        Log.d(TAG, "msg: phoneLoginCallBack: ");

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verificationId, forceResendingToken);
                Log.d(TAG,"onCodeSent:");

                mVerificationId= verificationId;
                forceResendingToken= token;

                progressDialog.dismiss();

                // OTP is sent so hide phone ui and show otp ui
                binding.phoneInputRL.setVisibility(View.GONE);

                binding.otpInputRL.setVisibility(View.VISIBLE);

// Show toast for success sending OTP
                MyUtils.toast(LoginPhoneActivity.this, "OTP sent to " + phoneNumberWithCode);

// show user a message that Please type the verification code sent to the phone number user has input
                binding.loginPhoneLabel.setText("Please type verification code sent to " + phoneNumberWithCode);
            }
            // no usages
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                Log.d(TAG,"OnVerificationCompleted");
                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            // no usages
            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.d(TAG,"OnVerificationFailed");

                // for instance if the the phone number format is not valid.

                progressDialog.dismiss();

                MyUtils.toast(LoginPhoneActivity.this, "Failed to verify due to "+e.getMessage());
            }

        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential){

        Log.d(TAG,"signInWirhPhoneAuthCredential");
        //show progress
        progressDialog.setMessage("Logging In...");
        progressDialog.show();

//Sign in to firebase auth using Phone Credentials
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "msg: onSuccess: ");
                        //SignIn Success, let's check if the user is new (New Account Register) or existing (Existing Login)
                        if (authResult.getAdditionalUserInfo().isNewUser()) {
                            Log.d(TAG,"OnSuccess:New User,Account created");
                            updateUserInfo();
                        } else {
                            Log.d(TAG,"OnSuccess:Existing User,Logged In");
                            Intent packageContext;
                            startActivity(new Intent(LoginPhoneActivity.this, MainActivity.class));
                            finishAffinity();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //SignIn failed, show exception message
                        Log.e(TAG, "msg: onFailure: ", e);
                        progressDialog.dismiss();
                        Object context;
                        MyUtils.toast(LoginPhoneActivity.this, "Login Failed due to "+e.getMessage());
                    }
                });

    }
    private void updateUserInfo(){
        Log.d(TAG, "msg: updateUserinfo: ");

        progressDialog.setMessage("Saving User Info...");
        progressDialog.show();

        long timestamp = MyUtils.timestamp();
        String registeredUserUid = firebaseAuth.getUid();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", registeredUserUid);
        hashMap.put("email", "");
        hashMap.put("name", "");
        hashMap.put("timestamp", timestamp);
        hashMap.put("phoneCode", ""+phoneCode);
        hashMap.put("phoneNumber", ""+phoneNumber);
        hashMap.put("profileImageUrl", "");
        hashMap.put("dob","");
        hashMap.put("userType", ""+ MyUtils.USER_TYPE_PHONE);
        hashMap.put("token", "");


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(registeredUserUid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "msg: onSuccess: User info saved...");
                        progressDialog.dismiss();

                        Intent packageContext;
                        startActivity(new Intent(LoginPhoneActivity.this, MainActivity.class));
                        finishAffinity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "msg: onFailure: ", e);
                        progressDialog.dismiss();
                        MyUtils.toast(LoginPhoneActivity.this, "Failed to save due to "+e.getMessage());
                    }
                });
    }
}
