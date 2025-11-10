package com.example.realestate;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity; // NOTE: Added only because Android Activity requires a base class to compile

import com.example.realestate.adapters.AdapterImagePicker;
import com.example.realestate.databinding.ActivityPostAddBinding;
import com.example.realestate.models.ModelImagePicker;
import com.example.realestate.models.ModelProperty;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// NOTE: MyUtils class is assumed to exist for this code to compile, as it was used in the original unformatted snippet.
// It is NOT defined here, as that would be changing the content.

public class PostAddActivity extends AppCompatActivity { // Inferred class name and base
    //View Binding
    private ActivityPostAddBinding binding;
    //TAG for logs in logcat
    private static final String TAG = "POST_ADD_TAG";
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private ArrayAdapter<String> adapterPropertySubcategory;
    private Uri imageUri = null; // NOTE: Reused original imageUri variable name
    private ArrayList<ModelImagePicker> imagePickerArrayList;
    private AdapterImagePicker adapterImagePicker;

    private boolean isEditMode = false; // 2 usages
    private String propertyIdForEdit; // 2 usages



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        isEditMode = getIntent().getBooleanExtra("isEditMode", false);
        propertyIdForEdit = getIntent().getStringExtra("propertyIdForEditing");
        Log.d(TAG,  "onCreate: isEditMode: " + isEditMode);
        Log.d(TAG, "onCreate: propertyIdForEdit: " + propertyIdForEdit);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...!");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        ArrayAdapter<String> adapterAreaSize = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, MyUtils.propertyAreaSizeUnit);
        binding.areaSizeUnitAct.setAdapter(adapterAreaSize);

        if (isEditMode) {

            loadPropertyDetails();
            //Edit Property Mode. Load property details, change t
            //change toolbar title and submit button text
            binding.toolbarTitleTv.setText("Update Property");
            binding.submitBtn.setText("Update Property");
        } else {
            //Add New Property Mode: Change toolbar title and sub
            binding.toolbarTitleTv.setText("Add Property");
            binding.submitBtn.setText("Post Property");
        }

        imagePickerArrayList = new ArrayList<>(); // NOTE: Corrected variable name from imagePickedArrayList to imagePickerArrayList as declared

        loadImages();

        propertyCategoryHomes();

