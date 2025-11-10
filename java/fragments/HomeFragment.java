package com.example.realestate.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.example.realestate.LocationPickerActivity;
import com.example.realestate.MyUtils;
import com.example.realestate.R;
import com.example.realestate.adapters.AdapterProperty;
import com.example.realestate.databinding.BsFilterCategoryBinding;
import com.example.realestate.databinding.FragmentHomeBinding;
import com.example.realestate.models.ModelProperty;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding; // 2 usages
    private static final String TAG = "HOME_TAG"; // no usages

    private Context mContext;

    private ArrayList<ModelProperty> propertyArrayList;
    private AdapterProperty adapterProperty;

    private SharedPreferences locationSp;

    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    private String currentAddress = "";
    private String currentCity = "";

    private String filterPurpose = MyUtils.PROPERTY_PURPOSE_ANY;
    private String filterCategory = ""; // no usages
    private String filterSubcategory = ""; // no usages
    private Double filterPriceMin = 0.0; // no usages
    private Double filterPriceMax = null;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        locationSp = mContext.getSharedPreferences("LOCATION_SP", MODE_PRIVATE);

        currentLatitude = locationSp.getFloat("CURRENT_LATITUDE", 0.0f);
        currentLongitude = locationSp.getFloat("CURRENT_LONGITUDE", 0.0f);
        currentAddress = locationSp.getString("CURRENT_ADDRESS", "");
        currentCity = locationSp.getString("CURRENT_CITY", "");

        if (!currentCity.isEmpty() && currentCity != null) {
            binding.cityTv.setText(currentCity);
        }

        loadProperties();

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                Log.d(TAG, "onTextChanged: Query: " + s);

                try {
                    String query = s.toString();
                    adapterProperty.getFilter().filter(query);
                } catch (Exception e) {
                    Log.e(TAG, "onTextChanged: ", e);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.cityTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, LocationPickerActivity.class);
                locationActivityResultLauncher.launch(intent);
            }
        });

        binding.filterTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialog();

            }
        });
    }

    private ActivityResultLauncher<Intent> locationActivityResultLauncher = registerForActivityResult( // 1 us
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "onActivityResult: Location picked");

                        Intent data = result.getData();

                        if (data != null) {
                            currentLatitude = data.getDoubleExtra("latitude", 0.0);
                            currentLongitude = data.getDoubleExtra("longitude", 0.0);
                            currentAddress = data.getStringExtra("address");
                            currentCity = data.getStringExtra("city");

                            locationSp.edit()
                                    .putFloat("CURRENT_LATITUDE", Float.parseFloat(""+currentLatitude))
                                    .putFloat("CURRENT_LONGITUDE", Float.parseFloat("" + currentLongitude))
                                    .putString("CURRENT_ADDRESS", currentAddress)
                                    .putString("CURRENT_CITY", currentCity)
                                    .apply();

//set the picked address
                            binding.cityTv.setText(currentCity);

//after picking address reload all ads again based on newly picked location
                            loadProperties();
                        }
                    } else {
                        Log.d(TAG, "onActivityResult: Cancelled");
                        MyUtils.toast(mContext, "Cancelled!");
                    }

                }
            }
    );

    private void loadProperties() { // 1 usage
        Log.d(TAG, "loadProperties: ");

        propertyArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Properties");
        ref.addValueEventListener(new ValueEventListener() {
            @Override // 2 usages
            // Inside HomeFragment.java, around line 105
            // Inside HomeFragment.java

            public void onDataChange(@NonNull DataSnapshot snapshot) {

                propertyArrayList.clear();

                Log.d(TAG, "onDataChange: Total Properties from DB: " + snapshot.getChildrenCount()); // DEBUG 1

                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        ModelProperty modelProperty = ds.getValue(ModelProperty.class);

                        // Null check for safety (especially if ModelProperty.class fails mapping)
                        if (modelProperty == null) {
                            Log.e(TAG, "onDataChange: ModelProperty is null for a child snapshot.");
                            continue; // Skip this iteration
                        }

                        // get latitude and longitude of the property
                        double propertyLatitude = modelProperty.getLatitude();
                        double propertyLongitude = modelProperty.getLongitude();

                        // Check if coordinates are valid before calculating distance
                        if (propertyLatitude == 0.0 && propertyLongitude == 0.0) {
                            Log.w(TAG, "onDataChange: Property '" + modelProperty.getTitle() + "' has 0.0 lat/lng. Skipping distance check.");
                        }

                        double distance = MyUtils.calculateDistanceKm(currentLatitude, currentLongitude, propertyLatitude, propertyLongitude);
                        Log.d(TAG, "onDataChange: distance: " + distance + " KM for Property: " + modelProperty.getTitle()); // DEBUG 2

                        boolean passesDistance = (distance <= MyUtils.MAX_DISTANCE_TO_LOAD_PROPERTIES);
                        boolean passesFilter = isPropertyMatchingFilter(modelProperty);

                        // DEBUG 3: Log why a property might be skipped
                        if (!passesDistance) {
                            Log.d(TAG, "onDataChange: FAILED DISTANCE. Max: " + MyUtils.MAX_DISTANCE_TO_LOAD_PROPERTIES);
                        }
                        if (!passesFilter) {
                            Log.d(TAG, "onDataChange: FAILED CUSTOM FILTER.");
                        }


                        // CHECK BOTH CONDITIONS: DISTANCE AND FILTER
                        if (passesDistance && passesFilter) {
                            // ONLY add the property if BOTH conditions are met
                            propertyArrayList.add(modelProperty);
                            Log.d(TAG, "onDataChange: SUCCESSFULLY ADDED Property: " + modelProperty.getTitle()); // DEBUG 4
                        }


                    } catch (Exception e) {
                        Log.e(TAG, "onDataChange: Error processing property: ", e);
                    }
                }

                Log.d(TAG, "onDataChange: Final List Size: " + propertyArrayList.size()); // DEBUG 5

                // After the loop finishes, set the adapter with the potentially filtered list
                adapterProperty = new AdapterProperty(mContext, propertyArrayList);
                binding.propertiesRv.setAdapter(adapterProperty);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private boolean isPropertyMatchingFilter(ModelProperty modelProperty) { // no usages
        // No filtering if purpose is "any"
        if (filterPurpose.equals(MyUtils.PROPERTY_PURPOSE_ANY)) {
            return true;
        }

        boolean matchesPurpose = modelProperty.getPurpose().equalsIgnoreCase(filterPurpose);
        boolean matchesCategory = modelProperty.getCategory().equalsIgnoreCase(filterCategory);
        boolean matchesSubcategory = modelProperty.getSubcategory().equalsIgnoreCase(filterSubcategory);

        boolean matchesAllTypes = matchesPurpose && matchesCategory && matchesSubcategory;
        boolean matchesPrice = modelProperty.getPrice() >= filterPriceMin &&
                (filterPriceMax == null || modelProperty.getPrice() <= filterPriceMax);

        return matchesAllTypes && matchesPrice;
    }

    private ArrayAdapter<String> stringArrayPropertyCategory; // no us
    private ArrayAdapter<String> stringArrayPropertySubcategory; // no us

    private void showFilterDialog() {
        BsFilterCategoryBinding bindingBs = BsFilterCategoryBinding.inflate(getLayoutInflater());

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mContext);
        bottomSheetDialog.setContentView(bindingBs.getRoot());

        bottomSheetDialog.show();

        if (!filterCategory.isEmpty()) {
            bindingBs.propertyCategoryAct.setText(filterCategory);
        }
//if we have previously selected subcategory set it to propertyS
        if (!filterSubcategory.isEmpty()) {
            bindingBs.propertySubcategoryAct.setText(filterSubcategory);
        }

        if (filterPriceMin != 0) {
            bindingBs.priceMinEt.setText(""+filterPriceMin);
        }

        if (filterPriceMax != null) {
            bindingBs.priceMaxEt.setText(""+filterPriceMax);
        }

        bindingBs.tabBuyTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Tab Buy is selected, set filterPurpose to Sell/Buy
                filterPurpose = MyUtils.PROPERTY_PURPOSE_SELL;
                //Format appearance of selected tab Buy
                bindingBs.tabBuyTv.setBackgroundResource(R.drawable.shape_rounded_white);
                bindingBs.tabBuyTv.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                bindingBs.tabBuyTv.setTypeface(null, Typeface.BOLD);

                //Format appearance of unselected tab Rent
                bindingBs.tabRentTv.setBackground(null);
                bindingBs.tabRentTv.setTextColor(ContextCompat.getColor(mContext, R.color.black));
                bindingBs.tabRentTv.setTypeface(null, Typeface.NORMAL);
            }
        });

        bindingBs.tabRentTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                filterPurpose = MyUtils.PROPERTY_PURPOSE_RENT;

                bindingBs.tabBuyTv.setBackground(null);
                bindingBs.tabBuyTv.setTextColor(ContextCompat.getColor(mContext, R.color.black));
                bindingBs.tabBuyTv.setTypeface(null, Typeface.NORMAL);

                bindingBs.tabRentTv.setBackgroundResource(R.drawable.shape_rounded_white);
                bindingBs.tabRentTv.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                bindingBs.tabRentTv.setTypeface(null, Typeface.BOLD);
            }
        });

        stringArrayPropertyCategory = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, MyUtils.propertyTypes);

        bindingBs.propertyCategoryAct.setAdapter(stringArrayPropertyCategory);
