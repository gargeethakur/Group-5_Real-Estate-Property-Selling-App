package com.example.realestate.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.realestate.MyUtils;
import com.example.realestate.R;
import com.example.realestate.models.ModelChat;

import java.util.ArrayList;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.HolderChat> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private ArrayList<ModelChat> chatArrayList;
    private String myUid;

    public AdapterChat(Context context, ArrayList<ModelChat> chatArrayList, String myUid) {
        this.context = context;
        this.chatArrayList = chatArrayList;
        this.myUid = myUid;
    }

    @NonNull
    @Override
    public HolderChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Differentiate message type to inflate the correct layout (left or right bubble)
        if (viewType == MSG_TYPE_RIGHT) {
            // Sent message: uses right-aligned layout
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);
            return new HolderChat(view);
        } else {
            // Received message: uses left-aligned layout
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);
            return new HolderChat(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HolderChat holder, int position) {
        // Get data
        ModelChat modelChat = chatArrayList.get(position);

        String message = modelChat.getMessage();
        long timestamp = modelChat.getTimestamp();
        String formattedTime = MyUtils.formatTimestampTime(timestamp); // Assuming you have a utility method for time formatting

        // Set data
        holder.messageTv.setText(message);
        holder.timeTv.setText(formattedTime);

        // TODO: Handle Image/Other message types here based on modelChat.getType() if implemented later.
    }

    @Override
    public int getItemCount() {
        return chatArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        // Determine the view type based on the sender UID
        if (chatArrayList.get(position).getSenderUid().equals(myUid)) {
            // Message sent by the current user
            return MSG_TYPE_RIGHT;
        } else {
            // Message sent by the other user
            return MSG_TYPE_LEFT;
        }
    }

    class HolderChat extends RecyclerView.ViewHolder {

        TextView messageTv;
        TextView timeTv;

        public HolderChat(@NonNull View itemView) {
            super(itemView);

            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }
}