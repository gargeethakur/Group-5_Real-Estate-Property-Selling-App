package com.example.realestate;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.realestate.adapters.AdapterChat;
import com.example.realestate.databinding.ActivityChatDetailsBinding;
import com.example.realestate.models.ModelChat;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

// NOTE: This class assumes the existence of ModelChat and AdapterChat for message handling.

public class ChatDetailsActivity extends AppCompatActivity {

    private ActivityChatDetailsBinding binding;
    private static final String TAG = "CHAT_DETAILS_TAG";

    private FirebaseAuth firebaseAuth;
    private String otherUserUid; // The UID of the person we are chatting with
    private String otherUserName = "";
    private String myUid;

    private ArrayList<ModelChat> chatArrayList;
    private AdapterChat adapterChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get UID of the other user from the intent
        otherUserUid = getIntent().getStringExtra("otherUserUid");

        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();

        loadOtherUserInfo();
        loadMessages();

        // Handle back button click
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Handle Send button click
        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndSendMessage();
            }
        });
    }

    private void loadOtherUserInfo() {
        Log.d(TAG, "loadOtherUserInfo: Loading info for UID: " + otherUserUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(otherUserUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = "" + snapshot.child("name").getValue();
                        String profileImageUrl = "" + snapshot.child("profileImageUrl").getValue();

                        otherUserName = name;

                        // Set the user's name in the toolbar
                        binding.toolbarTitleTv.setText(name);

                        // Load image into toolbar ImageView
                        try {
                            Glide.with(ChatDetailsActivity.this)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.person_black)
                                    .into(binding.profileIv);
                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange: Failed to load profile image", e);
                            binding.profileIv.setImageResource(R.drawable.person_black);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "onCancelled: Failed to load user info: " + error.getMessage());
                    }
                });
    }

    private void loadMessages() {
        Log.d(TAG, "loadMessages: Loading messages for chat...");
        chatArrayList = new ArrayList<>();

        // Setup RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true); // show latest message at bottom
        binding.chatRv.setLayoutManager(linearLayoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
        ref.child("Messages")
                .child(MyUtils.getChatId(myUid, otherUserUid))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        chatArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelChat modelChat = ds.getValue(ModelChat.class);
                            chatArrayList.add(modelChat);
                        }

                        adapterChat = new AdapterChat(ChatDetailsActivity.this, chatArrayList, myUid);
                        binding.chatRv.setAdapter(adapterChat);

                        // Scroll to the last message
                        binding.chatRv.scrollToPosition(chatArrayList.size() - 1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "onCancelled: Failed to load messages: " + error.getMessage());
                    }
                });
    }

    private void validateAndSendMessage() {
        String message = binding.messageEt.getText().toString().trim();

        if (TextUtils.isEmpty(message)) {
            MyUtils.toast(this, "Can't send empty message...");
            return;
        }

        sendMessage(message);
    }

    private void sendMessage(String message) {
        // Chat Node: Chats/Messages/{chatId}/{messageId}/...
        // Chat ID is a unique ID created using both UIDs to ensure consistency.

        String chatId = MyUtils.getChatId(myUid, otherUserUid);
        long timestamp = MyUtils.timestamp();

        DatabaseReference refChatMessages = FirebaseDatabase.getInstance().getReference("Chats")
                .child("Messages").child(chatId);

        String messageId = refChatMessages.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("senderUid", myUid);
        hashMap.put("receiverUid", otherUserUid);
        hashMap.put("message", message);
        hashMap.put("timestamp", timestamp);
        hashMap.put("messageId", messageId);
        hashMap.put("type", "text"); // message type text/image/pdf

        refChatMessages.child(messageId)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Message sent successfully: " + messageId);
                        binding.messageEt.setText("");
                        updateChatList(chatId, message, timestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: Failed to send message: " + e.getMessage());
                        MyUtils.toast(ChatDetailsActivity.this, "Failed to send message: " + e.getMessage());
                    }
                });
    }

    private void updateChatList(String chatId, String lastMessage, long timestamp) {
        // Chat List Node: Chats/{myUid}/Receivers/{otherUserUid}/...

        // Data for the current user's chat list
        DatabaseReference refMyChatList = FirebaseDatabase.getInstance().getReference("Chats").child(myUid).child("Receivers");
        refMyChatList.child(otherUserUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("chatId", chatId);
                        hashMap.put("lastMessage", lastMessage);
                        hashMap.put("lastMessageTimestamp", timestamp);

                        refMyChatList.child(otherUserUid).updateChildren(hashMap);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "onCancelled: Failed to update My Chat List: " + error.getMessage());
                    }
                });

        // Data for the other user's chat list
        DatabaseReference refOtherUserChatList = FirebaseDatabase.getInstance().getReference("Chats").child(otherUserUid).child("Receivers");
        refOtherUserChatList.child(myUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("chatId", chatId);
                        hashMap.put("lastMessage", lastMessage);
                        hashMap.put("lastMessageTimestamp", timestamp);

                        refOtherUserChatList.child(myUid).updateChildren(hashMap);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "onCancelled: Failed to update Other User Chat List: " + error.getMessage());
                    }
                });
    }

    /*
    NOTE: You will also need to define the ModelChat and AdapterChat classes.
    The ModelChat should minimally contain: senderUid, receiverUid, message, timestamp, messageId, type.
    The AdapterChat handles displaying messages, differentiating between sender and receiver layouts.
    */
}