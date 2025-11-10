package com.example.realestate.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.realestate.R;
import com.example.realestate.adapters.AdapterProperty;
import com.example.realestate.databinding.FragmentFavoritListBinding;
import com.example.realestate.models.ModelProperty;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class FavoritListFragment extends Fragment {

    private FragmentFavoritListBinding binding; // 2 usages
    private static final String TAG = "FAVORITE_LIST_TAG"; // no usages

    private Context mContext; // no usages

    private FirebaseAuth firebaseAuth; // no usages

    //adArrayList to hold ads list added to favorite by
    private ArrayList<ModelProperty> propertyArrayList;
    private AdapterProperty adapterProperty;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mContext = context;
    }

    public FavoritListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentFavoritListBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        loadFavoriteProperties();

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
    }

    private void loadFavoriteProperties() {
        Log.d(TAG, "loadFavoriteProperties: ");

        propertyArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        propertyArrayList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String propertyId = "" + ds.child("propertyId").getValue();

                            DatabaseReference propertyRef = FirebaseDatabase.getInstance().getReference("Properties");
                            propertyRef.child(propertyId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                                            try {
                                                //Prepare ModelAd with all data from Firebase DB
                                                ModelProperty modelProperty = snapshot.getValue(ModelProperty.class);

                                                propertyArrayList.add(modelProperty);
                                            } catch (Exception e){

                                                Log.e(TAG, "onDataChange: ", e);
                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }

                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                adapterProperty = new AdapterProperty(mContext, propertyArrayList);
                                binding.favoritesRv.setAdapter(adapterProperty);
                            }
                        }, 1000);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}