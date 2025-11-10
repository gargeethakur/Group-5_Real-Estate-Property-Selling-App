package com.example.realestate;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog; // Import needed
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker; // Import needed
import android.widget.PopupMenu;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.realestate.databinding.ActivityProfileEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar; // Import needed
import java.util.HashMap;
import java.util.Map;

public class ProfileEditActivity extends AppCompatActivity {

    private ActivityProfileEditBinding binding;

    private static final String TAG = "PROFILE_EDIT_TAG";

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    private String myUserType = "";

    private Uri imageUri = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        loadMyInfo();

        binding.toolbarBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.profileImagePickFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                imagePickDialog();

            }
        });

        // >>> START: Added click listener for DOB EditText to show DatePickerDialog
        binding.dobEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickDialog();
            }
        });
        // >>> END: Added click listener for DOB EditText

        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }

    // >>> START: Added method to show DatePickerDialog
    private void showDatePickDialog() {
        Log.d(TAG, "showDatePickDialog: ");

        // Get current date to set as default in DatePickerDialog
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        Log.d(TAG, "onDateSet: Year: " + year + " Month: " + month + " Day: " + day);

                        // Format the date to dd/MM/yyyy
                        // The month is 0-indexed, so we add 1
                        String dayFormatted = String.valueOf(day);
                        String monthFormatted = String.valueOf(month + 1);

                        // Pad with 0 if day/month is single digit
                        if (day < 10) {
                            dayFormatted = "0" + dayFormatted;
                        }
                        if (month + 1 < 10) {
                            monthFormatted = "0" + monthFormatted;
                        }

                        // Set the selected date to the EditText
                        String selectedDate = dayFormatted + "/" + monthFormatted + "/" + year;
                        binding.dobEt.setText(selectedDate);
                    }
                },
                year,
                month,
                day
        );

        // Optional: Set max date to today so user cannot select future dates
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());

        // Show the dialog
        datePickerDialog.show();
    }
    // >>> END: Added method to show DatePickerDialog


    private String name = "";
// ... rest of the code remains the same ...
// [The rest of the code is omitted for brevity, but should be the original code with the addition in onCreate and the new method]

    private String dob = "";
// ... rest of the code remains the same ...

    private String email = "";
// ... rest of the code remains the same ...

    private String phoneCode = "";
// ... rest of the code remains the same ...

    private String phoneNumber = "";
