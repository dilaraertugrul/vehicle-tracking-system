package com.example.vehicletrackingbackend.dto;

// Rastgele oluşturulan ve gerçek bir yola
// oturtulan konum bilgisini tutar.
public class RandomRoutePoint {

    private final String locationName;

    private final double latitude;
    private final double longitude;


    public RandomRoutePoint(
            String locationName,
            double latitude,
            double longitude
    ) {

        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public String getLocationName() {
        return locationName;
    }


    public double getLatitude() {
        return latitude;
    }


    public double getLongitude() {
        return longitude;
    }
}