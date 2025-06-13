package com.eles.driver;

public class SOSData {
    public String requestId;
    public String status;
    public double latitude;
    public double longitude;
    public double distance;

    public SOSData() {
        // Required for Firebase
    }

    public SOSData(String requestId, String status, double latitude, double longitude, double distance) {
        this.requestId = requestId;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
    }
}
