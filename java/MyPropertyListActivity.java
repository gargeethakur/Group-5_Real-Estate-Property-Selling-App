package com.example.realestate;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.realestate.adapters.AdapterProperty;
import com.example.realestate.databinding.ActivityMyPropertyListBinding;
import com.example.realestate.models.ModelProperty;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyPropertyListActivity extends AppCompatActivity {

    private ActivityMyPropertyListBinding binding; // 2 usages

    //TAG to show logs in logcat
    private static final String TAG = "MY_PROPERTY_LIST_TAG"; // no usages

    //Firebase Auth for auth related tasks
    private FirebaseAuth firebaseAuth; // 1 usage

    private ArrayList<ModelProperty> propertyArrayList;

    private AdapterProperty adapterProperty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init view binding... activity_my_property_list.xml = ActivityMyProperty
        binding = ActivityMyPropertyListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Firebase Auth for auth related tasks
        firebaseAuth = FirebaseAuth.getInstance();

        loadMyProperties();

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

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadMyProperties() { // 1 usage

        //init property arraylist
        propertyArrayList = new ArrayList<>();

        String myUid = "" + firebaseAuth.getUid();
        Log.d(TAG, "loadMyProperties: myUid: " + myUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Properties");
        ref.orderByChild("uid").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        propertyArrayList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            try {
                                ModelProperty modelProperty = ds.getValue(ModelProperty.class);

                                propertyArrayList.add(modelProperty);
                            } catch (Exception e) {
                                Log.e(TAG, "onDataChange: ", e);
                            }
                        }

                        adapterProperty = new AdapterProperty(MyPropertyListActivity.this, propertyArrayList);
                        binding.propertiesRv.setAdapter(adapterProperty);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}