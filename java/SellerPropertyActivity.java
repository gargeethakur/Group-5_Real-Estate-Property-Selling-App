package com.example.realestate;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.realestate.adapters.AdapterProperty;
import com.example.realestate.databinding.ActivitySellerPropertyBinding;
import com.example.realestate.models.ModelProperty;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SellerPropertyActivity extends AppCompatActivity {

    private ActivitySellerPropertyBinding binding; // 2 usages

    private static final String TAG = "SELLER_INFO_TAG"; // 1 usage

    private String sellerUid = ""; // 2 usages

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable( this);
        //init view binding... activity_seller_profile.xml = ActivitySellerProfileBinding
        binding = ActivitySellerPropertyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sellerUid = getIntent().getStringExtra("sellerUid");
        Log.d(TAG,  "onCreate: sellerUid" + sellerUid);

        loadSellerDetails();
        loadSellerProperties();

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });
    }

    private void loadSellerDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(sellerUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String name = "" + snapshot.child( "name").getValue();
                        String profileImageUrl = "" + snapshot.child("profileImageUrl").getValue();
                        String timestamp = "" + snapshot.child( "timestamp").getValue();

                        String formattedDate = MyUtils.formatTimestampDate(Long.parseLong(timestamp));

                        binding.sellerNameTv.setText(name);
                        binding.memberSinceTV.setText(formattedDate);

                        try {
                            Glide.with( SellerPropertyActivity.this)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.person_black)
                                    .into(binding.sellerProfileIV);

                        } catch (Exception e) {
                            Log.e(TAG,  "onDataChange: ", e);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadSellerProperties() {
        ArrayList<ModelProperty> propertyArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Properties");
        ref.orderByChild( "uid").equalTo(sellerUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        propertyArrayList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()){

                            try {

                                ModelProperty modelProperty = ds.getValue(ModelProperty.class);

                                propertyArrayList.add(modelProperty);

                            } catch (Exception e) {

                                Log.e(TAG,  "onDataChange: ", e);
                            }
                        }

                        AdapterProperty adapterProperty = new AdapterProperty(SellerPropertyActivity.this, propertyArrayList);
                        binding.propertiesRv.setAdapter(adapterProperty);

                        String propertiesCount = ""+propertyArrayList.size();
                        binding.publishedAdsCountTv.setText(propertiesCount);
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}