//handle propertyCategoryAct item click, set selected category to filterCategory
        bindingBs.propertyCategoryAct.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //get and save selected category
                filterCategory = (String) parent.getItemAtPosition(position);
                Log.d(TAG, "onItemClick: filterCategory:"+ filterCategory);
                //after selecting category, clear subcategory
                filterSubcategory = "";
                bindingBs.propertySubcategoryAct.setText(filterSubcategory);

                if (filterCategory.equals(MyUtils.propertyTypes[0])) {
                    //Property Homes selected, set adapter to propertySubcategoryAct with propertyTypesHomes
                    stringArrayPropertySubcategory = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, MyUtils.propertyTypesHomes);
                } else if (filterCategory.equals(MyUtils.propertyTypes[1])) {
                    //Property Plots selected, set adapter to propertySubcategoryAct with propertyTypesPlots
                    stringArrayPropertySubcategory = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, MyUtils.propertyTypesPlots);
                } else if (filterCategory.equals(MyUtils.propertyTypes[2])) {
                    //Property Commercial selected, set adapter to propertySubcategoryAct with propertyTypesCommercial
                    stringArrayPropertySubcategory = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, MyUtils.propertyTypesCommercial);
                }
//set adapter to propertySubcategoryAct
                bindingBs.propertySubcategoryAct.setAdapter(stringArrayPropertySubcategory);
            }
        });

        bindingBs.propertySubcategoryAct.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //get and save selected subcategory
                filterSubcategory = (String) parent.getItemAtPosition(position);
            }
        });

