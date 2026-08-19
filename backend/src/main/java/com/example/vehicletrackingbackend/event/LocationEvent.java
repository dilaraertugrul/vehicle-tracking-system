package com.example.vehicletrackingbackend.event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class LocationEvent {

    private String vehicleId;
    private double latitude;
    private double longitude;
    private double speed;
    private LocalDateTime timestamp;

    public LocationEvent(
            String vehicleId,
            double latitude,
            double longitude,
            double speed,
            LocalDateTime timestamp
    ) {
        this.vehicleId = vehicleId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.timestamp = timestamp;
    }
    public String getVehicleId() {
        return vehicleId;
    }
    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }
    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public double getSpeed() {
        return speed;
    }
    public void setSpeed(double speed) {
        this.speed = speed;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
