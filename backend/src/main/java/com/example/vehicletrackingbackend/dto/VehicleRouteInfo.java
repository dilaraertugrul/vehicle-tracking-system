package com.example.vehicletrackingbackend.dto;

public class VehicleRouteInfo {

    private final String vehicleId;

    private final String startLocationName;
    private final double startLatitude;
    private final double startLongitude;

    private final String destinationLocationName;
    private final double destinationLatitude;
    private final double destinationLongitude;


    public VehicleRouteInfo(
            String vehicleId,
            String startLocationName,
            double startLatitude,
            double startLongitude,
            String destinationLocationName,
            double destinationLatitude,
            double destinationLongitude
    ) {

        this.vehicleId = vehicleId;

        this.startLocationName = startLocationName;
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;

        this.destinationLocationName = destinationLocationName;
        this.destinationLatitude = destinationLatitude;
        this.destinationLongitude = destinationLongitude;
    }


    public String getVehicleId() {
        return vehicleId;
    }

    public String getStartLocationName() {
        return startLocationName;
    }

    public double getStartLatitude() {
        return startLatitude;
    }

    public double getStartLongitude() {
        return startLongitude;
    }

    public String getDestinationLocationName() {
        return destinationLocationName;
    }

    public double getDestinationLatitude() {
        return destinationLatitude;
    }

    public double getDestinationLongitude() {
        return destinationLongitude;
    }
}