package com.example.realestate.models;

public class ModelUser {

    private String uid;
    private String name;
    private String email;
    private String phoneCode;
    private String phoneNumber;
    private String profileImageUrl;
    private String userType;
    private long timestamp;

    public ModelUser() {
        // Required empty public constructor for Firebase
    }

    public ModelUser(String uid, String name, String email, String phoneCode, String phoneNumber, String profileImageUrl, String userType, long timestamp) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phoneCode = phoneCode;
        this.phoneNumber = phoneNumber;
        this.profileImageUrl = profileImageUrl;
        this.userType = userType;
        this.timestamp = timestamp;
    }

    // Getter and Setter methods

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneCode() {
        return phoneCode;
    }

    public void setPhoneCode(String phoneCode) {
        this.phoneCode = phoneCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
