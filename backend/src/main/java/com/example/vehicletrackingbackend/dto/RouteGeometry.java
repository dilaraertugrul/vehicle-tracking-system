package com.example.vehicletrackingbackend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
// OSRM'den gelen ama bizim sınıfta tanımlamadığımız alanları görmezden gelmek için kullanılır.

import java.util.List;
// Koordinatları liste şeklinde tutmak için kullanılır.

@JsonIgnoreProperties(ignoreUnknown = true)
// JSON içinde bizim kullanmadığımız başka alanlar varsa hata verme, onları görmezden gel.
public class RouteGeometry {

    private String type;

    private List<List<Double>> coordinates;
    // Rota üzerindeki bütün koordinatları tutar.

    public RouteGeometry() {
        // JSON verisinin Java nesnesine çevrilebilmesi için boş constructor.
    }


    public String getType() {
        // Geometry tipini döndürür.
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public List<List<Double>> getCoordinates() {
        // Bütün rota koordinatlarını döndürür.
        return coordinates;
    }

    public void setCoordinates(List<List<Double>> coordinates) {
        // JSON'dan gelen koordinat listesini değişkene atar.
        this.coordinates = coordinates;
    }
}


