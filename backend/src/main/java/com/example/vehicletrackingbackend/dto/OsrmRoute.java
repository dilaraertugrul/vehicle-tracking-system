package com.example.vehicletrackingbackend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class OsrmRoute {

    private RouteGeometry geometry;

    private double distance;
    private double duration;

    public OsrmRoute() {

    }

    public RouteGeometry getGeometry() {
        return geometry;
    }

    public void setGeometry(RouteGeometry geometry) {
        this.geometry = geometry;
    }

    public double getDistance() {
        return distance;
    }


    public void setDistance(double distance) {
        this.distance = distance;
    }


    // Rotanın tahmini süresini saniye olarak döndürür.
    public double getDuration() {
        return duration;
    }


    // OSRM'den gelen duration değerini nesneye atar.
    public void setDuration(double duration) {
        this.duration = duration;
    }
}