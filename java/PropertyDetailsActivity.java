package com.example.realestate;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.realestate.adapters.AdapterImageSlider;
import com.example.realestate.databinding.ActivityPropertyDetailsBinding;
import com.example.realestate.models.ModelImageSlider;
import com.example.realestate.models.ModelProperty;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PropertyDetailsActivity extends AppCompatActivity {

    //View Binding
    private ActivityPropertyDetailsBinding binding;
    //TAG for logs in logcat
    private static final String TAG = "PROPERTY_DETAILS_TAG";

    //Firebase Auth for auth related tasks
    private FirebaseAuth firebaseAuth;

    //Property Id, will get from intent
    private String propertyId = "";

    private double propertyLatitude = 0.0;
    private double propertyLongitude = 0.0;

    private String sellerUid = null;
    private String sellerPhone = "";
    private String propertyStatus = "";

    private boolean favorite= false;

    private ArrayList<ModelImageSlider> imageSliderArrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); //init view binding... activity_property_details.xml = ActivityPropertyDetailsBinding
        binding = ActivityPropertyDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


//hide some UI views in start. We will show the Edit, Delete option if the user is
        binding.toolbarEditBtn.setVisibility(View.GONE);
        binding.toolbarDeleteBtn.setVisibility(View.GONE);
        binding.chatBtn.setVisibility(View.GONE);
        binding.callBtn.setVisibility(View.GONE);
        binding.smsBtn.setVisibility(View.GONE);

        //get the id of the property (as we passed in AdapterProperty class while starting this activity)
        propertyId = getIntent().getStringExtra("propertyId");
        Log.d(TAG, "onCreate: propertyId: " + propertyId); //Firebase Auth for auth related tasks

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            checkIsFavorite();
        }

        loadPropertyDetails();
        loadPropertyImages();

        //handle toolbarBackBtn click, go back
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //handle toolbarDeleteBtn click, delete Ad
        binding.toolbarDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder( PropertyDetailsActivity.this);
                materialAlertDialogBuilder.setTitle("Delete Property")
                        .setMessage("Are you sure you want to delete this property?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG,"Delete clicked");
                                deleteProperty();

                            }
                        })
                        .setNegativeButton( "CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
        binding.toolbarFavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (favorite) {

                    MyUtils.removeFromFavorite( PropertyDetailsActivity.this, propertyId);
                }
                else {

                    MyUtils.addToFavorite( PropertyDetailsActivity.this, propertyId);
                }
            }
        });

        //handle toolbarFavBtn click, add/remove favorite
        //handle callBtn click, open Property owner's phone number in dialer
        binding.callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyUtils.callIntent( PropertyDetailsActivity.this, sellerPhone);
            }
        });

        binding.smsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyUtils.smsIntent(PropertyDetailsActivity.this, sellerPhone);
            }
        });

        //handle mapBtn click, open map with Property location
        binding.mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyUtils.mapIntent(PropertyDetailsActivity.this, propertyLatitude, propertyLongitude);
            }
        });

        binding.toolbarEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editOptions();
            }
        });
        binding.sellerProfileCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PropertyDetailsActivity.this, SellerPropertyActivity.class);
                intent.putExtra("sellerUid", sellerUid);
                startActivity(intent);
            }
        });
    }

    private void loadPropertyDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Properties");
        ref.child(propertyId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        try {
                            ModelProperty modelProperty = snapshot.getValue(ModelProperty.class);

                            sellerUid = modelProperty.getUid();
                            double price = modelProperty.getPrice();
                            String priceFormatted = MyUtils.formatCurrency(price);
                            long timestamp = modelProperty.getTimestamp();
                            String purpose = modelProperty.getPurpose();
                            String category = modelProperty.getCategory();
                            propertyStatus = modelProperty.getStatus();
                            String subcategory = modelProperty.getSubcategory();
                            long floors = modelProperty.getFloors();
                            long bedrooms = modelProperty.getBedRooms();
                            long bathrooms = modelProperty.getBathRooms();
                            double areaSize = modelProperty.getAreaSize();
                            String areaSizeUnit = modelProperty.getAreaSizeUnit();
                            String title = modelProperty.getTitle();
                            String description = modelProperty.getDescription();
                            String address = modelProperty.getAddress();

                            propertyLatitude = modelProperty.getLatitude();
                            propertyLongitude = modelProperty.getLongitude();

                            String formattedDate = MyUtils.formatTimestampDate(timestamp);
                            //check if the property is by currently signed in user
                            if (sellerUid.equals(firebaseAuth.getUid())){

                                binding.toolbarEditBtn.setVisibility(View.VISIBLE);
                                binding.toolbarDeleteBtn.setVisibility(View.VISIBLE);

                                binding.chatBtn.setVisibility(View.GONE);
                                binding.callBtn.setVisibility(View.GONE);
                                binding.smsBtn.setVisibility(View.GONE);
                                binding.sellerProfileCV.setVisibility(View.GONE);
                                binding.sellerProfileLabelTv.setVisibility(View.GONE);

                            } else {
                                binding.toolbarEditBtn.setVisibility(View.GONE);
                                binding.toolbarDeleteBtn.setVisibility(View.GONE);
                                binding.chatBtn.setVisibility(View.VISIBLE);
                                binding.callBtn.setVisibility(View.VISIBLE);
                                binding.smsBtn.setVisibility(View.VISIBLE);
                                binding.sellerProfileCV.setVisibility(View.VISIBLE);
                                binding.sellerProfileLabelTv.setVisibility(View.VISIBLE);
                            }

                            //show/hide status Sold
                            if (propertyStatus.equalsIgnoreCase(MyUtils.AD_STATUS_SOLD)){
                                binding.soldCv.setVisibility(View.VISIBLE);
                            } else {
                                binding.soldCv.setVisibility(View.GONE);
                            }

                            //set data to UI Views
                            binding.priceTv.setText(priceFormatted);
                            binding.dateTv.setText(formattedDate);
                            binding.purposeTV.setText(purpose);
                            binding.categoryTv.setText(category);
                            binding.subcategoryTv.setText(subcategory);
                            binding.floorsTv.setText("Floors: " + floors);
                            binding.bedsTv.setText("Bed Rooms: " + bedrooms);
                            binding.bathroomsTv.setText("Bath Rooms: " + bathrooms);
                            binding.areaSizeTv.setText("Area Size: " + areaSize + " " + areaSizeUnit);
                            binding.titleTv.setText(title);
                            binding.descriptionTv.setText(description);
                            binding.addressTv.setText(address);

                            loadSellerDetails();
                        } catch (Exception e) {

                            Log.e(TAG, "onDataChange: ", e);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }
    private void loadSellerDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference( "Users");
        ref.child(sellerUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String phoneCode = "" + snapshot.child("phoneCode").getValue();
                        String phoneNumber = "" + snapshot.child("phoneNumber").getValue();
                        String name = "" + snapshot.child("name").getValue();
                        String profileImageUrl = "" + snapshot.child("profileImageUrl").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        if (timestamp.isEmpty() || timestamp.equals("null")) {
                            timestamp = "0";
                        }

                        String formattedDate = MyUtils.formatTimestampDate(Long.parseLong(timestamp));

                        sellerPhone = phoneCode + phoneNumber;


                        binding.sellerNameTv.setText(name);
                        binding.memberSinceTv.setText(formattedDate);

                        try {
                            Glide.with(PropertyDetailsActivity.this)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.person_black)
                                    .into(binding.sellerProfileIV);
                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange: ", e);
                        }
                    }
                    @Override
                    public void onCancelled (@NonNull DatabaseError error){

                    }
                });

    }
    private void editOptions() { // usage
        //init/setup popup menu, First param is context, Second param is is View below/above which we want to show popup menu
        PopupMenu popupMenu = new PopupMenu( this, binding.toolbarEditBtn);
        //Add menu items to PopupMenu with params Group ID, Item ID, Order, Title
        popupMenu.getMenu().add(Menu.NONE,  0,  0,  "Edit");
        //Add "Mark as Sold" option to popup menu only if the property status is available
        if (propertyStatus.equalsIgnoreCase(MyUtils.AD_STATUS_AVAILABLE)) {
            popupMenu.getMenu().add(Menu.NONE,  1,  1,  "Mark as Sold");
        }

        //show popup menu
        popupMenu.show();

        //handle popup menu item click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                //get id of the menu item clicked
                int itemId = item.getItemId();
                Log.d(TAG,  "onMenuItemClick: itemId: " + itemId);

                if (itemId == 0) {

                    Log.d(TAG, "onMenuItemClick: Edit clicked");
                    Intent intent = new Intent(PropertyDetailsActivity.this, PostAddActivity.class);
                    intent.putExtra("isEditMode", true);
                    intent.putExtra("propertyIdForEditing", propertyId);
                    startActivity(intent);
                } else if (itemId == 1) {

                    Log.d(TAG,  "onMenuItemClick: Mark as sold clicked");
                    showMarkAsSoldDialog();
                }

                return true;
            }
        });
    }
    private void showMarkAsSoldDialog() { // usage
        //Material Alert Dialog - Setup and show
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(PropertyDetailsActivity.this);
        materialAlertDialogBuilder.setTitle("Mark as Sold")
                .setMessage("Are you sure you want to mark this property as Sold?")
                .setPositiveButton( "YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Log.d(TAG,  "onClick: Marking as sold");

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("status", MyUtils.AD_STATUS_SOLD);

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Properties");
                        reference.child(propertyId)
                                .updateChildren(hashMap) // Task<Void>
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                        Log.d(TAG,  "onSuccess: Marked as sold");

                                        MyUtils.toast(PropertyDetailsActivity.this, "Marked as solde"); // Note: "solde" appears to be a typo for "sold" in the source image

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Log.e(TAG, "onFailure: ", e);

                                        MyUtils.toast(PropertyDetailsActivity.this,  "Failed to mark as sode due to " + e.getMessage()); // Note: "sode" appears to be a typo for "sold" in the source image
                                    }
                                });

                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG,  "onClick: Cancelled");
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void checkIsFavorite()
    {
        //DB path to check if Ad is in Favorite of current user. Users > uid > Favorites > propertyId
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference( "Users");
        ref.child(firebaseAuth.getUid()).child( "Favorites").child(propertyId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //if snapshot exists (value is true) means the Ad is in favorite of current user otherwise no
                        boolean favorite = snapshot.exists();
                        Log.d(TAG, "onDataChange: favorite: " + favorite);

                        //check if favorite or not to set image of toolbarFavBtn accordingly
                        if (favorite) {
                            //Favorite, set image fav_yes_black to button toolbarFavBtn
                            binding.toolbarFavBtn.setImageResource(R.drawable.fav_yes_black);
                        } else {
                            //Not Favorite, set image fav_no_black to button toolbarFavBtn
                            binding.toolbarFavBtn.setImageResource(R.drawable.fav_no_black);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void loadPropertyImages() {
        imageSliderArrayList = new ArrayList<>();

        //Db path to load the Ad images. Properties > PropertyId > Images
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Properties");
        ref.child(propertyId).child("Images")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before starting adding data into it
                        imageSliderArrayList.clear();

                        //there might be multiple images, loop it to load all
                        for (DataSnapshot ds : snapshot.getChildren()) {

                            try {
                                //prepare model (spellings in model class should be same as in firebase)
                                ModelImageSlider modelImageSlider = ds.getValue(ModelImageSlider.class);

                                //add the prepared model to list
                                imageSliderArrayList.add(modelImageSlider);
                            } catch (Exception e) {
                                Log.e(TAG, "onDataChange: ", e);
                            }
                        }

                        //setup adapter and set to viewpager i.e. imageSliderVp
                        AdapterImageSlider adapterImageSlider = new AdapterImageSlider(PropertyDetailsActivity.this, imageSliderArrayList);
                        binding.imageSliderVP.setAdapter(adapterImageSlider);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }

                });


    }

    private void deleteProperty() { // usage
        Log.d(TAG, "deleteProperty: Deleting " + propertyId);

        //Db path to delete the property. Properties > PropertyId
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Properties");
        reference.child(propertyId)
                .removeValue() // Task<Void>
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //Delete Success
                        Log.d(TAG, "onSuccess: Deleted " + propertyId);
                        MyUtils.toast( PropertyDetailsActivity.this, "Deleted...");
                        //finish activity and go-back
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Delete failed, show exception
                        Log.e(TAG, "onFailure: ", e);
                        MyUtils.toast( PropertyDetailsActivity.this,  "Failed to delete due to " + e.getMessage());
                    }
                });
    }

}