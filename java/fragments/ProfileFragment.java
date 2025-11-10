package com.example.realestate.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.realestate.ChangePasswordActivity;
import com.example.realestate.DeleteProfileActivity;
import com.example.realestate.MainActivity;
import com.example.realestate.MyPropertyListActivity;
import com.example.realestate.MyUtils;
import com.example.realestate.PostAddActivity;
import com.example.realestate.ProfileEditActivity;
import com.example.realestate.R;
import com.example.realestate.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser; // Import needed
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;


    private static final String TAG = "PROFILE_TAG";


    private Context mContext;

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;



    @Override
    public void onAttach(@NonNull Context context) {
        mContext = context;
        super.onAttach(context);

    }

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressDialog = new ProgressDialog(mContext);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();



        loadMyInfo();

        binding.postAdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, PostAddActivity.class);
                startActivity(intent);
            }
        });

        binding.logoutCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // logout user
                firebaseAuth.signOut();

                startActivity(new Intent(mContext, MainActivity.class));
                getActivity().finishAffinity();
            }
        });

        binding.myPropertiesCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start MyPropertyListActivity to show Properties of currently logged-
                startActivity(new Intent(mContext, MyPropertyListActivity.class));
            }
        });

        binding.editProfileCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mContext, ProfileEditActivity.class));
            }
        });

        binding.changePasswordCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mContext, ChangePasswordActivity.class));
            }
        });

        // >>> START: Added click listener for Verify Account
        binding.verifyAccountCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmailVerification();
            }
        });
        // >>> END: Added click listener for Verify Account

        binding.deleteAccountCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, DeleteProfileActivity.class));
            }
        });


    }

    // >>> START: Added sendEmailVerification method
    private void sendEmailVerification() {
        Log.d(TAG, "sendEmailVerification: ");

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            progressDialog.setMessage("Sending email verification instructions...");
            progressDialog.show();

            firebaseUser.sendEmailVerification()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d(TAG, "onSuccess: Verification email sent successfully");
                            progressDialog.dismiss();
                            MyUtils.toast(mContext, "Verification instructions sent to your email: " + firebaseUser.getEmail());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "onFailure: Failed to send verification email: ", e);
                            progressDialog.dismiss();
                            MyUtils.toast(mContext, "Failed to send verification email due to: " + e.getMessage());
                        }
                    });
        }
    }
    // >>> END: Added sendEmailVerification method


    private void loadMyInfo() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child("" + firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    // 2 usages
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {



                        String dob = "" + snapshot.child("dob").getValue();
                        String email = "" + snapshot.child("email").getValue();
                        String name = "" + snapshot.child("name").getValue();
                        String phoneCode = "" + snapshot.child("phoneCode").getValue();
                        String phoneNumber = "" + snapshot.child("phoneNumber").getValue();
                        String profileImageUrl = "" + snapshot.child("profileImageUrl").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String userType = "" + snapshot.child("userType").getValue();

                        String phone = phoneCode + phoneNumber;

                        if (timestamp.equals("null")){
                            timestamp = "0";
                        }

                        String formattedDate = MyUtils.formatTimestampDate(Long.parseLong(timestamp));

                        binding.emailTv.setText(email);
                        binding.fullNameTv.setText(name);
                        binding.dobTv.setText(dob);
                        binding.phoneTv.setText(phone);
                        binding.memberSinceTv.setText(formattedDate);

                        if (userType.equals(MyUtils.USER_TYPE_EMAIL)) {

                            // Refresh the user's status before checking verification
                            firebaseAuth.getCurrentUser().reload().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    boolean isVerified = firebaseAuth.getCurrentUser().isEmailVerified();

                                    if (isVerified) {
                                        binding.verifyAccountCv.setVisibility(View.GONE);
                                        binding.verificationTv.setText("Verified");
                                    } else {
                                        binding.verifyAccountCv.setVisibility(View.VISIBLE);
                                        binding.verificationTv.setText("Not Verified");
                                    }
                                } else {
                                    Log.e(TAG, "onDataChange: Failed to reload user for verification status", task.getException());
                                    // Default to unverified if reload fails to avoid false positive
                                    binding.verifyAccountCv.setVisibility(View.VISIBLE);
                                    binding.verificationTv.setText("Not Verified");
                                }
                            });
                        } else {

                            binding.verifyAccountCv.setVisibility(View.GONE);
                            binding.verificationTv.setText("Verified");
                        }

                        try {
                            Glide.with(mContext)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.person_black)
                                    .into(binding.profileIv);
                        } catch (Exception e){
                            Log.e(TAG, "onDataChange: ", e);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {


                        Log.e(TAG, "onCancelled: " + error.getMessage());



                    }
                });
    }
}