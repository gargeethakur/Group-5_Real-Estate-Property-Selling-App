package com.example.realestate;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.text.format.DateFormat;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class MyUtils {

    public static final String AD_STATUS_AVAILABLE = "AVAILABLE";
    public static final String AD_STATUS_SOLD = "SOLD"; // no usages
    public static final String AD_STATUS_RENTED = "RENTED"; // no usages

    public static final String USER_TYPE_GOOGLE = "Google";
    public static final String USER_TYPE_EMAIL = "Email";
    public static final String USER_TYPE_PHONE = "Phone";

    public static final String[] propertyTypes = {"Homes", "Plots", "Commercial"};
    public static final String[] propertyTypesHomes = {"House", "Flat", "Upper Portion", "Lower Portion", "Farm House", "Room", "Penthouse"};
    public static final String[] propertyTypesPlots = {"Residential Plot", "Commercial Plot", "Agricultural Plot", "Industrial Plot", "Plot File", "Plot Form"};
    public static final String[] propertyTypesCommercial = {"Office", "Shop", "Warehouse", "Factory", "Building", "Other"};

    public static final String[] propertyAreaSizeUnit = {"Square Feet", "Square Yards", "Square Meters", "Marla", "Kanal"};

    public static final String PROPERTY_PURPOSE_ANY = "Any";
    public static final String PROPERTY_PURPOSE_SELL = "Sell";
    public static final String PROPERTY_PURPOSE_RENT = "Rent";

    public static final int MAX_DISTANCE_TO_LOAD_PROPERTIES = 10;



    public static void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static long timestamp() {
        return System.currentTimeMillis();
    }

    public static String formatTimestampDate(Long timestamp){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);

        String date = DateFormat.format("dd/MM/yyyy", calendar).toString();
        return date;
    }

    public static String formatCurrency(Double price) { // no usages
        NumberFormat numberFormat = NumberFormat.getNumberInstance();

        // Set minimum and maximum fraction digits for currency-style
        numberFormat.setMaximumFractionDigits(2);

        return numberFormat.format(price);
    }

    public static double calculateDistanceKm(double currentLatitude, double currentLongitude, double propertyLatitude, double propertyLongitude) {

        Location startPoint = new Location(LocationManager.NETWORK_PROVIDER);
        startPoint.setLatitude(currentLatitude);
        startPoint.setLongitude(currentLongitude);

        Location endPoint = new Location(LocationManager.NETWORK_PROVIDER);
        endPoint.setLatitude(propertyLatitude);
        endPoint.setLongitude(propertyLongitude);

        double distanceInMeters = startPoint.distanceTo(endPoint);
        double distanceInKm = distanceInMeters / 1000;

        return distanceInKm;
    }

    public static void addToFavorite(Context context, String propertyId) { // no usages

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null) {
            MyUtils.toast(context, "You're not logged-in!");
        } else {

            long timestamp = MyUtils.timestamp();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("propertyId", propertyId);
            hashMap.put("timestamp", timestamp);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(propertyId)
                    .setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            // Added to favorite
                            MyUtils.toast(context, "Added to favorite...!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to add to favorite
                            MyUtils.toast(context, "Failed to add to favorite due to " + e.getMessage());
                        }
                    });
        }
    }

    public static void removeFromFavorite(Context context, String propertyId) { // no usages
        //We can remove only if user is logged in
        //To Check if user is logged in create instance of FirebaseAuth
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        //Check if user is logged in
        if (firebaseAuth.getCurrentUser() == null) {
            //not logged in, can't remove from favorite
            MyUtils.toast(context, "You're not logged-in!");
        } else {
            //logged in, can remove from favorite
            // Remove data from db. Users > uid > Favorites > propertyId
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(propertyId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            MyUtils.toast(context, "Removed from favorites...!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            MyUtils.toast(context, "Failed to remove from favorites due to " + e.getMessage());
                        }
                    });
        }
    }

    public static void callIntent(Context context, String phoneNumber) {

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + phoneNumber));
        context.startActivity(intent);
    }

    public static void smsIntent(Context context, String phoneNumber) { // no usages

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + phoneNumber));
        context.startActivity(intent);
    }

    public static void mapIntent(Context context, double latitude, double longitude) { // no usages

        Uri mapIntentUri = Uri.parse("http://maps.google.com/maps?daddr=" + latitude + "," + longitude);

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapIntentUri);

        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(mapIntent);
        } else {
            MyUtils.toast(context, "Google Map not installed!");
        }
    }

    public static String formatTimestampTime(long timestamp) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);
        // Format to "hh:mm a" (e.g., 10:30 AM)
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
        return sdf.format(calendar.getTime());
    }

    // MyUtils.java

// Add this method inside your existing MyUtils class:

    public static String getChatId(String uid1, String uid2) {
        // 1. Create an array containing both UIDs.
        String[] uids = {uid1, uid2};

        // 2. Sort the UIDs alphabetically (lexicographically).
        // This is the core step: it guarantees the order is always consistent.
        java.util.Arrays.sort(uids);

        // 3. Concatenate the sorted UIDs with an underscore.
        // Example: If uids are ("Alice", "Bob"), the result is "Alice_Bob".
        // If they are ("Bob", "Alice"), the result is also "Alice_Bob".
        return uids[0] + "_" + uids[1];
    }




}