//handle resetBtn click, reset all filters and close bottom sheet dialog
        bindingBs.resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dismiss bottom sheet dialog
                bottomSheetDialog.dismiss();
                //reset filters by assigning default values
                filterPurpose = MyUtils.PROPERTY_PURPOSE_ANY;
                filterCategory = "";
                filterSubcategory = "";
                filterPriceMin = 0.0;
                filterPriceMax = null;

                loadProperties();
            }
        });

        bindingBs.applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Since the tab tabBuyTv is by default selected so assign it with "Sell"
                if (filterPurpose.equals(MyUtils.PROPERTY_PURPOSE_ANY)) {
                    filterPurpose = MyUtils.PROPERTY_PURPOSE_SELL;
                }

                //Category is required
                if (filterCategory.isEmpty()) {
                    bindingBs.propertyCategoryAct.setError("Choose Category");
                    bindingBs.propertyCategoryAct.requestFocus();

                    return;
                }

                if (filterSubcategory.isEmpty()) {
                    bindingBs.propertySubcategoryAct.setError("Choose subcategory");
                    bindingBs.propertySubcategoryAct.requestFocus();

                    return;

                }

                //input min and max price
                String priceMin = bindingBs.priceMinEt.getText().toString().trim();
                String priceMax = bindingBs.priceMaxEt.getText().toString().trim();

//if min price is empty then consider it as 0.0
                if (priceMin.isEmpty()) {
                    filterPriceMin = 0.0;
                } else {
                    filterPriceMin = Double.parseDouble(priceMin);
                }

//if max price is not entered then we will consider user doesn't want
                if (priceMax.isEmpty()) {
                    filterPriceMax = null;
                } else {
                    filterPriceMax = Double.parseDouble(priceMax);
                }

                bottomSheetDialog.dismiss();

                loadProperties();
            }
        });
    }

}