package com.example.realestate.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.realestate.R;
import com.example.realestate.adapters.AdapterUser;
import com.example.realestate.databinding.FragmentChatsListBinding;
import com.example.realestate.models.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

// NOTE: Placeholder imports for adapter and model.
// public class ModelUser { /* ... */ }
// public class AdapterUser extends RecyclerView.Adapter<AdapterUser.HolderUser> { /* ... */ }

public class ChatsListFragment extends Fragment {

    private FragmentChatsListBinding binding;

    private static final String TAG = "CHAT_LIST_TAG";

    private Context mContext;
    private FirebaseAuth firebaseAuth;

    // List to hold UIDs of users the current user has chats with
    private ArrayList<String> chatWithUidArrayList;

    // List to hold the ModelUser objects of the chat participants (Owners/Sellers)
    // NOTE: Replace ModelUser with the actual user model class
    private ArrayList<ModelUser> userArrayList;

    // NOTE: Placeholder for the actual user adapter
    private AdapterUser adapterUser;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment using View Binding
        binding = FragmentChatsListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize lists and RecyclerView setup
        chatWithUidArrayList = new ArrayList<>();
        userArrayList = new ArrayList<>(); // Initialize the user list

        // Assume the FragmentChatsListBinding includes a RecyclerView named 'chatsRv'
        binding.chatsRv.setLayoutManager(new LinearLayoutManager(mContext));

        loadChats();
    }

    private void loadChats() {
        Log.d(TAG, "loadChats: Loading chat participants...");

        // 1. Load UIDs of people the current user has chatted with
        DatabaseReference refChat = FirebaseDatabase.getInstance().getReference("Chats");
        refChat.child(firebaseAuth.getUid()).child("Receivers")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        chatWithUidArrayList.clear();
                        // userArrayList.clear(); // Clear the user list too

                        Log.d(TAG, "onDataChange: Found " + snapshot.getChildrenCount() + " chat participants UIDs.");

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            // Key is the UID of the other user in the chat
                            String chatWithUid = ds.getKey();
                            chatWithUidArrayList.add(chatWithUid);
                        }

                        // 2. Once UIDs are loaded, load the corresponding user profiles
                        loadUsers();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "onCancelled: Failed to load chat UIDs: " + error.getMessage());
                    }
                });
    }

    private void loadUsers() {
        Log.d(TAG, "loadUsers: Loading user profiles for chat participants...");

        userArrayList.clear(); // Clear previous data before loading new users

        DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("Users");

        // Loop through all UIDs we have chatted with
        for (String chatWithUid : chatWithUidArrayList) {
            refUsers.child(chatWithUid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            // NOTE: Replace ModelUser with your actual User Model Class
                            ModelUser modelUser = snapshot.getValue(ModelUser.class);
                            if (modelUser != null) {
                                 userArrayList.add(modelUser);
                            }

                            // Check if this is the last user being loaded to notify adapter once
                            if (chatWithUid.equals(chatWithUidArrayList.get(chatWithUidArrayList.size() - 1))) {
                                Log.d(TAG, "onDataChange: All users loaded. Final count: " + chatWithUidArrayList.size());
                                adapterUser = new AdapterUser(mContext, userArrayList);
                                binding.chatsRv.setAdapter(adapterUser);
                                adapterUser.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "onCancelled: Failed to load user profile for UID " + chatWithUid + ": " + error.getMessage());
                        }
                    });
        }

        // Handle case where chatWithUidArrayList is empty (no chats)
        if (chatWithUidArrayList.isEmpty()) {
            // binding.chatsRv.setAdapter(null); // Show empty state if applicable
            Log.d(TAG, "loadUsers: No chat participants found.");
        }
    }
}