// ... rest of the code remains the same ...

    private void validateData(){
// ... rest of the code remains the same ...
        name = binding.nameEt.getText().toString().trim();
        dob = binding.dobEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        phoneCode = binding.countryCodePicker.getSelectedCountryCodeWithPlus();
        phoneNumber = binding.phoneNumberEt.getText().toString().trim();

        if (imageUri == null) {
            updateProfileDb(null);

        } else {
            uploadProfileImageStorage();

        }

    }

    private void uploadProfileImageStorage() {
        Log.d(TAG, "uploadProfileImageStorage: ");

//show progress
        progressDialog.setMessage("Uploading user profile image...");
        progressDialog.show();

        String filePathAndName = "UserImages/" + firebaseAuth.getUid();

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        ref.putFile(imageUri)
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    // 1 usage
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        //Progress from 0 to 100
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        Log.d(TAG, "onSuccess: Progress: " + progress);

                        //show progress to progress dialog
                        progressDialog.setMessage("Uploading profile image. Progress: " + (int)progress + "%");
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Log.d(TAG, "onSuccess: Uploaded");

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();

                        while (!uriTask.isSuccessful());

                        String uploadedImageUrl = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()) {
                            updateProfileDb(uploadedImageUrl);
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        MyUtils.toast(ProfileEditActivity.this, "Failed to upload profile image due to " + e.getMessage());

                    }
                });

    }

    private void updateProfileDb(String imageUrl) {
        progressDialog.setMessage("Updating user info...");
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", "" + name);
        hashMap.put("dob", "" + dob);

        if (imageUrl != null) {
            hashMap.put("profileImageUrl", "" + imageUrl);
        }

        if (myUserType.equals(MyUtils.USER_TYPE_EMAIL) || myUserType.equals(MyUtils.USER_TYPE_GOOGLE)) {
            // User type is Google/Email, allow to update phone number not email
            hashMap.put("phoneCode", "" + phoneCode);
            hashMap.put("phoneNumber", "" + phoneNumber);
        } else if (myUserType.equals(MyUtils.USER_TYPE_PHONE)) {
            // User type is Phone, allow to update email, not phone number
            hashMap.put("email", "" + email);
        }

//Database reference of user to update info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child("" + firebaseAuth.getUid())
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Info updated");
                        progressDialog.dismiss();
                        MyUtils.toast(ProfileEditActivity.this, "Profile updated...");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        MyUtils.toast(ProfileEditActivity.this, "Failed to update due to " + e.getMessage());
                    }
                });

    }

    private void imagePickDialog() {
        PopupMenu popupMenu = new PopupMenu(this, binding.profileImagePickFab);

        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Gallery");

        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                int itemId = item.getItemId();

                if (itemId == 1) {
                    Log.d(TAG, "onMenuItemClick: Camera clicked, checking permissions");

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        requestCameraPermissions.launch(new String[]{Manifest.permission.CAMERA});
                    } else {
                        requestCameraPermissions.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
                    }
                } else if (itemId == 2) {

                    Log.d(TAG, "onMenuItemClick: Gallery Clicked...");

                    pickImageGallery();
                }
                return true;
            }
        });
    }

    private final ActivityResultLauncher<String[]> requestCameraPermissions = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    Log.d(TAG, "onActivityResult: " + result.toString());
                    // Check if all permissions are granted
                    boolean areAllGranted = true; // Iterate through the result values to check each permission
                    for (Boolean isGranted : result.values()) {
                        // Update the flag indicating whether all permissions are granted
                        areAllGranted = areAllGranted && isGranted;
                    }

                    if (areAllGranted) {
                        //All Permissions Camera, Storage are granted, we can now launch camera to capture image
                        pickImageCamera();
                    } else {
                        //Camera or Storage or Both permissions are denied, Can't launch camera to capture image
                        Log.d(TAG, "onActivityResult: All or either one permission denied...");
                        MyUtils.toast(ProfileEditActivity.this, "Camera or Storage or both permissions denied...");
                    }
                }
            }
    );

    private void pickImageCamera() {
        Log.d(TAG, "pickImageCamera: ");

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "temp_image");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "temp_image_description");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);

    }

    private final ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "onActivityResult: Image Picked: " + imageUri);

                        try {
                            Glide.with(ProfileEditActivity.this)
                                    .load(imageUri)
                                    .placeholder(R.drawable.person_black)
                                    .into(binding.profileIv);
                        } catch (Exception e) {
                            Log.e(TAG, "onActivityResult: ", e);
                        }
                    } else {
                        Log.d(TAG, "onActivityResult: Cancelled...");
                        MyUtils.toast(ProfileEditActivity.this, "Cancelled...");
                    }

                }
            }
    );

    private void pickImageGallery(){
        Log.d(TAG, "pickImageGallery: ");
        //Intent to launch Image Picker e.g. Gallery
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();

                        imageUri = data.getData();

                        Log.d(TAG, "onActivityResult: Image Picked From Gallery: " + imageUri);

                        try {
                            //set to profileIv
                            try {
                                Glide.with(ProfileEditActivity.this)
                                        .load(imageUri)
                                        .placeholder(R.drawable.person_black)
                                        .into(binding.profileIv);
                            } catch (Exception e) {
                                Log.e(TAG, "onActivityResult: ", e);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "onActivityResult: ", e);
                        }
                    } else {
                        Log.d(TAG, "onActivityResult: Cancelled...");
                        MyUtils.toast(ProfileEditActivity.this, "Cancelled...");
                    }

                }
            }
    );



    private void loadMyInfo() {

        Log.d(TAG, "loadMyInfo: ");

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
                        myUserType = "" + snapshot.child("userType").getValue();

                        String phone = phoneCode + phoneNumber;

                        if (myUserType.equals(MyUtils.USER_TYPE_EMAIL) || myUserType.equals(MyUtils.USER_TYPE_GOOGLE)) {
                            binding.emailTil.setEnabled(false);
                            binding.emailEt.setEnabled(false);
                        } else {
                            binding.phoneNumberTil.setEnabled(false);
                            binding.phoneNumberEt.setEnabled(false);
                            binding.countryCodePicker.setEnabled(false);
                        }

                        binding.emailEt.setText(email);
                        binding.dobEt.setText(dob);
                        binding.nameEt.setText(name);
                        binding.fullNameTv.setText(name);
                        binding.phoneNumberEt.setText(phoneNumber);

                        try {
                            int phoneCodeInt = Integer.parseInt(phoneCode.replace("+", ""));
                            binding.countryCodePicker.setCountryForPhoneCode(phoneCodeInt);
                        } catch (Exception e){
                            Log.e(TAG, "onDataChange: ", e);
                        }

                        try {
                            Glide.with(ProfileEditActivity.this)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.person_black)
                                    .into(binding.profileIv);
                        } catch (Exception e){
                            Log.e(TAG, "onDataChange: ", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}