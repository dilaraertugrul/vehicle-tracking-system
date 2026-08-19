package com.example.vehicletrackingbackend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity

@Table(name = "vehicle_locations")
public class VehicleLocation {
    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vehicleId;
    private Double latitude;
    private Double longitude;
    private Double speed;
    private LocalDateTime timestamp;

    public VehicleLocation() {

    }

    public VehicleLocation(
            String vehicleId,
            Double latitude,
            Double longitude,
            Double speed,
            LocalDateTime timestamp
    ){
        this.vehicleId = vehicleId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.timestamp = timestamp;
    }

    public Long getId(){
        return id;
    }
    public String getVehicleId() {
        return vehicleId;
    }
    public void setVehicleId(String vehicleId){
        this.vehicleId = vehicleId;
    }
    public Double getLatitude(){
        return latitude;
    }
    public void setLatitude(double latitude){
        this.latitude = latitude;
    }
    public Double getLongitude(){
        return longitude;
    }
    public void setLongitude(double longitude){
        this.longitude = longitude;
    }
    public Double getSpeed(){
        return speed;
    }
    public void setSpeed(double speed){
        this.speed = speed;
    }
    public LocalDateTime getTimestamp(){
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp){
        this.timestamp = timestamp;
    }
}