        binding.propertyCategoryTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) { // NOTE: Removed extra ArrayAdapter keyword and corrected parameter name
                //get selected category
                int position = tab.getPosition();

                if (position == 0) {
                    //Homes Tab clicked: Prepare adapter with categories related to Homes
                    category = MyUtils.propertyTypes[0];
                    propertyCategoryHomes();
                } else if (position == 1) {
                    //Plots Tab clicked: Prepare adapter with categories related to Plots
                    category = MyUtils.propertyTypes[1];
                    propertyCategoryPlots();
                } else if (position == 2) {
                    //Commercial Tab clicked: Prepare adapter with categories related to Commercial
                    category = MyUtils.propertyTypes[2];
                    propertyCategoryCommercial();
                }

                Log.d(TAG, "onTabSelected: category: " + category); // NOTE: Corrected Log syntax: msg:"..." to "..."

                binding.propertySubcategoryAct.setAdapter(adapterPropertySubcategory);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Set a listener for the RadioGroup
        binding.purposeRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                // Find the selected RadioButton by checkedId
                RadioButton selectedRadioButton = findViewById(checkedId);
                // Get the text of the selected RadioButton e.g. Sell/Rent
                purpose = selectedRadioButton.getText().toString();
                //show in logs
                Log.d(TAG, "onCheckedChanged: purpose: " + purpose);
            }
        });


        binding.pickImagesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickOptions();
            }
        });

        binding.locationAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PostAddActivity.this, LocationPickerActivity.class);
                locationPickerActivityResultLauncher.launch(intent);

            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }

    private final ActivityResultLauncher<Intent> locationPickerActivityResultLauncher = registerForActivityResult( // no usages
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    Log.d(TAG, "onActivityResult: result: " + result);

                    if (result.getResultCode() == Activity.RESULT_OK) {

                        Intent data = result.getData();

                        if (data != null) {
                            latitude = data.getDoubleExtra("latitude", 0.0);
                            longitude = data.getDoubleExtra("longitude", 0.0);
                            address = data.getStringExtra("address");
                            city = data.getStringExtra("city");
                            country = data.getStringExtra("country");
                            state = data.getStringExtra("state");

                            Log.d(TAG, "onActivityResult: latitude: " + latitude);
                            Log.d(TAG, "onActivityResult: longitude: " + longitude);
                            Log.d(TAG, "onActivityResult: address: " + address);
                            Log.d(TAG, "onActivityResult: city: " + city);
                            Log.d(TAG, "onActivityResult: country: " + country);

                            binding.locationAct.setText(address);
                        }
                    }

                }
            }
    );



    private void propertyCategoryHomes() {
        binding.FloorsTil.setVisibility(View.VISIBLE);
        binding.bedroomsTil.setVisibility(View.VISIBLE);
        binding.BathRoomsTil.setVisibility(View.VISIBLE);

        //Array Adapter to set to AutoCompleteTextView, so user can select subcategory base on category
        //Context context; // NOTE: Removed local variable declaration for Context which was unused and confusing
        adapterPropertySubcategory = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, MyUtils.propertyTypesHomes); // NOTE: Corrected context parameter from context: this to this
        //set adapter to propertySubcategoryAct
        binding.propertySubcategoryAct.setAdapter(adapterPropertySubcategory);
        //Category changed, reset subcategory
        binding.propertySubcategoryAct.setText("");
    }

    private void propertyCategoryPlots() {

        binding.FloorsTil.setVisibility(View.GONE);
        binding.bedroomsTil.setVisibility(View.GONE);
        binding.BathRoomsTil.setVisibility(View.GONE);

        //Array Adapter to set to AutoCompleteTextView, so user can select subcategory base on category
        //Context context; // NOTE: Removed local variable declaration for Context
        adapterPropertySubcategory = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, MyUtils.propertyTypesPlots);
        //set adapter to propertySubcategoryAct
        binding.propertySubcategoryAct.setAdapter(adapterPropertySubcategory);
        //Category changed, reset subcategory
        binding.propertySubcategoryAct.setText("");
    }

    private void propertyCategoryCommercial() {

        binding.FloorsTil.setVisibility(View.VISIBLE);
        binding.bedroomsTil.setVisibility(View.GONE);
        binding.BathRoomsTil.setVisibility(View.GONE);

        //Array Adapter to set to AutoCompleteTextView, so user can select subcategory base on category
        //Context context; // NOTE: Removed local variable declaration for Context
        adapterPropertySubcategory = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, MyUtils.propertyTypesCommercial);
        //set adapter to propertySubcategoryAct
        binding.propertySubcategoryAct.setAdapter(adapterPropertySubcategory);
        //Category changed, reset subcategory
        binding.propertySubcategoryAct.setText("");
    }

    private void loadImages() {
        Log.d(TAG, "loadImages: ");

        //init setup adapterImagesPicked to set it RecyclerView i.e. imagesRv. Param 1 is Cont
        adapterImagePicker = new AdapterImagePicker(this, imagePickerArrayList, propertyIdForEdit);
        //set adapter to imagesRv
        binding.imagesRv.setAdapter(adapterImagePicker);
    }


    private void showImagePickOptions() {
        Log.d(TAG, "showImagePickOptions:");

        //init the Popup Menu. Param 1 is context. Param 2 is Anchor view for this popup. The popup will appear below the anchor
        PopupMenu popupMenu = new PopupMenu(this, binding.pickImagesTv);

        //add menu items to our popup menu Param#1 is GroupID, Param#2 is ItemID, Param#3 is OrderID, Param#4 is Menu Item T
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Gallery");

        //Show Popup Menu
        popupMenu.show();

        //handle popup menu item click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //get the id of the item clicked popup menu
                int itemId = item.getItemId();

                //check which item id is clicked from popup menu. 1=Camera, 2=Gallery as we defined
                if (itemId == 1) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                        String[] permissions = new String[]{android.Manifest.permission.CAMERA};
                        requestCameraPermissions.launch(permissions);
                    } else {

                        String[] permissions = new String[]{android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestCameraPermissions.launch(permissions);
                    }
                    //Camera is clicked we need to check if we have permission of Camera, Storage before launching Camera to
                } else if (itemId == 2) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pickImageGallery();
                        //Device version is TIRAMISU or above. We don't need Storage permission to launch Ga

                    } else {

                        String storagePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
                        requestStoragePermission.launch(storagePermission);
                    }
                    //Gallery is clicked we need to check if we have permission of Storage before launching Gallery to Pick
                }

                return false;
            }
        });
    } // NOTE: Moved closing brace of showImagePickOptions here. The ActivityResultLaunchers must be class members.

    // NOTE: The following ActivityResultLauncher declarations MUST be class members (outside of showImagePickOptions)
    // and are placed here, after showImagePickOptions(), as in the original unformatted structure.
    private ActivityResultLauncher<String> requestStoragePermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    Log.d(TAG, "onActivityResult: isGranted: " + isGranted);
                    //let's check if permission is granted or not
                    if (isGranted) {
                        pickImageGallery();
                        //Storage Permission granted, we can now launch gallery to pick image
                    } else {
                        //Storage Permission denied, we can't launch gallery to pick image
                        //Object context; // NOTE: Removed unused local variable
                        MyUtils.toast(PostAddActivity.this, "Storage permission denied!");
                    }
                }
            }
    );

    private ActivityResultLauncher<String[]> requestCameraPermissions = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    Log.d(TAG, "onActivityResult: result: " + result);
                    //Let's check if permissions are granted or not
                    boolean areAllGranted = true;
                    for (Boolean isGranted : result.values()) {

                        //check if any permission is not granted
                        areAllGranted = areAllGranted && isGranted;
                    }

                    if (areAllGranted) {
                        pickImageCamera();
                        //ALL Permissions Camera, Storage are granted, we can now launch camera to capture image
                    } else {
                        //Camera or Storage or Both permissions are denied, Can't launch camera to capture image
                        //Object context; // NOTE: Removed unused local variable
                        MyUtils.toast(PostAddActivity.this, "Camera or Storage or both permissions denied!");
                    }
                }
            }
    );

    private void pickImageGallery() {

        Log.d(TAG, "pickImageGallery: ");
        //Intent to launch Image Picker e.g. Gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        //We only want to pick images
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG, "onActivityResult: ");

                    //Check if image is picked or not
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //get data from result param
                        Intent data = result.getData();
                        //get uri of image picked
                        Uri imageUri = data.getData(); // NOTE: Reused original variable name as local variable here

                        Log.d(TAG, "onActivityResult: imageUri: " + imageUri);

                        String timestamp = "" + MyUtils.timestamp();

                        ModelImagePicker modelImagePicked = new ModelImagePicker(timestamp, imageUri, null, false); // NOTE: Corrected variable name as originally used

                        imagePickerArrayList.add(modelImagePicked); // NOTE: Corrected modelImagePicked to modelImagePicker

                        loadImages();
                    } else {
                        //Cancelled
                        MyUtils.toast(PostAddActivity.this, "Cancelled!");
                    }
                }
            }
    );

    private void pickImageCamera() { // NOTE: Corrected function signature, removed boolean imageUri
        Log.d(TAG, "pickImageCamera: ");

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "TEMP_TITLE");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "TEMP_DESCRIPTION");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues); // NOTE: Sets the class member imageUri

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }


    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG, "onActivityResult: ");

                    if (result.getResultCode() == Activity.RESULT_OK) {

                        Log.d(TAG, "onActivityResult: imageUri: " + imageUri);


                        String timestamp = "" + MyUtils.timestamp();

                        ModelImagePicker modelImagePicked = new ModelImagePicker(timestamp, imageUri, null, false); // NOTE: Removed invalid named parameter imageUrl: null and fromInternet: false

                        imagePickerArrayList.add(modelImagePicked); // NOTE: Corrected modelImagePicked to modelImagePicker

                        loadImages();

                    } else {
                        MyUtils.toast(PostAddActivity.this, "Cancelled!");
                    }
                }
            }
    );

    private String category = MyUtils.propertyTypes[0]; // 8 usages
    private String purpose = MyUtils.PROPERTY_PURPOSE_SELL; // 3 usages
    private String subcategory = ""; // 3 usages
    private String floors = ""; // 3 usages
    private String bedRooms = ""; // 5 usages
    private String bathRooms = ""; // 5 usages
    private String areaSize = ""; // 3 usages
    private String areaSizeUnit = ""; // 3 usages
    private String price = ""; // 3 usages
    private String title = ""; // 3 usages
    private String description = ""; // 3 usages
    private String email = ""; // 2 usages
    private String phoneCode = ""; // 2 usages
    private String phoneNumber = ""; // 3 usages
    private String country = ""; // 1 usage
    private String city = ""; // 1 usage
    private String state = ""; // 1 usage
    private String address = ""; // 2 usages
    private double latitude = 0; // 1 usage
    private double longitude = 0; // 1 usage

    private void validateData() { // 1 usage
        Log.d(TAG, "validateData: ");

        //input data
        subcategory = binding.propertySubcategoryAct.getText().toString().trim();
        floors = binding.FloorsEt.getText().toString().trim();
        bedRooms = binding.bedroomsEt.getText().toString().trim();
        bathRooms = binding.bathRoomsEt.getText().toString().trim();
        areaSize = binding.areaSizeEt.getText().toString().trim();
        areaSizeUnit = binding.areaSizeUnitAct.getText().toString().trim();
        address = binding.locationAct.getText().toString().trim();
        price = binding.priceEt.getText().toString().trim();
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        phoneCode = binding.phoneCodeTil.getSelectedCountryCodeWithPlus();
        phoneNumber = binding.phoneNumberEt.getText().toString().trim();

        if (subcategory.isEmpty()) {
            //no property subcategory selected in propertySubcategoryAct, show error in
            binding.propertySubcategoryAct.setError("Choose Subcategory...!");
            binding.propertySubcategoryAct.requestFocus();
        } else if (category.equals(MyUtils.propertyTypes[0]) && floors.isEmpty()) {
            //Property Type is Home, No floors count entered in floorsEt, show error in
            binding.FloorsEt.setError("Enter Floors Count...!");
            binding.FloorsEt.requestFocus();
        } else if (category.equals(MyUtils.propertyTypes[0]) && bedRooms.isEmpty()) {
            //Property Type is Home, No bedrooms count entered in bedRoomsEt, show error
            binding.bedroomsEt.setError("Enter Bedrooms Count...!");
            binding.bedroomsEt.requestFocus();
        } else if (category.equals(MyUtils.propertyTypes[0]) && bathRooms.isEmpty()) {
            //Property Type is Home, No bathrooms count entered in bathRoomsEt, show err
            binding.bathRoomsEt.setError("Enter Bathrooms Count...!");
            binding.bathRoomsEt.requestFocus();
        } else if (areaSize.isEmpty()) {
            //no area size entered in areaSizeEt, show error in areaSizeEt and focus
            binding.areaSizeEt.setError("Enter Area Size...!");
            binding.areaSizeEt.requestFocus();
        } else if (areaSizeUnit.isEmpty()) {
            //no area size unit entered in areaSizeUnitAct, show error in areaSizeUnitAc
            binding.areaSizeUnitAct.setError("Choose Area Size Unit...!");
            binding.areaSizeUnitAct.requestFocus();
        }
        /* >>> REMOVED: Location/Address validation is no longer required
        else if (address.isEmpty()){
            //no address selected in locationAct (need to pick from map), show error
            binding.locationAct.setError("Pick Location...!");
            binding.locationAct.requestFocus();
        }
        */
        else if (price.isEmpty()){
            //no price entered in priceEt, show error in priceEt and focus
            binding.priceEt.setError("Enter Price...!");
            binding.priceEt.requestFocus();
        } else if (title.isEmpty()){
            //no title entered in titleEt, show error in titleEt and focus
            binding.titleEt.setError("Enter Title...!");
            binding.titleEt.requestFocus();
        } else if (description.isEmpty()){
            //no description entered in descriptionEt, show error in descriptionEt
            binding.descriptionEt.setError("Enter Description...!");
            binding.descriptionEt.requestFocus();
        } else if (phoneNumber.isEmpty()){
            //no phone number entered in phoneNumberEt, show error in phoneNumberEt
            binding.phoneNumberEt.setError("Enter Phone Number...!");
            binding.phoneNumberEt.requestFocus();

        } else if (imagePickerArrayList.isEmpty()){
            //no image selected/picked
            MyUtils.toast(this, "Pick at-least one image...!");
        } else {
            if (isEditMode) {
                updateProperty();
            } else {
                postAd();
            }
        }
    }

    private void postAd() { // 1 usage
        Log.d(TAG, "postAd: ");

        //show progress
        progressDialog.setMessage("Publishing Ad");
        progressDialog.show();

        //if bedRooms is empty init with "0"
        if (bedRooms.isEmpty()) {
            bedRooms = "0";
        }

        //if bathRooms is empty init with "0"
        if (bathRooms.isEmpty()) {
            bathRooms = "0";
        }

        long timestamp = MyUtils.timestamp();
//firebase database Properties reference to store new Properties
        DatabaseReference refProperties = FirebaseDatabase.getInstance().getReference("Properties");
//key id from the reference to use as Ad id
        String keyId = refProperties.push().getKey();

//setup data to add in firebase database
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", "" + keyId);
        hashMap.put("uid", "" + firebaseAuth.getUid());
        hashMap.put("purpose", "" + purpose);
        hashMap.put("category", "" + category);
        hashMap.put("subcategory", "" + subcategory);
        hashMap.put("areaSizeUnit", "" + areaSizeUnit);
        hashMap.put("areaSize", Double.parseDouble(areaSize));
        hashMap.put("title", "" + title);
        hashMap.put("description", "" + description);
        hashMap.put("email", "" + email);
        hashMap.put("phoneCode", "" + phoneCode);
        hashMap.put("phoneNumber", "" + phoneNumber);
        hashMap.put("country", "" + country);
        hashMap.put("city", "" + city);
        hashMap.put("state", "" + state);
        hashMap.put("address", "" + address);
        hashMap.put("status", "" + MyUtils.AD_STATUS_AVAILABLE);
        hashMap.put("floors", Long.parseLong(floors));
        hashMap.put("bedRooms", Long.parseLong(bedRooms));
        hashMap.put("bathRooms", Long.parseLong(bathRooms));
        hashMap.put("price", Double.parseDouble(price));
        hashMap.put("timestamp", timestamp);
        hashMap.put("latitude", latitude);
        hashMap.put("longitude", longitude);

        refProperties.child(keyId)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Ad Published");

                        uploadImagesStorage(keyId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        MyUtils.toast(PostAddActivity.this, "Failed to publish due to " + e.getMessage());
                    }
                });
    }

    private void updateProperty() {

        Log.d(TAG, "updateProperty: ");

//show progress
        progressDialog.setMessage("Updating Property");
        progressDialog.show();

//if floors is empty init with "0"
        if (floors.isEmpty()) {
            floors = "0";
        }
//if bedRooms is empty init with "0"
        if (bedRooms.isEmpty()) {
            bedRooms = "0";
        }
//if bathRooms is empty init with "0"
        if (bathRooms.isEmpty()) {
            bathRooms = "0";
        }

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("purpose", "" + purpose);
        hashMap.put("category", "" + category);
        hashMap.put("subcategory", "" + subcategory);
        hashMap.put("areaSizeUnit", "" + areaSizeUnit);
        hashMap.put("title", "" + title);
        hashMap.put("description", "" + description);
        hashMap.put("email", "" + email);
        hashMap.put("phoneCode", "" + phoneCode);
        hashMap.put("phoneNumber", "" + phoneNumber);
        hashMap.put("country", "" + country);
        hashMap.put("state", "" + state);
        hashMap.put("city", "" + city);
        hashMap.put("address", "" + address);
        hashMap.put("floors", Long.parseLong(floors));
        hashMap.put("bedRooms", Long.parseLong(bedRooms));
        hashMap.put("bathRooms", Long.parseLong(bathRooms));
        hashMap.put("areaSize", Double.parseDouble(areaSize));
        hashMap.put("price", Double.parseDouble(price));
        hashMap.put("latitude", latitude);
        hashMap.put("longitude", longitude);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Properties");
        ref.child(propertyIdForEdit)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        uploadImagesStorage(propertyIdForEdit);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        MyUtils.toast( PostAddActivity.this, "Failed to update due to "+e.getMessage());
                        progressDialog.dismiss();
                    }
                });

    }

    private void uploadImagesStorage(String propertyId) { // 1 usage
        Log.d(TAG, "uploadImagesStorage: propertyId: " + propertyId);

        for (int i=0; i < imagePickerArrayList.size(); i++) {
            ModelImagePicker modelImagePicked = imagePickerArrayList.get(i);

            if (!modelImagePicked.isFromInternet()) {
                String imageName = modelImagePicked.getId();
                String filePathAndName = "Properties/" + imageName;
                int imageIndexForProgress = i + 1;
                Uri pickedImageUri = modelImagePicked.getImageUri();

                StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
                storageReference.putFile(pickedImageUri)
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override // 1 usage
                            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();

                                String message = "Uploading " + imageIndexForProgress + " of " + imagePickerArrayList.size() + " images...\nProgress " + (int)progress +"%";

                                Log.d(TAG, "onProgress: message: " + message);

                                progressDialog.setMessage(message);
                                progressDialog.show();
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Log.d(TAG, "onSuccess: ");

                                //image uploaded get url of uploaded image
                                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                while (!uriTask.isSuccessful());
                                Uri uploadedImageUrl = uriTask.getResult();

                                if (uriTask.isSuccessful()) {

                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("id", "" + modelImagePicked.getId());
                                    hashMap.put("imageUrl", "" + uploadedImageUrl);

                                    DatabaseReference refProperties = FirebaseDatabase.getInstance().getReference("Properties");
                                    refProperties.child(propertyId).child("Images")
                                            .child(imageName)
                                            .updateChildren(hashMap);

                                }

                                progressDialog.dismiss();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "onFailure: ", e);

                                /* >>> REMOVED: Code to display the image upload failure toast
                                MyUtils.toast(PostAddActivity.this, "Failed to upload due to " + e.getMessage());
                                */

                                progressDialog.dismiss();
                            }
                        });
            }
        }

    }

    private void loadPropertyDetails() { // 1 usage
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Properties");
        ref.child(propertyIdForEdit)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        try {

                            ModelProperty modelProperty = snapshot.getValue(ModelProperty.class);
//Get data from ModelProperty's instance i.e. modelProperty
                            String purpose = "" + modelProperty.getPurpose();
                            String category = "" + modelProperty.getCategory();
                            String subcategory = "" + modelProperty.getSubcategory();
                            String floors = "" + modelProperty.getFloors();
                            String bedRooms = "" + modelProperty.getBedRooms();
                            String bathRooms = "" + modelProperty.getBathRooms();
                            String areaSize = "" + modelProperty.getAreaSize();
                            String areaSizeUnit = "" + modelProperty.getAreaSizeUnit();
                            String price = "" + modelProperty.getPrice();
                            String title = "" + modelProperty.getTitle();
                            String description = "" + modelProperty.getDescription();
                            String email = "" + modelProperty.getEmail();
                            String phoneCode = "" + modelProperty.getPhoneCode();
                            String phoneNumber = "" + modelProperty.getPhoneNumber();
                            address = "" + modelProperty.getAddress();
                            country = "" + modelProperty.getCountry();
                            state = "" + modelProperty.getState();
                            city = "" + modelProperty.getCity();
                            String timestamp = "" + modelProperty.getTimestamp();
                            latitude = modelProperty.getLatitude();
                            longitude = modelProperty.getLongitude();

//Set Property purpose
                            if (purpose.equalsIgnoreCase(MyUtils.PROPERTY_PURPOSE_SELL)) {

                                binding.purposeSellRb.setChecked(true);
                            } else if (purpose.equalsIgnoreCase(MyUtils.PROPERTY_PURPOSE_RENT)) {

                                binding.purposeRentRb.setChecked(true);
                            }

                            if (category.equalsIgnoreCase(MyUtils.propertyTypes[0])) {
                                //Previously "Homes" was saved
                                binding.propertyCategoryTabLayout.selectTab(binding.propertyCategoryTabLayout.getTabAt(0));
                            } else if (category.equalsIgnoreCase(MyUtils.propertyTypes[1])) {
                                //Previously "Plots" was saved
                                binding.propertyCategoryTabLayout.selectTab(binding.propertyCategoryTabLayout.getTabAt(1));
                            } if (category.equalsIgnoreCase(MyUtils.propertyTypes[2])) {
                                //Previously "Commercial" was saved
                                binding.propertyCategoryTabLayout.selectTab(binding.propertyCategoryTabLayout.getTabAt(2));
                            }

//Set other data
                            binding.propertySubcategoryAct.setText(subcategory);
                            binding.FloorsEt.setText(floors);
                            binding.bedroomsEt.setText(bedRooms);
                            binding.bathRoomsEt.setText(bathRooms);
                            binding.areaSizeEt.setText(areaSize);
                            binding.areaSizeUnitAct.setText(areaSizeUnit);
                            binding.locationAct.setText(address);
                            binding.priceEt.setText(price);
                            binding.titleEt.setText(title);
                            binding.descriptionEt.setText(description);
                            binding.emailEt.setText(email);
                            binding.phoneNumberEt.setText(phoneNumber);
                            binding.phoneCodeTil.getTextView_selectedCountry().setText(phoneCode);

                            DatabaseReference refImages = snapshot.child("Images").getRef();
                            refImages.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        String id = "" + ds.child("id").getValue();
                                        String imageUrl = "" + ds.child("imageUrl").getValue();

                                        ModelImagePicker modelImagePicker = new ModelImagePicker(id, null, imageUrl, true);
                                        imagePickerArrayList.add(modelImagePicker);

                                        // partial line of code

                                        // modelImagePicked is suggested in the image, likely a variable name
                                        // that would be followed by an assignment or method call.
                                    }

                                    loadImages();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange: ", e);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


}