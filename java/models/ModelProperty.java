package com.example.realestate.models;

public class ModelProperty {

    String id;
    String uid;
    String purpose;
    String category;
    String subcategory;
    String areaSizeUnit;
    Double areaSize;
    String title;
    String description;
    String email;
    String phoneCode;
    String phoneNumber;
    String country;
    String city;
    String state;
    String address;
    String status;
    long floors;
    long bedRooms;
    long bathRooms;
    Double price;
    long timestamp;
    Double latitude;
    Double longitude;
    boolean favorite;

    public ModelProperty() {

    }

    public ModelProperty(String id, String uid, String purpose, String category, String subcategory, String areaSizeUnit, String title, String description, String email, String phoneCode, String phoneNumber, String country, String city, String state, String address, String status, long floors, long bedRooms, long bathRooms, long timestamp, Double latitude, Double longitude, Double areaSize, Double price, boolean favorite) {
        this.id = id;
        this.uid = uid;
        this.purpose = purpose;
        this.category = category;
        this.subcategory = subcategory;
        this.areaSizeUnit = areaSizeUnit;
        this.title = title;
        this.description = description;
        this.email = email;
        this.phoneCode = phoneCode;
        this.phoneNumber = phoneNumber;
        this.country = country;
        this.city = city;
        this.state = state;
        this.address = address;
        this.status = status;
        this.floors = floors;
        this.bedRooms = bedRooms;
        this.bathRooms = bathRooms;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.areaSize = areaSize;
        this.price = price;
        this.favorite = favorite;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public String getAreaSizeUnit() {
        return areaSizeUnit;
    }

    public void setAreaSizeUnit(String areaSizeUnit) {
        this.areaSizeUnit = areaSizeUnit;
    }

    public Double getAreaSize() {
        return areaSize;
    }

    public void setAreaSize(Double areaSize) {
        this.areaSize = areaSize;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getFloors() {
        return floors;
    }

    public void setFloors(long floors) {
        this.floors = floors;
    }

    public long getBedRooms() {
        return bedRooms;
    }

    public void setBedRooms(long bedRooms) {
        this.bedRooms = bedRooms;
    }

    public long getBathRooms() {
        return bathRooms;
    }

    public void setBathRooms(long bathRooms) {
        this.bathRooms = bathRooms;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
