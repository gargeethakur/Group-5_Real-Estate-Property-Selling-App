package com.example.realestate.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.realestate.MyUtils;
import com.example.realestate.R;
import com.example.realestate.databinding.RowImagePickerBinding;
import com.example.realestate.models.ModelImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.Instant;
import java.util.ArrayList;

public class AdapterImagePicker extends RecyclerView.Adapter<AdapterImagePicker.HolderImagePicked> {

    private RowImagePickerBinding binding;
    private static final String TAG = "IMAGES_TAG";

    private Context context;

    private ArrayList<ModelImagePicker> imagePickerArrayList;

    private String propertyId;



    public AdapterImagePicker(Context context , ArrayList<ModelImagePicker> imagePickerArrayList, String propertyId) {
        this.imagePickerArrayList = imagePickerArrayList;
        this.context = context;
        this.propertyId = propertyId;
    }

    @NonNull
    @Override
    public HolderImagePicked onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Inflate/Bind the row_images_picked.xml
        binding = RowImagePickerBinding.inflate(LayoutInflater.from(context), parent,  false);

        return new HolderImagePicked(binding.getRoot());

    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull HolderImagePicked holder, int position) {

        ModelImagePicker modelImagePicker = imagePickerArrayList.get(position);

        if (modelImagePicker.isFromInternet()) {
            String imageUrl = modelImagePicker.getImageUrl();
            Log.d(TAG,"onBindViewHolder: imageUrl: " + imageUrl);

            try {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.image_gray)
                        .into(holder.imageIv);
            } catch (Exception e) {
                Log.e(TAG,"onBindViewHolder: ", e);
            }

        } else {
            //Image is picked from Gallery/Camera. Get image Uri of the im
            Uri imageUri = modelImagePicker.getImageUri();

            try {
                //set the image in imageIv
                Glide.with(context)
                        .load(imageUri)
                        .placeholder(R.drawable.image_gray)
                        .into(holder.imageIv);
            } catch (Exception e) {
                Log.e(TAG,"onBindViewHolder: ", e);
            }
        }

        holder.closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //image is from device storage, just remove from list.


                if (modelImagePicker.isFromInternet()) {
                    deleteImageFirebase(modelImagePicker, position);

                } else {
                    imagePickerArrayList.remove(modelImagePicker);
                    notifyItemRemoved(position);
                }
            }
        });

    }

    private void deleteImageFirebase(ModelImagePicker modelImagePicker, int position) { // 1 usage

        String imageId = modelImagePicker.getId();
        Log.d(TAG, "deleteImageFirebase: propertyId: " + propertyId);
        Log.d(TAG, "deleteImageFirebase: imageId: " + imageId);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference( "Properties");
        ref.child(propertyId).child("Images").child(imageId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        Log.d(TAG, "onSuccess: Image deleted...");
                        MyUtils.toast(context, "Image deleted...");

                        try {
                            imagePickerArrayList.remove(modelImagePicker);
                            notifyItemRemoved(position);
                        } catch (Exception e) {
                            Log.e(TAG, "onSuccess: ", e);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Delete Failure
                        Log.e(TAG, "onFailure: ", e);
                        MyUtils.toast(context, "Failed to delete due to " + e.getMessage());
                    }
                });

    }

    @Override
    public int getItemCount() {
        return imagePickerArrayList.size();
    }

    class HolderImagePicked extends RecyclerView.ViewHolder {

        ImageView imageIv;
        ImageButton closeBtn;
        public HolderImagePicked(@NonNull View itemView) {
            super(itemView);

            imageIv = binding.imageIv;
            closeBtn = binding.closeBtn;
        }
    }

}
