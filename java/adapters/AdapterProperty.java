package com.example.realestate.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.realestate.MyUtils;
import com.example.realestate.R;
import com.example.realestate.databinding.RowPropertyBinding;
import com.example.realestate.models.ModelProperty;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import com.example.realestate.PropertyDetailsActivity;

public class AdapterProperty extends RecyclerView.Adapter<AdapterProperty.HolderProperty> implements Filterable {

    private RowPropertyBinding binding; // 9 usages

    private static final String TAG = "PROPERTY_TAG";

    //Context of activity/fragment from where instance of A
    private Context context;

    //propertyArrayList The list of the Ads
    private ArrayList<ModelProperty> propertyArrayList;

    private ArrayList<ModelProperty> filterList;

    private FirebaseAuth firebaseAuth;

    private Filter filter;

    public AdapterProperty(Context context, ArrayList<ModelProperty> propertyArrayList) { // no usages
        this.context = context;
        this.propertyArrayList = new ArrayList<>(propertyArrayList);
        this.filterList = new ArrayList<>(propertyArrayList);

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderProperty onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPropertyBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderProperty(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProperty holder, int position) {

        ModelProperty modelProperty = propertyArrayList.get(position);

        String propertyId = modelProperty.getId();
        String title = modelProperty.getTitle();
        String description = modelProperty.getDescription();
        String address = modelProperty.getAddress();
        String purpose = modelProperty.getPurpose();
        String category = modelProperty.getCategory();
        String subcategory = modelProperty.getSubcategory();
        double price = modelProperty.getPrice();
        String formattedPrice = MyUtils.formatCurrency(price);
        long timestamp = modelProperty.getTimestamp();
        String formattedDate = MyUtils.formatTimestampDate(timestamp);

        loadPropertyFirstImage(modelProperty, holder);

        if (firebaseAuth.getCurrentUser() != null) {
            checkIsFavorite(modelProperty, holder);
        }

        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.purposeTv.setText(purpose);
        holder.categoryTv.setText(category);
        holder.subcategoryTv.setText(subcategory);
        holder.addressTv.setText(address);
        holder.dateTv.setText(formattedDate);
        holder.priceTv.setText(formattedPrice);

        holder.favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if ad is in favorite of current user or not - this is a temporary
                boolean favorite = modelProperty.isFavorite();

                if (favorite) {
                    //this Property is in favorite of current user, remove from favorite
                    MyUtils.removeFromFavorite(context, propertyId);
                } else {
                    //this Property is not in favorite of current user, add to favorite
                    MyUtils.addToFavorite(context, propertyId);
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PropertyDetailsActivity.class);
                intent.putExtra("propertyId", modelProperty.getId());
                context.startActivity(intent);
            }
        });


    }

    private void loadPropertyFirstImage(ModelProperty modelProperty, HolderProperty holder) { // 1 usage
        Log.d(TAG, "loadPropertyFirstImage: ");

        String propertyId = modelProperty.getId();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Properties");
        ref.child(propertyId).child("Images").limitToFirst(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override // 2 usages
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot ds: snapshot.getChildren()) {
                            String imageUrl = "" + ds.child("imageUrl").getValue();
                            Log.d(TAG, "onDataChange: imageUrl" + imageUrl);

                            try {
                                Glide.with(context)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.building_asset01)
                                        .into(holder.propertyIv);
                            } catch (Exception e){
                                Log.e(TAG, "onDataChange: ", e);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIsFavorite(ModelProperty modelProperty, HolderProperty holder) { // no usages

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites").child(modelProperty.getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override // 2 usages
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        boolean favorite = snapshot.exists();

                        modelProperty.setFavorite(favorite);

                        if (favorite) {
                            holder.favoriteBtn.setImageResource(R.drawable.fav_yes_black);
                        } else {
                            holder.favoriteBtn.setImageResource(R.drawable.favorite_no_black);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }



    @Override
    public int getItemCount() {
        return propertyArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();

                    ArrayList<ModelProperty> filteredList = new ArrayList<>();

                    if (constraint == null || constraint.length() == 0) {
                        filteredList.addAll(filterList);
                    } else {
                        String searchQuery = constraint.toString().toLowerCase().trim();

                        for (ModelProperty property : filterList) {
                            String title = property.getTitle().toLowerCase();
                            String description = property.getDescription().toLowerCase();
                            String category = property.getCategory().toLowerCase();
                            String subcategory = property.getSubcategory().toLowerCase();

                            if (title.contains(searchQuery)
                                    || description.contains(searchQuery)
                                    || category.contains(searchQuery)
                                    || subcategory.contains(searchQuery)
                            ) {
                                filteredList.add(property);
                            }
                        }


                    }

                    results.values = filteredList;
                    results.count = filteredList.size();
                    return results;

                }

                @Override // 5 usages
                protected void publishResults(CharSequence constraint, FilterResults results) {

                    propertyArrayList.clear();
                    propertyArrayList.addAll((ArrayList<ModelProperty>) results.values);
                    notifyDataSetChanged();

                }
            };
        }
        return filter;
    }

    class HolderProperty extends RecyclerView.ViewHolder { // no usages

        ShapeableImageView propertyIv; // 1 usage
        TextView titleTv, descriptionTv, purposeTv, categoryTv, subcategoryTv, addressTv, dateTv, priceTv;
        FloatingActionButton favoriteBtn;

        public HolderProperty(@NonNull View itemView) { // no usages
            super(itemView);

            propertyIv = binding.propertyIv;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            purposeTv = binding.purposeTv;
            categoryTv = binding.categoryTv;
            subcategoryTv = binding.subcategoryTv;
            addressTv = binding.addressTv;
            dateTv = binding.dateTv;
            priceTv = binding.priceTv;
            favoriteBtn = binding.favoriteBtn;
        }
    }
}
