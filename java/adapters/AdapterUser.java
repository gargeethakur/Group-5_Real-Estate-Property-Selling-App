package com.example.realestate.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.realestate.ChatDetailsActivity;
import com.example.realestate.R;
import com.example.realestate.models.ModelUser;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.HolderUser> {

    private Context context;
    public ArrayList<ModelUser> userArrayList;

    public AdapterUser(Context context, ArrayList<ModelUser> userArrayList) {
        this.context = context;
        this.userArrayList = userArrayList;
    }

    @NonNull
    @Override
    public HolderUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // NOTE: Assume row_user_chat.xml is the layout for each item
        View view = LayoutInflater.from(context).inflate(R.layout.row_user_chat, parent, false);
        return new HolderUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderUser holder, int position) {
        // Get data
        ModelUser modelUser = userArrayList.get(position);

        String uid = modelUser.getUid();
        String name = modelUser.getName();
        String profileImageUrl = modelUser.getProfileImageUrl();

        // Set data
        holder.nameTv.setText(name);

        // Load profile image using Glide
        try {
            Glide.with(context)
                    .load(profileImageUrl)
                    .placeholder(R.drawable.person_black) // Placeholder image
                    .into(holder.profileIv);
        } catch (Exception e) {
            holder.profileIv.setImageResource(R.drawable.person_black);
        }

        // Handle item click to start the chat
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Start the actual ChatDetailsActivity, passing the UID of the chat partner
                Intent intent = new Intent(context, ChatDetailsActivity.class);
                intent.putExtra("otherUserUid", uid);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userArrayList.size();
    }

    class HolderUser extends RecyclerView.ViewHolder {

        ShapeableImageView profileIv;
        TextView nameTv;
        TextView lastMessageTv; // Include if you want to show the last message

        public HolderUser(@NonNull View itemView) {
            super(itemView);

            // NOTE: Replace with actual IDs from your row_user_chat.xml layout
            profileIv = itemView.findViewById(R.id.profileIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            lastMessageTv = itemView.findViewById(R.id.lastMessageTv);
        }
    }
}
