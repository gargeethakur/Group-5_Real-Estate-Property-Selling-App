package com.example.realestate;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.realestate.databinding.ActivityMainBinding;
import com.example.realestate.fragments.ChatsListFragment;
import com.example.realestate.fragments.FavoritListFragment;
import com.example.realestate.fragments.HomeFragment;
import com.example.realestate.fragments.ProfileFragment;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = firebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null) {
            startLoginOptionsActivity();
        }

        showHomeFragment();

        binding.bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                if (itemId == R.id.item_home) {

                    showHomeFragment();
                    return true;

                } else if (itemId == R.id.item_chats) {

                    if(firebaseAuth.getCurrentUser() == null) {
                        MyUtils.toast(MainActivity.this, "Login Required...");
                        return false;
                    } else {
                        showChatsListFragment();
                        return true;
                    }

                } else if (itemId == R.id.item_favorite) {

                    if(firebaseAuth.getCurrentUser() == null) {
                        MyUtils.toast(MainActivity.this, "Login Required...");
                        return false;
                    } else {
                        showFavoritListFragment();
                        return true;
                    }

                } else if (itemId == R.id.item_profile) {

                    if(firebaseAuth.getCurrentUser() == null) {
                        MyUtils.toast(MainActivity.this, "Login Required...");
                        return false;
                    } else {
                        showProfileFragment();
                        return true;
                    }

                } else {
                    return false;
                }
            }
        });

    }



    private void showHomeFragment(){
        binding.toolbarTitleTv.setText("Home");

        HomeFragment homeFragment = new HomeFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.FragmentsFl.getId(), homeFragment, "HomeFragment");
        fragmentTransaction.commit();
    }

    private void showChatsListFragment(){
        binding.toolbarTitleTv.setText("Chats");

        ChatsListFragment chatsListFragment = new ChatsListFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.FragmentsFl.getId(), chatsListFragment, "ChatsListFragment");
        fragmentTransaction.commit();
    }

    private void showFavoritListFragment(){
        binding.toolbarTitleTv.setText("Favourites");

        FavoritListFragment favoritListFragment = new FavoritListFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.FragmentsFl.getId(), favoritListFragment, "FavoritListFragment");
        fragmentTransaction.commit();
    }

    private void showProfileFragment(){
        binding.toolbarTitleTv.setText("Profile");

        ProfileFragment profileFragment = new ProfileFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.FragmentsFl.getId(), profileFragment, "ProfileFragment");
        fragmentTransaction.commit();
    }

    private void startLoginOptionsActivity() {
        startActivity(new Intent(this, LoginOptionsActivity.class));
    }


}