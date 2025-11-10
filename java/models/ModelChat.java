package com.example.realestate.models;

public class ModelChat {

    private String messageId;
    private String senderUid;
    private String receiverUid;
    private String message; // Content of the message
    private String type;    // e.g., "text", "image"
    private long timestamp;

    public ModelChat() {
        // Required empty public constructor for Firebase
    }

    public ModelChat(String messageId, String senderUid, String receiverUid, String message, String type, long timestamp) {
        this.messageId = messageId;
        this.senderUid = senderUid;
        this.receiverUid = receiverUid;
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
    }

    // Getters and Setters

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }

    public String getReceiverUid() {
        return receiverUid;
    }

    public void setReceiverUid(String receiverUid) {
        this.receiverUid = receiverUid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}