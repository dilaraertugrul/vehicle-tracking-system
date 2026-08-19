package com.example.vehicletrackingbackend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
// OSRM'den gelip bizim tanımlamadığımız JSON alanlarını görmezden gelmek için.

@JsonIgnoreProperties(ignoreUnknown = true)
// OSRM cevabında bizim kullanmadığımız başka alanlar da olabilir.
// Bu alanlar Java sınıfında tanımlı olmasa bile hata oluşmasını engeller.
public class OsrmRoute {


    // OSRM cevabındaki geometry alanını tutar.
    // İçinde rotanın koordinatları bulunur.
    private RouteGeometry geometry;


    // OSRM'nin hesapladığı rota mesafesini tutar.
    // Birimi METRE'dir.
    private double distance;


    // OSRM'nin hesapladığı tahmini yolculuk süresini tutar.
    // Birimi SANİYE'dir.
    private double duration;


    // Jackson'ın JSON verisini Java nesnesine çevirebilmesi için
    // boş constructor gereklidir.
    public OsrmRoute() {

    }


    // Rotanın geometry bilgisini döndürür.
    public RouteGeometry getGeometry() {
        return geometry;
    }


    // Geometry değerini nesneye atar.
    public void setGeometry(RouteGeometry geometry) {
        this.geometry = geometry;
    }


    // Rotanın mesafesini metre olarak döndürür.
    public double getDistance() {
        return distance;
    }


    // OSRM'den gelen distance değerini nesneye atar.
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