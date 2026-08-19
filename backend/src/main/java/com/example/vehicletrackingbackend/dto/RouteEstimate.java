package com.example.vehicletrackingbackend.dto;

// Frontend'e kalan rota bilgilerini göndermek için kullandığımız DTO.
public class RouteEstimate {

    private double distanceMeters;
    private double durationSeconds;


    public RouteEstimate(
            double distanceMeters,
            double durationSeconds
    ) {

        this.distanceMeters = distanceMeters;
        this.durationSeconds = durationSeconds;
    }


    // Kalan mesafeyi döndürür.
    public double getDistanceMeters() {
        return distanceMeters;
    }


    // Kalan mesafeyi değiştirir.
    public void setDistanceMeters(double distanceMeters) {
        this.distanceMeters = distanceMeters;
    }


    // Kalan süreyi döndürür.
    public double getDurationSeconds() {
        return durationSeconds;
    }


    // Kalan süreyi değiştirir.
    public void setDurationSeconds(double durationSeconds) {
        this.durationSeconds = durationSeconds;
    }
}