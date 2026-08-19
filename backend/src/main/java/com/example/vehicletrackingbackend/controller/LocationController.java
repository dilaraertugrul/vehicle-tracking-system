package com.example.vehicletrackingbackend.controller;
import com.example.vehicletrackingbackend.kafka.LocationProducer;

import org.springframework.web.bind.annotation.PostMapping;
// HTTP POST isteklerini karşılamak için kullanılan @PostMapping annotation'ını içe aktarır.

import com.example.vehicletrackingbackend.event.LocationEvent;
import com.example.vehicletrackingbackend.kafka.LocationProducer;

import org.springframework.web.bind.annotation.RequestBody;
// HTTP isteğinin body kısmındaki veriyi Java metoduna almak için kullanılır.

import org.springframework.web.bind.annotation.RequestMapping;
// Controller'ın ana URL yolunu belirlemek için kullanılır.

import org.springframework.web.bind.annotation.RestController;

@RestController

@RequestMapping("/api/locations")
public class LocationController { // Dışardan gelen http isteklerini karşılar

    /* LocationController, kullanıcıdan API’den gelen isteği alacak ve LocationProducera iletecek.
    LocationProducer da Kafkaya gönderecek.
    */

    private final LocationProducer locationProducer;
    // Kafkaya mesaj göndermek için LocationProducer nesnesine ihtiyac var.


    public LocationController(LocationProducer locationProducer) {
        this.locationProducer = locationProducer;
    }

    @PostMapping("/send") // http post isteği geldiğinde çalışacak

    public String sendLocation(@RequestBody LocationEvent event) {
        locationProducer.sendLocation(event);
        return "Konum eventi Kafkaya gönderildi.";
    }
}
